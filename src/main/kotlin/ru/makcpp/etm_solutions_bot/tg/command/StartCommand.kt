package ru.makcpp.etm_solutions_bot.tg.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import ru.makcpp.etm_solutions_bot.tg.client.EtmTelegramClient

@Component
class StartCommand : Command {
    override val name: String = "start"

    override suspend fun handle(telegramClient: EtmTelegramClient, update: Update): UserStateHandler {
        telegramClient.sendMessage(
            SendMessage.builder()
                .chatId(update.message.chatId)
                .text("""
                    Привет, ${update.message?.from?.firstName ?: "незнакомец"}.
                    Чтобы узнать, как пользоваться ботом, используйте команду /help.
                """.trimIndent())
                .build()
        )
        return EmptyState
    }
}