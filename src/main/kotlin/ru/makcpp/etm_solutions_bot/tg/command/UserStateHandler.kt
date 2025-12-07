package ru.makcpp.etm_solutions_bot.tg.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import ru.makcpp.etm_solutions_bot.tg.client.EtmTelegramClient

interface UserStateHandler {
    suspend fun handle(telegramClient: EtmTelegramClient, update: Update): UserStateHandler
}

object EmptyStateHandler : UserStateHandler {
    override suspend fun handle(telegramClient: EtmTelegramClient, update: Update): UserStateHandler = this
}

interface Command : UserStateHandler {
    val name: String
}

@Component
class WrongCommand : UserStateHandler {
    override suspend fun handle(telegramClient: EtmTelegramClient, update: Update): UserStateHandler {
        telegramClient.sendMessage(
            SendMessage.builder()
                .chatId(update.message.chatId)
                .text(
                    """
                    |Неизвестная команда: ${update.message.text}
                    |Чтобы узнать список команд, вызовите /help
                    """.trimMargin()
                )
                .build()
        )
        return EmptyStateHandler
    }
}