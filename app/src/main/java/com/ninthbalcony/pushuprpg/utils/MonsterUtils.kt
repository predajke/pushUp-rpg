package com.ninthbalcony.pushuprpg.utils

import com.ninthbalcony.pushuprpg.data.model.Monster
import kotlin.random.Random

object MonsterUtils {

    private val monsters = listOf(
        Monster(id = 1, name = "Gopnik", nameRu = "Гопник", level = 1,                      maxHp = 61, damage = 9, dropRate = 0.07f, imageRes = "monster_1"),
        Monster(id = 2, name = "Anime-Thug", nameRu = "Аниме-Хулиган", level = 2,           maxHp = 104, damage = 12, dropRate = 0.08f, imageRes = "monster_2"),
        Monster(id = 3, name = "Evil spirit", nameRu = "Злой Дух", level = 3,               maxHp = 159, damage = 17, dropRate = 0.09f, imageRes = "monster_3"),
        Monster(id = 4, name = "Mini Ogre", nameRu = "Мини Огр", level = 4,                 maxHp = 227, damage = 22, dropRate = 0.10f, imageRes = "monster_4"),
        Monster(id = 5, name = "Zergodog", nameRu = "Зергопёс", level = 5,                  maxHp = 306, damage = 28, dropRate = 0.11f, imageRes = "monster_5"),
        Monster(id = 6, name = "BFG2K111", nameRu = "BFG2K111", level = 6,                  maxHp = 404, damage = 34, dropRate = 0.12f, imageRes = "monster_6"),
        Monster(id = 7, name = "Mage of the Dead", nameRu = "Маг мёртвых", level = 7,       maxHp = 515, damage = 40, dropRate = 0.13f, imageRes = "monster_7"),
        Monster(id = 8, name = "Templar", nameRu = "Храмовник", level = 8,                  maxHp = 637, damage = 48, dropRate = 0.14f, imageRes = "monster_8"),
        Monster(id = 9, name = "Resurrection Ninja", nameRu = "Воскрешенный Ниндзя", level = 9, maxHp = 784, damage = 56, dropRate = 0.15f, imageRes = "monster_9"),
        Monster(id = 10, name = "Overlord", nameRu = "Повелитель", level = 10,              maxHp = 980, damage = 67, dropRate = 0.17f, imageRes = "monster_10"),
        Monster(id = 11, name = "Mutand", nameRu = "Мутанд", level = 11,                    maxHp = 1225, damage = 86, dropRate = 0.19f, imageRes = "monster_11"),
        Monster(id = 12, name = "Киборг-убийца", nameRu = "Cyborg killer", level = 12,      maxHp = 1470, damage = 108, dropRate = 0.22f, imageRes = "monster_12"),
        Monster(id = 13, name = "Outworld", nameRu = "Чужесвет", level = 13,                maxHp = 1838, damage = 123, dropRate = 0.25f, imageRes = "monster_13"),
        Monster(id = 14, name = "Cy-BAal", nameRu = "Сай-БАал", level = 14,                 maxHp = 2144, damage = 150, dropRate = 0.28f, imageRes = "monster_14"),
        Monster(id = 15, name = "Slime Queen", nameRu = "Королева слизи", level = 15,       maxHp = 2695, damage = 159, dropRate = 0.30f, imageRes = "monster_15"),
        Monster(id = 16, name = "Flesh of pain", nameRu = "Плоть боли", level = 16,         maxHp = 2695, damage = 184, dropRate = 0.35f, imageRes = "monster_16"),
        Monster(id = 17, name = "Red Darkness", nameRu = "Красная Тьма", level = 17,        maxHp = 3001, damage = 208, dropRate = 0.35f, imageRes = "monster_17"),
        Monster(id = 18, name = "Sisters", nameRu = "Сёстры", level = 18,                   maxHp = 3553, damage = 239, dropRate = 0.36f, imageRes = "monster_18"),
        // Перенесён с 16→19 (был дубликатом Flesh of pain). Статы интерполированы L18↔L20.
        Monster(id = 20, name = "Trap", nameRu = "Ловушка", level = 19,                     maxHp = 4349, damage = 285, dropRate = 0.43f, imageRes = "monster_20"),
        // Перенесён с 17→21 (был дубликатом Red Darkness). Статы интерполированы L20↔L22.
        Monster(id = 19, name = "Ancient Tower", nameRu = "Древняя Башня", level = 21,      maxHp = 5942, damage = 368, dropRate = 0.51f, imageRes = "monster_19"),
        // Перенесён с 18→23 (был дубликатом Sisters). Статы интерполированы L22↔L24.
        Monster(id = 21, name = "Goblin Leader", nameRu = "Король Гоблинов", level = 23,    maxHp = 8392, damage = 506, dropRate = 0.53f, imageRes = "monster_21"),
        Monster(id = 22, name = "Roach King", nameRu = "Король Тараканов", level = 20,      maxHp = 5145, damage = 331, dropRate = 0.50f, imageRes = "monster_22"),
        Monster(id = 23, name = "Hellspawn", nameRu = "Порождение Ада", level = 22,         maxHp = 6738, damage = 404, dropRate = 0.52f, imageRes = "monster_23"),
        Monster(id = 24, name = "Hellspawn Elite", nameRu = "Элитный Порождение Ада", level = 24, maxHp = 10045, damage = 607, dropRate = 0.54f, imageRes = "monster_23"),
        Monster(id = 25, name = "Fog Stronghold", nameRu = "Туманная Крепость", level = 25, maxHp = 11638, damage = 674, dropRate = 0.55f, imageRes = "monster_24"),
        Monster(id = 26, name = "Swamp Mud", nameRu = "Болотная Грязь", level = 26,         maxHp = 9432, damage = 760, dropRate = 0.57f, imageRes = "monster_25"),
        Monster(id = 27, name = "Swamp Muds", nameRu = "Болотные Грязи", level = 28,        maxHp = 14149, damage = 1139, dropRate = 0.59f, imageRes = "monster_26"),
        Monster(id = 28, name = "The Swamp", nameRu = "Само Болото", level = 30,            maxHp = 21437, damage = 1715, dropRate = 0.61f, imageRes = "monster_27"),
        Monster(id = 29, name = "Demonic Face", nameRu = "Демоническое Лицо", level = 32,   maxHp = 26582, damage = 1899, dropRate = 0.63f, imageRes = "monster_28"),
        Monster(id = 30, name = "Giant Scarab", nameRu = "Гигантский Скарабей", level = 31, maxHp = 15435, damage = 1286, dropRate = 0.65f, imageRes = "monster_29"),
        Monster(id = 31, name = "Mind Shroom", nameRu = "Гриб-Контролёр", level = 33,       maxHp = 18865, damage = 1531, dropRate = 0.67f, imageRes = "monster_30"),
        Monster(id = 32, name = "Diabosaurus", nameRu = "Диабозавр", level = 35,            maxHp = 24010, damage = 1715, dropRate = 0.69f, imageRes = "monster_31"),
        Monster(id = 33, name = "Wasteland Nomad", nameRu = "Кочевник Пустоши", level = 36, maxHp = 25340, damage = 1800, dropRate = 0.7f, imageRes = "monster_33"),
        Monster(id = 34, name = "Сhitinous hunter", nameRu = "Хитиновый охотник", level = 37, maxHp = 26250, damage = 1900, dropRate = 0.7f, imageRes = "monster_34"),
        Monster(id = 35, name = "Dark Ninja", nameRu = "Тёмный Ниндзя", level = 38, maxHp = 28000, damage = 2050, dropRate = 0.71f, imageRes = "monster_35"),
        Monster(id = 36, name = "Werewolf", nameRu = "Волк-Оборотень", level = 39, maxHp = 30800, damage = 2200, dropRate = 0.72f, imageRes = "monster_36"),
        Monster(id = 37, name = "Gargoyle", nameRu = "Гаргулья", level = 40, maxHp = 33600, damage = 2000, dropRate = 0.73f, imageRes = "monster_37"),
        // Перенесён с 40→34 (был дубликатом Gargoyle). Статы интерполированы L33↔L35.
        Monster(id = 38, name = "Slime Mass", nameRu = "Слизевое Образование", level = 34, maxHp = 21437, damage = 1623, dropRate = 0.68f, imageRes = "monster_38"),
        Monster(id = 39, name = "Furious Orc", nameRu = "Недовольный Орк", level = 41, maxHp = 36400, damage = 2150, dropRate = 0.74f, imageRes = "monster_39"),
        Monster(id = 40, name = "Ember Fiend", nameRu = "Огненный Бес", level = 42, maxHp = 39200, damage = 2300, dropRate = 0.74f, imageRes = "monster_40"),
        // Уровни 43–49: усиленные версии существующих монстров
        Monster(id = 41, name = "Dark Ninja+", nameRu = "Тёмный Ниндзя+", level = 43, maxHp = 44100, damage = 2450, dropRate = 0.75f, imageRes = "monster_35"),
        Monster(id = 42, name = "Werewolf+", nameRu = "Волк-Оборотень+", level = 44, maxHp = 49000, damage = 2650, dropRate = 0.75f, imageRes = "monster_36"),
        Monster(id = 43, name = "Gargoyle+", nameRu = "Гаргулья+", level = 45, maxHp = 54600, damage = 2850, dropRate = 0.76f, imageRes = "monster_37"),
        Monster(id = 44, name = "Orc Warlord", nameRu = "Орк-Военачальник", level = 46, maxHp = 60200, damage = 3050, dropRate = 0.76f, imageRes = "monster_39"),
        Monster(id = 45, name = "Demon Emperor", nameRu = "Демон-Владыка", level = 47, maxHp = 66500, damage = 3300, dropRate = 0.77f, imageRes = "monster_28"),
        Monster(id = 46, name = "Void Titan", nameRu = "Титан Пустоты", level = 48, maxHp = 73500, damage = 3550, dropRate = 0.77f, imageRes = "monster_40"),
        Monster(id = 47, name = "The Abyss", nameRu = "Бездна", level = 49, maxHp = 80500, damage = 3800, dropRate = 0.78f, imageRes = "monster_40"),
        Monster(id = 48, name = "Pumped Clone", nameRu = "Накаченный Клон", level = 50, maxHp = 89600, damage = 4150, dropRate = 0.79f, imageRes = "monster_42"),
        Monster(id = 49, name = "Demon Knight Clone", nameRu = "Демонический Клон-Рыцарь", level = 52, maxHp = 103600, damage = 4600, dropRate = 0.80f, imageRes = "monster_43"),
        Monster(id = 50, name = "One-Arm Mutant", nameRu = "Однорукий Мутант", level = 54, maxHp = 117600, damage = 5000, dropRate = 0.81f, imageRes = "monster_44"),
        Monster(id = 51, name = "Cursed Warlock", nameRu = "Поражённый Колдун", level = 55, maxHp = 126000, damage = 5350, dropRate = 0.82f, imageRes = "monster_45"),
        )


