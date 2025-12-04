package ru.makcpp.etm_solutions_bot.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "bot", ignoreUnknownFields = false)
class EtmTelegramBotConfiguration(
    val token: String,
)