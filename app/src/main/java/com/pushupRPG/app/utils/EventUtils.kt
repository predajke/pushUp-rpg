package com.pushupRPG.app.utils

import com.pushupRPG.app.data.model.GameEvent
import com.pushupRPG.app.data.model.EventType
import kotlin.random.Random

object EventUtils {

    val EVENT_DURATION_MS = 3L * 60 * 60 * 1000  // 3 часа
    val EVENT_INTERVAL_MS = 1L * 60 * 60 * 1000  // каждый 1 час

    private val allEvents = listOf(
        GameEvent(
            id = 1,
            nameEn = "Lucky Clover",
            nameRu = "Клеверный спас",
            descriptionEn = "Drop rate from monsters +10%",
            descriptionRu = "Шанс дропа с монстров +10%",
            icon = "🍀",
            type = EventType.DROP_RATE_BONUS,
            value = 10f
        ),
        GameEvent(
            id = 2,
            nameEn = "Power Surge",
            nameRu = "Время силы",
            descriptionEn = "Power temporarily +15%",
            descriptionRu = "Сила временно +15%",
            icon = "⚡",
            type = EventType.POWER_BONUS,
            value = 15f
        ),
        GameEvent(
            id = 3,
            nameEn = "Holy Water",
            nameRu = "Святая вода",
            descriptionEn = "+50 HP for the duration",
            descriptionRu = "+50 HP на время события",
            icon = "💧",
            type = EventType.HEALTH_BONUS,
            value = 50f
        ),
        GameEvent(
            id = 4,
            nameEn = "Grinder",
            nameRu = "Мясорубка",
            descriptionEn = "Battle ticks 2x faster",
            descriptionRu = "Бой происходит в 2 раза чаще",
            icon = "⚙️",
            type = EventType.BATTLE_SPEED_BONUS,
            value = 2f
        ),
        GameEvent(
            id = 5,
            nameEn = "Iron Skin",
            nameRu = "Железная кожа",
            descriptionEn = "Armor +20%",
            descriptionRu = "Броня +20%",
            icon = "🛡️",
            type = EventType.ARMOR_BONUS,
            value = 20f
        ),
        GameEvent(
            id = 6,
            nameEn = "Berserker",
            nameRu = "Берсерк",
            descriptionEn = "Power +30% but Armor -50%",
            descriptionRu = "Сила +30%, но Броня -50%",
            icon = "🪓",
            type = EventType.BERSERKER,
            value = 30f
        ),
        GameEvent(
            id = 7,
            nameEn = "Meditation",
            nameRu = "Медитация",
            descriptionEn = "HP regeneration x3",
            descriptionRu = "Регенерация HP x3",
            icon = "🧘",
            type = EventType.REGEN_BONUS,
            value = 3f
        ),
        GameEvent(
            id = 8,
            nameEn = "Double XP",
            nameRu = "Двойной опыт",
            descriptionEn = "XP from push-ups x2",
            descriptionRu = "Опыт за отжимания x2",
            icon = "✨",
            type = EventType.XP_BONUS,
            value = 2f
        ),
        GameEvent(
            id = 9,
            nameEn = "Treasure Hunt",
            nameRu = "Охота за сокровищами",
            descriptionEn = "Drop rate +20%, only Rare & Epic",
            descriptionRu = "Дроп +20%, только Rare и Epic",
            icon = "💎",
            type = EventType.RARE_DROP_BONUS,
            value = 20f
        ),
        GameEvent(
            id = 10,
            nameEn = "Nightmare",
            nameRu = "Кошмар",
            descriptionEn = "Monsters x2 stronger, Drop rate x3",
            descriptionRu = "Монстры сильнее x2, Дроп x3",
            icon = "💀",
            type = EventType.NIGHTMARE,
            value = 2f
        ),
        GameEvent(
            id = 11,
            nameEn = "Enchanter's Luck",
            nameRu = "Удача Чародея",
            descriptionEn = "Enchanting success +5%, Legendary reroll chance +3%",
            descriptionRu = "Шанс заточки +5%, шанс легендарки в реролле +3%",
            icon = "🔮",
            type = EventType.ENCHANTERS_LUCK,
            value = 5f
        )
    )

    fun getEventById(id: Int): GameEvent? {
        return allEvents.find { it.id == id }
    }

    fun rollRandomEvent(): GameEvent {
        return allEvents.random()
    }

    fun getEventName(event: GameEvent, language: String): String {
        return if (language == "ru") event.nameRu else event.nameEn
    }

    fun getEventDescription(event: GameEvent, language: String): String {
        return if (language == "ru") event.descriptionRu else event.descriptionEn
    }

    // Проверяем нужно ли запустить новое событие
    fun shouldStartNewEvent(
        lastEventTime: Long,
        currentEventEndTime: Long
    ): Boolean {
        val now = System.currentTimeMillis()
        return now > currentEventEndTime &&
                now - lastEventTime >= EVENT_INTERVAL_MS
    }

    // Проверяем активно ли текущее событие
    fun isEventActive(eventEndTime: Long): Boolean {
        return System.currentTimeMillis() < eventEndTime
    }

    // Оставшееся время события в читаемом формате
    fun getRemainingTime(eventEndTime: Long): String {
        val remaining = eventEndTime - System.currentTimeMillis()
        if (remaining <= 0) return ""
        val hours = remaining / 3_600_000
        val minutes = (remaining % 3_600_000) / 60_000
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }
}