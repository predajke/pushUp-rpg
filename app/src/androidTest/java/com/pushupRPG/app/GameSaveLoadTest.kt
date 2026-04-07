package com.pushupRPG.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pushupRPG.app.data.db.AppDatabase
import com.pushupRPG.app.data.db.GameStateEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test: saves a level-20 character with 10 inventory items + equipped slots,
 * re-reads (simulating restart), verifies persistence, then upgrades stats and verifies again.
 */
@RunWith(AndroidJUnit4::class)
class GameSaveLoadTest {

    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private val tenItems =
        "sword_001,axe_001,shield_001,helmet_001,boots_001,necklace_001,pants_001,sword_002,axe_002,shield_002"

    @Test
    fun saveAndLoad_level20Character() = runBlocking {
        val dao = db.pushUpDao()

        // Scenario 1: create level-20 character with stats 200/200/200/50, 10 items, head + boots equipped
        val initial = GameStateEntity(
            playerLevel    = 20,
            basePower      = 200,
            baseArmor      = 200,
            baseHealth     = 200,
            baseLuck       = 50f,
            inventoryItems = tenItems,
            equippedHead   = "helmet_001",
            equippedBoots  = "boots_001"
        )
        dao.saveGameState(initial)

        // Simulate restart: re-read from DB
        val loaded = dao.getGameState()
        assertNotNull(loaded)
        assertEquals(20, loaded!!.playerLevel)
        assertEquals(200, loaded.basePower)
        assertEquals(200, loaded.baseArmor)
        assertEquals(200, loaded.baseHealth)
        assertEquals(50f, loaded.baseLuck)
        assertEquals("helmet_001", loaded.equippedHead)
        assertEquals("boots_001", loaded.equippedBoots)
        val loadedItems = loaded.inventoryItems.split(",").filter { it.isNotEmpty() }
        assertEquals(10, loadedItems.size)

        // Scenario 2: upgrade stats to 300/300/300/50, save, then verify
        dao.saveGameState(loaded.copy(
            basePower  = 300,
            baseArmor  = 300,
            baseHealth = 300
        ))

        val upgraded = dao.getGameState()
        assertNotNull(upgraded)
        assertEquals(300, upgraded!!.basePower)
        assertEquals(300, upgraded.baseArmor)
        assertEquals(300, upgraded.baseHealth)
        assertEquals(50f, upgraded.baseLuck)
        // Inventory and equipment must be intact
        assertEquals("helmet_001", upgraded.equippedHead)
        assertEquals("boots_001", upgraded.equippedBoots)
        val upgradedItems = upgraded.inventoryItems.split(",").filter { it.isNotEmpty() }
        assertEquals(10, upgradedItems.size)
    }
}
