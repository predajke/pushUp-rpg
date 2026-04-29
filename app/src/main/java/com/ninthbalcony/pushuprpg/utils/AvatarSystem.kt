package com.ninthbalcony.pushuprpg.utils

import com.ninthbalcony.pushuprpg.data.db.GameStateEntity

data class AvatarDef(
    val id: String,
    val nameRu: String,
    val nameEn: String,
    val conditionRu: String,
    val conditionEn: String,
    val isBase: Boolean = false
)

object AvatarSystem {

    val AVATARS = listOf(
        AvatarDef("avatar_base", "Стандартный", "Default",       "",                        "",                       isBase = true),
        AvatarDef("avatar_1",    "Атлет",        "Athlete",       "1000 отжиманий",           "1000 push-ups"),
        AvatarDef("avatar_2",    "Легендарный",  "Legendary",     "Все 6 слотов Legendary",   "All 6 slots Legendary"),
        AvatarDef("avatar_3",    "Богач",         "Rich",          "10 000 🦷 за всё время",   "10,000 🦷 total"),
        AvatarDef("avatar_4",    "Охотник",       "Hunter",        "250 убийств монстров",     "250 monster kills"),
        AvatarDef("avatar_5",    "Стойкий",       "Steadfast",     "Стрик 30+ дней",           "30+ day streak"),
        AvatarDef("avatar_6",    "Чемпион",       "Champion",      "Достичь 20 уровня",        "Reach level 20"),
        AvatarDef("avatar_7",    "Возрождённый", "Reborn",        "Первый Reset",             "First reset"),
    )

    fun drawableId(avatarId: String, gender: String): String =
        "${avatarId}_${if (gender == "female") "f" else "m"}"

    fun getUnlocked(raw: String): Set<String> = parseUnlocked(raw)

    private fun parseUnlocked(raw: String): MutableSet<String> {
        val base = mutableSetOf("avatar_base")
        if (raw.isNotBlank()) base += raw.split(",").filter { it.isNotBlank() }
        return base
    }

    fun serialize(ids: Set<String>): String = ids.joinToString(",")

    fun checkAndUnlock(state: GameStateEntity): GameStateEntity {
        val unlocked = parseUnlocked(state.unlockedAvatarIds)
        val sizeBefore = unlocked.size

        // Skip checks for avatars already in the set — called on every battle tick.
        if ("avatar_1" !in unlocked && state.totalPushUpsAllTime >= 1000) unlocked += "avatar_1"
        if ("avatar_2" !in unlocked && allSlotsLegendary(state))         unlocked += "avatar_2"
        if ("avatar_3" !in unlocked && state.totalTeethEarned >= 10000)  unlocked += "avatar_3"
        if ("avatar_4" !in unlocked && state.monstersKilled >= 250)      unlocked += "avatar_4"
        if ("avatar_5" !in unlocked && state.currentStreak >= 30)        unlocked += "avatar_5"
        // prestige resets level, so anyone who prestiged once already cleared 20.
        if ("avatar_6" !in unlocked && (state.playerLevel >= 20 || state.prestigeLevel >= 1)) unlocked += "avatar_6"
        if ("avatar_7" !in unlocked && state.prestigeLevel >= 1)         unlocked += "avatar_7"

        if (unlocked.size == sizeBefore) return state
        return state.copy(unlockedAvatarIds = serialize(unlocked))
    }

    private fun allSlotsLegendary(state: GameStateEntity): Boolean {
        val slots = arrayOf(
            state.equippedHead, state.equippedNecklace, state.equippedWeapon1,
            state.equippedWeapon2, state.equippedPants, state.equippedBoots
        )
        for (slot in slots) {
            if (slot.isEmpty()) return false
            val baseId = ItemUtils.getBaseItemId(slot.substringBefore(':'))
            if (ItemUtils.getItemById(baseId)?.rarity != "legendary") return false
        }
        return true
    }
}
