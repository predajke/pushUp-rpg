package com.pushupRPG.app.utils

import com.pushupRPG.app.data.db.GameStateEntity
import com.pushupRPG.app.data.model.Item
import kotlin.math.roundToInt
import kotlin.random.Random

object GameCalculations {

    // --- Уровень и XP ---
    const val HP_PER_LEVEL = 10
    const val STAT_POINTS_PER_LEVEL = 3
    private val XP_THRESHOLDS = listOf(
        0,    // Уровень 1
        100,  // Уровень 2
        200,  // Уровень 3
        300,  // Уровень 4
        500,  // Уровень 5
        1000, // Уровень 6
        1500, // Уровень 7
        2000, // Уровень 8
        2500, // Уровень 9
        3000,  // Уровень 10
        3500, 4000, 4500, 5000, 5500, 6000, 6500, 7000, 7500, 8000, 8500, 9000, 9500, 10000,
        10500, 11100, 11700, 12300, 12900, 13500, 14100, 14700, 15300, 15900, 16600, 17300,
        17900, 18500, 19000, 19700, 20300, 21000, 22000, 23000, 24000, 25000, 26000, 27000, 28000,
        29000, 30000, 31000, 32000, 33000, 34000
    )
    fun getLevelFromXp(xp: Int): Int {
        for (i in XP_THRESHOLDS.indices.reversed()) {
            if (xp >= XP_THRESHOLDS[i]) return i + 1
        }
        return 1
    }

    fun getXpThresholdForLevel(level: Int): Int {
        return if (level <= XP_THRESHOLDS.size) {
            XP_THRESHOLDS[level - 1]
        } else {
            XP_THRESHOLDS.last() + (level - XP_THRESHOLDS.size) * 500
        }
    }

    fun getXpForNextLevel(currentXp: Int): Int {
        val currentLevel = getLevelFromXp(currentXp)
        val nextThreshold = getXpThresholdForLevel(currentLevel + 1)
        return nextThreshold - currentXp
    }

    fun getXpProgress(currentXp: Int): Float {
        val currentLevel = getLevelFromXp(currentXp)
        val currentThreshold = getXpThresholdForLevel(currentLevel)
        val nextThreshold = getXpThresholdForLevel(currentLevel + 1)
        val range = nextThreshold - currentThreshold
        val progress = currentXp - currentThreshold
        return if (range > 0) progress.toFloat() / range.toFloat() else 0f
    }

    // --- Максимальное HP ---
    fun getMaxHp(level: Int, baseHealth: Int, itemHealthBonus: Int): Int {
        return baseHealth + (level * HP_PER_LEVEL) + itemHealthBonus
    }

    // --- Урон персонажа ---
    fun calculatePlayerDamage(totalPower: Int, isCrit: Boolean): Int {
        val base = totalPower.toFloat()
        val randomMultiplier = Random.nextFloat() * 0.4f + 0.8f // 0.8 - 1.2
        val damage = (base * randomMultiplier).roundToInt()
        return if (isCrit) (damage * 1.5f).roundToInt() else damage
    }

    // --- Крит шанс ---
    fun isCriticalHit(luck: Float): Boolean {
        val critChance = (luck * 1f).coerceAtMost(50f) // макс 50%
        return Random.nextFloat() * 100f < critChance
    }

    // --- Поглощение урона ---
    fun calculateDamageReduction(armor: Int): Float {
        return armor.toFloat() / (armor.toFloat() + 100f)
    }

    fun calculateDamageTaken(monsterDamage: Int, armor: Int): Int {
        val dr = calculateDamageReduction(armor)
        return ((monsterDamage * (1f - dr)).roundToInt()).coerceAtLeast(1)
    }

    // --- Шанс дропа ---
    fun isItemDropped(luck: Float, baseDropRate: Float = 0.07f): Boolean {
        val dropChance = (baseDropRate * 100f) + (luck * 0.5f)
        return Random.nextFloat() * 100f < dropChance
    }

    // --- Монстр ---
    fun generateMonsterHp(monsterLevel: Int): Int {
        return MonsterUtils.getMonsterByLevel(monsterLevel).maxHp
    }

    fun generateMonsterDamage(monsterLevel: Int): Int {
        return MonsterUtils.getMonsterByLevel(monsterLevel).damage
    }

    // --- Суммарные статы с учётом вещей ---
    fun calculateTotalStats(
        state: GameStateEntity,
        equippedItems: List<Item>
    ): TotalStats {
        val itemPower = equippedItems.sumOf { it.stats.power }
        val itemArmor = equippedItems.sumOf { it.stats.armor }
        val itemHealth = equippedItems.sumOf { it.stats.health }
        val itemLuck = equippedItems.sumOf { it.stats.luck.toDouble() }.toFloat()

        return TotalStats(
            power = state.basePower + itemPower,
            armor = state.baseArmor + itemArmor,
            health = state.baseHealth + itemHealth,
            luck = state.baseLuck + itemLuck
        )
    }

    // --- Регенерация HP ---
    // 1% от макс HP в минуту
    fun calculateHpRegen(maxHp: Int, minutesPassed: Long): Int {
        return ((maxHp * 0.01f) * minutesPassed).roundToInt()
    }

    // --- Burst атака (50+ отжиманий сразу) ---
    const val BURST_THRESHOLD = 50
    const val BURST_MULTIPLIER = 2

    fun isBurstAttack(pushUpsEntered: Int): Boolean {
        return pushUpsEntered >= BURST_THRESHOLD
    }

    // --- Зубы ---
    const val TEETH_DROP_CHANCE = 10f // 10% шанс зуба с удара

    fun isTeethDropped(): Boolean {
        return Random.nextFloat() * 100f < TEETH_DROP_CHANCE
    }

    fun getTeethFromMonster(monsterLevel: Int): Int {
        // За убийство монстра: от 1 до monsterLevel зубов
        val max = monsterLevel.coerceAtLeast(2)
        return Random.nextInt(1, max + 1)
    }

    fun getTeethFromSell(rarity: String): Int {
        return when (rarity) {
            "common" -> 1
            "uncommon" -> 2
            "rare" -> 3
            "epic" -> 5
            else -> 1
        }
    }
}

data class TotalStats(
    val power: Int,
    val armor: Int,
    val health: Int,
    val luck: Float
)