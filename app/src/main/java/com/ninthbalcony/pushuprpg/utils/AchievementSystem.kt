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
        AchievementDef("ach_first_blood",     "Первая кровь",          "First Blood",        "Убей первого монстра",            "Kill your first monster",          "monster_1",           AchBonusType.DAMAGE_PERCENT,    0.02f),
        AchievementDef("ach_berserker",       "Берсерк",               "Berserker",          "50+ отжиманий за одну сессию",    "50+ push-ups in one session",      "ach_berserker",       AchBonusType.XP_PERCENT,        0.05f),
        AchievementDef("ach_unstoppable",     "Неудержимый",           "Unstoppable",        "Стрик 30 дней",                   "30-day streak",                    "ach_unstoppable",     AchBonusType.DAMAGE_PERCENT,    0.03f),
        AchievementDef("ach_rich",            "Богач",                 "Rich",               "Заработай 5000 зубов",            "Earn 5000 teeth total",            "ach_rich",            AchBonusType.TEETH_RATE_PERCENT,0.05f),
        AchievementDef("ach_phoenix",         "Феникс",                "Phoenix",            "Вернись после 7 дней отсутствия", "Return after 7 days absent",       "ach_phoenix",         AchBonusType.HP_FLAT,           20f),
        AchievementDef("ach_legendary_catch", "Легендарный улов",      "Legendary Catch",    "Получи предмет legendary",        "Get a legendary item",             "ach_legendary_catch", AchBonusType.DROP_RATE_PERCENT, 0.02f),
        AchievementDef("ach_epic_catch",      "Эпический улов",        "Epic Catch",         "Получи предмет epic",             "Get an epic item",                 "ach_epic_catch",      AchBonusType.DROP_RATE_PERCENT, 0.01f),
        AchievementDef("ach_full_wardrobe",   "Полный гардероб",       "Full Wardrobe",      "Заполни все 6 слотов экипировки", "Fill all 6 equipment slots",       "ach_full_wardrobe",   AchBonusType.ARMOR_PERCENT,     0.05f),
        AchievementDef("ach_failed_enchants", "Коллекционер затычек",  "Failure Collector",  "100 провалов заточки",            "100 failed enchantments",          "ach_failed_enchants", AchBonusType.ENCHANT_FLAT,      6f),
        AchievementDef("ach_no_sweat",        "Ни капли пота",         "No Sweat",           "Убей босса с HP > 50%",           "Kill a boss with HP above 50%",    "ach_no_sweat",        AchBonusType.DAMAGE_PERCENT,    0.05f),
        AchievementDef("ach_night_shift",     "Ночная смена",          "Night Shift",        "Отжимания после 23:00",           "Push-ups after 23:00",             "ach_night_shift",     AchBonusType.XP_PERCENT,        0.03f),
        AchievementDef("ach_early_bird",      "Ранняя пташка",         "Early Bird",         "Отжимания до 7:00",               "Push-ups before 7:00",             "ach_early_bird",      AchBonusType.XP_PERCENT,        0.03f),
        AchievementDef("ach_immortal",        "Бессмертный",           "Immortal",           "7-дневный стрик",                 "7-day streak",                     "ach_immortal",        AchBonusType.HP_FLAT,           25f),
        AchievementDef("ach_alchemist",       "Алхимик",               "Alchemist",          "10 операций в Forge",             "10 Forge operations",              "ach_alchemist",       AchBonusType.ENCHANT_FLAT,      4f),
        AchievementDef("ach_dragon_slayer",   "Покоритель дракона",    "Dragon Slayer",      "Убей Ancient Dragon",             "Kill the Ancient Dragon",          "boss_ancient_dragon", AchBonusType.DAMAGE_PERCENT,    0.08f),
        AchievementDef("ach_critical",        "Критический момент",    "Critical Moment",    "Крит во время Burst-атаки",       "Land a crit during a Burst attack","ach_critical",        AchBonusType.CRIT_PERCENT,      0.02f),
        AchievementDef("ach_abyssal_reaper",  "Губитель Бездны",       "Abyss Slayer",       "Убей Бездонного Жнеца 20 раз",    "Kill Abyssal Reaper 20 times",     "boss_abyssal_reaper", AchBonusType.DAMAGE_PERCENT,    0.04f),
        AchievementDef("ach_skull_crusher",   "Истребитель Черепов",   "Skull Hunter",       "Убей Сокрушителя Черепов 20 раз", "Kill Skull Crusher 20 times",      "boss_skull_crusher",  AchBonusType.CRIT_PERCENT,      0.04f),
        AchievementDef("ach_sky_sentry",      "Покоритель Небес",      "Sky Conqueror",      "Убей Стража Небес 10 раз",        "Kill Sky Sentry 10 times",         "boss_sky_sentry",     AchBonusType.XP_PERCENT,        0.08f),
        AchievementDef("ach_heat_cannon",     "Огнеборец",             "Flame Fighter",      "Убей Пушку Пламени 5 раз",        "Kill Heat Cannon 5 times",         "boss_heat_cannon",    AchBonusType.HP_FLAT,           60f),
        AchievementDef("ach_void",            "VOID ПОЛУЧЕН. VOID ПОЛУЧИЛ ТЕБЯ.", "YOU GOT THE VOID, VOID GOT YOU.", "Экипируй все 5 предметов набора VOID", "Equip all 5 pieces of the VOID set", "ach_void",        AchBonusType.DAMAGE_PERCENT,    0.12f),
        AchievementDef("ach_diablo_first",    "Повелитель Ада",        "Hell Lord",          "Убей Дьявола Бездны 3 раза",      "Kill Diablo 3 times",              "boss_dib",            AchBonusType.DAMAGE_PERCENT,    0.08f),
        AchievementDef("ach_iron_bull_5",     "Бычья охота",           "Bull Hunt",          "Убей Iron Bull 5 раз",            "Kill Iron Bull 5 times",           "boss_oven",           AchBonusType.HP_FLAT,           80f),
        AchievementDef("ach_king_slayer",     "Убийца Королей",        "King Slayer",        "Убей Королевского Клона 3 раза",  "Kill King Clone 3 times",          "boss_king",           AchBonusType.DAMAGE_PERCENT,    0.10f),
        AchievementDef("ach_unrealm_set",     "Облачение Преисподней", "Underrealm Clad",    "Надень 4/4 Unrealm",              "Equip 4/4 Unrealm",                "boss_underworld_demon",AchBonusType.XP_PERCENT,        0.15f),
        AchievementDef("ach_holy_set",        "Благодать Эмпирея",     "Empyrean Grace",     "Надень 4/4 Empyrean",             "Equip 4/4 Empyrean",               "boss_iron_golem",     AchBonusType.ARMOR_PERCENT,     0.20f),
        AchievementDef("ach_clone_killer",    "Убийца клонов",         "Clone Killer",       "Убей Накаченного Клона 10 раз",   "Kill Pumped Clone 10 times",       "monster_42",          AchBonusType.DAMAGE_PERCENT,    0.06f),
        AchievementDef("ach_cursed_hunter",   "Охотник на проклятых",  "Cursed Hunter",      "Убей Поражённого Колдуна 10 раз", "Kill Cursed Warlock 10 times",     "monster_45",          AchBonusType.DROP_RATE_PERCENT, 0.05f),
        AchievementDef("ach_enchant_done_2_night","Первая ночная заточка","First Night Forge","Используй Night Grindstone 1 раз","Use Night Grindstone once",        "ach_enchant_done",    AchBonusType.ENCHANT_FLAT,      3f),
        AchievementDef("ach_forge_dmg_9",     "Закалённая сталь",      "Hardened Steel",     "Заточи вещь до +9",               "Enchant an item to +9",            "ach_enchant_done",    AchBonusType.DAMAGE_PERCENT,    0.02f),
        AchievementDef("ach_forge_dmg_15",    "Мощь заточки",          "Enchant Power",      "Заточи вещь до +15",              "Enchant an item to +15",           "ach_enchant_done",    AchBonusType.DAMAGE_PERCENT,    0.03f),
        AchievementDef("ach_forge_dmg_20",    "Сила разрушения",       "Destructive Force",  "Заточи вещь до +20",              "Enchant an item to +20",           "ach_enchant_done",    AchBonusType.DAMAGE_PERCENT,    0.05f),
        AchievementDef("ach_forge_dmg_25",    "Абсолютная заточка",    "Absolute Edge",      "Заточи вещь до +25",              "Enchant an item to +25",           "ach_enchant_done",    AchBonusType.DAMAGE_PERCENT,    0.05f),
        // 10 новых достижений
        AchievementDef("ach_stone_giant_3",   "Каменный охотник",      "Stone Hunter",       "Убей Каменного Великана 3 раза",  "Kill Stone Giant 3 times",         "boss_stone_giant",    AchBonusType.ARMOR_PERCENT,     0.03f),
        AchievementDef("ach_iron_golem_5",    "Голем-охотник",         "Golem Hunter",       "Убей Железного Голема 5 раз",     "Kill Iron Golem 5 times",          "boss_iron_golem",     AchBonusType.HP_FLAT,           50f),
        AchievementDef("ach_shadow_lord_5",   "Победитель Тьмы",       "Shadow Breaker",     "Убей Повелителя Теней 5 раз",     "Kill Shadow Lord 5 times",         "boss_shadow_lord",    AchBonusType.DAMAGE_PERCENT,    0.05f),
        AchievementDef("ach_blood_witch_3",   "Охотник на ведьм",      "Witch Hunter",       "Убей Кровавую Ведьму 3 раза",     "Kill Blood Witch 3 times",         "boss_blood_witch",    AchBonusType.DROP_RATE_PERCENT, 0.03f),
        AchievementDef("ach_bone_cube_3",     "Кубоборец",             "Cube Breaker",       "Убей Костяного Куба 3 раза",      "Kill Bone Cube 3 times",           "boss_cube",           AchBonusType.ARMOR_PERCENT,     0.05f),
        AchievementDef("ach_fleshmeat_5",     "Мясорубщик",            "Flesh Reaper",       "Убей Монстра Плоти 5 раз",        "Kill Flesh Monster 5 times",       "boss_fleshmeat",      AchBonusType.DAMAGE_PERCENT,    0.04f),
        AchievementDef("ach_grinder_5",       "Перемолотый",           "Grind Master",       "Убей Скрежет 5 раз",              "Kill The Grinder 5 times",         "boss_the_grinder",    AchBonusType.XP_PERCENT,        0.05f),
        AchievementDef("ach_underworld_3",    "Повелитель демонов",    "Demon Lord",         "Убей Подземного Демона 3 раза",   "Kill Underworld Demon 3 times",    "boss_underworld_demon",AchBonusType.DAMAGE_PERCENT,    0.06f),
        AchievementDef("ach_goblin_hunter",   "Охотник на гоблинов",   "Goblin Hunter",      "Победи Золотого Гоблина",         "Defeat the Golden Goblin",         "monster_goblin_gold", AchBonusType.TEETH_RATE_PERCENT,0.05f),
        AchievementDef("ach_prestige_1",      "Элита",                 "Elite",              "Достигни Prestige 1",             "Reach Prestige 1",                 "ach_master_enchant",  AchBonusType.DAMAGE_PERCENT,    0.08f),
        // ===== TASK 5: новые уникальные =====
        AchievementDef("ach_iron_fists",      "Стальные кулаки",       "Iron Fists",         "500 критических ударов",          "500 critical hits",                "ach_critical",        AchBonusType.CRIT_PERCENT,      0.03f),
        AchievementDef("ach_hoarder",         "Накопитель",            "Hoarder",            "Собери 100 предметов",            "Collect 100 items",                "ach_full_wardrobe",   AchBonusType.DROP_RATE_PERCENT, 0.03f),
        AchievementDef("ach_destroyer",       "Разрушитель",           "Destroyer",          "1 000 000 суммарного урона",      "1,000,000 total damage dealt",     "ach_first_blood",     AchBonusType.DAMAGE_PERCENT,    0.03f),
        AchievementDef("ach_prestige_3",      "Ветеран",               "Veteran",            "Достигни Prestige 3",             "Reach Prestige 3",                 "ach_master_enchant",  AchBonusType.DAMAGE_PERCENT,    0.06f),
        AchievementDef("ach_big_burst",       "Мощный рывок",          "Power Surge",        "100+ отжиманий за сессию",        "100+ push-ups in one session",     "ach_berserker",       AchBonusType.XP_PERCENT,        0.06f),
        AchievementDef("ach_forge_master",    "Великий Кузнец",        "Grand Smith",        "50 слияний предметов",            "50 item merges",                   "ach_alchemist",       AchBonusType.ENCHANT_FLAT,      5f),
        AchievementDef("ach_conqueror",       "Покоритель",            "Conqueror",          "Убей монстра 50+ уровня",         "Kill a level 50+ monster",         "boss_fleshmeat",      AchBonusType.DAMAGE_PERCENT,    0.04f),
        AchievementDef("ach_teeth_hoarder",   "Зубной Барон",          "Teeth Baron",        "25 000 зубов суммарно",           "25,000 total teeth earned",        "ach_teeth",           AchBonusType.TEETH_RATE_PERCENT,0.08f),
        AchievementDef("ach_sniper",          "Снайпер",               "Sniper",             "Один удар > 2000 урона",          "Single hit over 2000 damage",      "ach_skull_crusher",   AchBonusType.CRIT_PERCENT,      0.04f),
        // ===== TASK 6: доп. тиры боссов =====
        AchievementDef("ach_stone_giant_10",  "Каменный истребитель",  "Stone Exterminator", "Убей Каменного Великана 10 раз",  "Kill Stone Giant 10 times",        "boss_stone_giant",    AchBonusType.ARMOR_PERCENT,     0.05f),
        AchievementDef("ach_shadow_lord_15",  "Тёмный охотник",        "Shadow Hunter",      "Убей Повелителя Теней 15 раз",   "Kill Shadow Lord 15 times",        "boss_shadow_lord",    AchBonusType.DAMAGE_PERCENT,    0.04f),
        AchievementDef("ach_blood_witch_10",  "Ведьмовской следопыт",  "Witch Tracker",      "Убей Кровавую Ведьму 10 раз",    "Kill Blood Witch 10 times",        "boss_blood_witch",    AchBonusType.DROP_RATE_PERCENT, 0.05f),
        AchievementDef("ach_fleshmeat_15",    "Мясник",                "Butcher",            "Убей Монстра Плоти 15 раз",      "Kill Flesh Monster 15 times",      "boss_fleshmeat",      AchBonusType.DAMAGE_PERCENT,    0.03f),
        AchievementDef("ach_underworld_10",   "Демонолог",             "Demonologist",       "Убей Подземного Демона 10 раз",  "Kill Underworld Demon 10 times",   "boss_underworld_demon",AchBonusType.DAMAGE_PERCENT,   0.03f),
        AchievementDef("ach_iron_golem_10",   "Железный охотник",      "Iron Hunter",        "Убей Железного Голема 10 раз",   "Kill Iron Golem 10 times",         "boss_iron_golem",     AchBonusType.ARMOR_PERCENT,     0.03f),
        // ===== TASK 7: 50 убийств поздних монстров =====
        AchievementDef("ach_cursed_50",       "Охотник на проклятых II","Cursed Slayer II",  "Убей Поражённого Колдуна 50 раз","Kill Cursed Warlock 50 times",     "monster_45",          AchBonusType.DROP_RATE_PERCENT, 0.05f),
        AchievementDef("ach_one_arm_50",      "Охотник Мутантов",      "Mutant Hunter",      "Убей Однорукого Мутанта 50 раз", "Kill One-Arm Mutant 50 times",     "monster_44",          AchBonusType.DAMAGE_PERCENT,    0.04f),
        AchievementDef("ach_demon_knight_50", "Убийца Клонов II",      "Clone Slayer II",    "Убей Клона-Рыцаря 50 раз",       "Kill Demon Knight Clone 50 times", "monster_43",          AchBonusType.CRIT_PERCENT,      0.04f),
        AchievementDef("ach_clone_50",        "Клономор",              "Clone Reaper",       "Убей Накачанного Клона 50 раз",  "Kill Pumped Clone 50 times",       "monster_42",          AchBonusType.DAMAGE_PERCENT,    0.05f),
        AchievementDef("ach_abyss_50",        "Пожиратель Бездны",     "Abyss Devourer",     "Убей Бездну 50 раз",             "Kill The Abyss 50 times",          "monster_40",          AchBonusType.ARMOR_PERCENT,     0.05f),
        AchievementDef("ach_void_titan_50",   "Титанобой",             "Titan Slayer",       "Убей Титана Пустоты 50 раз",     "Kill Void Titan 50 times",         "monster_40",          AchBonusType.DAMAGE_PERCENT,    0.03f)
    )

    private val PROGRESSIVE: List<AchievementDef> = listOf(
        // ach_kills: 10/50/100/500/1000
        AchievementDef("ach_kills_1", "Охотник I",    "Hunter I",    "10 убийств",    "10 kills",    "ach_kills", AchBonusType.DAMAGE_PERCENT, 0.01f, tier = 1),
        AchievementDef("ach_kills_2", "Охотник II",   "Hunter II",   "50 убийств",    "50 kills",    "ach_kills", AchBonusType.DAMAGE_PERCENT, 0.02f, tier = 2),
        AchievementDef("ach_kills_3", "Охотник III",  "Hunter III",  "100 убийств",   "100 kills",   "ach_kills", AchBonusType.DAMAGE_PERCENT, 0.03f, tier = 3),
        AchievementDef("ach_kills_4", "Охотник IV",   "Hunter IV",   "500 убийств",   "500 kills",   "ach_kills", AchBonusType.DAMAGE_PERCENT, 0.04f, tier = 4),
        AchievementDef("ach_kills_5", "Охотник V",    "Hunter V",    "1000 убийств",  "1000 kills",  "ach_kills", AchBonusType.DAMAGE_PERCENT, 0.05f, tier = 5),
        AchievementDef("ach_kills_6", "Легенда боя",  "War Legend",  "2000 убийств",  "2000 kills",  "ach_kills", AchBonusType.DAMAGE_PERCENT, 0.06f, tier = 6),
        AchievementDef("ach_kills_7", "Истребитель", "Exterminator","5000 убийств",  "5000 kills",  "ach_kills", AchBonusType.DAMAGE_PERCENT, 0.08f, tier = 7),
        // ach_pushups: 100/500/1k/5k/10k/20k/30k/50k
        AchievementDef("ach_pushups_1", "Новичок I",     "Rookie I",      "100 отжиманий",   "100 push-ups",   "ach_pushups", AchBonusType.XP_PERCENT, 0.01f, tier = 1),
        AchievementDef("ach_pushups_2", "Новичок II",    "Rookie II",     "500 отжиманий",   "500 push-ups",   "ach_pushups", AchBonusType.XP_PERCENT, 0.02f, tier = 2),
        AchievementDef("ach_pushups_3", "Атлет I",       "Athlete I",     "1000 отжиманий",  "1000 push-ups",  "ach_pushups", AchBonusType.XP_PERCENT, 0.03f, tier = 3),
        AchievementDef("ach_pushups_4", "Атлет II",      "Athlete II",    "5000 отжиманий",  "5000 push-ups",  "ach_pushups", AchBonusType.XP_PERCENT, 0.04f, tier = 4),
        AchievementDef("ach_pushups_5", "Чемпион",       "Champion",      "10000 отжиманий", "10000 push-ups", "ach_pushups", AchBonusType.XP_PERCENT, 0.05f, tier = 5),
        AchievementDef("ach_pushups_6", "Легенда",       "Legend",        "20000 отжиманий", "20000 push-ups", "ach_pushups", AchBonusType.XP_PERCENT, 0.08f, tier = 6),
        AchievementDef("ach_pushups_7", "Величайший",    "Greatest",      "30000 отжиманий", "30000 push-ups", "ach_pushups", AchBonusType.XP_PERCENT, 0.10f, tier = 7),
        AchievementDef("ach_pushups_8", "Бог отжиманий", "Push-up God",   "50000 отжиманий", "50000 push-ups", "ach_pushups", AchBonusType.XP_PERCENT, 0.15f, tier = 8),
        // ach_streak: 3/7/14/30/60/90
        AchievementDef("ach_streak_1", "Стойкий I",     "Steadfast I",   "Стрик 3 дня",    "3-day streak",   "ach_streak", AchBonusType.XP_PERCENT, 0.01f, tier = 1),
        AchievementDef("ach_streak_2", "Стойкий II",    "Steadfast II",  "Стрик 7 дней",   "7-day streak",   "ach_streak", AchBonusType.XP_PERCENT, 0.02f, tier = 2),
        AchievementDef("ach_streak_3", "Стойкий III",   "Steadfast III", "Стрик 14 дней",  "14-day streak",  "ach_streak", AchBonusType.XP_PERCENT, 0.03f, tier = 3),
        AchievementDef("ach_streak_4", "Железная воля", "Iron Will",     "Стрик 30 дней",  "30-day streak",  "ach_streak", AchBonusType.XP_PERCENT, 0.04f, tier = 4),
        AchievementDef("ach_streak_5", "Несломленный",  "Unbroken",      "Стрик 60 дней",  "60-day streak",  "ach_streak", AchBonusType.XP_PERCENT, 0.05f, tier = 5),
        AchievementDef("ach_streak_6", "Железный монах","Iron Monk",     "Стрик 90 дней",  "90-day streak",  "ach_streak", AchBonusType.XP_PERCENT, 0.06f, tier = 6),
        // ach_enchant_done: 5/20/50/100/250/500
        AchievementDef("ach_enchant_done_1", "Новичок",      "Newbie",      "5 заточек",   "5 enchants",   "ach_enchant_done", AchBonusType.ENCHANT_FLAT, 2f, tier = 1),
        AchievementDef("ach_enchant_done_2", "Ученик",       "Student",     "20 заточек",  "20 enchants",  "ach_enchant_done", AchBonusType.ENCHANT_FLAT, 3f, tier = 2),
        AchievementDef("ach_enchant_done_3", "Подмастерье",  "Apprentice",  "50 заточек",  "50 enchants",  "ach_enchant_done", AchBonusType.ENCHANT_FLAT, 4f, tier = 3),
        AchievementDef("ach_enchant_done_4", "Мастер",       "Master",      "100 заточек",  "100 enchants",  "ach_enchant_done", AchBonusType.ENCHANT_FLAT, 6f, tier = 4),
        AchievementDef("ach_enchant_done_5", "Гроссмейстер", "Grandmaster", "250 заточек",  "250 enchants",  "ach_enchant_done", AchBonusType.ENCHANT_FLAT, 8f, tier = 5),
        AchievementDef("ach_enchant_done_6", "Серийный мастер","Serial Master","500 заточек", "500 enchants",  "ach_enchant_done", AchBonusType.ENCHANT_FLAT, 10f, tier = 6),
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
        val pct = (v * 100).toInt()
        return when (def.bonusType) {
            AchBonusType.XP_PERCENT        -> "+$pct% XP"
            AchBonusType.DAMAGE_PERCENT    -> "+$pct% " + when (language) { "ru" -> "урон"; "es" -> "daño"; "fr" -> "dégâts"; "de" -> "Schaden"; "pt" -> "dano"; else -> "DMG" }
            AchBonusType.DROP_RATE_PERCENT -> "+$pct% " + when (language) { "ru" -> "дроп"; "es" -> "botín"; "fr" -> "butin"; "de" -> "Beute"; "pt" -> "loot"; else -> "drop" }
            AchBonusType.ARMOR_PERCENT     -> "+$pct% " + when (language) { "ru" -> "броня"; "es" -> "armadura"; "fr" -> "armure"; "de" -> "Rüstung"; "pt" -> "armadura"; else -> "armor" }
            AchBonusType.HP_FLAT           -> "+${v.toInt()} HP"
            AchBonusType.CRIT_PERCENT      -> "+$pct% " + when (language) { "ru" -> "крит"; "es" -> "crítico"; "fr" -> "critique"; "de" -> "Krit"; "pt" -> "crítico"; else -> "crit" }
            AchBonusType.ENCHANT_FLAT      -> "+${v.toInt()} " + when (language) { "ru" -> "заточка"; "es" -> "forja"; "fr" -> "forge"; "de" -> "Schmieden"; "pt" -> "forja"; else -> "forge" }
            AchBonusType.TEETH_RATE_PERCENT-> "+$pct% " + when (language) { "ru" -> "зубы"; "es" -> "dientes"; "fr" -> "dents"; "de" -> "Zähne"; "pt" -> "dentes"; else -> "teeth" }
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
        if (state.monstersKilled >= 1)                                               tryUnlock("ach_first_blood")
        if (state.bestSingleSession >= 50)                                           tryUnlock("ach_berserker")
        if (state.currentStreak >= 30)                                               tryUnlock("ach_unstoppable")
        if (state.currentStreak >= 7)                                                tryUnlock("ach_immortal")
        if (state.totalTeethEarned >= 5000)                                          tryUnlock("ach_rich")
        if ((state.totalEnchantAttempts - state.totalEnchantmentsSuccess) >= 100)    tryUnlock("ach_failed_enchants")
        if (state.prestigeLevel >= 1)                                                tryUnlock("ach_prestige_1")
        if (state.prestigeLevel >= 3)                                                tryUnlock("ach_prestige_3")
        if (state.totalCriticalHits >= 500)                                          tryUnlock("ach_iron_fists")
        if (state.itemsCollected >= 100)                                             tryUnlock("ach_hoarder")
        if (state.totalDamageDealt >= 1_000_000)                                     tryUnlock("ach_destroyer")
        if (state.bestSingleSession >= 100)                                          tryUnlock("ach_big_burst")
        if (state.totalItemsMerged >= 50)                                            tryUnlock("ach_forge_master")
        if (state.highestMonsterLevelKilled >= 50)                                   tryUnlock("ach_conqueror")
        if (state.totalTeethEarned >= 25_000)                                        tryUnlock("ach_teeth_hoarder")
        if (state.highestDamage >= 2_000)                                            tryUnlock("ach_sniper")
        val allSlotsEquipped = listOf(state.equippedHead, state.equippedNecklace, state.equippedWeapon1,
            state.equippedWeapon2, state.equippedPants, state.equippedBoots).all { it.isNotEmpty() }
        if (allSlotsEquipped)                                                        tryUnlock("ach_full_wardrobe")
        if (state.totalItemsMerged >= 10)                                            tryUnlock("ach_alchemist")

        // --- Боссы: убийства конкретных боссов ---
        val bossKills: Map<String, Int> = try {
            val type = object : TypeToken<Map<String, Int>>() {}.type
            gson.fromJson(state.bossKillsJson, type) ?: emptyMap()
        } catch (e: Exception) { emptyMap() }
        if ((bossKills["Ancient Dragon"]         ?: 0) >=  1) tryUnlock("ach_dragon_slayer")
        if ((bossKills["Abyssal Reaper"]         ?: 0) >= 20) tryUnlock("ach_abyssal_reaper")
        if ((bossKills["Skull Crusher"]          ?: 0) >= 20) tryUnlock("ach_skull_crusher")
        if ((bossKills["Sky Sentry"]             ?: 0) >= 10) tryUnlock("ach_sky_sentry")
        if ((bossKills["Heat Cannon"]            ?: 0) >=  5) tryUnlock("ach_heat_cannon")
        if ((bossKills["Diablo"]                 ?: 0) >=  3) tryUnlock("ach_diablo_first")
        if ((bossKills["Iron Bull"]              ?: 0) >=  5) tryUnlock("ach_iron_bull_5")
        if ((bossKills["King Clone"]             ?: 0) >=  3) tryUnlock("ach_king_slayer")
        if ((bossKills["Stone Giant"]            ?: 0) >= 10) tryUnlock("ach_stone_giant_10")
        if ((bossKills["Shadow Lord"]            ?: 0) >= 15) tryUnlock("ach_shadow_lord_15")
        if ((bossKills["Blood Witch"]            ?: 0) >= 10) tryUnlock("ach_blood_witch_10")
        if ((bossKills["Flesh and Meat Monster"] ?: 0) >= 15) tryUnlock("ach_fleshmeat_15")
        if ((bossKills["Underworld Demon"]       ?: 0) >= 10) tryUnlock("ach_underworld_10")
        if ((bossKills["Iron Golem"]             ?: 0) >= 10) tryUnlock("ach_iron_golem_10")
        if ((bossKills["Stone Giant"]            ?: 0) >=  3) tryUnlock("ach_stone_giant_3")
        if ((bossKills["Iron Golem"]             ?: 0) >=  5) tryUnlock("ach_iron_golem_5")
        if ((bossKills["Shadow Lord"]            ?: 0) >=  5) tryUnlock("ach_shadow_lord_5")
        if ((bossKills["Blood Witch"]            ?: 0) >=  3) tryUnlock("ach_blood_witch_3")
        if ((bossKills["Bone Cube"]              ?: 0) >=  3) tryUnlock("ach_bone_cube_3")
        if ((bossKills["Flesh and Meat Monster"] ?: 0) >=  5) tryUnlock("ach_fleshmeat_5")
        if ((bossKills["The Grinder"]            ?: 0) >=  5) tryUnlock("ach_grinder_5")
        if ((bossKills["Underworld Demon"]       ?: 0) >=  3) tryUnlock("ach_underworld_3")

        // --- Бестиарий: обычные монстры ---
        val bestiary: Map<String, Int> = try {
            val type = object : TypeToken<Map<String, Int>>() {}.type
            gson.fromJson(state.bestiaryJson, type) ?: emptyMap()
        } catch (e: Exception) { emptyMap() }
        if ((bestiary["Pumped Clone"]        ?: 0) >= 10) tryUnlock("ach_clone_killer")
        if ((bestiary["Cursed Warlock"]      ?: 0) >= 10) tryUnlock("ach_cursed_hunter")
        if ((bestiary["Cursed Warlock"]      ?: 0) >= 50) tryUnlock("ach_cursed_50")
        if ((bestiary["One-Arm Mutant"]      ?: 0) >= 50) tryUnlock("ach_one_arm_50")
        if ((bestiary["Demon Knight Clone"]  ?: 0) >= 50) tryUnlock("ach_demon_knight_50")
        if ((bestiary["Pumped Clone"]        ?: 0) >= 50) tryUnlock("ach_clone_50")
        if ((bestiary["The Abyss"]           ?: 0) >= 50) tryUnlock("ach_abyss_50")
        if ((bestiary["Void Titan"]          ?: 0) >= 50) tryUnlock("ach_void_titan_50")

        // --- Сеты: экипированные предметы ---
        val equipped6 = listOf(state.equippedHead, state.equippedNecklace,
            state.equippedWeapon1, state.equippedWeapon2, state.equippedPants, state.equippedBoots)
        val unrealmCount = equipped6.count { it.startsWith("set_un_") }
        val holyCount    = equipped6.count { it.startsWith("set_holy_") }
        if (unrealmCount >= 4) tryUnlock("ach_unrealm_set")
        if (holyCount    >= 4) tryUnlock("ach_holy_set")

        // --- Заточка: максимальный уровень в инвентаре ---
        val maxEnchantInInv = state.inventoryItems.split(",")
            .mapNotNull { it.split(":").getOrNull(1)?.toIntOrNull() }
            .maxOrNull() ?: 0
        if (maxEnchantInInv >= 9)  tryUnlock("ach_forge_dmg_9")
        if (maxEnchantInInv >= 10) tryUnlock("ach_enchant_done_2_night")
        if (maxEnchantInInv >= 15) tryUnlock("ach_forge_dmg_15")
        if (maxEnchantInInv >= 20) tryUnlock("ach_forge_dmg_20")
        if (maxEnchantInInv >= 25) tryUnlock("ach_forge_dmg_25")

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
        if (kills >= 5000) tryUnlock("ach_kills_7")


        // --- Прогрессивные: отжимания ---
        val pushups = state.totalPushUpsAllTime
        if (pushups >= 100)   tryUnlock("ach_pushups_1")
        if (pushups >= 500)   tryUnlock("ach_pushups_2")
        if (pushups >= 1000)  tryUnlock("ach_pushups_3")
        if (pushups >= 5000)  tryUnlock("ach_pushups_4")
        if (pushups >= 10000) tryUnlock("ach_pushups_5")
        if (pushups >= 20000) tryUnlock("ach_pushups_6")
        if (pushups >= 30000) tryUnlock("ach_pushups_7")
        if (pushups >= 50000) tryUnlock("ach_pushups_8")

        // --- Прогрессивные: стрик ---
        val streak = state.currentStreak
        if (streak >= 3)  tryUnlock("ach_streak_1")
        if (streak >= 7)  tryUnlock("ach_streak_2")
        if (streak >= 14) tryUnlock("ach_streak_3")
        if (streak >= 30) tryUnlock("ach_streak_4")
        if (streak >= 60) tryUnlock("ach_streak_5")
        if (streak >= 90) tryUnlock("ach_streak_6")

        // --- Прогрессивные: заточки ---
        val enchants = state.totalEnchantmentsSuccess
        if (enchants >= 5)  tryUnlock("ach_enchant_done_1")
        if (enchants >= 20) tryUnlock("ach_enchant_done_2")
        if (enchants >= 50) tryUnlock("ach_enchant_done_3")
        if (enchants >= 100) tryUnlock("ach_enchant_done_4")
        if (enchants >= 250) tryUnlock("ach_enchant_done_5")
        if (enchants >= 500) tryUnlock("ach_enchant_done_6")

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

    fun getProgressText(def: AchievementDef, state: GameStateEntity, language: String): String? {
        val mapType = object : TypeToken<Map<String, Int>>() {}.type
        val bossKills: Map<String, Int> = try { gson.fromJson(state.bossKillsJson, mapType) ?: emptyMap() } catch (e: Exception) { emptyMap() }
        val bestiary: Map<String, Int>  = try { gson.fromJson(state.bestiaryJson, mapType)  ?: emptyMap() } catch (e: Exception) { emptyMap() }
        val maxEnchant: Int = state.inventoryItems.split(",").mapNotNull { it.split(":").getOrNull(1)?.toIntOrNull() }.maxOrNull() ?: 0

        val (current, target) = when {
            // Specific IDs before startsWith patterns to avoid false matches
            def.id == "ach_teeth_hoarder"  -> state.totalTeethEarned to 25_000
            def.id == "ach_enchant_done_2_night" -> maxEnchant to 10

            def.id.startsWith("ach_kills_") -> {
                val t = when (def.tier) { 1->10; 2->50; 3->100; 4->500; 5->1000; 6->2000; 7->5000; else->0 }
                state.monstersKilled to t
            }
            def.id.startsWith("ach_pushups_") -> {
                val t = when (def.tier) { 1->100; 2->500; 3->1000; 4->5000; 5->10000; 6->20000; 7->30000; 8->50000; else->0 }
                state.totalPushUpsAllTime to t
            }
            def.id.startsWith("ach_streak_") -> {
                val t = when (def.tier) { 1->3; 2->7; 3->14; 4->30; 5->60; 6->90; else->0 }
                state.currentStreak to t
            }
            def.id.startsWith("ach_enchant_done_") -> {
                val t = when (def.tier) { 1->5; 2->20; 3->50; 4->100; 5->250; 6->500; else->0 }
                state.totalEnchantmentsSuccess to t
            }
            def.id.startsWith("ach_teeth_") -> {
                val t = when (def.tier) { 1->500; 2->1000; 3->2500; 4->5000; 5->10000; else->0 }
                state.totalTeethEarned to t
            }
            // State field checks
            def.id == "ach_first_blood"    -> state.monstersKilled to 1
            def.id == "ach_rich"           -> state.totalTeethEarned to 5000
            def.id == "ach_unstoppable"    -> state.currentStreak to 30
            def.id == "ach_berserker"      -> state.bestSingleSession to 50
            def.id == "ach_immortal"       -> state.currentStreak to 7
            def.id == "ach_failed_enchants"-> (state.totalEnchantAttempts - state.totalEnchantmentsSuccess) to 100
            def.id == "ach_alchemist"      -> state.totalItemsMerged to 10
            def.id == "ach_prestige_1"     -> state.prestigeLevel to 1
            def.id == "ach_prestige_3"     -> state.prestigeLevel to 3
            def.id == "ach_iron_fists"     -> state.totalCriticalHits to 500
            def.id == "ach_hoarder"        -> state.itemsCollected to 100
            def.id == "ach_destroyer"      -> state.totalDamageDealt to 1_000_000
            def.id == "ach_big_burst"      -> state.bestSingleSession to 100
            def.id == "ach_forge_master"   -> state.totalItemsMerged to 50
            def.id == "ach_conqueror"      -> state.highestMonsterLevelKilled to 50
            def.id == "ach_sniper"         -> state.highestDamage to 2_000
            def.id == "ach_full_wardrobe"  -> {
                val count = listOf(state.equippedHead, state.equippedNecklace, state.equippedWeapon1,
                    state.equippedWeapon2, state.equippedPants, state.equippedBoots).count { it.isNotEmpty() }
                count to 6
            }
            // Enchant level achievements
            def.id == "ach_forge_dmg_9"    -> maxEnchant to 9
            def.id == "ach_forge_dmg_15"   -> maxEnchant to 15
            def.id == "ach_forge_dmg_20"   -> maxEnchant to 20
            def.id == "ach_forge_dmg_25"   -> maxEnchant to 25
            // Set piece achievements
            def.id == "ach_void"           -> {
                val count = listOf(state.equippedHead, state.equippedNecklace, state.equippedWeapon1,
                    state.equippedWeapon2, state.equippedPants, state.equippedBoots).count { it.startsWith("set_void_") }
                count to 5
            }
            def.id == "ach_unrealm_set"    -> {
                val count = listOf(state.equippedHead, state.equippedNecklace, state.equippedWeapon1,
                    state.equippedWeapon2, state.equippedPants, state.equippedBoots).count { it.startsWith("set_un_") }
                count to 4
            }
            def.id == "ach_holy_set"       -> {
                val count = listOf(state.equippedHead, state.equippedNecklace, state.equippedWeapon1,
                    state.equippedWeapon2, state.equippedPants, state.equippedBoots).count { it.startsWith("set_holy_") }
                count to 4
            }
            // Boss kill achievements
            def.id == "ach_dragon_slayer"  -> (bossKills["Ancient Dragon"] ?: 0) to 1
            def.id == "ach_abyssal_reaper" -> (bossKills["Abyssal Reaper"] ?: 0) to 20
            def.id == "ach_skull_crusher"  -> (bossKills["Skull Crusher"] ?: 0) to 20
            def.id == "ach_sky_sentry"     -> (bossKills["Sky Sentry"] ?: 0) to 10
            def.id == "ach_heat_cannon"    -> (bossKills["Heat Cannon"] ?: 0) to 5
            def.id == "ach_diablo_first"   -> (bossKills["Diablo"] ?: 0) to 3
            def.id == "ach_iron_bull_5"    -> (bossKills["Iron Bull"] ?: 0) to 5
            def.id == "ach_king_slayer"    -> (bossKills["King Clone"] ?: 0) to 3
            def.id == "ach_stone_giant_3"  -> (bossKills["Stone Giant"] ?: 0) to 3
            def.id == "ach_iron_golem_5"   -> (bossKills["Iron Golem"] ?: 0) to 5
            def.id == "ach_shadow_lord_5"  -> (bossKills["Shadow Lord"] ?: 0) to 5
            def.id == "ach_blood_witch_3"  -> (bossKills["Blood Witch"] ?: 0) to 3
            def.id == "ach_bone_cube_3"    -> (bossKills["Bone Cube"] ?: 0) to 3
            def.id == "ach_fleshmeat_5"    -> (bossKills["Flesh and Meat Monster"] ?: 0) to 5
            def.id == "ach_grinder_5"      -> (bossKills["The Grinder"] ?: 0) to 5
            def.id == "ach_underworld_3"   -> (bossKills["Underworld Demon"] ?: 0) to 3
            def.id == "ach_stone_giant_10" -> (bossKills["Stone Giant"] ?: 0) to 10
            def.id == "ach_shadow_lord_15" -> (bossKills["Shadow Lord"] ?: 0) to 15
            def.id == "ach_blood_witch_10" -> (bossKills["Blood Witch"] ?: 0) to 10
            def.id == "ach_fleshmeat_15"   -> (bossKills["Flesh and Meat Monster"] ?: 0) to 15
            def.id == "ach_underworld_10"  -> (bossKills["Underworld Demon"] ?: 0) to 10
            def.id == "ach_iron_golem_10"  -> (bossKills["Iron Golem"] ?: 0) to 10
            // Bestiary achievements
            def.id == "ach_clone_killer"   -> (bestiary["Pumped Clone"] ?: 0) to 10
            def.id == "ach_cursed_hunter"  -> (bestiary["Cursed Warlock"] ?: 0) to 10
            def.id == "ach_cursed_50"      -> (bestiary["Cursed Warlock"] ?: 0) to 50
            def.id == "ach_one_arm_50"     -> (bestiary["One-Arm Mutant"] ?: 0) to 50
            def.id == "ach_demon_knight_50"-> (bestiary["Demon Knight Clone"] ?: 0) to 50
            def.id == "ach_clone_50"       -> (bestiary["Pumped Clone"] ?: 0) to 50
            def.id == "ach_abyss_50"       -> (bestiary["The Abyss"] ?: 0) to 50
            def.id == "ach_void_titan_50"  -> (bestiary["Void Titan"] ?: 0) to 50
            else -> return if (language == "ru") def.descRu else def.descEn
        }
        if (target == 0) return if (language == "ru") def.descRu else def.descEn
        return "$current / $target"
    }
}
