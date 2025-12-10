package ru.makcpp.etm_solutions_bot.tg.command.admin

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.makcpp.etm_solutions_bot.enums.Role
import ru.makcpp.etm_solutions_bot.service.TasksService
import ru.makcpp.etm_solutions_bot.service.UserService
import ru.makcpp.etm_solutions_bot.tg.client.EtmTelegramClient
import ru.makcpp.etm_solutions_bot.tg.command.EmptyState
import ru.makcpp.etm_solutions_bot.tg.command.UserStateHandler

@Component
class AdminReplyToTasksHandler(
    private val userService: UserService,
    private val tasksService: TasksService
) : UserStateHandler {
    override suspend fun handle(
        telegramClient: EtmTelegramClient,
        update: Update
    ): UserStateHandler {
        val taskId = update.message.replyToMessage.messageId

        val isAnswer = userService.getUserRole(update.message.chatId) != Role.ADMIN
                && tasksService.isThereTask(taskId)
        if (!isAnswer) {
            return EmptyState
        }
        val userChatId = tasksService.getChatIdByTaskId(taskId)!! // TODO: убрать !!
        TODO()
    }
}