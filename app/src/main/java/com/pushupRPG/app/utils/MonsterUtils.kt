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
            level = 16,
            maxHp = 2200,
            damage = 150,
            dropRate = 0.35f,
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
        Monster(
            id = 21,
            name = "Goblin Leader",
            nameRu = "Король Гоблинов",
            level = 18,
            maxHp = 3500,
            damage = 240,
            dropRate = 0.47f,
            imageRes = "monster_21"
        ),
        Monster(
            id = 22,
            name = "Roach King",
            nameRu = "Король Тараканов",
            level = 20,
            maxHp = 4200,
            damage = 270,
            dropRate = 0.50f,
            imageRes = "monster_22"
        ),
        Monster(
            id = 23,
            name = "Hellspawn",
            nameRu = "Порождение Ада",
            level = 22,
            maxHp = 5500,
            damage = 330,
            dropRate = 0.52f,
            imageRes = "monster_23"
        ),
        Monster(
            id = 24,
            name = "Hellspawn Elite",
            nameRu = "Элитный Порождение Ада",
            level = 24,
            maxHp = 8200,
            damage = 495,
            dropRate = 0.54f,
            imageRes = "monster_23"
        ),
        Monster(
            id = 25,
            name = "Fog Stronghold",
            nameRu = "Туманная Крепость",
            level = 25,
            maxHp = 9500,
            damage = 550,
            dropRate = 0.55f,
            imageRes = "monster_24"
        ),
        Monster(
            id = 26,
            name = "Swamp Mud",
            nameRu = "Болотная Грязь",
            level = 26,
            maxHp = 11000,
            damage = 620,
            dropRate = 0.57f,
            imageRes = "monster_25"
        ),
        Monster(
            id = 27,
            name = "Swamp Muds",
            nameRu = "Болотные Грязи",
            level = 28,
            maxHp = 16500,
            damage = 930,
            dropRate = 0.59f,
            imageRes = "monster_26"
        ),
        Monster(
            id = 28,
            name = "The Swamp",
            nameRu = "Само Болото",
            level = 30,
            maxHp = 25000,
            damage = 1400,
            dropRate = 0.61f,
            imageRes = "monster_27"
        ),
        Monster(
            id = 29,
            name = "Demonic Face",
            nameRu = "Демоническое Лицо",
            level = 32,
            maxHp = 15000,
            damage = 900,
            dropRate = 0.63f,
            imageRes = "monster_28"
        ),

    )

    fun getMonsterById(id: Int): Monster? {
        return monsters.find { it.id == id }
    }

    fun getImageResByName(name: String): String =
        monsters.find { it.name == name }?.imageRes ?: "monster_1"

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
        val maxMonsterLevel = monsters.maxOf { it.level }
        val roll = Random.nextFloat() * 100f
        val monsterLevel = when {
            roll < 20f -> {
                // 20% — слабый монстр (уровень героя - 2)
                (heroLevel - 2).coerceAtLeast(1)
            }
            roll < 90f -> {
                // 70% — монстр уровня героя ±1
                val offset = if (Random.nextBoolean()) 0 else 1
                (heroLevel + offset - 1).coerceIn(1, maxMonsterLevel)
            }
            else -> {
                // 10% — сильный монстр (уровень героя + 2)
                (heroLevel + 2).coerceAtMost(maxMonsterLevel)
            }
        }
        return getMonsterByLevel(monsterLevel)
    }

    fun getMonsterName(monster: Monster, language: String): String {
        return if (language == "ru") monster.nameRu else monster.name
    }

    fun getAllMonsters(): List<Monster> = monsters

    // Герой меняет облик в зависимости от уровня
    fun getHeroImageRes(heroLevel: Int): String {
        return when {
            heroLevel <= 10 -> "hero_1"
            heroLevel <= 20 -> "hero_2"
            else -> "hero_3"
        }
    }
}