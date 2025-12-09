package ru.makcpp.etm_solutions_bot.tg.utils

import org.telegram.telegrambots.meta.api.objects.Update

/**
 * Достать все entity одного типа в порядке появления в сообщении обновления.
 *
 * @see ru.makcpp.etm_solutions_bot.tg.utils.MessageUtilsKt.findEntities
 */
fun Update.findEntitiesInMessage(entityType: String): List<String> {
    return message?.findEntities(entityType) ?: emptyList()
}