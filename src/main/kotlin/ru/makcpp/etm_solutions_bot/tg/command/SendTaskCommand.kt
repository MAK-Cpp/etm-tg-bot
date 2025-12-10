package ru.makcpp.etm_solutions_bot.tg.command

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.media.InputMedia
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto
import ru.makcpp.etm_solutions_bot.config.EtmTelegramBotConfiguration
import ru.makcpp.etm_solutions_bot.service.TasksService
import ru.makcpp.etm_solutions_bot.tg.client.EtmTelegramClient
import ru.makcpp.etm_solutions_bot.tg.utils.hasPhoto

@Component
class SendTaskCommand(
    private val configuration: EtmTelegramBotConfiguration,
    private val tasksService: TasksService
) : Command {
    companion object {
        private val log = LoggerFactory.getLogger(SendTaskCommand::class.java)
    }

    override val name: String = "sendTask"

    override suspend fun handle(
        telegramClient: EtmTelegramClient,
        update: Update
    ): UserStateHandler {
        val chatId = update.message.chat.id
        return if (tasksService.isChatHasTasks(chatId)) {
            telegramClient.sendMessage(
                SendMessage.builder()
                    .chatId(chatId)
                    .text(
                        """
                        У вас уже отправлены задачи, ожидайте.
                        """.trimIndent()
                    )
                    .build()
            )
            EmptyState
        } else {
            telegramClient.sendMessage(
                SendMessage.builder()
                    .chatId(chatId)
                    .text("Отправьте ваше ФИО:")
                    .build()
            )
            return GetFioOfUser(configuration.etnodaryUserId)
        }
    }

    class GetFioOfUser(private val adminChatId: Long) : UserStateHandler {
        override suspend fun handle(
            telegramClient: EtmTelegramClient,
            update: Update
        ): UserStateHandler {
            val chatId = update.message.chat.id
            val fio = update.message.text
            telegramClient.sendMessage(
                SendMessage.builder()
                    .chatId(chatId)
                    .text("Отправьте задачи фотографиями, максимум 10.")
                    .build()
            )
            return SendTasksToAdminState(adminChatId, fio)
        }
    }

    class SendTasksToAdminState(
        private val adminChatId: Long,
        private val fio: String,
        private val medias: MutableList<InputMedia> = mutableListOf(),
    ) : UserStateHandler {
        private suspend fun sendPhotosToAdmin(telegramClient: EtmTelegramClient, chatIdFrom: Long) {
            val success = when (medias.size) {
                1 -> telegramClient.sendTask(
                    SendPhoto.builder()
                        .chatId(adminChatId)
                        .photo(InputFile(medias[0].media))
                        .caption(medias[0].caption)
                        .build(),
                    chatIdFrom
                )

                else -> telegramClient.sendTasks(
                    SendMediaGroup.builder()
                        .chatId(adminChatId)
                        .medias(medias)
                        .build(),
                    chatIdFrom
                )
            }

            telegramClient.sendMessage(
                SendMessage.builder()
                    .chatId(chatIdFrom)
                    .text(
                        if (success)
                            """
                            Ваши фото были отправлены на проверку, ожидайте ответа.
                            """.trimIndent()
                        else
                            """
                            У вас уже отправлены задачи, ожидайте.
                            """.trimIndent()
                    )
                    .build()
            )
        }

        private suspend fun checkUpdate(telegramClient: EtmTelegramClient, update: Update): UserStateHandler? {
            if (!update.hasPhoto()) {
                return if (medias.isEmpty()) {
                    telegramClient.sendMessage(
                        SendMessage.builder()
                            .chatId(update.message.chatId)
                            .replyToMessageId(update.message.messageId)
                            .text(
                                """
                                Ожидалось фотография(-и), пожалуйста, пришлите фото.
                                """.trimIndent()
                            )
                            .build()
                    )
                    this
                } else {
                    sendPhotosToAdmin(telegramClient, update.message.chatId)
                    EmptyState
                }
            }
            return null
        }

        private fun addPhoto(update: Update) {
            log.trace("Got a photo with media group id {}", update.message.mediaGroupId)

            val photoFileId = update.message.photo.last().fileId

            medias += if (medias.isEmpty()) {
                InputMediaPhoto.builder()
                    .media(photoFileId)
                    .caption(
                        """
                        Задачи от пользователя @${update.message.chat.userName}
                        ФИО: $fio
                        """.trimIndent()
                    )
                    .build()
            } else {
                InputMediaPhoto(photoFileId)
            }
        }

        override suspend fun handle(
            telegramClient: EtmTelegramClient,
            update: Update
        ): UserStateHandler {
            checkUpdate(telegramClient, update)?.let { return it }
            addPhoto(update)

            return if (medias.size < 10) this
            else {
                sendPhotosToAdmin(telegramClient, update.message.chatId)
                SkipPhotos(update.message.mediaGroupId)
            }
        }

        override suspend fun handleLast(
            telegramClient: EtmTelegramClient,
            update: Update
        ): UserStateHandler {
            checkUpdate(telegramClient, update)?.let { return it }
            addPhoto(update)
            sendPhotosToAdmin(telegramClient, update.message.chatId)
            return EmptyState
        }
    }

    data class SkipPhotos(val mediaGroupId: String) : UserStateHandler {
        override suspend fun handle(
            telegramClient: EtmTelegramClient,
            update: Update
        ): UserStateHandler {
            if (update.message.mediaGroupId != mediaGroupId) {
                return EmptyState
            }
            return this
        }
    }
}