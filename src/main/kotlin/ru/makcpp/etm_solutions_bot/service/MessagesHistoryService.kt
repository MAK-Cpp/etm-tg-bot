package ru.makcpp.etm_solutions_bot.service

import okhttp3.internal.toImmutableMap
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.message.Message
import java.util.concurrent.ConcurrentHashMap

@Service
class MessagesHistoryService {
    // TODO: replace with database
    private val history = ConcurrentHashMap<Long, ChatHistory>()

    private val Message.usernameOrId: String
        get() {
            val result = chat.userName
            return if (result != null) {
                "@$result"
            } else {
                chatId.toString()
            }
        }

    suspend fun addUserMessage(message: Message) {
        history
            .getOrPut(message.chatId) { ChatHistory(message.usernameOrId) }
            .addUserMessage(message.messageId)
    }

    suspend fun addBotMessage(message: Message) {
        history
            .getOrPut(message.chatId) { ChatHistory(message.usernameOrId) }
            .addBotMessage(message.messageId)
    }

    suspend fun getChatHistory(chatId: Long): ChatHistory? {
        return history[chatId]
    }

    suspend fun getAllHistories(): Map<Long, ChatHistory> = history.toImmutableMap()

    class ChatHistory(val username: String) {
        val userMessageIds: MutableList<Int> = mutableListOf()
        val botMessageIds: MutableList<Int> = mutableListOf()

        fun addUserMessage(messageId: Int) {
            userMessageIds.add(messageId)
        }

        fun addBotMessage(messageId: Int) {
            botMessageIds.add(messageId)
        }

        fun messagesCount(): Int = userMessageIds.size + botMessageIds.size
    }
}