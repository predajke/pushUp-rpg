package com.ninthbalcony.pushuprpg.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninthbalcony.pushuprpg.data.db.GameStateEntity
import com.ninthbalcony.pushuprpg.data.exception.CheatCooldownException
import com.ninthbalcony.pushuprpg.data.model.EnchantResult
import com.ninthbalcony.pushuprpg.data.model.ForgeResult
import com.ninthbalcony.pushuprpg.data.model.Item
import com.ninthbalcony.pushuprpg.data.model.PeriodStats
import com.ninthbalcony.pushuprpg.data.repository.IGameRepository
import com.ninthbalcony.pushuprpg.managers.OnboardingManager
import com.ninthbalcony.pushuprpg.managers.AdType
import com.ninthbalcony.pushuprpg.utils.ItemUtils
import com.ninthbalcony.pushuprpg.utils.EventUtils
import com.ninthbalcony.pushuprpg.utils.DailyRewardUtils
import com.ninthbalcony.pushuprpg.utils.QuestSystem
import com.ninthbalcony.pushuprpg.utils.ActiveQuest
import com.ninthbalcony.pushuprpg.utils.AchievementSystem
import com.ninthbalcony.pushuprpg.utils.UnlockedAchievement
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

class GameViewModel(private val repository: IGameRepository) : ViewModel() {

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
            viewModelScope.launch {
                repository.saveGameState(gameState.copy(isFirstLaunch = false))
            }
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

    // ==================== REWARDED ADS ====================
    private var adManager: com.ninthbalcony.pushuprpg.managers.AdManager? = null

    fun setAdManager(manager: com.ninthbalcony.pushuprpg.managers.AdManager) {
        adManager = manager
    }

    fun setPlayGamesManager(manager: com.ninthbalcony.pushuprpg.managers.PlayGamesManager) {
        viewModelScope.launch {
            repository.setPlayGamesManager(manager)
        }
    }

    private val _adRewardPending = MutableStateFlow(0)
    val adRewardPending: StateFlow<Int> = _adRewardPending.asStateFlow()

    fun requestAdReward(teeth: Int) { _adRewardPending.value = teeth }

    fun playRewardedAd(activity: android.app.Activity) {
        adManager?.showRewardedAd(
            activity,
            onRewardEarned = {
                val amount = _adRewardPending.value
                _adRewardPending.value = 0
                viewModelScope.launch { repository.addTeeth(amount) }
            },
            onAdDismissed = { _adRewardPending.value = 0 }
        )
    }

    fun dismissAdReward() { _adRewardPending.value = 0 }

    private val _adQuestRerollPending = MutableStateFlow(false)
    val adQuestRerollPending: StateFlow<Boolean> = _adQuestRerollPending.asStateFlow()

    fun requestAdQuestReroll() { _adQuestRerollPending.value = true }

    fun playAdQuestReroll(activity: android.app.Activity) {
        adManager?.showRewardedAd(
            activity,
            onRewardEarned = {
                _adQuestRerollPending.value = false
                viewModelScope.launch { repository.adRerollDailyQuests() }
            },
            onAdDismissed = { _adQuestRerollPending.value = false }
        )
    }

    fun dismissAdQuestReroll() { _adQuestRerollPending.value = false }

