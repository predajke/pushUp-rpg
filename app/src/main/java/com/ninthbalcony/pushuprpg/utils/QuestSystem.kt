package com.ninthbalcony.pushuprpg.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

enum class QuestType { PUSHUPS_DAY, KILLS_DAY, BOSS_KILLS, ENCHANT, FORGE, BUY_ITEM, PUSHUPS_SESSION, PUSHUPS_AND_KILLS, TEETH_SPENT }

data class QuestDef(
    val id: String,
    val type: QuestType,
    val target: Int,
    val rewardTeeth: Int = 0,
    val rewardItemRarity: String? = null,
    val nameEn: String,
    val nameRu: String,
    val descEn: String,
    val descRu: String,
    val isWeekly: Boolean = false
)

data class ActiveQuest(
    val defId: String,
    val progress: Int = 0,
    val claimed: Boolean = false
) {
    val isCompleted: Boolean get() = QuestSystem.getDefById(defId)?.let { progress >= it.target } ?: false
}

object QuestSystem {

    private val gson = Gson()

    private val DAILY_POOL = listOf(
        QuestDef("pushups_100", QuestType.PUSHUPS_DAY, 100, rewardTeeth = 30,
            nameEn = "Daily Push-Ups", nameRu = "Ежедневные отжимания",
            descEn = "Do 100 push-ups today", descRu = "Сделай 100 отжиманий сегодня"),
        QuestDef("pushups_50", QuestType.PUSHUPS_DAY, 50, rewardTeeth = 15,
            nameEn = "Warm Up", nameRu = "Разминка",
            descEn = "Do 50 push-ups today", descRu = "Сделай 50 отжиманий сегодня"),
        QuestDef("kills_5", QuestType.KILLS_DAY, 5, rewardItemRarity = "common",
            nameEn = "Monster Slayer", nameRu = "Истребитель",
            descEn = "Kill 5 monsters today", descRu = "Убей 5 монстров сегодня"),
        QuestDef("kills_3", QuestType.KILLS_DAY, 3, rewardTeeth = 20,
            nameEn = "Fighter", nameRu = "Боец",
            descEn = "Kill 3 monsters today", descRu = "Убей 3 монстра сегодня"),
        QuestDef("enchant_1", QuestType.ENCHANT, 1, rewardTeeth = 20,
            nameEn = "Enchanter", nameRu = "Заточник",
            descEn = "Enchant any item", descRu = "Заточи любой предмет"),
        QuestDef("forge_1", QuestType.FORGE, 1, rewardItemRarity = "uncommon",
            nameEn = "Forgemaster", nameRu = "Кузнец дня",
            descEn = "Merge 2 items in Forge", descRu = "Переработай 2 вещи в Кузнице"),
        QuestDef("forge_2", QuestType.FORGE, 5, rewardItemRarity = "rare",
            nameEn = "Forgemaster of the day", nameRu = "Мастер-Кузнец дня",
            descEn = "Merge 10 items in Forge", descRu = "Переработай 10 вещи в Кузнице"),
        QuestDef("buy_1", QuestType.BUY_ITEM, 1, rewardTeeth = 15,
            nameEn = "Shopper", nameRu = "Покупатель",
            descEn = "Buy an item from shop", descRu = "Купи вещь в магазине"),
        QuestDef("buy_2", QuestType.BUY_ITEM, 5, rewardTeeth = 50,
            nameEn = "Shopaholic", nameRu = "Шопоголик",
            descEn = "Buy 5 items from shop", descRu = "Купи 5 вещей в магазине"),
        QuestDef("session_50", QuestType.PUSHUPS_SESSION, 50, rewardTeeth = 25,
            nameEn = "Big Session", nameRu = "Большая сессия",
            descEn = "Do 50+ push-ups in one set", descRu = "Сделай 50+ отжиманий за 1 подход"),
        QuestDef("session_30", QuestType.PUSHUPS_SESSION, 30, rewardTeeth = 15,
            nameEn = "Push Hard", nameRu = "Жми давай",
            descEn = "Do 30+ push-ups in one set", descRu = "Сделай 30+ отжиманий за 1 подход"),
        QuestDef("pushups_200", QuestType.PUSHUPS_DAY, 200, rewardItemRarity = "epic",
            nameEn = "Spartan Fury", nameRu = "Ярость спартанца",
            descEn = "Do 200 push-ups today", descRu = "Сделай 200 отжиманий сегодня"),
        QuestDef("daily_pushups_75", QuestType.PUSHUPS_DAY, 75, rewardTeeth = 20,
            nameEn = "Solid Effort", nameRu = "Хорошая работа",
            descEn = "Do 75 push-ups today", descRu = "Сделай 75 отжиманий сегодня"),
        QuestDef("daily_session_20", QuestType.PUSHUPS_SESSION, 20, rewardTeeth = 10,
            nameEn = "Quick Set", nameRu = "Быстрый подход",
            descEn = "Do 20+ push-ups in one set", descRu = "Сделай 20+ отжиманий за 1 подход"),
        QuestDef("daily_kills_10", QuestType.KILLS_DAY, 10, rewardTeeth = 30,
            nameEn = "Warlord", nameRu = "Повелитель войны",
            descEn = "Kill 10 monsters today", descRu = "Убей 10 монстров сегодня"),
        QuestDef("daily_combo_50_5", QuestType.PUSHUPS_AND_KILLS, 2, rewardTeeth = 30,
            nameEn = "Double Threat", nameRu = "Двойная угроза",
            descEn = "Do 50 push-ups AND kill 5 monsters", descRu = "Сделай 50 отжиманий И убей 5 монстров"),
        QuestDef("daily_buy_3", QuestType.BUY_ITEM, 3, rewardTeeth = 20,
            nameEn = "Collector", nameRu = "Коллекционер",
            descEn = "Buy 3 items from shop", descRu = "Купи 3 предмета в магазине"),
    )

