package ru.makcpp.etm_solutions_bot.tg.utils

import org.telegram.telegrambots.meta.api.objects.message.Message

/**
 * Достать все entity одного типа в порядке появления в сообщении.
 *
 * @see org.telegram.telegrambots.meta.api.objects.EntityType
 */
fun Message.findEntities(entityType: String): List<String> {
    val entities = this.entities ?: return emptyList()
    return entities
        .filter { it.type == entityType }
        .sortedBy { it.offset }
        .map { text.substring(it.offset, it.offset + it.length) }
}