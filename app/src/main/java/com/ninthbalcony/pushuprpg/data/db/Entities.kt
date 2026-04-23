package com.ninthbalcony.pushuprpg.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_state")
data class GameStateEntity(
    @PrimaryKey val id: Int = 1,

    // Персонаж
    val playerName: String = "Hero",
    val playerLevel: Int = 1,
    val totalXp: Int = 0,
    val unspentStatPoints: Int = 0,

    // Базовые статы
    val basePower: Int = 5,
    val baseArmor: Int = 0,
    val baseHealth: Int = 100,
    val baseLuck: Float = 0f,

    // HP
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

    // Бой
    val lastBattleTick: Long = System.currentTimeMillis(),
    val isPlayerDead: Boolean = false,
    val deathTime: Long = 0L,

    // Экипировка
    val equippedHead: String = "",
    val equippedNecklace: String = "",
    val equippedWeapon1: String = "",
    val equippedWeapon2: String = "",
    val equippedPants: String = "",
    val equippedBoots: String = "",

    // Инвентарь (хранится как строка через запятую)
    val inventoryItems: String = "",

    // Статистика
    val totalPushUpsAllTime: Int = 0,
    val totalDamageDealt: Int = 0,
    val highestDamage: Int = 0,
    val itemsCollected: Int = 0,

    // Настройки
    val language: String = "en",
    val heroAvatar: String = "hero_1",

    // Текущее событие
    val activeEventId: Int = 0,
    val eventStartTime: Long = 0L,
    val eventEndTime: Long = 0L,
    val lastEventTime: Long = 0L,

    // Зубы (валюта)
    val teeth: Int = 0,

    // Магазин
    val shopItems: String = "",
    val shopLastRefresh: Long = 0L,

    // Кузница
    val forgeSlot1: String = "",
    val forgeSlot2: String = "",

    // Clover Box
    val cloverBoxUsedToday: Int = 0,
    val freePointsUsedToday: Int = 0,
    val lastDailyReset: String = "",
    // Дата создания персонажа
    val characterBirthDate: String = "",

    // Боссы
    val isCurrentBoss: Boolean = false,
    val currentBossId: Int = 0,

    // Ежедневная награда
    val lastDailyRewardDate: String = "",
    val dailyRewardDay: Int = 1,

    // Квесты
    val activeQuestsJson: String = "",
    val lastQuestRefreshDate: String = "",

    // Расширенная статистика
    val totalEnchantmentsSuccess: Int = 0,
    val totalItemsMerged: Int = 0,
    val totalTeethSpent: Int = 0,
    val bestSingleSession: Int = 0,
    val totalCriticalHits: Int = 0,
    val totalTeethEarned: Int = 0,
    val highestMonsterLevelKilled: Int = 0,

    // Достижения
    val achievementsJson: String = "",
    val activeAchievementIds: String = "",

    // Бестиарий и лог предметов
    val bestiaryJson: String = "",
    val itemLogJson: String = "",

    // Убийства боссов (Map<String, Int> — EN-имя босса → кол-во убийств)
    val bossKillsJson: String = "",

    // Онбординг и аналитика
    val isFirstLaunch: Boolean = true,
    val prestigeLevel: Int = 0,
    val installDate: Long = System.currentTimeMillis(),

    // Защита от читеров
    val lastSaveTime: Long = System.currentTimeMillis(),

    // Синхронизация с Firebase
    val lastSyncTime: Long = 0L,

    // Rate Us Dialog
    val rateUsLastShowDate: Long = 0L,
    val rateUsDoNotShowAgain: Boolean = false,

    // Reroll cost escalation (resets every 5 min)
    val shopRerollCount: Int = 0,
    val shopRerollResetTime: Long = 0L,

    // Ad quest reroll (once per day)
    val lastAdQuestRerollDate: String = "",

    // Ad cooldown escalation (resets daily)
    val adShopViewCount: Int = 0,
    val adShopLastViewTime: Long = 0L,

    // ===== Daily Spin =====
    val dailySpinUsedToday: Int = 0,              // Бесплатный спин: 0 (не использован) или 1 (использован)
    val dailySpinAdViewsToday: Int = 0,           // Просмотры рекламы сегодня (макс 10)
    val lastDailySpinReset: String = "",          // Дата последнего сброса спинов
    val lastHourlySpinGrantTime: Long = 0L,       // Timestamp последнего начисления часового спина

    // ===== Spin Generation Counters (Total) =====
    val totalShopPurchases: Int = 0,              // Всего покупок в магазине (для отслеживания каждые 40)
    val totalEnchantAttempts: Int = 0,            // Всего попыток заточки (для каждые 25)
    val totalMergeAttempts: Int = 0,              // Всего попыток слияния (для каждые 25)

    // ===== Spin Token Wallet =====
    val spinTokens: Int = 0,                      // Кошелёк токенов спина (накапливается, тратится по 1 за спин)

    // ===== Teeth Sources =====
    val teethFromQuests: Int = 0,                 // Зубы с квестов
    val teethFromAds: Int = 0,                    // Зубы с рекламы
    val teethFromSpin: Int = 0,                   // Зубы с вращения ленты
    val itemsFromSpin: Int = 0                    // Предметы с вращения ленты
)

@Entity(tableName = "pushup_records")
data class PushUpRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val count: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "log_entries")
data class LogEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val messageRu: String
)