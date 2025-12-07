package ru.makcpp.etm_solutions_bot.tg.client

import kotlinx.coroutines.future.await
import org.springframework.stereotype.Component
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.message.Message
import ru.makcpp.etm_solutions_bot.config.EtmTelegramBotConfiguration
import ru.makcpp.etm_solutions_bot.service.MessagesHistoryService

@Component
class EtmTelegramClient(
    configuration: EtmTelegramBotConfiguration,
    private val messagesHistoryService: MessagesHistoryService
) : OkHttpTelegramClient(configuration.token) {
    suspend fun sendMessage(message: BotApiMethod<Message>): Message {
        val message = super.executeAsync(message).await()
        messagesHistoryService.addBotMessage(message)
        return message
    }
}