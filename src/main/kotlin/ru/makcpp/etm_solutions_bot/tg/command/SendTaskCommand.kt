package ru.makcpp.etm_solutions_bot.tg.command

import kotlinx.coroutines.future.await
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.media.InputMedia
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto
import ru.makcpp.etm_solutions_bot.config.EtmTelegramBotConfiguration
import ru.makcpp.etm_solutions_bot.tg.client.EtmTelegramClient

@Component
class SendTaskCommand(private val configuration: EtmTelegramBotConfiguration) : Command {
    override val name: String = "sendTask"

    override suspend fun handle(
        telegramClient: EtmTelegramClient,
        update: Update
    ): UserStateHandler {
        telegramClient.sendMessage(
            SendMessage.builder()
                .chatId(update.message.chat.id)
                .text("Отправьте задачи в виде фотографий одним сообщением")
                .build()
        )
        return SendTasksToAdminState(configuration.etnodaryUserId)
    }

    data class SendTasksToAdminState(
        private val adminChatId: Long,
        private val medias: List<InputMedia> = listOf()
    ) : UserStateHandler {
        override suspend fun handle(
            telegramClient: EtmTelegramClient,
            update: Update
        ): UserStateHandler {

            val medias = update.message.photo.mapIndexed { i, photo ->
                InputMediaPhoto.builder()
                    .media(photo.fileId)
                    .run {
                        if (i == 0) caption("Задачи от пользователя @${update.message.chat.userName}")
                        else this
                    }
                    .build()
            }

            return EmptyState
        }
    }
}