    private val WEEKLY_POOL = listOf(
        QuestDef("weekly_boss_2", QuestType.BOSS_KILLS, 2, rewardItemRarity = "rare",
            nameEn = "Boss Hunter", nameRu = "Охотник на боссов",
            descEn = "Kill 2 bosses this week", descRu = "Убей 2 боссов за неделю",
            isWeekly = true),
        QuestDef("weekly_pushups_500", QuestType.PUSHUPS_DAY, 500, rewardTeeth = 90,
            nameEn = "Weekly Grind", nameRu = "Недельный гринд",
            descEn = "Do 500 push-ups this week", descRu = "Сделай 500 отжиманий за неделю",
            isWeekly = true),
        QuestDef("weekly_pushups_800", QuestType.PUSHUPS_DAY, 800, rewardTeeth = 120,
            nameEn = "Weekly HardCore Grind", nameRu = "Недельный хардкорный гринд",
            descEn = "Do 800 push-ups this week", descRu = "Сделай 800 отжиманий за неделю",
            isWeekly = true),
        QuestDef("weekly_pushups_1200", QuestType.PUSHUPS_DAY, 1200, rewardItemRarity = "legendary", rewardTeeth = 100,
            nameEn = "Weekly Legendary Grind", nameRu = "Недельный Легендарный гринд",
            descEn = "Do 1200 push-ups this week", descRu = "Сделай 1200 отжиманий за неделю",
            isWeekly = true),
        QuestDef("weekly_enchant_3", QuestType.ENCHANT, 3, rewardItemRarity = "epic",
            nameEn = "Master Enchanter", nameRu = "Мастер заточки",
            descEn = "Enchant 3 items this week", descRu = "Заточи 3 предмета за неделю",
            isWeekly = true),
        QuestDef("weekly_enchant_9", QuestType.ENCHANT, 9, rewardItemRarity = "epic", rewardTeeth = 50,
            nameEn = "Legend Enchanter", nameRu = "Легенда заточки",
            descEn = "Enchant 9 items this week", descRu = "Заточи 9 предмета за неделю",
            isWeekly = true),
        QuestDef("weekly_kills_20", QuestType.KILLS_DAY, 20, rewardTeeth = 40,
            nameEn = "Serial Killer", nameRu = "Серийный убийца",
            descEn = "Kill 20 monsters this week", descRu = "Убей 20 монстров за неделю",
            isWeekly = true),
        QuestDef("weekly_kills_50", QuestType.KILLS_DAY, 50, rewardTeeth = 100,
            nameEn = "Crazy Killer", nameRu = "Безумный убийца",
            descEn = "Kill 50 monsters this week", descRu = "Убей 50 монстров за неделю",
            isWeekly = true),
        QuestDef("weekly_pushups_300", QuestType.PUSHUPS_DAY, 300, rewardTeeth = 50,
            nameEn = "Weekly Effort", nameRu = "Недельное усилие",
            descEn = "Do 300 push-ups this week", descRu = "Сделай 300 отжиманий за неделю",
            isWeekly = true),
        QuestDef("weekly_forge_5", QuestType.FORGE, 5, rewardItemRarity = "rare",
            nameEn = "Weekly Forgemaster", nameRu = "Кузнец недели",
            descEn = "Merge 5 items this week", descRu = "Слей 5 предметов за неделю",
            isWeekly = true),
        QuestDef("weekly_teeth_spent_300", QuestType.TEETH_SPENT, 300, rewardItemRarity = "rare", rewardTeeth = 100,
            nameEn = "Big Spender", nameRu = "Транжира",
            descEn = "Spend 300 teeth this week", descRu = "Потрать 300 зубов за неделю",
            isWeekly = true),
    )

