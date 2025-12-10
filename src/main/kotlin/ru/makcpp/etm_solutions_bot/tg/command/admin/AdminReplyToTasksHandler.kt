package ru.makcpp.etm_solutions_bot.tg.command.admin

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto
import ru.makcpp.etm_solutions_bot.enums.Role
import ru.makcpp.etm_solutions_bot.service.TasksService
import ru.makcpp.etm_solutions_bot.service.UserService
import ru.makcpp.etm_solutions_bot.tg.client.EtmTelegramClient
import ru.makcpp.etm_solutions_bot.tg.command.EmptyState
import ru.makcpp.etm_solutions_bot.tg.command.NoCommand
import ru.makcpp.etm_solutions_bot.tg.command.UserStateHandler
import ru.makcpp.etm_solutions_bot.tg.utils.getPhotoFileId
import ru.makcpp.etm_solutions_bot.tg.utils.hasPhoto
import java.util.concurrent.ConcurrentHashMap

@Component
class AdminReplyToTasksHandler(
    private val userService: UserService,
    private val tasksService: TasksService
) : UserStateHandler {
    companion object {
        private val log = LoggerFactory.getLogger(AdminReplyToTasksHandler::class.java)
    }

    private val solutions: ConcurrentHashMap<Long, MutableList<String>> = ConcurrentHashMap()

    private suspend fun validate(
        telegramClient: EtmTelegramClient,
        update: Update
    ): UserStateHandler? {
        val taskId = update.message.replyToMessage.messageId
        val adminId = update.message.chatId
        if (userService.getUserRole(adminId) != Role.ADMIN) {
            return NoCommand.handle(telegramClient, update)
        }
        if (!update.hasPhoto()) {
            telegramClient.sendMessage(
                SendMessage.builder()
                    .chatId(adminId)
                    .replyToMessageId(update.message.messageId)
                    .text("Ожидалось фото")
                    .build()
            )
            return EmptyState
        }
        if (!tasksService.isThereTask(taskId)) {
            telegramClient.sendMessage(
                SendMessage.builder()
                    .chatId(adminId)
                    .text("Вы ответили не на задачу, либо уже отвечали на эту задачу.")
                    .build()
            )
            return EmptyState
        }
        return null
    }

    private fun addSolution(update: Update): Long {
        val taskId = update.message.replyToMessage.messageId
        val userChatId = tasksService.getChatIdByTaskId(taskId)!! // TODO: убрать !!
        log.trace("reply by admin: {}", update)
        val photoFileId = update.getPhotoFileId()
        solutions.getOrPut(userChatId) { mutableListOf() }.add(photoFileId)
        return userChatId
    }

    override suspend fun handle(
        telegramClient: EtmTelegramClient,
        update: Update
    ): UserStateHandler {
        log.trace("AdminReplyToTasksHandler handle()")
        validate(telegramClient, update)?.let { return it }
        addSolution(update)
        return EmptyState
    }

    override suspend fun handleLast(
        telegramClient: EtmTelegramClient,
        update: Update
    ): UserStateHandler {
        log.trace("AdminReplyToTasksHandler handleLast()")
        validate(telegramClient, update)?.let { return it }
        val userChatId = addSolution(update)
        val adminId = update.message.chatId

        val solutionInputMediaPhotos = solutions[userChatId]!!.mapIndexed { i, fileId ->
            if (i == 0) {
                InputMediaPhoto.builder()
                    .caption("Вот решения на ваши задачи")
                    .media(fileId)
                    .build()
            } else {
                InputMediaPhoto(fileId)
            }
        }
        when (solutionInputMediaPhotos.size) { // TODO: убрать !!
            1 -> telegramClient.sendSolution(
                SendPhoto.builder()
                    .chatId(userChatId)
                    .photo(InputFile(solutionInputMediaPhotos[0].media))
                    .caption(solutionInputMediaPhotos[0].caption)
                    .build()
            )

            else -> telegramClient.sendSolutions(
                SendMediaGroup.builder()
                    .chatId(userChatId)
                    .medias(solutionInputMediaPhotos)
                    .build()
            )
        }
        telegramClient.sendMessage(
            SendMessage.builder()
                .chatId(adminId)
                .text("Решения успешно отправлены пользователю")
                .build()
        )
        return EmptyState
    }
}