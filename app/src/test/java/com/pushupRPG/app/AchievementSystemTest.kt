package com.ninthbalcony.pushuprpg

import com.google.gson.Gson
import com.ninthbalcony.pushuprpg.data.db.GameStateEntity
import com.ninthbalcony.pushuprpg.utils.AchBonusType
import com.ninthbalcony.pushuprpg.utils.AchievementBonuses
import com.ninthbalcony.pushuprpg.utils.AchievementSystem
import org.junit.Assert.*
import org.junit.Test

/**
 * Comprehensive tests for the 8 new multi-bonus achievements added in v1.2,
 * plus multi-bonus system validation and balance change verification.
 */
class AchievementSystemTest {

    private val today = "2026-05-29"
    private val gson = Gson()

    private fun hasAchievement(state: GameStateEntity, achId: String): Boolean {
        return AchievementSystem.getUnlocked(state.achievementsJson).any { it.defId == achId }
    }

    private fun bestiaryJson(kills: Map<String, Int>): String = gson.toJson(kills)

    // ══════════════════════════════════════════════════════════════════════
    //  1. ach_pentagram — Crooked Casino: 10 winning Night Spins
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `Crooked Casino — 9 wins is not enough`() {
        val state = GameStateEntity(nightSpinWins = 9)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertFalse("9 wins should NOT unlock Crooked Casino", hasAchievement(result, "ach_pentagram"))
    }

    @Test
    fun `Crooked Casino — exactly 10 wins unlocks`() {
        val state = GameStateEntity(nightSpinWins = 10)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertTrue("10 wins should unlock Crooked Casino", hasAchievement(result, "ach_pentagram"))
    }

    @Test
    fun `Crooked Casino — 50 spins with mixed results (user scenario 1)`() {
        // User does 50 spins. Night spin has ~10% chance to win (stat boost + items).
        // If only 8 out of 50 were wins, achievement should NOT unlock.
        val state = GameStateEntity(nightSpinWins = 8, nightSpinNothing = 42)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertFalse("8 wins out of 50 should NOT unlock", hasAchievement(result, "ach_pentagram"))
    }

    @Test
    fun `Crooked Casino — 50 spins with 12 wins unlocks`() {
        val state = GameStateEntity(nightSpinWins = 12, nightSpinNothing = 38)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertTrue("12 wins out of 50 should unlock", hasAchievement(result, "ach_pentagram"))
    }

    // ══════════════════════════════════════════════════════════════════════
    //  2. ach_red_flag — Collector's Banner: 100+ items in inventory
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `Collector's Banner — 99 items is not enough`() {
        val items = (1..99).joinToString(",") { "weapon_01_$it:0" }
        val state = GameStateEntity(inventoryItems = items)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertFalse("99 items should NOT unlock Collector's Banner", hasAchievement(result, "ach_red_flag"))
    }

    @Test
    fun `Collector's Banner — 100 items unlocks`() {
        val items = (1..100).joinToString(",") { "weapon_01_$it:0" }
        val state = GameStateEntity(inventoryItems = items)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertTrue("100 items should unlock Collector's Banner", hasAchievement(result, "ach_red_flag"))
    }

    @Test
    fun `Collector's Banner — buy 99 then buy 1 more and sell 10 (user scenario 2)`() {
        // User has 99 items → buys 1 more = 100 → unlocks
        val items100 = (1..100).joinToString(",") { "weapon_01_$it:0" }
        val state100 = GameStateEntity(inventoryItems = items100)
        val result100 = AchievementSystem.checkAndUnlock(state100, today)
        assertTrue("100 items should unlock", hasAchievement(result100, "ach_red_flag"))

        // User sells 10 → 90 items left. Achievement already unlocked stays unlocked.
        val items90 = (1..90).joinToString(",") { "weapon_01_$it:0" }
        val stateAfterSell = result100.copy(inventoryItems = items90)
        val resultAfterSell = AchievementSystem.checkAndUnlock(stateAfterSell, today)
        assertTrue("Achievement should persist after selling items", hasAchievement(resultAfterSell, "ach_red_flag"))
    }

    @Test
    fun `Collector's Banner — empty inventory does not crash`() {
        val state = GameStateEntity(inventoryItems = "")
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertFalse(hasAchievement(result, "ach_red_flag"))
    }

