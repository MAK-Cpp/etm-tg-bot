package ru.makcpp.etm_solutions_bot.service

import org.springframework.stereotype.Service
import ru.makcpp.etm_solutions_bot.config.EtmTelegramBotConfiguration
import ru.makcpp.etm_solutions_bot.enums.UserRole

// TODO: replace to database
@Service
class UserService(private val config: EtmTelegramBotConfiguration) {
    fun getUserRole(chatId: Long): UserRole {
        return if (config.etnodaryUserId == chatId) UserRole.ADMIN
        else UserRole.USER
    }
}