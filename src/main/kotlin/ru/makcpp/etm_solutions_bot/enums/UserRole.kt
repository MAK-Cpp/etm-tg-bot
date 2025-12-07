package ru.makcpp.etm_solutions_bot.enums

enum class UserRole(val permissionLevel: Int) {
    USER(0),
    ADMIN(1),
    ;

    fun isEnough(other: UserRole): Boolean {
        return permissionLevel >= other.permissionLevel
    }
}