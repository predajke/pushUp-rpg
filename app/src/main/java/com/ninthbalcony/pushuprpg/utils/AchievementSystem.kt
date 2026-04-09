package com.ninthbalcony.pushuprpg.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ninthbalcony.pushuprpg.data.db.GameStateEntity

enum class AchBonusType {
    XP_PERCENT, DAMAGE_PERCENT, DROP_RATE_PERCENT,
    ARMOR_PERCENT, HP_FLAT, CRIT_PERCENT, ENCHANT_FLAT, TEETH_RATE_PERCENT
}

data class AchievementDef(
    val id: String,
    val nameRu: String,
    val nameEn: String,
    val descRu: String,
    val descEn: String,
    val imageRes: String,
    val bonusType: AchBonusType,
    val bonusValue: Float,
    val tier: Int = 0
)

data class UnlockedAchievement(val defId: String, val unlockedDate: String = "")

data class AchievementBonuses(
    val xpPercent: Float = 0f,
    val damagePercent: Float = 0f,
    val dropRatePercent: Float = 0f,
    val armorPercent: Float = 0f,
    val hpFlat: Int = 0,
    val critPercent: Float = 0f,
    val enchantFlat: Float = 0f,
    val teethRatePercent: Float = 0f
)

object AchievementSystem {

    private val gson = Gson()

    // ===================== ОПРЕДЕЛЕНИЯ =====================

    private val UNIQUE: List<AchievementDef> = listOf(
        AchievementDef("ach_first_blood",     "Первая кровь",          "First Blood",        "Убей первого монстра",            "Kill your first monster",          "ach_first_blood",     AchBonusType.DAMAGE_PERCENT,    0.02f),
        AchievementDef("ach_berserker",       "Берсерк",               "Berserker",          "50+ отжиманий за одну сессию",    "50+ push-ups in one session",      "ach_berserker",       AchBonusType.XP_PERCENT,        0.05f),
        AchievementDef("ach_master_enchant",  "Мастер заточки",        "Master Enchanter",   "Заточи вещь до +9",               "Enchant an item to +9",            "ach_master_enchant",  AchBonusType.ENCHANT_FLAT,      5f),
        AchievementDef("ach_unstoppable",     "Неудержимый",           "Unstoppable",        "Стрик 30 дней",                   "30-day streak",                    "ach_unstoppable",     AchBonusType.DAMAGE_PERCENT,    0.03f),
        AchievementDef("ach_rich",            "Богач",                 "Rich",               "Заработай 5000 зубов",            "Earn 5000 teeth total",            "ach_rich",            AchBonusType.TEETH_RATE_PERCENT,0.05f),
        AchievementDef("ach_phoenix",         "Феникс",                "Phoenix",            "Вернись после 7 дней отсутствия", "Return after 7 days absent",       "ach_phoenix",         AchBonusType.HP_FLAT,           20f),
        AchievementDef("ach_legendary_catch", "Легендарный улов",      "Legendary Catch",    "Получи предмет legendary",        "Get a legendary item",             "ach_legendary_catch", AchBonusType.DROP_RATE_PERCENT, 0.02f),
        AchievementDef("ach_epic_catch",      "Эпический улов",        "Epic Catch",         "Получи предмет epic",             "Get an epic item",                 "ach_epic_catch",      AchBonusType.DROP_RATE_PERCENT, 0.01f),
        AchievementDef("ach_full_wardrobe",   "Полный гардероб",       "Full Wardrobe",      "Заполни все 6 слотов экипировки", "Fill all 6 equipment slots",       "ach_full_wardrobe",   AchBonusType.ARMOR_PERCENT,     0.05f),
        AchievementDef("ach_failed_enchants", "Коллекционер затычек",  "Failure Collector",  "100 провалов заточки",            "100 failed enchantments",          "ach_failed_enchants", AchBonusType.ENCHANT_FLAT,      10f),
        AchievementDef("ach_no_sweat",        "Ни капли пота",         "No Sweat",           "Убей босса с HP > 50%",           "Kill a boss with HP above 50%",    "ach_no_sweat",        AchBonusType.DAMAGE_PERCENT,    0.05f),
        AchievementDef("ach_night_shift",     "Ночная смена",          "Night Shift",        "Отжимания после 23:00",           "Push-ups after 23:00",             "ach_night_shift",     AchBonusType.XP_PERCENT,        0.03f),
        AchievementDef("ach_early_bird",      "Ранняя пташка",         "Early Bird",         "Отжимания до 7:00",               "Push-ups before 7:00",             "ach_early_bird",      AchBonusType.XP_PERCENT,        0.03f),
        AchievementDef("ach_immortal",        "Бессмертный",           "Immortal",           "7 дней без смерти",               "7 days without dying",             "ach_immortal",        AchBonusType.HP_FLAT,           25f),
        AchievementDef("ach_alchemist",       "Алхимик",               "Alchemist",          "10 операций в Forge",             "10 Forge operations",              "ach_alchemist",       AchBonusType.ENCHANT_FLAT,      10f),
        AchievementDef("ach_dragon_slayer",   "Покоритель дракона",    "Dragon Slayer",      "Убей Ancient Dragon",             "Kill the Ancient Dragon",          "ach_dragon_slayer",   AchBonusType.DAMAGE_PERCENT,    0.08f),
        AchievementDef("ach_critical",        "Критический момент",    "Critical Moment",    "Крит во время Burst-атаки",       "Land a crit during a Burst attack","ach_critical",        AchBonusType.CRIT_PERCENT,      0.02f),
        AchievementDef("ach_abyssal_reaper", "Губитель Бездны",       "Abyss Slayer",       "Убей Бездонного Жнеца 20 раз",    "Kill Abyssal Reaper 20 times",     "ach_abyssal_reaper",  AchBonusType.DAMAGE_PERCENT,    0.04f),
        AchievementDef("ach_skull_crusher",  "Истребитель Черепов",   "Skull Hunter",       "Убей Сокрушителя Черепов 15 раз", "Kill Skull Crusher 15 times",      "ach_skull_crusher",   AchBonusType.CRIT_PERCENT,      0.04f),
        AchievementDef("ach_sky_sentry",     "Покоритель Небес",      "Sky Conqueror",      "Убей Стража Небес 10 раз",        "Kill Sky Sentry 10 times",         "ach_sky_sentry",      AchBonusType.XP_PERCENT,        0.08f),
        AchievementDef("ach_heat_cannon",    "Огнеборец",             "Flame Fighter",      "Убей Пушку Пламени 5 раз",        "Kill Heat Cannon 5 times",         "ach_heat_cannon",     AchBonusType.HP_FLAT,           60f),
        AchievementDef("ach_void",           "VOID ПОЛУЧЕН. VOID ПОЛУЧИЛ ТЕБЯ.", "YOU GOT THE VOID, VOID GOT YOU.", "Экипируй все 5 предметов набора VOID", "Equip all 5 pieces of the VOID set", "ach_void",          AchBonusType.DAMAGE_PERCENT,    0.25f)
    )

