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
    fun `getLevelFromXp - уровень 1 при xp=100`() {
        assertEquals(1, GameCalculations.getLevelFromXp(100))
    }

    @Test
    fun `getLevelFromXp - уровень 7 при xp=3000`() {
        // 2700 < 3000 < 3500, поэтому уровень 7
        assertEquals(7, GameCalculations.getLevelFromXp(3000))
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
    fun `getXpThresholdForLevel - уровень 10 = 5400`() {
        // Сумма: 200 + 300 + 400 + 500 + 600 + 700 + 800 + 900 + 1000 = 5400
        val threshold = GameCalculations.getXpThresholdForLevel(10)
        assertEquals(5400, threshold)
    }

    @Test
    fun `getXpProgress - прогресс от 0 до 1 в пределах уровня`() {
        val progress = GameCalculations.getXpProgress(50) // в начале 1→2 уровня (0..100)
        assertTrue("Прогресс должен быть между 0 и 1", progress in 0f..1f)
    }

    @Test
    fun `getXpForNextLevel - положительное значение`() {
        val needed = GameCalculations.getXpForNextLevel(0) // нужно 200 до уровня 2
        assertEquals(200, needed)
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
        // 100 + 5*HP_PER_LEVEL + 20 = 100 + 5*20 + 20 = 220
        assertEquals(220, maxHp)
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
        assertEquals(2, GameCalculations.getTeethFromSell("common"))
        assertEquals(4, GameCalculations.getTeethFromSell("uncommon"))
        assertEquals(8, GameCalculations.getTeethFromSell("rare"))
        assertEquals(22, GameCalculations.getTeethFromSell("epic"))
        assertEquals(40, GameCalculations.getTeethFromSell("legendary"))
    }

    @Test
    fun `getLuckFromSell - только epic и legendary дают удачу`() {
        assertEquals(0f, GameCalculations.getLuckFromSell("common"), 0.0001f)
        assertEquals(0f, GameCalculations.getLuckFromSell("uncommon"), 0.0001f)
        assertEquals(0f, GameCalculations.getLuckFromSell("rare"), 0.0001f)
        assertEquals(0.10f, GameCalculations.getLuckFromSell("epic"), 0.0001f)
        assertEquals(0.25f, GameCalculations.getLuckFromSell("legendary"), 0.0001f)
        assertEquals(0f, GameCalculations.getLuckFromSell("unknown"), 0.0001f)
    }

    @Test
    fun `getTeethFromMonster - масштабируется с уровнем монстра`() {
        repeat(100) {
            val teeth = GameCalculations.getTeethFromMonster(10)
            assertTrue("Зубы от монстра уровня 10 должны быть от 5 до 10", teeth in 5..10)
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

    // ==================== calculateTotalStats(state) overload ====================

    @Test
    fun `calculateTotalStats(state) - без экипа возвращает базовые статы`() {
        val state = com.ninthbalcony.pushuprpg.data.db.GameStateEntity(
            basePower = 7, baseArmor = 3, baseHealth = 150, baseLuck = 2f
        )
        val r = GameCalculations.calculateTotalStats(state)
        // Без вещей, без ачивок, без сетов, prestigeLevel=0:
        // power = (7 + 0) * (1f + 0 + 0 + 0) = 7
        assertEquals(7, r.power)
        assertEquals(3, r.armor)
        assertEquals(2f, r.luck, 0.001f)
    }

    @Test
    fun `calculateTotalStats(state) - эквивалентен explicit-версии при пустых входах`() {
        // В unit-тесте ItemUtils.allItems пустой (нет Context для загрузки items.json),
        // так что обе ветки дают идентичный результат — это и проверяем.
        val state = com.ninthbalcony.pushuprpg.data.db.GameStateEntity(
            basePower = 10, baseHealth = 100, playerLevel = 3, prestigeLevel = 1
        )
        val viaOverload = GameCalculations.calculateTotalStats(state)
        val viaExplicit = GameCalculations.calculateTotalStats(
            state, emptyList(), emptyList(),
            com.ninthbalcony.pushuprpg.utils.AchievementBonuses(),
            com.ninthbalcony.pushuprpg.utils.SetBonuses()
        )
        assertEquals(viaExplicit.power, viaOverload.power)
        assertEquals(viaExplicit.armor, viaOverload.armor)
        assertEquals(viaExplicit.health, viaOverload.health)
        assertEquals(viaExplicit.luck, viaOverload.luck, 0.001f)
    }
}
