package ru.makcpp.etm_solutions_bot.tg.client

import kotlinx.coroutines.future.await
import org.springframework.stereotype.Component
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup
import org.telegram.telegrambots.meta.api.objects.File
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

    suspend fun sendMediaGroup(mediaGroup: SendMediaGroup): List<Message> {
        val messages = super.executeAsync(mediaGroup).await()
        messages.forEach { messagesHistoryService.addBotMessage(it) }
        return messages
    }

    suspend fun getFile(fileId: String): File {
        return executeAsync(GetFile.builder().fileId(fileId).build()).await()
    }
}