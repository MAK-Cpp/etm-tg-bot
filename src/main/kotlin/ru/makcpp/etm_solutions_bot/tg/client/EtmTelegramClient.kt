package ru.makcpp.etm_solutions_bot.tg.client

import org.springframework.stereotype.Component
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import ru.makcpp.etm_solutions_bot.config.EtmTelegramBotConfiguration

@Component
class EtmTelegramClient(
    val configuration: EtmTelegramBotConfiguration
) : OkHttpTelegramClient(configuration.token)