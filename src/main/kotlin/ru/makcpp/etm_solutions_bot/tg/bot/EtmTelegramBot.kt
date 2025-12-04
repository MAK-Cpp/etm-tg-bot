package ru.makcpp.etm_solutions_bot.tg.bot

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import ru.makcpp.etm_solutions_bot.config.EtmTelegramBotConfiguration
import ru.makcpp.etm_solutions_bot.tg.client.EtmTelegramClient
import ru.makcpp.etm_solutions_bot.tg.update_consumer.LongPollingCoroutinesUpdateConsumer

@Component
class EtmTelegramBot(
    val configuration: EtmTelegramBotConfiguration,
    val telegramClient: EtmTelegramClient,
) : LongPollingCoroutinesUpdateConsumer(), SpringLongPollingBot {
    companion object {
        private val log = LoggerFactory.getLogger(EtmTelegramBot::class.java)
    }

    override fun getBotToken(): String = configuration.token

    override fun getUpdatesConsumer(): LongPollingUpdateConsumer = this

    override suspend fun consume(update: Update) {
        val message = update.message
        val text = message.text
        val chatId = message.chatId

        val result = SendMessage.builder()
            .chatId(chatId)
            .text("Your text is '$text'")
            .build()

        withContext(Dispatchers.IO) {
            telegramClient.execute(result)
        }
    }

    @AfterBotRegistration
    fun botIsReady() {
        log.info("Bot is ready")
    }
}