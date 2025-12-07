package ru.makcpp.etm_solutions_bot.tg.bot

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.makcpp.etm_solutions_bot.config.EtmTelegramBotConfiguration
import ru.makcpp.etm_solutions_bot.service.MessagesHistoryService
import ru.makcpp.etm_solutions_bot.tg.client.EtmTelegramClient
import ru.makcpp.etm_solutions_bot.tg.command.CommandHandler
import ru.makcpp.etm_solutions_bot.tg.command.EmptyStateHandler
import ru.makcpp.etm_solutions_bot.tg.update_consumer.LongPollingCoroutinesUpdateConsumer
import ru.makcpp.etm_solutions_bot.tg.command.UserStateHandler
import java.util.concurrent.ConcurrentHashMap

@Component
class EtmTelegramBot(
    private val configuration: EtmTelegramBotConfiguration,
    private val commandHandler: CommandHandler,
    private val telegramClient: EtmTelegramClient,
    private val messagesHistoryService: MessagesHistoryService
) : LongPollingCoroutinesUpdateConsumer(), SpringLongPollingBot {
    companion object {
        private val log = LoggerFactory.getLogger(EtmTelegramBot::class.java)
    }

    private val userStates: ConcurrentHashMap<Long, UserStateHandler> = ConcurrentHashMap()

    override fun getBotToken(): String = configuration.token

    override fun getUpdatesConsumer(): LongPollingUpdateConsumer = this

    override suspend fun consume(update: Update) {
        val chatId = update.message.chatId
        messagesHistoryService.addUserMessage(update.message)
        userStates[chatId] = when (val state = userStates[chatId]) {
            EmptyStateHandler, null -> commandHandler
            else -> state
        }.handle(telegramClient, update)
    }

    @AfterBotRegistration
    fun botIsReady() {
        log.info("Bot is ready")
    }
}