    // ══════════════════════════════════════════════════════════════════════
    //  3. ach_roach — Arachnophobe: 200 kills of level 1-5 monsters
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `Arachnophobe — 100 lvl1 + 90 lvl2 + 9 lvl5 = 199, not enough`() {
        val bestiary = bestiaryJson(mapOf(
            "Gopnik" to 100,          // level 1
            "Anime-Thug" to 90,       // level 2
            "Zergodog" to 9           // level 5
        ))
        val state = GameStateEntity(bestiaryJson = bestiary)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertFalse("199 kills should NOT unlock Arachnophobe", hasAchievement(result, "ach_roach"))
    }

    @Test
    fun `Arachnophobe — 100 lvl1 + 90 lvl2 + 11 lvl5 = 201, unlocks (user scenario 3)`() {
        val bestiary = bestiaryJson(mapOf(
            "Gopnik" to 100,          // level 1
            "Anime-Thug" to 90,       // level 2
            "Zergodog" to 11          // level 5
        ))
        val state = GameStateEntity(bestiaryJson = bestiary)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertTrue("201 low-level kills should unlock Arachnophobe", hasAchievement(result, "ach_roach"))
    }

    @Test
    fun `Arachnophobe — high level monster kills do NOT count`() {
        val bestiary = bestiaryJson(mapOf(
            "Overlord" to 500,        // level 10 — too high
            "Pumped Clone" to 200     // level 50 — way too high
        ))
        val state = GameStateEntity(bestiaryJson = bestiary)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertFalse("High-level kills should NOT count", hasAchievement(result, "ach_roach"))
    }

    @Test
    fun `Arachnophobe — level 6 monsters do NOT count`() {
        val bestiary = bestiaryJson(mapOf(
            "BFG2K111" to 300         // level 6 — just above threshold
        ))
        val state = GameStateEntity(bestiaryJson = bestiary)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertFalse("Level 6 kills should NOT count for 1-5 requirement", hasAchievement(result, "ach_roach"))
    }

    @Test
    fun `Arachnophobe — exactly 200 lvl1 kills unlocks`() {
        val bestiary = bestiaryJson(mapOf("Gopnik" to 200))
        val state = GameStateEntity(bestiaryJson = bestiary)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertTrue("200 lvl1 kills should unlock", hasAchievement(result, "ach_roach"))
    }

    // ══════════════════════════════════════════════════════════════════════
    //  4. ach_night_ench — Night Enchanter: night enchant +21+
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `Night Enchanter — max night enchant level 20 is not enough`() {
        val state = GameStateEntity(nightEnchantMaxLevel = 20)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertFalse("Night enchant +20 should NOT unlock", hasAchievement(result, "ach_night_ench"))
    }

    @Test
    fun `Night Enchanter — max night enchant level 21 unlocks`() {
        val state = GameStateEntity(nightEnchantMaxLevel = 21)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertTrue("Night enchant +21 should unlock", hasAchievement(result, "ach_night_ench"))
    }

    @Test
    fun `Night Enchanter — weapon at +15 with sequence of successes and failures (user scenario 4)`() {
        // User starts at +15, gets: +1, fail, fail, +1, fail, +1, fail, +1, fail, +1
        // Final level: 15 + 5 = 20. NOT enough (need 21).
        val state = GameStateEntity(nightEnchantMaxLevel = 20)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertFalse("+15 → +20 (5 successes) should NOT unlock", hasAchievement(result, "ach_night_ench"))
    }

    @Test
    fun `Night Enchanter — one more success to +21 unlocks`() {
        // Continuation: one more success → +21
        val state = GameStateEntity(nightEnchantMaxLevel = 21)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertTrue("+21 should unlock Night Enchanter", hasAchievement(result, "ach_night_ench"))
    }

    @Test
    fun `Night Enchanter — level 25 also unlocks`() {
        val state = GameStateEntity(nightEnchantMaxLevel = 25)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertTrue("+25 should unlock", hasAchievement(result, "ach_night_ench"))
    }

