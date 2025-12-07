package ru.makcpp.etm_solutions_bot.tg.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import ru.makcpp.etm_solutions_bot.tg.client.EtmTelegramClient

interface UserStateHandler {
    suspend fun handle(update: Update): UserStateHandler
}

object EmptyStateHandler : UserStateHandler {
    override suspend fun handle(update: Update): UserStateHandler = this
}

interface Command : UserStateHandler {
    val name: String
}

@Component
class WrongCommand(private val telegramClient: EtmTelegramClient) : UserStateHandler {
    override suspend fun handle(update: Update): UserStateHandler {
        telegramClient.executeAsync(
            SendMessage.builder()
                .chatId(update.message.chatId)
                .text("Неизвестная команда: ${update.message.text}")
                .build()
        )
        return EmptyStateHandler
    }
}