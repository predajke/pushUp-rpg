package com.ninthbalcony.pushuprpg.utils

import com.ninthbalcony.pushuprpg.data.model.Monster

object BossUtils {

    private val bosses = listOf(
        Monster(id = 101, name = "Stone Giant", nameRu = "Каменный Великан",
            level = 5, maxHp = 800, damage = 40, dropRate = 1.0f,
            imageRes = "boss_stone_giant", isBoss = true, dropRarityMin = "uncommon"),
        Monster(id = 102, name = "Shadow Lord", nameRu = "Повелитель Теней",
            level = 8, maxHp = 1400, damage = 70, dropRate = 1.0f,
            imageRes = "boss_shadow_lord", isBoss = true, dropRarityMin = "rare"),
        Monster(id = 103, name = "Iron Golem", nameRu = "Железный Голем",
            level = 11, maxHp = 2200, damage = 90, dropRate = 1.0f,
            imageRes = "boss_iron_golem", isBoss = true, dropRarityMin = "rare"),
        Monster(id = 104, name = "Blood Witch", nameRu = "Кровавая Ведьма",
            level = 14, maxHp = 3000, damage = 120, dropRate = 1.0f,
            imageRes = "boss_blood_witch", isBoss = true, dropRarityMin = "rare"),
        Monster(id = 105, name = "Ancient Dragon", nameRu = "Древний Дракон",
            level = 17, maxHp = 5500, damage = 200, dropRate = 1.0f,
            imageRes = "boss_ancient_dragon", isBoss = true, dropRarityMin = "epic"),
        Monster(id = 106, name = "Abyssal Reaper", nameRu = "Бездонный Жнец",
            level = 20, maxHp = 8000, damage = 280, dropRate = 1.0f,
            imageRes = "boss_abyssal_reaper", isBoss = true, dropRarityMin = "epic"),
        Monster(id = 107, name = "Skull Crusher", nameRu = "Сокрушитель Черепов",
            level = 23, maxHp = 11000, damage = 350, dropRate = 1.0f,
            imageRes = "boss_skull_crusher", isBoss = true, dropRarityMin = "epic"),
        Monster(id = 108, name = "Sky Sentry", nameRu = "Страж Небес",
            level = 27, maxHp = 16000, damage = 450, dropRate = 1.0f,
            imageRes = "boss_sky_sentry", isBoss = true, dropRarityMin = "epic"),
        Monster(id = 109, name = "Heat Cannon", nameRu = "Пушка Пламени",
            level = 30, maxHp = 22000, damage = 600, dropRate = 1.0f,
            imageRes = "boss_heat_cannon", isBoss = true, dropRarityMin = "legendary"),
        Monster(id = 110, name = "Flesh and Meat Monster", nameRu = "Монстр Плоти и Мяса",
            level = 32, maxHp = 28000, damage = 720, dropRate = 1.0f,
            imageRes = "boss_fleshmeat", isBoss = true, dropRarityMin = "legendary"),
        Monster(id = 111, name = "Underworld Demon", nameRu = "Подземный Демон",
            level = 35, maxHp = 35000, damage = 880, dropRate = 1.0f,
            imageRes = "boss_underworld_demon", isBoss = true, dropRarityMin = "legendary"),
        // после уже идут эндгейм боссы.
        Monster(id = 112, name = "The Grinder", nameRu = "Скрежет",
            level = 38, maxHp = 40000, damage = 999, dropRate = 1.0f,
            imageRes = "boss_the_grinder", isBoss = true, dropRarityMin = "legendary"),
        Monster(id = 113, name = "Bone Cube", nameRu = "Костяной Куб",
            level = 41, maxHp = 60000, damage = 1300, dropRate = 1.0f,
            imageRes = "boss_cube", isBoss = true, dropRarityMin = "legendary"),
        Monster(id = 114, name = "Iron Bull", nameRu = "Железный Бык",
            level = 46, maxHp = 90000, damage = 1700, dropRate = 1.0f,
            imageRes = "boss_oven", isBoss = true, dropRarityMin = "legendary"),
        Monster(id = 115, name = "Diablo", nameRu = "Дьявол Бездны",
            level = 50, maxHp = 115000, damage = 2200, dropRate = 1.0f,
            imageRes = "boss_dib", isBoss = true, dropRarityMin = "legendary"),
        )

    /** Каждые 10 убийств — босс. Каждые 50 — Ancient Dragon. */
    fun shouldSpawnBoss(totalKills: Int): Boolean =
        totalKills > 0 && totalKills % 10 == 0

    fun getBossForKillCount(totalKills: Int): Monster {
        val boss = when {
            totalKills % 50 == 0 && totalKills >= 150 -> bosses.find { it.id == 109 }!!
            totalKills % 50 == 0 && totalKills >= 100 -> bosses.find { it.id == 108 }!!
            totalKills % 50 == 0                      -> bosses.find { it.id == 105 }!!
            else -> bosses[((totalKills / 10 - 1) % bosses.size).coerceAtLeast(0)]
        }
        return scaleToKills(boss, totalKills)
    }

    /** Масштабируем HP и урон боссу относительно прогресса игрока */
    private fun scaleToKills(boss: Monster, totalKills: Int): Monster {
        val scale = 1f + (totalKills / 50f).coerceAtMost(3f)
        return boss.copy(
            maxHp = (boss.maxHp * scale).toInt(),
            damage = (boss.damage * scale).toInt()
        )
    }

    fun getBossById(id: Int): Monster? = bosses.find { it.id == id }

    fun getBossName(boss: Monster, language: String) =
        if (language == "ru") boss.nameRu else boss.name

    fun getAllBosses(): List<Monster> = bosses
}
