package com.ninthbalcony.pushuprpg

import com.ninthbalcony.pushuprpg.utils.GameCalculations
import org.junit.Assert.*
import org.junit.Test

class GameCalculationsTest {

    // ==================== XP и уровни ====================

    @Test
    fun `getLevelFromXp - уровень 1 при xp=0`() {
        assertEquals(1, GameCalculations.getLevelFromXp(0))
    }

    @Test
    fun `getLevelFromXp - уровень 2 при xp=100`() {
        assertEquals(2, GameCalculations.getLevelFromXp(100))
    }

    @Test
    fun `getLevelFromXp - уровень 10 при xp=3000`() {
        assertEquals(10, GameCalculations.getLevelFromXp(3000))
    }

    @Test
    fun `getLevelFromXp - уровень не падает ниже 1 при отрицательном xp`() {
        assertEquals(1, GameCalculations.getLevelFromXp(-100))
    }

    @Test
    fun `getXpThresholdForLevel - уровень 1 = 0`() {
        assertEquals(0, GameCalculations.getXpThresholdForLevel(1))
    }

    @Test
    fun `getXpThresholdForLevel - уровень за пределами массива = линейный рост`() {
        // XP_THRESHOLDS.size = 55, last = 34000, шаг = 500
        val threshold = GameCalculations.getXpThresholdForLevel(56)
        assertEquals(34000 + 500, threshold)
    }

    @Test
    fun `getXpProgress - прогресс от 0 до 1 в пределах уровня`() {
        val progress = GameCalculations.getXpProgress(50) // в начале 1→2 уровня (0..100)
        assertTrue("Прогресс должен быть между 0 и 1", progress in 0f..1f)
    }

    @Test
    fun `getXpForNextLevel - положительное значение`() {
        val needed = GameCalculations.getXpForNextLevel(0) // нужно 100 до уровня 2
        assertEquals(100, needed)
    }

    // ==================== Урон ====================

    @Test
    fun `calculatePlayerDamage - урон всегда больше 0`() {
        repeat(50) {
            val dmg = GameCalculations.calculatePlayerDamage(1, false)
            assertTrue("Урон должен быть > 0", dmg > 0)
        }
    }

    @Test
    fun `calculatePlayerDamage - крит всегда больше обычного при одном power`() {
        // При большом power крит всегда больше
        val normal = GameCalculations.calculatePlayerDamage(1000, false)
        val crit = GameCalculations.calculatePlayerDamage(1000, true)
        // Крит = обычный * 1.5, при power=1000 минимальный обычный = round(800) = 800, крит = 1200
        assertTrue("Крит ($crit) должен быть >= обычного ($normal)", crit >= normal)
    }

    @Test
    fun `calculateDamageTaken - с нулевой броней = весь урон`() {
        val dmg = GameCalculations.calculateDamageTaken(100, 0)
        assertEquals(100, dmg)
    }

    @Test
    fun `calculateDamageTaken - минимальный урон = 1`() {
        // С очень высокой бронёй урон не уходит в 0
        val dmg = GameCalculations.calculateDamageTaken(1, 100000)
        assertEquals(1, dmg)
    }

    @Test
    fun `calculateDamageReduction - формула armor divide (armor + 100)`() {
        val dr = GameCalculations.calculateDamageReduction(100)
        assertEquals(100f / 200f, dr, 0.001f)
    }

    // ==================== HP ====================

    @Test
    fun `getMaxHp - суммирует базу, уровень и предметы`() {
        val maxHp = GameCalculations.getMaxHp(level = 5, baseHealth = 100, itemHealthBonus = 20)
        // 100 + 5*10 + 20 = 170
        assertEquals(170, maxHp)
    }

    @Test
    fun `calculateHpRegen - 0 минут = 0 реген`() {
        assertEquals(0, GameCalculations.calculateHpRegen(100, 0))
    }

    @Test
    fun `calculateHpRegen - 1% от maxHp в минуту`() {
        val regen = GameCalculations.calculateHpRegen(200, 1)
        assertEquals(2, regen) // 200 * 0.01 = 2
    }

    // ==================== Зубы ====================

    @Test
    fun `getTeethFromSell - редкость определяет цену`() {
        assertEquals(1, GameCalculations.getTeethFromSell("common"))
        assertEquals(2, GameCalculations.getTeethFromSell("uncommon"))
        assertEquals(3, GameCalculations.getTeethFromSell("rare"))
        assertEquals(5, GameCalculations.getTeethFromSell("epic"))
    }

    @Test
    fun `getTeethFromMonster - от 1 до monsterLevel включительно`() {
        repeat(100) {
            val teeth = GameCalculations.getTeethFromMonster(5)
            assertTrue("Зубы должны быть от 1 до 5", teeth in 1..5)
        }
    }

    // ==================== Burst ====================

    @Test
    fun `isBurstAttack - 50 отжиманий = burst`() {
        assertTrue(GameCalculations.isBurstAttack(50))
    }

    @Test
    fun `isBurstAttack - 49 отжиманий = не burst`() {
        assertFalse(GameCalculations.isBurstAttack(49))
    }
}
