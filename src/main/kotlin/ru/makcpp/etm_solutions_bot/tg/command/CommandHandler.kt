package ru.makcpp.etm_solutions_bot.tg.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.EntityType
import org.telegram.telegrambots.meta.api.objects.Update
import ru.makcpp.etm_solutions_bot.config.EtmTelegramBotConfiguration
import ru.makcpp.etm_solutions_bot.service.UserService
import ru.makcpp.etm_solutions_bot.tg.client.EtmTelegramClient
import ru.makcpp.etm_solutions_bot.tg.utils.findEntities

@Component
class CommandHandler(
    commands: List<Command>,
    configuration: EtmTelegramBotConfiguration,
    private val userService: UserService,
    private val wrongCommand: WrongCommand,
    private val noCommand: NoCommand,
) : UserStateHandler {
    private val commands = commands.associateBy { "/${it.name}" }
    private val commandsRoles = configuration.commands.associate { it.name to it.roles }

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
        val message = update.message
            ?: return noCommand.handle(telegramClient, update)
        val commandName = message.findEntities(EntityType.BOTCOMMAND).firstOrNull()
            ?: return noCommand.handle(telegramClient, update)
        val command = commands[commandName]?.let { command ->
            val commandRoles = commandsRoles[command.name]
            val userRole = userService.getUserRole(message.chatId)
            if (commandRoles != null && commandRoles.contains(userRole)) command
            else wrongCommand
        } ?: wrongCommand
        return command.handle(telegramClient, update)
    }
}