package com.pushupRPG.app.data.model

data class GameEvent(
    val id: Int,
    val nameEn: String,
    val nameRu: String,
    val descriptionEn: String,
    val descriptionRu: String,
    val icon: String,
    val type: EventType,
    val value: Float
)

enum class EventType {
    DROP_RATE_BONUS,
    POWER_BONUS,
    HEALTH_BONUS,
    BATTLE_SPEED_BONUS,
    ARMOR_BONUS,
    BERSERKER,
    REGEN_BONUS,
    XP_BONUS,
    RARE_DROP_BONUS,
    NIGHTMARE,
    ENCHANTERS_LUCK
}