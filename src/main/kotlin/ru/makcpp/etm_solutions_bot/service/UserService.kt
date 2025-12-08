package ru.makcpp.etm_solutions_bot.service

import org.springframework.stereotype.Service
import ru.makcpp.etm_solutions_bot.config.EtmTelegramBotConfiguration
import ru.makcpp.etm_solutions_bot.enums.Role
import ru.makcpp.etm_solutions_bot.tg.command.UserStateHandler

// TODO: replace to database
@Service
class UserService(private val config: EtmTelegramBotConfiguration) {


    suspend fun getUserRole(chatId: Long): Role {
        return if (config.etnodaryUserId == chatId) Role.ADMIN
        else Role.USER
    }

    suspend fun setUserState(state: UserStateHandler) {

    }
}