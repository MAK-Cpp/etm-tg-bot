package ru.makcpp.etm_solutions_bot.tg.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class HelpCommand : Command {
    override val name: String
        get() = TODO("Not yet implemented")

    override suspend fun handle(update: Update): UserStateHandler {
        TODO("Not yet implemented")
    }
}