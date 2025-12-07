package ru.makcpp.etm_solutions_bot.tg.command.admin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import ru.makcpp.etm_solutions_bot.service.MessagesHistoryService
import ru.makcpp.etm_solutions_bot.tg.client.EtmTelegramClient
import ru.makcpp.etm_solutions_bot.tg.command.Command
import ru.makcpp.etm_solutions_bot.tg.command.EmptyStateHandler
import ru.makcpp.etm_solutions_bot.tg.command.UserStateHandler

@Component
class ClearAllCommand(private val messagesHistoryService: MessagesHistoryService) : Command {
    companion object {
        private val log = LoggerFactory.getLogger(ClearAllCommand::class.java)

        suspend fun deleteAllMessagesInChat(
            telegramClient: EtmTelegramClient,
            chatId: Long,
            chatHistory: MessagesHistoryService.ChatHistory
        ) {
            val deletedBotMessages: Set<Int>
            val deletedUserMessages: Set<Int>
            withContext(Dispatchers.IO) {
                deletedBotMessages = deleteMessages(telegramClient, chatId, chatHistory.botMessageIds)
                deletedUserMessages = deleteMessages(telegramClient, chatId, chatHistory.userMessageIds)
            }

            chatHistory.userMessageIds.removeIf { deletedUserMessages.contains(it) }
            chatHistory.botMessageIds.removeIf { deletedBotMessages.contains(it) }
        }

        private suspend fun deleteMessages(
            telegramClient: EtmTelegramClient,
            chatId: Long,
            messageIds: List<Int>
        ): Set<Int> = buildSet {
            for (messageId in messageIds) {
                try {
                    val isSuccess = telegramClient.executeAsync(
                        DeleteMessage.builder()
                            .chatId(chatId)
                            .messageId(messageId)
                            .build()
                    ).await()

                    if (isSuccess) {
                        log.trace("deleted message {} from chat {}", messageId, chatId)
                        add(messageId)
                    }
                } catch (e: TelegramApiRequestException) {
                    log.error("Error deleting message {}", messageId, e)
                    if (e.errorCode == 400) {
                        add(messageId)
                    }
                } catch (e: Exception) {
                    log.error("Unexpected error while deleting message {}", messageId, e)
                }
            }
        }
    }

    override val name: String = "clearAll"

    override suspend fun handle(
        telegramClient: EtmTelegramClient,
        update: Update
    ): UserStateHandler {
        val currentUserChatId = update.message.chat.id
        val resultMessage = buildString {
            appendLine("Итог выполнения команды:")
            messagesHistoryService.getAllHistories().forEach { (chatId, chatHistory) ->
                if (currentUserChatId != chatId) {
                    val messagesCountBefore = chatHistory.messagesCount()
                    deleteAllMessagesInChat(telegramClient, chatId, chatHistory)
                    val messagesCountAfter = chatHistory.messagesCount()
                    val deletedMessages = messagesCountBefore - messagesCountAfter
                    appendLine("У пользователя ${chatHistory.username} удалено $deletedMessages из $messagesCountBefore сообщений")
                }
            }
        }
        telegramClient.sendMessage(
            SendMessage.builder()
                .chatId(currentUserChatId)
                .text(resultMessage)
                .build()
        )
        return EmptyStateHandler
    }
}