    // ══════════════════════════════════════════════════════════════════════
    //  5. ach_purple_skull — Insane Gambler: 100 "nothing" in Night Spin
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `Insane Gambler — 99 nothing is not enough`() {
        val state = GameStateEntity(nightSpinNothing = 99)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertFalse("99 nothing should NOT unlock", hasAchievement(result, "ach_purple_skull"))
    }

    @Test
    fun `Insane Gambler — exactly 100 nothing unlocks`() {
        val state = GameStateEntity(nightSpinNothing = 100)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertTrue("100 nothing should unlock Insane Gambler", hasAchievement(result, "ach_purple_skull"))
    }

    @Test
    fun `Insane Gambler — 200 nothing also unlocks`() {
        val state = GameStateEntity(nightSpinNothing = 200)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertTrue("200 nothing should unlock", hasAchievement(result, "ach_purple_skull"))
    }

    // ══════════════════════════════════════════════════════════════════════
    //  6. ach_red_sword — Sunset Blade: weapon with enchant +18+
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `Sunset Blade — weapon at +17 is not enough`() {
        val state = GameStateEntity(inventoryItems = "weapon_01_1234567890:17")
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertFalse("+17 weapon should NOT unlock", hasAchievement(result, "ach_red_sword"))
    }

    @Test
    fun `Sunset Blade — weapon at +18 unlocks`() {
        val state = GameStateEntity(inventoryItems = "weapon_01_1234567890:18")
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertTrue("+18 weapon should unlock Sunset Blade", hasAchievement(result, "ach_red_sword"))
    }

    @Test
    fun `Sunset Blade — non-weapon at +20 does NOT unlock`() {
        val state = GameStateEntity(inventoryItems = "head_01_1234567890:20")
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertFalse("+20 helm should NOT unlock Sunset Blade", hasAchievement(result, "ach_red_sword"))
    }

    @Test
    fun `Sunset Blade — mixed inventory with one weapon +18`() {
        val items = listOf(
            "head_01_111:5",
            "boots_03_222:10",
            "weapon_32_333:18",
            "necklace_01_444:3"
        ).joinToString(",")
        val state = GameStateEntity(inventoryItems = items)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertTrue("Mixed inventory with weapon +18 should unlock", hasAchievement(result, "ach_red_sword"))
    }

    @Test
    fun `Sunset Blade — weapon without enchant level does not crash`() {
        val state = GameStateEntity(inventoryItems = "weapon_01_1234567890")
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertFalse("Weapon without enchant level should NOT unlock", hasAchievement(result, "ach_red_sword"))
    }

    // ══════════════════════════════════════════════════════════════════════
    //  7. ach_goold — Scrooge McFang: spend 10000 teeth
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `Scrooge McFang — 9999 teeth spent is not enough`() {
        val state = GameStateEntity(totalTeethSpent = 9999)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertFalse("9999 teeth should NOT unlock", hasAchievement(result, "ach_goold"))
    }

    @Test
    fun `Scrooge McFang — exactly 10000 teeth unlocks`() {
        val state = GameStateEntity(totalTeethSpent = 10000)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertTrue("10000 teeth should unlock Scrooge McFang", hasAchievement(result, "ach_goold"))
    }

    @Test
    fun `Scrooge McFang — 50000 teeth also unlocks`() {
        val state = GameStateEntity(totalTeethSpent = 50000)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertTrue("50000 teeth should unlock", hasAchievement(result, "ach_goold"))
    }

    // ══════════════════════════════════════════════════════════════════════
    //  8. ach_fighter — Street Fighter: 150 total punches
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `Street Fighter — 149 punches is not enough`() {
        val state = GameStateEntity(totalPunchesAllTime = 149)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertFalse("149 punches should NOT unlock", hasAchievement(result, "ach_fighter"))
    }

    @Test
    fun `Street Fighter — exactly 150 punches unlocks`() {
        val state = GameStateEntity(totalPunchesAllTime = 150)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertTrue("150 punches should unlock Street Fighter", hasAchievement(result, "ach_fighter"))
    }

