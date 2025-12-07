package ru.makcpp.etm_solutions_bot.tg.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import ru.makcpp.etm_solutions_bot.config.EtmTelegramBotConfiguration
import ru.makcpp.etm_solutions_bot.enums.UserRole
import ru.makcpp.etm_solutions_bot.service.UserService
import ru.makcpp.etm_solutions_bot.tg.client.EtmTelegramClient

@Component
class HelpCommand(
    config: EtmTelegramBotConfiguration,
    private val userService: UserService
) : Command {
    override val name: String = "help"

    private val helpMessages = UserRole.entries.associateWith { role ->
        val availableCommands =
            config.commands.filter { command -> command.permission.permissionLevel <= role.permissionLevel }
        buildString {
            if (role != UserRole.USER) {
                appendLine("Ваша роль: $role")
            }
            appendLine("Доступные команды: ")
            availableCommands.forEachIndexed { index, command ->
                appendLine("[${index + 1}] /${command.name} -- ${command.description}")
            }
        }
    }

    override suspend fun handle(telegramClient: EtmTelegramClient, update: Update): UserStateHandler {
        val chatId = update.message.chatId
        val userRole = userService.getUserRole(chatId)
        val helpText = requireNotNull(helpMessages[userRole]) { "There is no help message for role $userRole" }
        telegramClient.sendMessage(
            SendMessage.builder()
                .chatId(chatId)
                .text(helpText)
                .build()
        )
        return EmptyStateHandler
    }
}