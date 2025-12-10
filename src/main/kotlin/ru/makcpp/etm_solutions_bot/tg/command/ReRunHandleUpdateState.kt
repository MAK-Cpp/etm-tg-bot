package ru.makcpp.etm_solutions_bot.tg.command

import org.telegram.telegrambots.meta.api.objects.Update
import ru.makcpp.etm_solutions_bot.tg.client.EtmTelegramClient

object ReRunHandleUpdateState : UserStateHandler {
    override suspend fun handle(
        telegramClient: EtmTelegramClient,
        update: Update
    ): UserStateHandler {
        error("Необходимо при получении этого состояния заного вызвать handleUpdate, а состояние пользователя удалить")
    }
}