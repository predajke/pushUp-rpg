package com.ninthbalcony.pushuprpg.utils

import com.ninthbalcony.pushuprpg.data.db.GameStateEntity
import com.ninthbalcony.pushuprpg.data.model.Item
import kotlin.math.roundToInt
import kotlin.random.Random

object GameCalculations {

    // --- Уровень и XP ---
    const val HP_PER_LEVEL = 15
    const val STAT_POINTS_PER_LEVEL = 3
    // XP для перехода с level на level+1:
    // Уровни 1-9: начинаем с 200, каждый +100 (200, 300, ..., 1000)
    // Уровни 10+: каждый +150 (1150, 1300, 1450, ...)
    private fun xpPerLevel(level: Int): Int = when {
        level < 10 -> 200 + (level - 1) * 100
        else       -> 1000 + (level - 9) * 150
    }

    fun getLevelFromXp(xp: Int): Int {
        var level = 1
        var threshold = 0
        while (true) {
            val next = threshold + xpPerLevel(level)
            if (xp < next) return level
            threshold = next
            level++
            if (level > 10_000) return level
        }
    }

    fun getXpThresholdForLevel(level: Int): Int {
        if (level <= 1) return 0
        var total = 0
        for (l in 1 until level) total += xpPerLevel(l)
        return total
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
    fun getMaxHp(level: Int, baseHealth: Int, itemHealthBonus: Int, achHpFlat: Int = 0): Int {
        return baseHealth + (level * HP_PER_LEVEL) + itemHealthBonus + achHpFlat
    }

    // --- Урон персонажа ---
    fun calculatePlayerDamage(totalPower: Int, isCrit: Boolean): Int {
        val base = totalPower.toFloat()
        val randomMultiplier = Random.nextFloat() * 0.4f + 0.8f // 0.8 - 1.2
        val damage = (base * randomMultiplier).roundToInt()
        return if (isCrit) (damage * 1.5f).roundToInt() else damage
    }

    // --- Крит шанс ---
    fun isCriticalHit(luck: Float, achCritBonus: Float = 0f): Boolean {
        val critChance = (luck * 1f + achCritBonus).coerceAtMost(50f) // макс 50%
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

    // --- Суммарные статы с учётом вещей, уровней заточки, бонусов ачивок и сетов ---
    fun calculateTotalStats(
        state: GameStateEntity,
        equippedItems: List<Item>,
        enchantLevels: List<Int> = emptyList(),
        achBonuses: AchievementBonuses = AchievementBonuses(),
        setBonuses: SetBonuses = SetBonuses()
    ): TotalStats {
        var itemPower = 0; var itemArmor = 0; var itemHealth = 0; var itemLuck = 0f
        equippedItems.forEachIndexed { i, item ->
            val lvl = enchantLevels.getOrElse(i) { 0 }
            // Уровни 19–25 (ночная заточка): +2 к Power/Armor/Health за уровень вместо +1
            val pahBonus = if (lvl >= 19) 18 + (lvl - 18) * 2 else lvl
            itemPower  += item.stats.power  + pahBonus
            itemArmor  += item.stats.armor  + pahBonus
            itemHealth += item.stats.health + pahBonus
            itemLuck   += item.stats.luck   + lvl
        }
        return TotalStats(
            power  = ((state.basePower  + itemPower)  * (1f + achBonuses.damagePercent + setBonuses.damagePercent)).toInt(),
            armor  = ((state.baseArmor  + itemArmor)  * (1f + achBonuses.armorPercent + setBonuses.armorPercent)).toInt(),
            health = state.baseHealth + (state.playerLevel * HP_PER_LEVEL) + itemHealth + achBonuses.hpFlat,
            luck   = state.baseLuck   + itemLuck   + achBonuses.critPercent * 100f
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
        val base = (monsterLevel * 0.5f).roundToInt().coerceAtLeast(1)
        return Random.nextInt(base, (base * 2) + 1)
    }

    // --- Стрик-бонус XP (постоянный пока стрик активен) ---
    fun getStreakXpBonus(streak: Int): Float = when {
        streak >= 90 -> 0.25f
        streak >= 60 -> 0.20f
        streak >= 30 -> 0.15f
        streak >= 21 -> 0.10f
        streak >= 14 -> 0.07f
        streak >= 7  -> 0.05f
        streak >= 3  -> 0.03f
        streak >= 1  -> 0.02f
        else         -> 0f
    }

    fun getStreakBonusText(streak: Int): String {
        val bonus = getStreakXpBonus(streak)
        return if (bonus > 0) "+${(bonus * 100).toInt()}% XP" else ""
    }

    fun getTeethFromSell(rarity: String): Int {
        return when (rarity) {
            "common" -> 2
            "uncommon" -> 4
            "rare" -> 8
            "epic" -> 15
            "legendary" -> 25
            else -> 2
        }
    }

    /**
     * Luck-бонус за продажу. Только epic/legendary дают удачу — иначе массовая
     * продажа ширпотреба превращается в эксплойт (60 продаж × 0.05 = +3 Luck).
     * Шкала привязана к spendStatPoint("luck") = +0.10.
     */
    fun getLuckFromSell(rarity: String): Float = when (rarity) {
        "epic" -> 0.10f
        "legendary" -> 0.25f
        else -> 0f
    }
}

data class TotalStats(
    val power: Int,
    val armor: Int,
    val health: Int,
    val luck: Float
)