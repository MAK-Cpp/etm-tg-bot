package ru.makcpp.etm_solutions_bot.tg.utils

import org.telegram.telegrambots.meta.api.objects.EntityType
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.message.Message

/**
 * Достать все entity одного типа в порядке появления в сообщении обновления.
 *
 * @see ru.makcpp.etm_solutions_bot.tg.utils.MessageUtilsKt.findEntities
 */
fun Update.findEntitiesInMessage(entityType: String): List<String> {
    return message?.findEntities(entityType) ?: emptyList()
}

sealed interface UpdateType {
    data object None : UpdateType

    data class Command(val command: String) : UpdateType

    data class MessageReply(val replyTo: Message) : UpdateType
}

val Update.type: UpdateType
    get() {
        val commands = findEntitiesInMessage(EntityType.BOTCOMMAND)
        if (commands.isNotEmpty()) {
            return UpdateType.Command(commands[0])
        }

        val replyToMessage = message.replyToMessage
        if (replyToMessage != null) {
            return UpdateType.MessageReply(replyToMessage)
        }

        return UpdateType.None
    }

fun Update.hasPhoto(): Boolean = hasMessage() && message.hasPhoto()