package ru.makcpp.etm_solutions_bot.tg.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.makcpp.etm_solutions_bot.config.EtmTelegramBotConfiguration

@Component
class CommandHandler(
    commands: List<Command>,
    configuration: EtmTelegramBotConfiguration,
    private val wrongCommand: WrongCommand,
) : UserStateHandler {
    private val commands = commands.associateBy { "/${it.name}" }

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

    override suspend fun handle(update: Update): UserStateHandler {
        val command = commands[update.message.text.split(" ").firstOrNull()] ?: wrongCommand
        return command.handle(update)
    }
}