    val totalStats: StateFlow<com.ninthbalcony.pushuprpg.utils.TotalStats?> = gameState.filterNotNull().map { state ->
        val slots = listOf(
            state.equippedHead, state.equippedNecklace, state.equippedWeapon1,
            state.equippedWeapon2, state.equippedPants, state.equippedBoots
        ).filter { it.isNotEmpty() }
        val items = slots.mapNotNull { ItemUtils.getItemById(it.split(":")[0]) }
        val levels = slots.map { it.split(":").getOrNull(1)?.toIntOrNull() ?: 0 }
        val achBonuses = com.ninthbalcony.pushuprpg.utils.AchievementSystem.getActiveBonuses(state.activeAchievementIds)
        val setBonuses = ItemUtils.getSetBonuses(items)
        com.ninthbalcony.pushuprpg.utils.GameCalculations.calculateTotalStats(state, items, levels, achBonuses, setBonuses)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _activeEvent = MutableStateFlow<com.ninthbalcony.pushuprpg.data.model.GameEvent?>(null)
    val activeEvent: StateFlow<com.ninthbalcony.pushuprpg.data.model.GameEvent?> = _activeEvent.asStateFlow()

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
            triggerRealtimeTick()
            callback(success)
        }
    }

    fun checkAndRefreshQuests() {
        viewModelScope.launch { repository.checkAndRefreshQuests() }
    }

    // ==================== ACHIEVEMENT TOAST ====================
    private val _achievementToast = MutableStateFlow<com.ninthbalcony.pushuprpg.utils.AchievementDef?>(null)
    val achievementToast: StateFlow<com.ninthbalcony.pushuprpg.utils.AchievementDef?> = _achievementToast.asStateFlow()

    fun clearAchievementToast() { _achievementToast.value = null }

    // ==================== BATTLE ANIMATION (Save → серия ударов) ====================
    /**
     * Текущий "кадр" анимации серии ударов от Save отжиманий.
     * UI читает это вместо real state'а пока анимация идёт, чтобы HP-bar монстра
     * и damage-number плавно обновлялись по 1 хиту.
     */
    private val _battleAnimation = MutableStateFlow<com.ninthbalcony.pushuprpg.data.model.BattleHit?>(null)
    val battleAnimation: StateFlow<com.ninthbalcony.pushuprpg.data.model.BattleHit?> = _battleAnimation.asStateFlow()

    private var battleAnimationJob: kotlinx.coroutines.Job? = null

    /** Длительность одного хита в анимации, мс. */
    private val battleHitDelayMs = 80L

    init {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getGameState()
            gameState.filterNotNull().collect { state ->
                _activeEvent.value = if (EventUtils.isEventActive(state.eventEndTime)) {
                    EventUtils.getEventById(state.activeEventId)
                } else null
                _isLoading.value = false
            }
        }
        // Подписка на серии ударов от Save отжиманий → проигрываем анимацию.
        viewModelScope.launch {
            repository.battleChain.collect { chain ->
                battleAnimationJob?.cancel()
                battleAnimationJob = viewModelScope.launch {
                    for (hit in chain.hits) {
                        _battleAnimation.value = hit
                        kotlinx.coroutines.delay(battleHitDelayMs)
                    }
                    _battleAnimation.value = null
                }
            }
        }

        // Следим за новыми достижениями и показываем тост
        viewModelScope.launch {
            var known = emptySet<String>()
            gameState.filterNotNull().collect { state ->
                val current = AchievementSystem.getUnlocked(state.achievementsJson)
                    .map { it.defId }.toSet()
                if (known.isNotEmpty() && current.size > known.size) {
                    val newId = (current - known).firstOrNull()
                    val def = AchievementSystem.ALL.find { it.id == newId }
                    if (def != null) _achievementToast.value = def
                }
                known = current
            }
        }

        // Запускаем таймер гоблина при старте события, останавливаем при завершении
        viewModelScope.launch {
            var wasGoblinActive = false
            gameState.filterNotNull().collect { state ->
                if (state.isGoldenGoblinActive && !wasGoblinActive) {
                    wasGoblinActive = true
                    startGoblinTimer(state.goldenGoblinEndTime)
                } else if (!state.isGoldenGoblinActive && wasGoblinActive) {
                    wasGoblinActive = false
                    goblinTimerJob?.cancel()
                    _goblinTimeRemaining.value = 0L
                }
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

        val rateUsManager = com.ninthbalcony.pushuprpg.managers.RateUsManager()
        val shouldShow = rateUsManager.shouldShowRateUsDialog(
            installDate = gameState.installDate,
            rateUsLastShowDate = gameState.rateUsLastShowDate,
            rateUsDoNotShowAgain = gameState.rateUsDoNotShowAgain
        )
        _showRateUsDialog.value = shouldShow
    }

    fun rateUsAction(action: com.ninthbalcony.pushuprpg.data.repository.RateUsAction) {
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
            _shopItems.value = repository.getOrRefreshShop()
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
            triggerRealtimeTick()
            callback(result)
        }
    }

    fun recycleForgeSlots() {
        viewModelScope.launch {
            repository.recycleToForgeSlots()
        }
    }

    fun selectEnchantItem(item: Item?) {
        _selectedEnchantItem.value = item
    }

    fun enchantItemWithCallback(itemId: String, callback: (EnchantResult) -> Unit) {
        viewModelScope.launch {
            val result = repository.enchantItem(itemId)
            triggerRealtimeTick()
            callback(result)
        }
    }

    // ==================== DAILY SPIN ====================
    private val _availableSpins = MutableStateFlow(0)
    val availableSpins: StateFlow<Int> = _availableSpins.asStateFlow()

    private val _spinResult = MutableStateFlow<com.ninthbalcony.pushuprpg.utils.SpinResult?>(null)
    val spinResult: StateFlow<com.ninthbalcony.pushuprpg.utils.SpinResult?> = _spinResult.asStateFlow()

    private val _isSpinning = MutableStateFlow(false)
    val isSpinning: StateFlow<Boolean> = _isSpinning.asStateFlow()

    private val _adViewsToday = MutableStateFlow(0)
    val adViewsToday: StateFlow<Int> = _adViewsToday.asStateFlow()

    /** Тратит 1 токен, запускает анимацию спина */
    fun performDailySpin() {
        viewModelScope.launch {
            _isSpinning.value = true
            _spinResult.value = repository.performDailySpin()
            _availableSpins.value = repository.getAvailableSpins()
            _adViewsToday.value = repository.getGameState().dailySpinAdViewsToday
            triggerRealtimeTick()
            _isSpinning.value = false
        }
    }

    /** Смотрит рекламу → добавляет 1 токен (без запуска анимации) */
    fun watchAdForSpin() {
        viewModelScope.launch {
            repository.addSpinFromAd()
            _availableSpins.value = repository.getAvailableSpins()
            _adViewsToday.value = repository.getGameState().dailySpinAdViewsToday
        }
    }

    fun refreshSpinCounters() {
        viewModelScope.launch {
            repository.checkAndResetDaily()
            repository.checkAndGrantHourlySpins()
            _availableSpins.value = repository.getAvailableSpins()
            _adViewsToday.value = repository.getGameState().dailySpinAdViewsToday
        }
    }

    fun clearSpinResult() {
        _spinResult.value = null
    }

    // ==================== ПЕРЕМЕННЫЕ ДЛЯ СТАТИСТИКИ (STATISTICS) ====================
    private val _periodStats = MutableStateFlow<PeriodStats?>(null)
    val periodStats: StateFlow<PeriodStats?> = _periodStats.asStateFlow()

    private val _weekStats = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val weekStats: StateFlow<List<Pair<String, Int>>> = _weekStats.asStateFlow()

    private val _yearStats = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val yearStats: StateFlow<List<Pair<String, Int>>> = _yearStats.asStateFlow()

    fun loadPeriodStats() {
        viewModelScope.launch {
            _periodStats.value = repository.getStatsForPeriod()

            val rawWeek = repository.getLast7DaysStats().associate { it.date to it.count }
            val today = java.time.LocalDate.now()
            _weekStats.value = (6 downTo 0).map { i ->
                val d = today.minusDays(i.toLong())
                val label = d.format(java.time.format.DateTimeFormatter.ofPattern("EEE", java.util.Locale.ENGLISH)).take(3)
                label to (rawWeek[d.toString()] ?: 0)
            }

            val rawYear = repository.getLast12MonthsStats().associate { it.date to it.count }
            _yearStats.value = (11 downTo 0).map { i ->
                val m = today.minusMonths(i.toLong())
                val key = m.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"))
                val label = m.format(java.time.format.DateTimeFormatter.ofPattern("MMM", java.util.Locale.ENGLISH))
                label to (rawYear[key] ?: 0)
            }
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

    fun sellItems(itemIds: List<String>) {
        viewModelScope.launch { itemIds.forEach { repository.sellItem(it) } }
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

    fun updatePlayerGender(gender: String) {
        viewModelScope.launch {
            val state = repository.getGameState()
            repository.saveGameState(state.copy(playerGender = gender))
        }
    }

    fun updateLanguage(lang: String) {
        viewModelScope.launch {
            val state = repository.getGameState()
            repository.saveGameState(state.copy(language = lang))
        }
    }

    fun updateBodyWeight(weightKg: Float) {
        viewModelScope.launch {
            val state = repository.getGameState()
            repository.saveGameState(state.copy(bodyWeightKg = weightKg))
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

    // ==================== GOLDEN GOBLIN ====================
    private val _goblinTimeRemaining = MutableStateFlow(0L)
    val goblinTimeRemaining: StateFlow<Long> = _goblinTimeRemaining.asStateFlow()

    private val _goblinEndTeeth = MutableStateFlow<Int?>(null)
    val goblinEndTeeth: StateFlow<Int?> = _goblinEndTeeth.asStateFlow()

    fun performGoblinPunch() {
        viewModelScope.launch { repository.performGoblinPunch() }
    }

    fun clearGoblinEndTeeth() { _goblinEndTeeth.value = null }

    private var goblinTimerJob: kotlinx.coroutines.Job? = null

    private fun startGoblinTimer(endTime: Long) {
        goblinTimerJob?.cancel()
        goblinTimerJob = viewModelScope.launch {
            while (true) {
                val remaining = endTime - System.currentTimeMillis()
                if (remaining <= 0L) {
                    _goblinTimeRemaining.value = 0L
                    val teeth = repository.endGoldenGoblin()
                    if (teeth > 0) _goblinEndTeeth.value = teeth
                    break
                }
                _goblinTimeRemaining.value = remaining
                kotlinx.coroutines.delay(100L)
            }
        }
    }

    // ==================== PUNCH ====================
    private val _punchCooldownUntil = MutableStateFlow(0L)
    val punchCooldownUntil: StateFlow<Long> = _punchCooldownUntil.asStateFlow()

    private val _lastPunchDamage = MutableStateFlow<Int?>(null)
    val lastPunchDamage: StateFlow<Int?> = _lastPunchDamage.asStateFlow()

    fun performPunch() {
        if (System.currentTimeMillis() < _punchCooldownUntil.value) return
        viewModelScope.launch {
            val result = repository.performPunch()
            when {
                result > 0 -> {
                    _punchCooldownUntil.value = System.currentTimeMillis() + 3000L
                    _lastPunchDamage.value = result
                    kotlinx.coroutines.delay(900L)
                    _lastPunchDamage.value = null
                    triggerRealtimeTick()
                }
            }
        }
    }

    fun getPunchesRemaining(state: GameStateEntity): Int {
        val today = com.ninthbalcony.pushuprpg.utils.DateUtils.getTodayString()
        val used = if (state.lastPunchDate == today) state.punchesUsedToday else 0
        return (25 - used).coerceAtLeast(0)
    }

    // ==================== DEV CONSOLE ====================
    private val _cheatFeedback = MutableStateFlow("")
    val cheatFeedback: StateFlow<String> = _cheatFeedback.asStateFlow()

    fun executeCheat(command: String) {
        val parts = command.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (parts.isEmpty()) return

        viewModelScope.launch {
            val state = repository.getGameState()
            val feedback = when {
                parts[0] == "give" && parts.getOrNull(1) == "lvl" -> {
                    val lvl = parts.getOrNull(2)?.toIntOrNull()?.coerceIn(1, 49)
                        ?: return@launch run { _cheatFeedback.value = "Usage: give lvl <1-49>" }
                    val xp = com.ninthbalcony.pushuprpg.utils.GameCalculations.getXpThresholdForLevel(lvl)
                    val points = (lvl - 1) * com.ninthbalcony.pushuprpg.utils.GameCalculations.STAT_POINTS_PER_LEVEL
                    val monster = com.ninthbalcony.pushuprpg.utils.MonsterUtils.rollNextMonster(lvl)
                    val maxHp = com.ninthbalcony.pushuprpg.utils.GameCalculations.getMaxHp(lvl, state.baseHealth, 0)
                    repository.saveGameState(state.copy(
                        totalXp = xp,
                        playerLevel = lvl,
                        unspentStatPoints = points,
                        currentHp = maxHp,
                        isPlayerDead = false,
                        monsterName = monster.name,
                        monsterLevel = monster.level,
                        monsterImageRes = monster.imageRes,
                        monsterMaxHp = monster.maxHp,
                        monsterCurrentHp = monster.maxHp,
                        monsterDamage = monster.damage,
                        isCurrentBoss = false,
                        currentBossId = 0
                    ))
                    "✅ Level $lvl set. $points stat points granted"
                }

                parts[0] == "give" && parts.getOrNull(1) == "teeth" -> {
                    val amount = parts.getOrNull(2)?.toIntOrNull()
                        ?: return@launch run { _cheatFeedback.value = "Usage: give teeth <amount>" }
                    repository.saveGameState(state.copy(teeth = state.teeth + amount))
                    "✅ +$amount teeth (total: ${state.teeth + amount})"
                }

                parts[0] == "give" && parts.getOrNull(1) == "item" -> {
                    val itemId = parts.getOrNull(2)
                        ?: return@launch run { _cheatFeedback.value = "Usage: give item <item_id>" }
                    val item = ItemUtils.getItemById(itemId)
                        ?: return@launch run { _cheatFeedback.value = "❌ Item '$itemId' not found" }
                    val uniqueId = "${itemId}_${System.currentTimeMillis()}"
                    val inv = state.inventoryItems.split(",").filter { it.isNotEmpty() }.toMutableList()
                    inv.add("$uniqueId:0")
                    repository.saveGameState(state.copy(
                        inventoryItems = inv.joinToString(","),
                        itemsCollected = state.itemsCollected + 1
                    ))
                    "✅ Added ${item.name_en} to inventory"
                }

                parts[0] == "give" && parts.getOrNull(1) == "items" -> {
                    repository.addDebugItemsForTest()
                    "✅ Debug items + 100k teeth added"
                }

                parts[0] == "give" && parts.getOrNull(1) == "spins" -> {
                    val amount = parts.getOrNull(2)?.toIntOrNull()
                        ?: return@launch run { _cheatFeedback.value = "Usage: give spins <amount>" }
                    repository.saveGameState(state.copy(spinTokens = state.spinTokens + amount))
                    "✅ +$amount spins (total: ${state.spinTokens + amount})"
                }

                parts[0] == "give" && parts.getOrNull(1) == "hp" -> {
                    val maxHp = com.ninthbalcony.pushuprpg.utils.GameCalculations.getMaxHp(
                        state.playerLevel, state.baseHealth, 0
                    )
                    repository.saveGameState(state.copy(currentHp = maxHp, isPlayerDead = false))
                    "✅ HP restored to $maxHp"
                }

                else -> "❌ Unknown command. Tap [?] for help"
            }
            _cheatFeedback.value = feedback
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
        val isNight = state.activeEventId in NIGHT_ENCHANT_EVENT_IDS &&
            com.ninthbalcony.pushuprpg.utils.EventUtils.isEventActive(state.eventEndTime)
        val chance = repository.calculateEnchantChance(state.baseLuck, state.currentStreak, isNight = isNight)
        val cost = repository.calculateEnchantCost(item.rarity, enchantLevel, isNight)
        return Pair(chance, cost)
    }

    companion object {
        val NIGHT_ENCHANT_EVENT_IDS = setOf(6, 9, 10, 11)
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

    fun getBossKills(state: GameStateEntity): Map<String, Int> {
        if (state.bossKillsJson.isBlank()) return emptyMap()
        return try {
            Gson().fromJson(state.bossKillsJson, object : TypeToken<Map<String, Int>>() {}.type) ?: emptyMap()
        } catch (e: Exception) { emptyMap() }
    }

    fun getItemLog(state: GameStateEntity): List<String> {
        if (state.itemLogJson.isBlank()) return emptyList()
        return try {
            Gson().fromJson(state.itemLogJson, object : TypeToken<List<String>>() {}.type) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    /** Возвращает Set базовых ID предметов, которые когда-либо были у игрока (из лога + инвентаря) */
    fun getCollectedItemBaseIds(state: GameStateEntity): Set<String> {
        // itemLogJson — это JSON-массив строк-идентификаторов
        val fromLog: Set<String> = if (state.itemLogJson.isBlank()) emptySet() else try {
            val type = object : TypeToken<List<String>>() {}.type
            val list: List<String>? = Gson().fromJson(state.itemLogJson, type)
            list?.map { ItemUtils.getBaseItemId(it) }?.toSet() ?: emptySet()
        } catch (_: Exception) { emptySet() }

        // inventoryItems — это CSV-строка вида "id1:0,id2:3,..."
        val fromInventory: Set<String> = state.inventoryItems
            .split(",")
            .filter { it.isNotBlank() }
            .map { ItemUtils.getBaseItemId(it.substringBefore(":")) }
            .toSet()

        return fromLog + fromInventory
    }
}
