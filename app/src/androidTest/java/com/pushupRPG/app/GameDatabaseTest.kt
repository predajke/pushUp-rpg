package com.ninthbalcony.pushuprpg

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ninthbalcony.pushuprpg.data.db.AppDatabase
import com.ninthbalcony.pushuprpg.data.db.GameStateEntity
import com.ninthbalcony.pushuprpg.data.db.PushUpRecordEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameDatabaseTest {

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

    // ==================== GameState ====================

    @Test
    fun saveAndLoadGameState() = runBlocking {
        val dao = db.pushUpDao()
        val state = GameStateEntity(
            playerName = "TestHero",
            playerLevel = 5,
            totalXp = 1000,
            teeth = 42
        )
        dao.saveGameState(state)

        val loaded = dao.getGameState()
        assertNotNull(loaded)
        assertEquals("TestHero", loaded!!.playerName)
        assertEquals(5, loaded.playerLevel)
        assertEquals(1000, loaded.totalXp)
        assertEquals(42, loaded.teeth)
    }

    @Test
    fun initialGameState_isNull_beforeAnyInsert() = runBlocking {
        val dao = db.pushUpDao()
        val state = dao.getGameState()
        assertNull(state)
    }

    @Test
    fun gameStateFlow_emitsUpdates() = runBlocking {
        val dao = db.pushUpDao()
        val state = GameStateEntity(playerName = "FlowHero")
        dao.saveGameState(state)

        val fromFlow = dao.getGameStateFlow().first()
        assertNotNull(fromFlow)
        assertEquals("FlowHero", fromFlow!!.playerName)
    }

    @Test
    fun inventoryItems_defaultIsEmptyString() = runBlocking {
        val dao = db.pushUpDao()
        val state = GameStateEntity()
        dao.saveGameState(state)

        val loaded = dao.getGameState()
        assertEquals("", loaded!!.inventoryItems)
    }

    @Test
    fun updateGameState_replacesPrevious() = runBlocking {
        val dao = db.pushUpDao()
        dao.saveGameState(GameStateEntity(playerLevel = 1))
        dao.saveGameState(GameStateEntity(playerLevel = 10))

        val loaded = dao.getGameState()
        assertEquals(10, loaded!!.playerLevel)
    }

    // ==================== PushUp Records ====================

    @Test
    fun insertAndQueryPushUpRecord() = runBlocking {
        val dao = db.pushUpDao()
        dao.insertPushUpRecord(PushUpRecordEntity(date = "2026-04-04", count = 50))

        val total = dao.getPushUpsForDate("2026-04-04")
        assertEquals(50, total)
    }

    @Test
    fun getPushUpsForDate_returnsNullIfNoRecords() = runBlocking {
        val dao = db.pushUpDao()
        val result = dao.getPushUpsForDate("2000-01-01")
        assertNull(result)
    }

    @Test
    fun getLast7DaysStats_returnsCorrectDates() = runBlocking {
        val dao = db.pushUpDao()
        dao.insertPushUpRecord(PushUpRecordEntity(date = "2026-04-01", count = 30))
        dao.insertPushUpRecord(PushUpRecordEntity(date = "2026-04-02", count = 20))
        dao.insertPushUpRecord(PushUpRecordEntity(date = "2026-04-02", count = 10)) // та же дата

        val stats = dao.getLast7DaysStats()
        assertEquals(2, stats.size) // 2 уникальных даты
        val apr2 = stats.find { it.date == "2026-04-02" }
        assertNotNull(apr2)
        assertEquals(30, apr2!!.count) // 20 + 10 = 30
    }

    @Test
    fun getTotalPushUps_sumsAllRecords() = runBlocking {
        val dao = db.pushUpDao()
        dao.insertPushUpRecord(PushUpRecordEntity(date = "2026-04-01", count = 100))
        dao.insertPushUpRecord(PushUpRecordEntity(date = "2026-04-02", count = 150))

        val total = dao.getTotalPushUps()
        assertEquals(250, total)
    }

    @Test
    fun deleteAllPushUpRecords_clearsTable() = runBlocking {
        val dao = db.pushUpDao()
        dao.insertPushUpRecord(PushUpRecordEntity(date = "2026-04-01", count = 50))
        dao.deleteAllPushUpRecords()

        val total = dao.getTotalPushUps()
        assertNull(total)
    }
}
