package ru.makcpp.etm_solutions_bot.config

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import ru.makcpp.etm_solutions_bot.enums.UserRole

@ConfigurationProperties(prefix = "bot", ignoreUnknownFields = false)
@Validated
data class EtmTelegramBotConfiguration(
    @field:NotBlank
    val token: String,
    val etnodaryUserId: Long,
    val commands: List<CommandDescription>,
) {
    @Validated
    data class CommandDescription(
        @field:NotBlank
        val name: String,
        @field:NotBlank
        val description: String,
        val permission: UserRole = UserRole.USER
    )
}