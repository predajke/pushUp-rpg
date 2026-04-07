package com.pushupRPG.app.utils

object DailyRewardUtils {

    data class DailyReward(
        val day: Int,
        val teeth: Int = 0,
        val itemRarity: String? = null,
        val isCloverBox: Boolean = false
    ) {
        fun descriptionEn(): String = when {
            isCloverBox -> "Clover Box 🍀"
            itemRarity != null -> "${itemRarity.replaceFirstChar { it.uppercase() }} item"
            else -> "+$teeth 🦷"
        }
        fun descriptionRu(): String = when {
            isCloverBox -> "Клеверная коробка 🍀"
            itemRarity != null -> when (itemRarity) {
                "common" -> "Обычный предмет"
                "uncommon" -> "Необычный предмет"
                "rare" -> "Редкий предмет"
                "epic" -> "Эпический предмет"
                else -> itemRarity
            }
            else -> "+$teeth 🦷"
        }
    }

    val CYCLE = listOf(
        DailyReward(day = 1, teeth = 2),
        DailyReward(day = 2, teeth = 5),
        DailyReward(day = 3, itemRarity = "uncommon"),
        DailyReward(day = 4, teeth = 10),
        DailyReward(day = 5, isCloverBox = true),
        DailyReward(day = 6, teeth = 20),
        DailyReward(day = 7, itemRarity = "rare"),
    )

    fun getRewardForDay(day: Int): DailyReward {
        val idx = ((day - 1).coerceAtLeast(0)) % 7
        return CYCLE[idx]
    }

    fun needsReward(lastClaimDate: String, today: String): Boolean =
        lastClaimDate != today

    /** Возвращает следующий день цикла (1-7) */
    fun nextDay(currentDay: Int): Int = (currentDay % 7) + 1
}
