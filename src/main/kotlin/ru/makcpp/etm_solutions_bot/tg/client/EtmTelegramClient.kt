package ru.makcpp.etm_solutions_bot.tg.client

import kotlinx.coroutines.future.await
import org.springframework.stereotype.Component
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.File
import org.telegram.telegrambots.meta.api.objects.message.Message
import ru.makcpp.etm_solutions_bot.config.EtmTelegramBotConfiguration
import ru.makcpp.etm_solutions_bot.service.MessagesHistoryService
import ru.makcpp.etm_solutions_bot.service.TasksService

@Component
class EtmTelegramClient(
    configuration: EtmTelegramBotConfiguration,
    private val messagesHistoryService: MessagesHistoryService,
    private val tasksService: TasksService
) : OkHttpTelegramClient(configuration.token) {
    suspend fun sendMessage(message: BotApiMethod<Message>): Message {
        val message = super.executeAsync(message).await()
        messagesHistoryService.addBotMessage(message)
        return message
    }

    suspend fun sendPhoto(photo: SendPhoto): Message {
        val message = super.executeAsync(photo).await()
        messagesHistoryService.addBotMessage(message)
        return message
    }

    suspend fun sendMediaGroup(group: SendMediaGroup): List<Message> {
        val messages = super.executeAsync(group).await()
        messages.forEach { messagesHistoryService.addBotMessage(it) }
        return messages
    }

    suspend fun sendTask(task: SendPhoto, from: Long): Boolean {
        if (tasksService.isChatHasTasks(from)) {
            return false
        }
        val message = sendPhoto(task)
        tasksService.registerTasks(from, message.messageId)
        return true
    }

    suspend fun sendTasks(tasks: SendMediaGroup, from: Long): Boolean {
        if (tasksService.isChatHasTasks(from)) {
            return false
        }
        val messages = sendMediaGroup(tasks)
        tasksService.registerTasks(from, messages.first().messageId)
        return true
    }

    suspend fun sendSolution(solution: SendPhoto) {
        sendPhoto(solution)
        tasksService.removeTaskByChatId(solution.chatId.toLong())
    }

    suspend fun sendSolutions(solutions: SendMediaGroup) {
        sendMediaGroup(solutions)
        tasksService.removeTaskByChatId(solutions.chatId.toLong())
    }

    suspend fun getFile(fileId: String): File {
        return executeAsync(GetFile.builder().fileId(fileId).build()).await()
    }
}