    private val PROGRESSIVE: List<AchievementDef> = listOf(
        // ach_kills: 10/50/100/500/1000
        AchievementDef("ach_kills_1", "Охотник I",    "Hunter I",    "10 убийств",    "10 kills",    "ach_kills", AchBonusType.DAMAGE_PERCENT, 0.01f, tier = 1),
        AchievementDef("ach_kills_2", "Охотник II",   "Hunter II",   "50 убийств",    "50 kills",    "ach_kills", AchBonusType.DAMAGE_PERCENT, 0.02f, tier = 2),
        AchievementDef("ach_kills_3", "Охотник III",  "Hunter III",  "100 убийств",   "100 kills",   "ach_kills", AchBonusType.DAMAGE_PERCENT, 0.03f, tier = 3),
        AchievementDef("ach_kills_4", "Охотник IV",   "Hunter IV",   "500 убийств",   "500 kills",   "ach_kills", AchBonusType.DAMAGE_PERCENT, 0.04f, tier = 4),
        AchievementDef("ach_kills_5", "Охотник V",    "Hunter V",    "1000 убийств",  "1000 kills",  "ach_kills", AchBonusType.DAMAGE_PERCENT, 0.05f, tier = 5),
        AchievementDef("ach_kills_6", "Легенда боя",  "War Legend",  "2000 убийств",  "2000 kills",  "ach_kills", AchBonusType.DAMAGE_PERCENT, 0.06f, tier = 6),
        // ach_pushups: 100/500/1k/5k/10k/50k
        AchievementDef("ach_pushups_1", "Новичок I",     "Rookie I",      "100 отжиманий",   "100 push-ups",   "ach_pushups", AchBonusType.XP_PERCENT, 0.01f, tier = 1),
        AchievementDef("ach_pushups_2", "Новичок II",    "Rookie II",     "500 отжиманий",   "500 push-ups",   "ach_pushups", AchBonusType.XP_PERCENT, 0.02f, tier = 2),
        AchievementDef("ach_pushups_3", "Атлет I",       "Athlete I",     "1000 отжиманий",  "1000 push-ups",  "ach_pushups", AchBonusType.XP_PERCENT, 0.03f, tier = 3),
        AchievementDef("ach_pushups_4", "Атлет II",      "Athlete II",    "5000 отжиманий",  "5000 push-ups",  "ach_pushups", AchBonusType.XP_PERCENT, 0.04f, tier = 4),
        AchievementDef("ach_pushups_5", "Чемпион",       "Champion",      "10000 отжиманий", "10000 push-ups", "ach_pushups", AchBonusType.XP_PERCENT, 0.05f, tier = 5),
        AchievementDef("ach_pushups_6", "Легенда",       "Legend",        "20000 отжиманий", "20000 push-ups", "ach_pushups", AchBonusType.XP_PERCENT, 0.08f, tier = 6),
        AchievementDef("ach_pushups_7", "Величайший",    "Greatest",      "30000 отжиманий", "30000 push-ups", "ach_pushups", AchBonusType.XP_PERCENT, 0.10f, tier = 7),
        // ach_streak: 3/7/14/30/60
        AchievementDef("ach_streak_1", "Стойкий I",     "Steadfast I",   "Стрик 3 дня",    "3-day streak",   "ach_streak", AchBonusType.XP_PERCENT, 0.01f, tier = 1),
        AchievementDef("ach_streak_2", "Стойкий II",    "Steadfast II",  "Стрик 7 дней",   "7-day streak",   "ach_streak", AchBonusType.XP_PERCENT, 0.02f, tier = 2),
        AchievementDef("ach_streak_3", "Стойкий III",   "Steadfast III", "Стрик 14 дней",  "14-day streak",  "ach_streak", AchBonusType.XP_PERCENT, 0.03f, tier = 3),
        AchievementDef("ach_streak_4", "Железная воля", "Iron Will",     "Стрик 30 дней",  "30-day streak",  "ach_streak", AchBonusType.XP_PERCENT, 0.04f, tier = 4),
        AchievementDef("ach_streak_5", "Несломленный",  "Unbroken",      "Стрик 60 дней",  "60-day streak",  "ach_streak", AchBonusType.XP_PERCENT, 0.05f, tier = 5),
        // ach_enchant_done: 5/20/50
        AchievementDef("ach_enchant_done_1", "Новичок",      "Newbie",      "5 заточек",   "5 enchants",   "ach_enchant_done", AchBonusType.ENCHANT_FLAT, 3f, tier = 1),
        AchievementDef("ach_enchant_done_2", "Ученик",       "Student",     "20 заточек",  "20 enchants",  "ach_enchant_done", AchBonusType.ENCHANT_FLAT, 5f, tier = 2),
        AchievementDef("ach_enchant_done_3", "Подмастерье",  "Apprentice",  "50 заточек",  "50 enchants",  "ach_enchant_done", AchBonusType.ENCHANT_FLAT, 8f, tier = 3),
        AchievementDef("ach_enchant_done_4", "Мастер",       "Master",      "100 заточек",  "100 enchants",  "ach_enchant_done", AchBonusType.ENCHANT_FLAT, 12f, tier = 4),
        AchievementDef("ach_enchant_done_5", "Гроссмейстер", "Grandmaster", "250 заточек",  "250 enchants",  "ach_enchant_done", AchBonusType.ENCHANT_FLAT, 16f, tier = 5),
        // ach_teeth: 500/2k/5k
        AchievementDef("ach_teeth_1", "Коллектор I",   "Collector I",   "500 зубов",   "500 teeth",   "ach_teeth", AchBonusType.DROP_RATE_PERCENT, 0.01f, tier = 1),
        AchievementDef("ach_teeth_2", "Коллектор II",  "Collector II",  "1000 зубов",  "1000 teeth",  "ach_teeth", AchBonusType.DROP_RATE_PERCENT, 0.02f, tier = 2),
        AchievementDef("ach_teeth_3", "Коллектор III", "Collector III", "2500 зубов",  "2500 teeth",  "ach_teeth", AchBonusType.DROP_RATE_PERCENT, 0.03f, tier = 3),
        AchievementDef("ach_teeth_4", "Коллектор IV",  "Collector IV",  "5000 зубов",  "5000 teeth",  "ach_teeth", AchBonusType.DROP_RATE_PERCENT, 0.04f, tier = 4),
        AchievementDef("ach_teeth_5", "Коллектор V",   "Collector V",   "10000 зубов",  "10000 teeth",  "ach_teeth", AchBonusType.DROP_RATE_PERCENT, 0.05f, tier = 5)
    )