    @Test
    fun `Street Fighter — 1000 punches also unlocks`() {
        val state = GameStateEntity(totalPunchesAllTime = 1000)
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertTrue("1000 punches should unlock", hasAchievement(result, "ach_fighter"))
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Multi-bonus system validation
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `Multi-bonus — Crooked Casino gives both crit and XP`() {
        val bonuses = AchievementSystem.getActiveBonuses("ach_pentagram")
        assertEquals("Crit should be 3%", 0.03f, bonuses.critPercent, 0.001f)
        assertEquals("XP should be 3%", 0.03f, bonuses.xpPercent, 0.001f)
    }

    @Test
    fun `Multi-bonus — Insane Gambler gives teeth + dmg + crit (3 bonuses)`() {
        val bonuses = AchievementSystem.getActiveBonuses("ach_purple_skull")
        assertEquals("Teeth should be 4%", 0.04f, bonuses.teethRatePercent, 0.001f)
        assertEquals("DMG should be 1%", 0.01f, bonuses.damagePercent, 0.001f)
        assertEquals("Crit should be 1%", 0.01f, bonuses.critPercent, 0.001f)
    }

    @Test
    fun `Multi-bonus — Night Enchanter gives dmg + drop + forge (3 bonuses)`() {
        val bonuses = AchievementSystem.getActiveBonuses("ach_night_ench")
        assertEquals("DMG should be 3%", 0.03f, bonuses.damagePercent, 0.001f)
        assertEquals("Drop should be 8%", 0.08f, bonuses.dropRatePercent, 0.001f)
        assertEquals("Forge should be 10", 10f, bonuses.enchantFlat, 0.001f)
    }

    @Test
    fun `Multi-bonus — Scrooge McFang gives drop + HP + teeth (3 bonuses)`() {
        val bonuses = AchievementSystem.getActiveBonuses("ach_goold")
        assertEquals("Drop should be 5%", 0.05f, bonuses.dropRatePercent, 0.001f)
        assertEquals("HP should be 10", 10, bonuses.hpFlat)
        assertEquals("Teeth should be 10%", 0.10f, bonuses.teethRatePercent, 0.001f)
    }

    @Test
    fun `Multi-bonus — Street Fighter gives dmg + xp + crit`() {
        val bonuses = AchievementSystem.getActiveBonuses("ach_fighter")
        assertEquals("DMG should be 1%", 0.01f, bonuses.damagePercent, 0.001f)
        assertEquals("XP should be 2%", 0.02f, bonuses.xpPercent, 0.001f)
        assertEquals("Crit should be 3%", 0.03f, bonuses.critPercent, 0.001f)
    }

    @Test
    fun `Multi-bonus — Arachnophobe gives xp + HP + drop`() {
        val bonuses = AchievementSystem.getActiveBonuses("ach_roach")
        assertEquals("XP should be 3%", 0.03f, bonuses.xpPercent, 0.001f)
        assertEquals("HP should be 30", 30, bonuses.hpFlat)
        assertEquals("Drop should be 3%", 0.03f, bonuses.dropRatePercent, 0.001f)
    }

    @Test
    fun `Multi-bonus — Sunset Blade gives dmg + crit`() {
        val bonuses = AchievementSystem.getActiveBonuses("ach_red_sword")
        assertEquals("DMG should be 4%", 0.04f, bonuses.damagePercent, 0.001f)
        assertEquals("Crit should be 1%", 0.01f, bonuses.critPercent, 0.001f)
    }

    @Test
    fun `Multi-bonus — Collector's Banner gives armor + drop`() {
        val bonuses = AchievementSystem.getActiveBonuses("ach_red_flag")
        assertEquals("Armor should be 5%", 0.05f, bonuses.armorPercent, 0.001f)
        assertEquals("Drop should be 5%", 0.05f, bonuses.dropRatePercent, 0.001f)
    }

    @Test
    fun `Multi-bonus — stacking two multi-bonus achievements`() {
        // Crooked Casino (+3% crit, +3% XP) + Street Fighter (+1% dmg, +2% XP, +3% crit)
        val bonuses = AchievementSystem.getActiveBonuses("ach_pentagram,ach_fighter")
        assertEquals("Crit should stack: 3+3 = 6%", 0.06f, bonuses.critPercent, 0.001f)
        assertEquals("XP should stack: 3+2 = 5%", 0.05f, bonuses.xpPercent, 0.001f)
        assertEquals("DMG should be 1%", 0.01f, bonuses.damagePercent, 0.001f)
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Balance change verification (existing achievements)
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `Dragon Slayer bonus changed from 8% to 5%`() {
        val bonuses = AchievementSystem.getActiveBonuses("ach_dragon_slayer")
        assertEquals("Dragon Slayer should be 5% DMG", 0.05f, bonuses.damagePercent, 0.001f)
    }

    @Test
    fun `VOID bonus changed from 12% to 10%`() {
        val bonuses = AchievementSystem.getActiveBonuses("ach_void")
        assertEquals("VOID should be 10% DMG", 0.10f, bonuses.damagePercent, 0.001f)
    }

    @Test
    fun `King Slayer bonus changed from 10% to 9%`() {
        val bonuses = AchievementSystem.getActiveBonuses("ach_king_slayer")
        assertEquals("King Slayer should be 9% DMG", 0.09f, bonuses.damagePercent, 0.001f)
    }

    // ══════════════════════════════════════════════════════════════════════
    //  getBonusLabel — multi-bonus display
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `getBonusLabel — single bonus shows one entry`() {
        val def = AchievementSystem.getDefById("ach_dragon_slayer")!!
        val label = AchievementSystem.getBonusLabel(def, "en")
        assertEquals("+5% DMG", label)
    }

    @Test
    fun `getBonusLabel — dual bonus shows two entries`() {
        val def = AchievementSystem.getDefById("ach_pentagram")!!
        val label = AchievementSystem.getBonusLabel(def, "en")
        assertTrue("Label should contain crit", label.contains("crit"))
        assertTrue("Label should contain XP", label.contains("XP"))
        assertTrue("Label should have comma separator", label.contains(","))
    }

    @Test
    fun `getBonusLabel — triple bonus shows three entries`() {
        val def = AchievementSystem.getDefById("ach_night_ench")!!
        val label = AchievementSystem.getBonusLabel(def, "en")
        val parts = label.split(",").map { it.trim() }
        assertEquals("Triple bonus should have 3 parts", 3, parts.size)
    }

    @Test
    fun `getBonusLabel — Russian localization works`() {
        val def = AchievementSystem.getDefById("ach_goold")!!
        val label = AchievementSystem.getBonusLabel(def, "ru")
        assertTrue("Should contain дроп", label.contains("дроп"))
        assertTrue("Should contain зубы", label.contains("зубы"))
    }

    // ══════════════════════════════════════════════════════════════════════
    //  getProgressText — new achievements
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `getProgressText — Crooked Casino shows wins vs target`() {
        val def = AchievementSystem.getDefById("ach_pentagram")!!
        val state = GameStateEntity(nightSpinWins = 7)
        val text = AchievementSystem.getProgressText(def, state, "en")
        assertEquals("7 / 10", text)
    }

    @Test
    fun `getProgressText — Insane Gambler shows nothing count`() {
        val def = AchievementSystem.getDefById("ach_purple_skull")!!
        val state = GameStateEntity(nightSpinNothing = 42)
        val text = AchievementSystem.getProgressText(def, state, "en")
        assertEquals("42 / 100", text)
    }

    @Test
    fun `getProgressText — Street Fighter shows punches`() {
        val def = AchievementSystem.getDefById("ach_fighter")!!
        val state = GameStateEntity(totalPunchesAllTime = 88)
        val text = AchievementSystem.getProgressText(def, state, "en")
        assertEquals("88 / 150", text)
    }

    @Test
    fun `getProgressText — Scrooge McFang shows teeth spent`() {
        val def = AchievementSystem.getDefById("ach_goold")!!
        val state = GameStateEntity(totalTeethSpent = 3500)
        val text = AchievementSystem.getProgressText(def, state, "en")
        assertEquals("3500 / 10000", text)
    }

    @Test
    fun `getProgressText — Night Enchanter shows max level`() {
        val def = AchievementSystem.getDefById("ach_night_ench")!!
        val state = GameStateEntity(nightEnchantMaxLevel = 18)
        val text = AchievementSystem.getProgressText(def, state, "en")
        assertEquals("18 / 21", text)
    }

    @Test
    fun `getProgressText — Collector's Banner shows inventory size`() {
        val def = AchievementSystem.getDefById("ach_red_flag")!!
        val items = (1..42).joinToString(",") { "weapon_01_$it:0" }
        val state = GameStateEntity(inventoryItems = items)
        val text = AchievementSystem.getProgressText(def, state, "en")
        assertEquals("42 / 100", text)
    }

    @Test
    fun `getProgressText — Sunset Blade shows max weapon enchant`() {
        val def = AchievementSystem.getDefById("ach_red_sword")!!
        val items = "weapon_01_111:12,head_01_222:20,weapon_02_333:15"
        val state = GameStateEntity(inventoryItems = items)
        val text = AchievementSystem.getProgressText(def, state, "en")
        assertEquals("15 / 18", text)
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Edge cases and idempotency
    // ══════════════════════════════════════════════════════════════════════

    @Test
    fun `checkAndUnlock is idempotent — calling twice does not duplicate`() {
        val state = GameStateEntity(nightSpinWins = 10, totalPunchesAllTime = 150)
        val result1 = AchievementSystem.checkAndUnlock(state, today)
        val result2 = AchievementSystem.checkAndUnlock(result1, today)

        val count1 = AchievementSystem.getUnlocked(result1.achievementsJson).size
        val count2 = AchievementSystem.getUnlocked(result2.achievementsJson).size
        assertEquals("Second call should not add duplicates", count1, count2)
    }

    @Test
    fun `All 8 new achievements can unlock simultaneously`() {
        val bestiary = bestiaryJson(mapOf("Gopnik" to 200))
        val items = (1..100).joinToString(",") { "weapon_01_$it:18" }
        val state = GameStateEntity(
            nightSpinWins = 10,
            nightSpinNothing = 100,
            nightEnchantMaxLevel = 25,
            totalPunchesAllTime = 150,
            totalTeethSpent = 10000,
            inventoryItems = items,
            bestiaryJson = bestiary
        )
        val result = AchievementSystem.checkAndUnlock(state, today)
        assertTrue(hasAchievement(result, "ach_pentagram"))
        assertTrue(hasAchievement(result, "ach_red_flag"))
        assertTrue(hasAchievement(result, "ach_roach"))
        assertTrue(hasAchievement(result, "ach_night_ench"))
        assertTrue(hasAchievement(result, "ach_purple_skull"))
        assertTrue(hasAchievement(result, "ach_red_sword"))
        assertTrue(hasAchievement(result, "ach_goold"))
        assertTrue(hasAchievement(result, "ach_fighter"))
    }

    @Test
    fun `Default state unlocks nothing new`() {
        val state = GameStateEntity()
        val result = AchievementSystem.checkAndUnlock(state, today)
        val unlocked = AchievementSystem.getUnlocked(result.achievementsJson)
        val newAchIds = listOf(
            "ach_pentagram", "ach_red_sword", "ach_red_flag", "ach_purple_skull",
            "ach_roach", "ach_night_ench", "ach_goold", "ach_fighter"
        )
        for (id in newAchIds) {
            assertFalse("$id should NOT unlock on default state", unlocked.any { it.defId == id })
        }
    }

    @Test
    fun `Empty activeIdsStr returns zero bonuses`() {
        val bonuses = AchievementSystem.getActiveBonuses("")
        assertEquals(0f, bonuses.xpPercent, 0.001f)
        assertEquals(0f, bonuses.damagePercent, 0.001f)
        assertEquals(0f, bonuses.critPercent, 0.001f)
        assertEquals(0f, bonuses.dropRatePercent, 0.001f)
        assertEquals(0, bonuses.hpFlat)
    }

    @Test
    fun `Invalid achievement ID in activeIds is silently ignored`() {
        val bonuses = AchievementSystem.getActiveBonuses("nonexistent_ach,ach_pentagram")
        // Only ach_pentagram bonuses should apply
        assertEquals(0.03f, bonuses.critPercent, 0.001f)
        assertEquals(0.03f, bonuses.xpPercent, 0.001f)
    }

    @Test
    fun `All 8 new achievements exist in AchievementSystem ALL list`() {
        val ids = listOf(
            "ach_pentagram", "ach_red_sword", "ach_red_flag", "ach_purple_skull",
            "ach_roach", "ach_night_ench", "ach_goold", "ach_fighter"
        )
        for (id in ids) {
            assertNotNull("$id should exist in ALL", AchievementSystem.getDefById(id))
        }
    }
}
