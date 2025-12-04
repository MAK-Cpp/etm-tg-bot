package ru.makcpp.etm_solutions_bot.config

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "bot", ignoreUnknownFields = false)
@Validated
class EtmTelegramBotConfiguration(
    @field:NotBlank
    val token: String,
)