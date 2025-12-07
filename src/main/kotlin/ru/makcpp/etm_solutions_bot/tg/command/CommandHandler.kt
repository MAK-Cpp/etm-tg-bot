package ru.makcpp.etm_solutions_bot.tg.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.makcpp.etm_solutions_bot.config.EtmTelegramBotConfiguration
import ru.makcpp.etm_solutions_bot.service.UserService
import ru.makcpp.etm_solutions_bot.tg.client.EtmTelegramClient

@Component
class CommandHandler(
    commands: List<Command>,
    configuration: EtmTelegramBotConfiguration,
    private val userService: UserService,
    private val wrongCommand: WrongCommand,
) : UserStateHandler {
    private val commands = commands.associateBy { "/${it.name}" }
    private val commandPermissions = configuration.commands.associate { it.name to it.permission }

    init {
        val configCommands = configuration.commands.map { "/${it.name}" }.toSet()
        val commandNames = this.commands.keys
        require(commandNames == configCommands) {
            """
            |Несоответсвие реализованных и описанных команд:
            |  Описанные команды: $configCommands
            |  vs
            |  Реализованные команды: $commandNames
            |Реализуйте недостающие команды и/или опишите реализованные команды
            """.trimMargin()
        }
    }

    override suspend fun handle(telegramClient: EtmTelegramClient, update: Update): UserStateHandler {
        val command = commands[update.message.text.split(" ").firstOrNull()]?.let { command ->
            val commandPermission = commandPermissions[command.name]
            val userRole = userService.getUserRole(update.message.chatId)
            if (commandPermission != null && userRole.isEnough(commandPermission)) command
            else wrongCommand
        } ?: wrongCommand
        return command.handle(telegramClient, update)
    }
}