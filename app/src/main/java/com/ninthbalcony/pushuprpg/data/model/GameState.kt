package com.ninthbalcony.pushuprpg.data.model

data class GameState(
    // Персонаж
    val playerName: String = "Hero",
    val playerLevel: Int = 1,
    val totalXp: Int = 0,
    val unspentStatPoints: Int = 0,

    // Базовые статы (без учёта вещей)
    val basePower: Int = 0,
    val baseArmor: Int = 0,
    val baseHealth: Int = 100,
    val baseLuck: Float = 0f,

    // HP персонажа
    val currentHp: Int = 100,

    // Отжимания
    val pushUpsToday: Int = 0,
    val lastResetDate: String = "",

    // Стрик
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastLoginDate: String = "",

    // Монстр
    val monsterName: String = "Thug",
    val monsterLevel: Int = 1,
    val monsterMaxHp: Int = 50,
    val monsterCurrentHp: Int = 50,
    val monsterDamage: Int = 5,
    val monstersKilled: Int = 0,

    // Время последнего боевого тика
    val lastBattleTick: Long = System.currentTimeMillis(),

    // Флаг смерти
    val isPlayerDead: Boolean = false,
    val deathTime: Long = 0L,

    // Экипировка (id предметов или "" если слот пуст)
    val equippedHead: String = "",
    val equippedNecklace: String = "",
    val equippedWeapon1: String = "",
    val equippedWeapon2: String = "",
    val equippedPants: String = "",
    val equippedBoots: String = "",

    // Инвентарь (список id предметов)
    val inventoryItems: List<String> = emptyList(),

    // Статистика
    val totalPushUpsAllTime: Int = 0,
    val totalDamageDealt: Int = 0,
    val highestDamage: Int = 0,
    val itemsCollected: Int = 0,

    // Язык
    val language: String = "en"
)