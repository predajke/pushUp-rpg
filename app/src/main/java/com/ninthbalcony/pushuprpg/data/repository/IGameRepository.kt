package com.ninthbalcony.pushuprpg.data.repository

import com.ninthbalcony.pushuprpg.data.db.DayStats
import com.ninthbalcony.pushuprpg.data.db.GameStateEntity
import com.ninthbalcony.pushuprpg.data.db.LogEntryEntity
import com.ninthbalcony.pushuprpg.data.model.EnchantResult
import com.ninthbalcony.pushuprpg.data.model.ForgeResult
import com.ninthbalcony.pushuprpg.data.model.Item
import com.ninthbalcony.pushuprpg.data.model.PeriodStats
import com.ninthbalcony.pushuprpg.managers.PlayGamesManager
import com.ninthbalcony.pushuprpg.utils.DailyRewardUtils
import com.ninthbalcony.pushuprpg.utils.SpinResult
import kotlinx.coroutines.flow.Flow

interface IGameRepository {

    // ===== Flows =====
    fun getGameStateFlow(): Flow<GameStateEntity?>
    fun getRecentLogsFlow(): Flow<List<LogEntryEntity>>
    fun getAllLogsFlow(): Flow<List<LogEntryEntity>>

    // ===== Game State =====
    suspend fun getGameState(): GameStateEntity
    suspend fun saveGameState(state: GameStateEntity)

    // ===== Push-ups =====
    suspend fun addPushUps(count: Int): Int

    // ===== Battle =====
    suspend fun processBattleTick()

    // ===== Streak =====
    suspend fun updateStreakOnLogin()

    // ===== Statistics =====
    suspend fun getStatsForPeriod(): PeriodStats
    suspend fun getLast7DaysStats(): List<DayStats>
    suspend fun getLast12MonthsStats(): List<DayStats>

    // ===== Logs =====
    suspend fun addLog(message: String, messageRu: String)

    // ===== Equipment =====
    suspend fun equipItem(itemId: String, slot: String)
    suspend fun unequipItem(slot: String)
    suspend fun sellItem(itemId: String)

    // ===== Reset =====
    suspend fun resetAllProgress()

    // ===== Stat Points =====
    suspend fun spendStatPoint(stat: String)
    suspend fun useFreePoints(): Boolean

    // ===== Events =====
    suspend fun checkAndUpdateEvent(): GameStateEntity

    // ===== Shop =====
    suspend fun getOrRefreshShop(): List<Item>
    suspend fun buyShopItem(itemId: String): Boolean
    suspend fun addTeeth(amount: Int)
    suspend fun rerollShop(): Boolean

    // ===== Forge =====
    suspend fun setForgeSlot(slot: Int, itemId: String)
    suspend fun recycleToForgeSlots()
    suspend fun mergeItems(): ForgeResult

    // ===== Daily Reset =====
    suspend fun checkAndResetDaily()
    suspend fun checkAndGrantHourlySpins(): Int

    // ===== Spin =====
    suspend fun performDailySpin(): SpinResult?
    suspend fun addSpinFromAd(): Boolean
    suspend fun getAvailableSpins(): Int

    // ===== Clover Box =====
    suspend fun useCloverBox(): Item?

    // ===== Enchant =====
    fun calculateEnchantChance(luck: Float, streak: Int, achBonus: Float = 0f): Float
    fun calculateEnchantCost(rarity: String, currentEnchantLevel: Int): Int
    suspend fun enchantItem(itemId: String): EnchantResult

    // ===== Quests =====
    suspend fun setActiveAchievements(ids: List<String>)
    suspend fun checkAndRefreshQuests()
    suspend fun claimQuestReward(defId: String): Boolean
    suspend fun adRerollDailyQuests(): Boolean

    // ===== Daily Reward =====
    suspend fun claimDailyReward(): DailyRewardUtils.DailyReward?

    // ===== Rate Us =====
    suspend fun updateRateUsState(action: RateUsAction)

    // ===== Play Games =====
    fun setPlayGamesManager(manager: PlayGamesManager)

    // ===== Debug =====
    suspend fun addDebugItemsForTest()
}
