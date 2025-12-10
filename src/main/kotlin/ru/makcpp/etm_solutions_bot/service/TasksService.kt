package ru.makcpp.etm_solutions_bot.service

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class TasksService {
    private val tasksByChats: ConcurrentHashMap<Long, Int> = ConcurrentHashMap()
    private val chatsByTasks: ConcurrentHashMap<Int, Long> = ConcurrentHashMap()

    fun registerTasks(chatIdFrom: Long, tasksFirstMessageId: Int) {
        tasksByChats[chatIdFrom] = tasksFirstMessageId
        chatsByTasks[tasksFirstMessageId] = chatIdFrom
    }

    fun isChatHasTasks(chatId: Long): Boolean {
        return tasksByChats.containsKey(chatId)
    }

    fun isThereTask(taskId: Int): Boolean {
        return chatsByTasks.containsKey(taskId)
    }

    fun getChatIdByTaskId(taskId: Int): Long? {
        return chatsByTasks[taskId]
    }

    fun removeTaskByChatId(chatId: Long) {
        tasksByChats.remove(chatId)?.let { chatsByTasks.remove(it) }
    }
}