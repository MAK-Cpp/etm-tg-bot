package ru.makcpp.etm_solutions_bot.tg.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.makcpp.etm_solutions_bot.service.MessagesHistoryService
import ru.makcpp.etm_solutions_bot.tg.client.EtmTelegramClient
import ru.makcpp.etm_solutions_bot.tg.command.admin.ClearAllCommand

@Component
class ClearCommand(private val messagesHistoryService: MessagesHistoryService) : Command {
    override val name: String = "clear"

    override suspend fun handle(telegramClient: EtmTelegramClient, update: Update): UserStateHandler {
        val chatId = update.message.chat.id
        messagesHistoryService.getChatHistory(chatId)?.let { history ->
            ClearAllCommand.deleteAllMessagesInChat(telegramClient, chatId, history)
        }
        return EmptyState
    }
}