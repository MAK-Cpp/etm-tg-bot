package ru.makcpp.etm_solutions_bot.tg.utils

import org.telegram.telegrambots.meta.api.objects.MessageEntity
import org.telegram.telegrambots.meta.api.objects.message.Message

private fun Message.getEntity(entity: MessageEntity): String =
    text.substring(entity.offset, entity.offset + entity.length)

/**
 * Достать все entity типа [entityType] в порядке появления в сообщении.
 *
 * @see org.telegram.telegrambots.meta.api.objects.EntityType
 */
fun Message.findEntities(entityType: String): List<String> {
    val entities = this.entities ?: return emptyList()
    return entities
        .filter { it.type == entityType }
        .sortedBy { it.offset }
        .map { getEntity(it) }
}

/**
 * Достать первый в порядке появления в сообщении entity типа [entityType].
 *
 * @see org.telegram.telegrambots.meta.api.objects.EntityType
 */
fun Message.findFirstEntity(entityType: String): String? {
    val entities = this.entities ?: return null
    return entities
        .filter { it.type == entityType }
        .minByOrNull { it.offset }
        ?.let { getEntity(it) }
}