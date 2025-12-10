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
import ru.makcpp.etm_solutions_bot.tg.command.EmptyState
import ru.makcpp.etm_solutions_bot.tg.command.NoCommand
import ru.makcpp.etm_solutions_bot.tg.command.ReRunHandleUpdateState
import ru.makcpp.etm_solutions_bot.tg.command.UserStateHandler
import ru.makcpp.etm_solutions_bot.tg.update_consumer.LongPollingCoroutinesUpdateConsumer
import ru.makcpp.etm_solutions_bot.tg.utils.UpdateType
import ru.makcpp.etm_solutions_bot.tg.utils.type
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

    override suspend fun consume(update: Update, isLast: Boolean) {
        messagesHistoryService.addUserMessage(update.message)
        handleUpdate(update, isLast)
    }

    private suspend fun handleUpdate(update: Update, isLast: Boolean) {
        val chatId = update.message.chatId
        val state = when (val updateType = update.type) {
            is UpdateType.Command -> commandHandler
            is UpdateType.MessageReply -> TODO()
            UpdateType.None -> userStates[chatId] ?: NoCommand
        }
        if (state is ReRunHandleUpdateState) {
            userStates.remove(chatId)
            handleUpdate(update, isLast)
        } else {
            if (isLast) {
                val newState = state.handleLast(telegramClient, update)
                if (newState is EmptyState) {
                    userStates.remove(chatId)
                } else {
                    userStates[chatId] = newState
                }
            } else {
                userStates[chatId] = state.handle(telegramClient, update)
            }
        }
    }

    @AfterBotRegistration
    fun botIsReady() {
        log.info("Bot is ready")
    }
}