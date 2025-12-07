package ru.makcpp.etm_solutions_bot.tg.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class CommandHandler(commands: List<Command>, private val wrongCommand: WrongCommand) : UserStateHandler {
    private val commands = commands.associateBy<Command, String?> { "/${it.name}" }

    override suspend fun handle(update: Update): UserStateHandler {
        val command = commands[update.message.text.split(" ").firstOrNull()] ?: wrongCommand
        return command.handle(update)
    }
}