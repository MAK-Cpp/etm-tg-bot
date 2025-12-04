package ru.makcpp.etm_solutions_bot.tg.update_consumer

import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer
import org.telegram.telegrambots.meta.api.objects.Update

abstract class LongPollingCoroutinesUpdateConsumer : LongPollingUpdateConsumer {
    companion object {
        private val log = LoggerFactory.getLogger(LongPollingCoroutinesUpdateConsumer::class.java)
    }

    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineName("LongPollingCoroutinesUpdateConsumer"))

    final override fun consume(updates: List<Update>) {
        updates.forEach { update ->
            log.trace("handling update {}", update)
            scope.launch {
                consume(update)
            }
        }
    }

    abstract suspend fun consume(update: Update)

    @PreDestroy
    fun shutdown() {
        scope.cancel()
    }
}