    val ALL: List<AchievementDef> = UNIQUE + PROGRESSIVE

    // ===================== СЕРИАЛИЗАЦИЯ =====================

    fun serialize(list: List<UnlockedAchievement>): String =
        if (list.isEmpty()) "" else gson.toJson(list)

    fun serializeUnlocked(list: List<UnlockedAchievement>): String = serialize(list)
    fun getUnlocked(json: String): List<UnlockedAchievement> = deserialize(json)

    fun deserialize(json: String): List<UnlockedAchievement> {
        if (json.isBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<UnlockedAchievement>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    fun getDefById(id: String): AchievementDef? = ALL.find { it.id == id }

    // ===================== БОНУСЫ =====================

    fun getActiveBonuses(activeIdsStr: String): AchievementBonuses {
        if (activeIdsStr.isBlank()) return AchievementBonuses()
        val ids = activeIdsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        var xp = 0f; var dmg = 0f; var drop = 0f; var armor = 0f
        var hp = 0f; var crit = 0f; var enchant = 0f; var teeth = 0f
        for (id in ids) {
            val def = getDefById(id) ?: continue
            when (def.bonusType) {
                AchBonusType.XP_PERCENT          -> xp      += def.bonusValue
                AchBonusType.DAMAGE_PERCENT       -> dmg     += def.bonusValue
                AchBonusType.DROP_RATE_PERCENT    -> drop    += def.bonusValue
                AchBonusType.ARMOR_PERCENT        -> armor   += def.bonusValue
                AchBonusType.HP_FLAT              -> hp      += def.bonusValue
                AchBonusType.CRIT_PERCENT         -> crit    += def.bonusValue
                AchBonusType.ENCHANT_FLAT         -> enchant += def.bonusValue
                AchBonusType.TEETH_RATE_PERCENT   -> teeth   += def.bonusValue
            }
        }
        return AchievementBonuses(xp, dmg, drop, armor, hp.toInt(), crit, enchant, teeth)
    }

    fun getBonusLabel(def: AchievementDef, language: String): String {
        val v = def.bonusValue
        return when (def.bonusType) {
            AchBonusType.XP_PERCENT          -> "+${(v * 100).toInt()}% XP"
            AchBonusType.DAMAGE_PERCENT       -> "+${(v * 100).toInt()}% урон"
            AchBonusType.DROP_RATE_PERCENT    -> "+${(v * 100).toInt()}% дроп"
            AchBonusType.ARMOR_PERCENT        -> "+${(v * 100).toInt()}% броня"
            AchBonusType.HP_FLAT              -> "+${v.toInt()} HP"
            AchBonusType.CRIT_PERCENT         -> "+${(v * 100).toInt()}% крит"
            AchBonusType.ENCHANT_FLAT         -> "+${v.toInt()}% заточка"
            AchBonusType.TEETH_RATE_PERCENT   -> "+${(v * 100).toInt()}% зубы"
        }
    }

    // ===================== ПРОВЕРКА И РАЗБЛОКИРОВКА =====================

    fun checkAndUnlock(state: GameStateEntity, today: String): GameStateEntity {
        val unlocked = deserialize(state.achievementsJson).toMutableList()
        val unlockedIds = unlocked.map { it.defId }.toMutableSet()
        val failedEnchants = (state.totalEnchantmentsSuccess.let {
            // estimate: spent - success (stored in state separately)
            // Используем totalTeethSpent как прокси — но это неточно.
            // Вместо этого добавляем отдельный счётчик в состояние через косвенный расчет:
            // Для упрощения: проверяем накопленные данные
            0
        })

        fun tryUnlock(id: String) {
            if (id !in unlockedIds) {
                unlocked.add(UnlockedAchievement(id, today))
                unlockedIds.add(id)
            }
        }

        // --- Уникальные ---
        if (state.monstersKilled >= 1)           tryUnlock("ach_first_blood")
        if (state.currentStreak >= 30)            tryUnlock("ach_unstoppable")
        if (state.totalTeethEarned >= 5000)       tryUnlock("ach_rich")
        if (state.totalEnchantmentsSuccess >= 1 && state.highestDamage > 0) { /* проверяется при событии */ }
        val allSlotsEquipped = listOf(state.equippedHead, state.equippedNecklace, state.equippedWeapon1,
            state.equippedWeapon2, state.equippedPants, state.equippedBoots).all { it.isNotEmpty() }
        if (allSlotsEquipped)                     tryUnlock("ach_full_wardrobe")
        if (state.totalItemsMerged >= 10)         tryUnlock("ach_alchemist")

        // --- Боссы: убийства конкретных боссов ---
        val bossKills: Map<String, Int> = try {
            val type = object : TypeToken<Map<String, Int>>() {}.type
            gson.fromJson(state.bossKillsJson, type) ?: emptyMap()
        } catch (e: Exception) { emptyMap() }
        if ((bossKills["Abyssal Reaper"] ?: 0) >= 20) tryUnlock("ach_abyssal_reaper")
        if ((bossKills["Skull Crusher"]  ?: 0) >= 15) tryUnlock("ach_skull_crusher")
        if ((bossKills["Sky Sentry"]     ?: 0) >= 10) tryUnlock("ach_sky_sentry")
        if ((bossKills["Heat Cannon"]    ?: 0) >=  5) tryUnlock("ach_heat_cannon")

        // --- VOID: экипированы все 5 предметов набора ---
        val w1base = state.equippedWeapon1.split(":")[0].let { s -> s.split("_").let { p -> if (p.size > 2 && p.last().all { it.isDigit() } && p.last().length > 8) p.dropLast(1).joinToString("_") else s } }
        val w2base = state.equippedWeapon2.split(":")[0].let { s -> s.split("_").let { p -> if (p.size > 2 && p.last().all { it.isDigit() } && p.last().length > 8) p.dropLast(1).joinToString("_") else s } }
        val hasVoidHelm   = state.equippedHead.startsWith("set_void_head")
        val hasVoidW1     = w1base == "set_void_weapon1" || w2base == "set_void_weapon1"
        val hasVoidW2     = w1base == "set_void_weapon2" || w2base == "set_void_weapon2"
        val hasVoidPants  = state.equippedPants.startsWith("set_void_pants")
        val hasVoidBoots  = state.equippedBoots.startsWith("set_void_boots")
        if (hasVoidHelm && hasVoidW1 && hasVoidW2 && hasVoidPants && hasVoidBoots) tryUnlock("ach_void")

        // --- Прогрессивные: убийства ---
        val kills = state.monstersKilled
        if (kills >= 10)   tryUnlock("ach_kills_1")
        if (kills >= 50)   tryUnlock("ach_kills_2")
        if (kills >= 100)  tryUnlock("ach_kills_3")
        if (kills >= 500)  tryUnlock("ach_kills_4")
        if (kills >= 1000) tryUnlock("ach_kills_5")
        if (kills >= 2000) tryUnlock("ach_kills_6")


        // --- Прогрессивные: отжимания ---
        val pushups = state.totalPushUpsAllTime
        if (pushups >= 100)   tryUnlock("ach_pushups_1")
        if (pushups >= 500)   tryUnlock("ach_pushups_2")
        if (pushups >= 1000)  tryUnlock("ach_pushups_3")
        if (pushups >= 5000)  tryUnlock("ach_pushups_4")
        if (pushups >= 10000) tryUnlock("ach_pushups_5")
        if (pushups >= 20000) tryUnlock("ach_pushups_6")
        if (pushups >= 30000) tryUnlock("ach_pushups_7")

        // --- Прогрессивные: стрик ---
        val streak = state.currentStreak
        if (streak >= 3)  tryUnlock("ach_streak_1")
        if (streak >= 7)  tryUnlock("ach_streak_2")
        if (streak >= 14) tryUnlock("ach_streak_3")
        if (streak >= 30) tryUnlock("ach_streak_4")
        if (streak >= 60) tryUnlock("ach_streak_5")

        // --- Прогрессивные: заточки ---
        val enchants = state.totalEnchantmentsSuccess
        if (enchants >= 5)  tryUnlock("ach_enchant_done_1")
        if (enchants >= 20) tryUnlock("ach_enchant_done_2")
        if (enchants >= 50) tryUnlock("ach_enchant_done_3")
        if (enchants >= 100) tryUnlock("ach_enchant_done_4")
        if (enchants >= 250) tryUnlock("ach_enchant_done_5")

        // --- Прогрессивные: зубы ---
        val teeth = state.totalTeethEarned
        if (teeth >= 500)  tryUnlock("ach_teeth_1")
        if (teeth >= 1000) tryUnlock("ach_teeth_2")
        if (teeth >= 2500) tryUnlock("ach_teeth_3")
        if (teeth >= 5000) tryUnlock("ach_teeth_4")
        if (teeth >= 10000) tryUnlock("ach_teeth_5")

        if (unlocked.size == deserialize(state.achievementsJson).size) return state
        return state.copy(achievementsJson = serialize(unlocked))
    }
}