    fun getDefById(id: String): QuestDef? = (DAILY_POOL + WEEKLY_POOL).find { it.id == id }

    fun rollDailyQuests(): List<ActiveQuest> =
        DAILY_POOL.shuffled().take(3).map { ActiveQuest(it.id) }

    fun rollWeeklyQuest(): ActiveQuest = ActiveQuest(WEEKLY_POOL.random().id)

    fun serialize(quests: List<ActiveQuest>): String = gson.toJson(quests)

    fun deserialize(json: String): List<ActiveQuest> {
        if (json.isEmpty()) return emptyList()
        return try {
            val type = object : TypeToken<List<ActiveQuest>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) { emptyList() }
    }

    /** Добавить прогресс по типу квеста. PUSHUPS_SESSION — не кумулятивный (берём max). */
    fun addProgress(quests: List<ActiveQuest>, type: QuestType, amount: Int): List<ActiveQuest> =
        quests.map { quest ->
            if (quest.claimed) return@map quest
            val def = getDefById(quest.defId) ?: return@map quest
            if (def.type != type) return@map quest
            val newProgress = if (type == QuestType.PUSHUPS_SESSION)
                maxOf(quest.progress, amount)
            else
                (quest.progress + amount).coerceAtMost(def.target)
            quest.copy(progress = newProgress)
        }

    /** Максимум прогресса среди активных дневных KILLS_DAY квестов — прокси для "убийств сегодня". */
    fun getDailyKillsFromQuests(quests: List<ActiveQuest>): Int =
        quests.mapNotNull { q ->
            val def = getDefById(q.defId) ?: return@mapNotNull null
            if (def.type == QuestType.KILLS_DAY && !def.isWeekly) q.progress else null
        }.maxOrNull() ?: 0

    fun getIsoWeekNumber(dateStr: String): Int {
        return try {
            val parts = dateStr.split("-")
            val cal = Calendar.getInstance()
            cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            cal.get(Calendar.WEEK_OF_YEAR)
        } catch (_: Exception) { -1 }
    }

    fun getName(def: QuestDef, language: String) = if (language == "ru") def.nameRu else def.nameEn
    fun getDesc(def: QuestDef, language: String) = if (language == "ru") def.descRu else def.descEn

    fun getRewardText(def: QuestDef, language: String): String {
        return when {
            def.rewardTeeth > 0 && def.rewardItemRarity != null ->
                "+${def.rewardTeeth} 🦷 + ${if (language == "ru") "вещь" else "item"}"
            def.rewardTeeth > 0 -> "+${def.rewardTeeth} 🦷"
            def.rewardItemRarity != null -> {
                val rarity = def.rewardItemRarity
                if (language == "ru") rarityRu(rarity) else rarity.replaceFirstChar { it.uppercase() }
            }
            else -> ""
        }
    }

    private fun rarityRu(r: String) = when (r) {
        "common" -> "Обычная вещь"
        "uncommon" -> "Необычная вещь"
        "rare" -> "Редкая вещь"
        "epic" -> "Эпическая вещь"
        "legendary" -> "Легендарная вещь"
        else -> r
    }
}
