package ru.makcpp.etm_solutions_bot.tg.update_consumer

import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.concurrent.ConcurrentHashMap

abstract class LongPollingCoroutinesUpdateConsumer : LongPollingUpdateConsumer {
    companion object {
        private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            log.error("Error in coroutineScope: ", throwable)
        }
        private val log = LoggerFactory.getLogger(LongPollingCoroutinesUpdateConsumer::class.java)
    }

    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineName("LongPollingCoroutinesUpdateConsumer") + exceptionHandler)
    private val userLocks: ConcurrentHashMap<Long, Mutex> = ConcurrentHashMap()

    final override fun consume(updates: List<Update>) {
        updates
            .filter { it.hasMessage() }
            .groupBy { it.message.chatId }
            .forEach { (chatId, chatUpdates) ->
                scope.launch {
                    log.trace("handling ${chatUpdates.size} updates for chat $chatId")
                    userLocks.getOrPut(chatId) { Mutex() }
                        .withLock {
                            chatUpdates.forEachIndexed { i, update -> consume(update, i == chatUpdates.size - 1) }
                        }
                }
            }
    }

    abstract suspend fun consume(update: Update, isLast: Boolean)

    @PreDestroy
    fun shutdown() {
        scope.cancel()
    }
}