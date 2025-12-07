package ru.makcpp.etm_solutions_bot.tg.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import ru.makcpp.etm_solutions_bot.config.EtmTelegramBotConfiguration
import ru.makcpp.etm_solutions_bot.tg.client.EtmTelegramClient

@Component
class HelpCommand(
    config: EtmTelegramBotConfiguration,
    private val telegramClient: EtmTelegramClient
) : Command {
    override val name: String = "help"

    private val helpMessage = buildString {
        appendLine("Доступные команды: ")
        config.commands.forEachIndexed { index, command ->
            appendLine("[${index + 1}] /${command.name} -- ${command.description}")
        }
    }

    override suspend fun handle(update: Update): UserStateHandler {
        telegramClient.execute(SendMessage.builder()
            .chatId(update.message.chatId)
            .text(helpMessage)
            .build())
        return EmptyStateHandler
    }
}