    fun getMonsterById(id: Int): Monster? {
        return monsters.find { it.id == id }
    }

    fun getImageResByName(name: String): String =
        monsters.find { it.name == name }?.imageRes ?: "monster_1"

    fun getMonsterByName(name: String): Monster? = monsters.find { it.name == name }

    /**
     * Локализованное имя монстра ИЛИ босса по сохранённому английскому имени.
     * Гарантированно стабильно — НЕ random'ит как getMonsterByLevel для уровней
     * с несколькими монстрами. Используется в UI вместо переcчёта по level.
     */
    fun localizedName(englishName: String, language: String): String {
        monsters.find { it.name == englishName }?.let {
            return if (language == "ru") it.nameRu else it.name
        }
        BossUtils.getAllBosses().find { it.name == englishName }?.let {
            return if (language == "ru") it.nameRu else it.name
        }
        return englishName
    }

    fun getMonsterByLevel(level: Int): Monster {
        val monstersOfLevel = monsters.filter { it.level == level }
        return when {
            // Если точного уровня нет — берём ближайший, а не сильнейший
            monstersOfLevel.isEmpty() -> monsters.minByOrNull { kotlin.math.abs(it.level - level) } ?: monsters.first()
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

    /** Returns the level of a monster by its English name, or 0 if not found. */
    fun getMonsterLevelByName(name: String): Int =
        monsters.find { it.name == name }?.level ?: 0

    // Герой меняет облик в зависимости от уровня
    fun getHeroImageRes(heroLevel: Int): String {
        return when {
            heroLevel <= 10 -> "hero_1"
            heroLevel <= 20 -> "hero_2"
            else -> "hero_3"
        }
    }
}