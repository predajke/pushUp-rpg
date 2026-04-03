package com.pushupRPG.app.utils

import com.pushupRPG.app.data.model.Monster
import kotlin.random.Random

object MonsterUtils {

    private val monsters = listOf(
        Monster(
            id = 1,
            name = "Gopnik",
            nameRu = "Гопник",
            level = 1,
            maxHp = 50,
            damage = 7,
            dropRate = 0.07f,
            imageRes = "monster_1"
        ),
        Monster(
            id = 2,
            name = "Anime-Thug",
            nameRu = "Аниме-Хулиган",
            level = 2,
            maxHp = 85,
            damage = 10,
            dropRate = 0.08f,
            imageRes = "monster_2"
        ),
        Monster(
            id = 3,
            name = "Evil spirit",
            nameRu = "Злой Дух",
            level = 3,
            maxHp = 130,
            damage = 14,
            dropRate = 0.09f,
            imageRes = "monster_3"
        ),
        Monster(
            id = 4,
            name = "Mini Ogre",
            nameRu = "Мини Огр",
            level = 4,
            maxHp = 185,
            damage = 18,
            dropRate = 0.10f,
            imageRes = "monster_4"
        ),
        Monster(
            id = 5,
            name = "Zergodog",
            nameRu = "Зергопёс",
            level = 5,
            maxHp = 250,
            damage = 23,
            dropRate = 0.11f,
            imageRes = "monster_5"
        ),
        Monster(
            id = 6,
            name = "BFG2K111",
            nameRu = "BFG2K111",
            level = 6,
            maxHp = 330,
            damage = 28,
            dropRate = 0.12f,
            imageRes = "monster_6"
        ),
        Monster(
            id = 7,
            name = "Mage of the Dead",
            nameRu = "Маг мёртвых",
            level = 7,
            maxHp = 420,
            damage = 33,
            dropRate = 0.13f,
            imageRes = "monster_7"
        ),
        Monster(
            id = 8,
            name = "Templar",
            nameRu = "Храмовник",
            level = 8,
            maxHp = 520,
            damage = 39,
            dropRate = 0.14f,
            imageRes = "monster_8"
        ),
        Monster(
            id = 9,
            name = "Resurrection Ninja",
            nameRu = "Воскрешенный Ниндзя",
            level = 9,
            maxHp = 640,
            damage = 46,
            dropRate = 0.15f,
            imageRes = "monster_9"
        ),
        Monster(
            id = 10,
            name = "Overlord",
            nameRu = "Повелитель",
            level = 10,
            maxHp = 800,
            damage = 55,
            dropRate = 0.17f,
            imageRes = "monster_10"
        ),
        Monster(
            id = 11,
            name = "Mutand",
            nameRu = "Мутанд",
            level = 11,
            maxHp = 1000,
            damage = 70,
            dropRate = 0.19f,
            imageRes = "monster_11"
        ),
        Monster(
            id = 12,
            name = "Киборг-убийца",
            nameRu = "Cyborg killer",
            level = 12,
            maxHp = 1200,
            damage = 88,
            dropRate = 0.22f,
            imageRes = "monster_12"
        ),
        Monster(
            id = 13,
            name = "Outworld",
            nameRu = "Чужесвет",
            level = 13,
            maxHp = 1500,
            damage = 100,
            dropRate = 0.25f,
            imageRes = "monster_13"
        ),
        Monster(
            id = 14,
            name = "Cy-BAal",
            nameRu = "Сай-БАал",
            level = 14,
            maxHp = 1750,
            damage = 122,
            dropRate = 0.28f,
            imageRes = "monster_14"
        ),
        Monster(
            id = 15,
            name = "Slime Queen",
            nameRu = "Королева слизи",
            level = 15,
            maxHp = 2200,
            damage = 130,
            dropRate = 0.30f,
            imageRes = "monster_15"
        ),
        Monster(
            id = 16,
            name = "Flesh of pain",
            nameRu = "Плоть боли",
            level = 10,
            maxHp = 777,
            damage = 77,
            dropRate = 0.22f,
            imageRes = "monster_16"
        ),
        Monster(
            id = 17,
            name = "Red Darkness",
            nameRu = "Красная Тьма",
            level = 16,
            maxHp = 2200,
            damage = 150,
            dropRate = 0.35f,
            imageRes = "monster_17"
        ),
        Monster(
            id = 18,
            name = "Sisters",
            nameRu = "Сёстры",
            level = 16,
            maxHp = 2500,
            damage = 140,
            dropRate = 0.36f,
            imageRes = "monster_18"
        ),
        Monster(
            id = 19,
            name = "Ancient Tower",
            nameRu = "Древняя Башня",
            level = 17,
            maxHp = 3000,
            damage = 200,
            dropRate = 0.42f,
            imageRes = "monster_19"
        ),
        Monster(
            id = 20,
            name = "Trap",
            nameRu = "Ловушка",
            level = 16,
            maxHp = 2500,
            damage = 220,
            dropRate = 0.44f,
            imageRes = "monster_20"
        ),

    )

    fun getMonsterById(id: Int): Monster? {
        return monsters.find { it.id == id }
    }

    fun getMonsterByLevel(level: Int): Monster {
        val monstersOfLevel = monsters.filter { it.level == level }
        return when {
            monstersOfLevel.isEmpty() -> monsters.last()
            monstersOfLevel.size == 1 -> monstersOfLevel.first()
            else -> monstersOfLevel.random()
        }
    }

    // Выбор монстра на основе уровня героя
    fun rollNextMonster(heroLevel: Int): Monster {
        val roll = Random.nextFloat() * 100f
        val monsterLevel = when {
            roll < 20f -> {
                // 20% — слабый монстр (уровень героя - 2)
                (heroLevel - 2).coerceAtLeast(1)
            }
            roll < 90f -> {
                // 70% — монстр уровня героя ±1
                val offset = if (Random.nextBoolean()) 0 else 1
                (heroLevel + offset - 1).coerceIn(1, 10)
            }
            else -> {
                // 10% — сильный монстр (уровень героя + 2)
                (heroLevel + 2).coerceAtMost(10)
            }
        }
        return getMonsterByLevel(monsterLevel)
    }

    fun getMonsterName(monster: Monster, language: String): String {
        return if (language == "ru") monster.nameRu else monster.name
    }

    // Герой меняет облик в зависимости от уровня
    fun getHeroImageRes(heroLevel: Int): String {
        return when {
            heroLevel <= 10 -> "hero_1"
            heroLevel <= 20 -> "hero_2"
            else -> "hero_3"
        }
    }
}