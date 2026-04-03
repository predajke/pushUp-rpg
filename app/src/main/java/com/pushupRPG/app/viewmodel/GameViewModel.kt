package com.pushupRPG.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushupRPG.app.data.db.GameStateEntity
import com.pushupRPG.app.data.model.EnchantResult
import com.pushupRPG.app.data.model.Item
import com.pushupRPG.app.data.repository.GameRepository
import com.pushupRPG.app.utils.ItemUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    val gameState = repository.getGameStateFlow()
    val recentLogs = repository.getRecentLogsFlow()
    val allLogs = repository.getAllLogsFlow()

    // ==================== ПЕРЕМЕННЫЕ ДЛЯ ГЛАВНОГО МЕНЮ ====================
    private val _inputValue = MutableStateFlow(0)
    val inputValue: StateFlow<Int> = _inputValue.asStateFlow()

    private val _showLevelUpDialog = MutableStateFlow(false)
    val showLevelUpDialog: StateFlow<Boolean> = _showLevelUpDialog.asStateFlow()

    private val _newLevel = MutableStateFlow(0)
    val newLevel: StateFlow<Int> = _newLevel.asStateFlow()

    private val _totalStats = MutableStateFlow<com.pushupRPG.app.utils.TotalStats?>(null)
    val totalStats: StateFlow<com.pushupRPG.app.utils.TotalStats?> = _totalStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _activeEvent = MutableStateFlow<com.pushupRPG.app.data.model.GameEvent?>(null)
    val activeEvent: StateFlow<com.pushupRPG.app.data.model.GameEvent?> = _activeEvent.asStateFlow()

    fun addToInput(amount: Int) {
        val current = _inputValue.value
        _inputValue.value = if (current + amount < 0) 0 else current + amount
    }

    fun resetInput() {
        _inputValue.value = 0
    }

    fun savePushUps() {
        val count = _inputValue.value
        if (count > 0) {
            addPushUps(count)
            _inputValue.value = 0
        }
    }

    fun dismissLevelUpDialog() {
        _showLevelUpDialog.value = false
    }

    fun triggerRealtimeTick() {
        viewModelScope.launch { repository.checkAndUpdateEvent() }
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
                ItemUtils.getItemById(getBaseId(entry))
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
        val entry = state.inventoryItems.split(",").find { it.contains(itemId) } ?: return 0
        return entry.split(":").getOrNull(1)?.toIntOrNull() ?: 0
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

    // ==================== ОСНОВНЫЕ ИГРОВЫЕ ДЕЙСТВИЯ ====================
    fun addPushUps(count: Int) {
        viewModelScope.launch { repository.addPushUps(count) }
    }

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
}