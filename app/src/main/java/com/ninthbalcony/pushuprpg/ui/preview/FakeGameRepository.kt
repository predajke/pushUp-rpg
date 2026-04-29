package com.ninthbalcony.pushuprpg.ui.preview

import com.ninthbalcony.pushuprpg.data.db.DayStats
import com.ninthbalcony.pushuprpg.data.db.GameStateEntity
import com.ninthbalcony.pushuprpg.data.db.LogEntryEntity
import com.ninthbalcony.pushuprpg.data.model.BattleChain
import com.ninthbalcony.pushuprpg.data.model.EnchantResult
import com.ninthbalcony.pushuprpg.data.model.ForgeResult
import com.ninthbalcony.pushuprpg.data.model.Item
import com.ninthbalcony.pushuprpg.data.model.PeriodStats
import com.ninthbalcony.pushuprpg.data.repository.IGameRepository
import com.ninthbalcony.pushuprpg.data.repository.RateUsAction
import com.ninthbalcony.pushuprpg.managers.PlayGamesManager
import com.ninthbalcony.pushuprpg.utils.DailyRewardUtils
import com.ninthbalcony.pushuprpg.utils.SpinResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow

internal fun mockGameState() = GameStateEntity(
    playerName = "Hero",
    playerLevel = 5,
    totalXp = 2500,
    currentHp = 97,
    baseHealth = 120,
    basePower = 15,
    baseArmor = 5,
    baseLuck = 0.5f,
    pushUpsToday = 297,
    teeth = 840,
    currentStreak = 3,
    isFirstLaunch = false,
    language = "en",
    monsterLevel = 3,
    monsterCurrentHp = 55,
    monsterMaxHp = 61,
    monsterName = "Gopnik",
    monsterDamage = 8,
    isPlayerDead = false,
    unspentStatPoints = 0
)

class FakeGameRepository : IGameRepository {

    private val state = MutableStateFlow<GameStateEntity?>(mockGameState())
    private val logs = MutableStateFlow<List<LogEntryEntity>>(
        listOf(
            LogEntryEntity(message = "⚔️ Auto-attack: -6 to Gopnik", messageRu = "⚔️ Авто-атака: -6 Гопник", timestamp = System.currentTimeMillis() - 120000),
            LogEntryEntity(message = "🐾 Gopnik attacks: -9 HP", messageRu = "🐾 Гопник атакует: -9 HP", timestamp = System.currentTimeMillis() - 60000),
            LogEntryEntity(message = "⬆️ Level Up! Now level 5!", messageRu = "⬆️ Повышение уровня! Уровень 5!", timestamp = System.currentTimeMillis() - 30000),
        )
    )

    override fun getGameStateFlow(): Flow<GameStateEntity?> = state
    override fun getRecentLogsFlow(): Flow<List<LogEntryEntity>> = logs
    override fun getAllLogsFlow(): Flow<List<LogEntryEntity>> = logs
    override val battleChain: SharedFlow<BattleChain> = MutableSharedFlow()

    override suspend fun getGameState(): GameStateEntity = state.value ?: mockGameState()
    override suspend fun saveGameState(state: GameStateEntity) { this.state.value = state }

    override suspend fun addPushUps(count: Int): Int = 0
    override suspend fun processBattleTick() {}
    override suspend fun updateStreakOnLogin() {}

    override suspend fun getStatsForPeriod(): PeriodStats =
        PeriodStats(lastWeek = 1240, lastMonth = 4800, lastQuarter = 14000, lastYear = 52000, total = 87000)

    override suspend fun getLast7DaysStats(): List<DayStats> =
        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            .mapIndexed { i, d -> DayStats(date = d, count = (50..300).random()) }

    override suspend fun getLast12MonthsStats(): List<DayStats> =
        (1..12).map { m -> DayStats(date = "2025-${m.toString().padStart(2,'0')}", count = (800..3000).random()) }

    override suspend fun addLog(message: String, messageRu: String) {}

    override suspend fun equipItem(itemId: String, slot: String) {}
    override suspend fun unequipItem(slot: String) {}
    override suspend fun sellItem(itemId: String) {}
    override suspend fun resetAllProgress() {}
    override suspend fun spendStatPoint(stat: String) {}
    override suspend fun useFreePoints(): Boolean = true

    override suspend fun checkAndUpdateEvent(): GameStateEntity = getGameState()

    override suspend fun getOrRefreshShop(): List<Item> = emptyList()
    override suspend fun buyShopItem(itemId: String): Boolean = true
    override suspend fun addTeeth(amount: Int) {}
    override suspend fun rerollShop(): Boolean = true

    override suspend fun setForgeSlot(slot: Int, itemId: String) {}
    override suspend fun recycleToForgeSlots() {}
    override suspend fun mergeItems(): ForgeResult = ForgeResult.Fail

    override suspend fun checkAndResetDaily() {}
    override suspend fun checkAndGrantHourlySpins(): Int = 0

    override suspend fun performDailySpin(): SpinResult? = null
    override suspend fun addSpinFromAd(): Boolean = true
    override suspend fun getAvailableSpins(): Int = 3

    override suspend fun useCloverBox(): Item? = null

    override fun calculateEnchantChance(luck: Float, streak: Int, achBonus: Float, isNight: Boolean): Float = 35f
    override fun calculateEnchantCost(rarity: String, currentEnchantLevel: Int, isNight: Boolean): Int = 5
    override suspend fun enchantItem(itemId: String): EnchantResult = EnchantResult.SUCCESS

    override suspend fun setActiveAchievements(ids: List<String>) {}
    override suspend fun checkAndRefreshQuests() {}
    override suspend fun claimQuestReward(defId: String): Boolean = true
    override suspend fun adRerollDailyQuests(): Boolean = true

    override suspend fun claimDailyReward(): DailyRewardUtils.DailyReward? = null
    override suspend fun updateRateUsState(action: RateUsAction) {}
    override fun setPlayGamesManager(manager: PlayGamesManager) {}
    override suspend fun performPunch(): Int = 142
    override suspend fun performGoblinPunch(): Int = 0
    override suspend fun endGoldenGoblin(): Int = 0
    override suspend fun addDebugItemsForTest() {}
}
