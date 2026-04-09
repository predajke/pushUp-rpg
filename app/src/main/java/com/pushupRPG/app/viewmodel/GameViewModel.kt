package com.pushupRPG.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushupRPG.app.data.db.GameStateEntity
import com.pushupRPG.app.data.exception.CheatCooldownException
import com.pushupRPG.app.data.model.EnchantResult
import com.pushupRPG.app.data.model.ForgeResult
import com.pushupRPG.app.data.model.Item
import com.pushupRPG.app.data.model.PeriodStats
import com.pushupRPG.app.data.repository.GameRepository
import com.pushupRPG.app.managers.OnboardingManager
import com.pushupRPG.app.managers.AdType
import com.pushupRPG.app.utils.ItemUtils
import com.pushupRPG.app.utils.EventUtils
import com.pushupRPG.app.utils.DailyRewardUtils
import com.pushupRPG.app.utils.QuestSystem
import com.pushupRPG.app.utils.ActiveQuest
import com.pushupRPG.app.utils.AchievementSystem
import com.pushupRPG.app.utils.UnlockedAchievement
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    val gameState = repository.getGameStateFlow()
    val recentLogs = repository.getRecentLogsFlow()
    val allLogs = repository.getAllLogsFlow()

    // ==================== ОНБОРДИНГ ====================
    private val onboardingManager = OnboardingManager()
    val onboardingStep: StateFlow<Int> = onboardingManager.currentStep
    val isOnboardingComplete: StateFlow<Boolean> = onboardingManager.isOnboardingComplete

    fun initializeOnboarding(gameState: GameStateEntity?) {
        if (gameState != null && gameState.isFirstLaunch) {
            onboardingManager.startOnboarding()
        }
    }

    fun nextOnboardingStep() {
        onboardingManager.nextStep()
    }

    fun completeOnboarding(gameState: GameStateEntity?) {
        onboardingManager.completeOnboarding()
        // Save that onboarding is complete
        if (gameState != null) {
            viewModelScope.launch {
                repository.saveGameState(gameState.copy(isFirstLaunch = false))
            }
        }
    }

    fun skipOnboarding(gameState: GameStateEntity?) {
        onboardingManager.skipOnboarding()
        if (gameState != null) {
            viewModelScope.launch {
                repository.saveGameState(gameState.copy(isFirstLaunch = false))
            }
        }
    }

    fun getOnboardingManager(): OnboardingManager = onboardingManager

    // ==================== ПЕРЕМЕННЫЕ ДЛЯ ГЛАВНОГО МЕНЮ ====================
    private val _inputValue = MutableStateFlow(0)
    val inputValue: StateFlow<Int> = _inputValue.asStateFlow()

    private val _showLevelUpDialog = MutableStateFlow(false)
    val showLevelUpDialog: StateFlow<Boolean> = _showLevelUpDialog.asStateFlow()

    private val _newLevel = MutableStateFlow(0)
    val newLevel: StateFlow<Int> = _newLevel.asStateFlow()

    // Anti-cheat
    data class AntiCheatCooldown(
        val remainingMs: Long,
        val adType: AdType,
        val attemptNumber: Int
    )

    private val _antiCheatCooldown = MutableStateFlow<AntiCheatCooldown?>(null)
    val antiCheatCooldown: StateFlow<AntiCheatCooldown?> = _antiCheatCooldown.asStateFlow()

    // Rate Us
    private val _showRateUsDialog = MutableStateFlow(false)
    val showRateUsDialog: StateFlow<Boolean> = _showRateUsDialog.asStateFlow()

    val totalStats: StateFlow<com.pushupRPG.app.utils.TotalStats?> = gameState.filterNotNull().map { state ->
        val slots = listOf(
            state.equippedHead, state.equippedNecklace, state.equippedWeapon1,
            state.equippedWeapon2, state.equippedPants, state.equippedBoots
        ).filter { it.isNotEmpty() }
        val items = slots.mapNotNull { ItemUtils.getItemById(it.split(":")[0]) }
        val levels = slots.map { it.split(":").getOrNull(1)?.toIntOrNull() ?: 0 }
        com.pushupRPG.app.utils.GameCalculations.calculateTotalStats(state, items, levels)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _activeEvent = MutableStateFlow<com.pushupRPG.app.data.model.GameEvent?>(null)
    val activeEvent: StateFlow<com.pushupRPG.app.data.model.GameEvent?> = _activeEvent.asStateFlow()

    // ==================== ЕЖЕДНЕВНАЯ НАГРАДА ====================
    private val _showDailyReward = MutableStateFlow(false)
    val showDailyReward: StateFlow<Boolean> = _showDailyReward.asStateFlow()

    private val _pendingDailyReward = MutableStateFlow<DailyRewardUtils.DailyReward?>(null)
    val pendingDailyReward: StateFlow<DailyRewardUtils.DailyReward?> = _pendingDailyReward.asStateFlow()

    fun dismissDailyReward() { _showDailyReward.value = false }

    fun claimDailyReward() {
        viewModelScope.launch {
            val reward = repository.claimDailyReward()
            if (reward != null) {
                _pendingDailyReward.value = reward
                _showDailyReward.value = true
            }
        }
    }

    // ==================== КВЕСТЫ ====================
    fun getActiveQuests(state: GameStateEntity): List<ActiveQuest> =
        QuestSystem.deserialize(state.activeQuestsJson)

    fun claimQuestReward(defId: String, callback: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.claimQuestReward(defId)
            callback(success)
        }
    }

    fun checkAndRefreshQuests() {
        viewModelScope.launch { repository.checkAndRefreshQuests() }
    }

    init {
        _isLoading.value = true
        viewModelScope.launch {
            // Убедимся что начальное состояние создано
            repository.getGameState()

            gameState.filterNotNull().collect { state ->
                _activeEvent.value = if (EventUtils.isEventActive(state.eventEndTime)) {
                    EventUtils.getEventById(state.activeEventId)
                } else null
                _isLoading.value = false
            }
        }
    }

    fun addToInput(amount: Int) {
        val current = _inputValue.value
        _inputValue.value = (current + amount).coerceIn(0, 99)
    }

    fun resetInput() {
        _inputValue.value = 0
    }

    fun savePushUps() {
        val count = _inputValue.value
        if (count > 0) {
            _inputValue.value = 0
            viewModelScope.launch {
                try {
                    val leveledUpTo = repository.addPushUps(count)
                    if (leveledUpTo > 0) {
                        _newLevel.value = leveledUpTo
                        _showLevelUpDialog.value = true
                    }
                } catch (e: CheatCooldownException) {
                    _antiCheatCooldown.value = AntiCheatCooldown(
                        remainingMs = e.remainingMs,
                        adType = e.adType,
                        attemptNumber = e.attemptNumber
                    )
                }
            }
        }
    }

    fun clearAntiCheatCooldown() {
        _antiCheatCooldown.value = null
    }

    fun dismissLevelUpDialog() {
        _showLevelUpDialog.value = false
    }

    // Rate Us
    fun checkAndShowRateUs(gameState: GameStateEntity?) {
        if (gameState == null) return

        val rateUsManager = com.pushupRPG.app.managers.RateUsManager()
        val shouldShow = rateUsManager.shouldShowRateUsDialog(
            installDate = gameState.installDate,
            rateUsLastShowDate = gameState.rateUsLastShowDate,
            rateUsDoNotShowAgain = gameState.rateUsDoNotShowAgain
        )
        _showRateUsDialog.value = shouldShow
    }

    fun rateUsAction(action: com.pushupRPG.app.data.repository.RateUsAction) {
        viewModelScope.launch {
            repository.updateRateUsState(action)
            _showRateUsDialog.value = false
        }
    }

    fun dismissRateUsDialog() {
        _showRateUsDialog.value = false
    }

    fun triggerRealtimeTick() {
        viewModelScope.launch {
            repository.checkAndUpdateEvent()
            repository.processBattleTick()
            repository.checkAndRefreshQuests()
        }
    }

    // ==================== ПЕРЕМЕННЫЕ ДЛЯ ИНВЕНТАРЯ ====================
    private val _selectedInventoryItem = MutableStateFlow<Item?>(null)
    val selectedInventoryItem: StateFlow<Item?> = _selectedInventoryItem.asStateFlow()

    fun selectInventoryItem(item: Item?) {
        _selectedInventoryItem.value = item
    }

    fun getInventoryItems(state: GameStateEntity): List<Item> {
        return state.inventoryItems.split(",")
            .filter { it.isNotEmpty() }
            .mapNotNull { entry ->
                val uniqueId = entry.split(":")[0]  // "boots_002_1717499234567"
                ItemUtils.getItemById(entry)?.copy(id = uniqueId)
            }
    }

    fun getEquippedItems(state: GameStateEntity): List<Item> {
        val slots = listOf(
            state.equippedHead, state.equippedNecklace, state.equippedWeapon1,
            state.equippedWeapon2, state.equippedPants, state.equippedBoots
        ).filter { it.isNotEmpty() }

        return slots.mapNotNull { entry ->
            ItemUtils.getItemById(getBaseId(entry))
        }
    }

    fun getEnchantLevel(state: GameStateEntity, itemId: String): Int {
        // Сначала ищем в инвентаре
        val invEntry = state.inventoryItems.split(",").find { it.split(":")[0] == itemId }
        if (invEntry != null) return invEntry.split(":").getOrNull(1)?.toIntOrNull() ?: 0
        // Затем ищем в equipped slots (вещь могла быть экипирована)
        val equippedEntry = listOf(
            state.equippedHead, state.equippedNecklace, state.equippedWeapon1,
            state.equippedWeapon2, state.equippedPants, state.equippedBoots
        ).find { it.split(":")[0] == itemId }
        return equippedEntry?.split(":")?.getOrNull(1)?.toIntOrNull() ?: 0
    }

    private fun getBaseId(entry: String): String {
        val idPart = entry.split(":")[0]
        val parts = idPart.split("_")
        return if (parts.size > 2 && parts.last().all { it.isDigit() } && parts.last().length > 8) {
            parts.dropLast(1).joinToString("_")
        } else {
            idPart
        }
    }

    // ==================== ПЕРЕМЕННЫЕ ДЛЯ МАГАЗИНА (SHOP) ====================
    private val _shopItems = MutableStateFlow<List<Item>>(emptyList())
    val shopItems: StateFlow<List<Item>> = _shopItems.asStateFlow()

    private val _selectedEnchantItem = MutableStateFlow<Item?>(null)
    val selectedEnchantItem: StateFlow<Item?> = _selectedEnchantItem.asStateFlow()

    fun loadShop() {
        viewModelScope.launch {
            val state = repository.getGameState()
            val itemIds = state.shopItems.split(",").filter { it.isNotEmpty() }
            _shopItems.value = itemIds.mapNotNull { ItemUtils.getItemById(it) }
        }
    }

    fun buyShopItem(itemId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repository.buyShopItem(itemId)
                loadShop() // Обновляем магазин после покупки
                callback(true)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }

    fun rerollShop() {
        viewModelScope.launch {
            repository.rerollShop()
            loadShop()
        }
    }

    fun setForgeSlot(slotNumber: Int, itemId: String) {
        viewModelScope.launch {
            repository.setForgeSlot(slotNumber, itemId)
        }
    }

    fun mergeItems(callback: (ForgeResult) -> Unit) {
        viewModelScope.launch {
            val result = repository.mergeItems()
            callback(result)
        }
    }

    fun selectEnchantItem(item: Item?) {
        _selectedEnchantItem.value = item
    }

    fun enchantItemWithCallback(itemId: String, callback: (EnchantResult) -> Unit) {
        viewModelScope.launch {
            val result = repository.enchantItem(itemId)
            callback(result)
        }
    }

    // ==================== ПЕРЕМЕННЫЕ ДЛЯ СТАТИСТИКИ (STATISTICS) ====================
    private val _periodStats = MutableStateFlow<PeriodStats?>(null)
    val periodStats: StateFlow<PeriodStats?> = _periodStats.asStateFlow()

    private val _weekStats = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val weekStats: StateFlow<List<Pair<String, Int>>> = _weekStats.asStateFlow()

    fun loadPeriodStats() {
        viewModelScope.launch {
            _periodStats.value = repository.getStatsForPeriod()
            _weekStats.value = repository.getLast7DaysStats().map { it.date to it.count }
        }
    }

    // ==================== ОСНОВНЫЕ ИГРОВЫЕ ДЕЙСТВИЯ ====================
    fun updateStreakOnLogin() {
        viewModelScope.launch { repository.updateStreakOnLogin() }
    }

    fun equipItem(itemId: String, slot: String) {
        viewModelScope.launch { repository.equipItem(itemId, slot) }
    }

    fun unequipItem(slot: String) {
        viewModelScope.launch { repository.unequipItem(slot) }
    }

    fun sellItem(itemId: String) {
        viewModelScope.launch {
            repository.sellItem(itemId)
            _selectedInventoryItem.value = null
        }
    }

    fun spendStatPoint(stat: String) {
        viewModelScope.launch { repository.spendStatPoint(stat) }
    }

    fun buyShopItem(itemId: String) {
        viewModelScope.launch { repository.buyShopItem(itemId) }
    }

    fun enchantItem(itemId: String) {
        viewModelScope.launch { repository.enchantItem(itemId) }
    }

    fun resetAllProgress() {
        viewModelScope.launch { repository.resetAllProgress() }
    }

    // ==================== ДЛЯ НАСТРОЕК (SETTINGS) ====================
    fun updatePlayerName(newName: String) {
        viewModelScope.launch {
            val state = repository.getGameState()
            repository.saveGameState(state.copy(playerName = newName))
        }
    }

    fun updateHeroAvatar(avatar: String) {
        viewModelScope.launch {
            val state = repository.getGameState()
            repository.saveGameState(state.copy(heroAvatar = avatar))
        }
    }

    fun updateLanguage(lang: String) {
        viewModelScope.launch {
            val state = repository.getGameState()
            repository.saveGameState(state.copy(language = lang))
        }
    }

    fun resetProgress(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.resetAllProgress()
            onComplete()
        }
    }

    // ==================== ДОПОЛНИТЕЛЬНЫЕ ФУНКЦИИ ДЛЯ МАГАЗИНА ====================
    fun useCloverBox(callback: (Item?) -> Unit) {
        viewModelScope.launch {
            val result = repository.useCloverBox()
            callback(result)
        }
    }

    fun useFreePoints(callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.useFreePoints()
            callback(success)
        }
    }

    // ==================== DEBUG / TEST HELPERS ====================
    /** Добавляет тестовые вещи и зубы. Использовать только для тестирования! */
    fun addDebugItems(callback: () -> Unit = {}) {
        viewModelScope.launch {
            repository.addDebugItemsForTest()
            callback()
        }
    }

    fun getEnchantInfo(state: GameStateEntity, item: Item): Pair<Float, Int> {
        val enchantLevel = getEnchantLevel(state, item.id)
        val chance = repository.calculateEnchantChance(state.baseLuck, state.currentStreak)
        val cost = repository.calculateEnchantCost(item.rarity, enchantLevel)
        return Pair(chance, cost)
    }

    // ==================== ДОСТИЖЕНИЯ ====================

    fun getUnlockedAchievements(state: GameStateEntity): List<UnlockedAchievement> =
        AchievementSystem.deserialize(state.achievementsJson)

    fun getActiveAchievementIds(state: GameStateEntity): List<String> =
        state.activeAchievementIds.split(",").filter { it.isNotEmpty() }

    fun setActiveAchievements(ids: List<String>) {
        viewModelScope.launch { repository.setActiveAchievements(ids) }
    }

    fun getBestiary(state: GameStateEntity): Map<String, Int> {
        if (state.bestiaryJson.isBlank()) return emptyMap()
        return try {
            Gson().fromJson(state.bestiaryJson, object : TypeToken<Map<String, Int>>() {}.type) ?: emptyMap()
        } catch (e: Exception) { emptyMap() }
    }

    fun getItemLog(state: GameStateEntity): List<String> {
        if (state.itemLogJson.isBlank()) return emptyList()
        return try {
            Gson().fromJson(state.itemLogJson, object : TypeToken<List<String>>() {}.type) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }
}
