package com.pushupRPG.app.data.repository

import android.content.Context
import com.pushupRPG.app.data.db.AppDatabase
import com.pushupRPG.app.data.db.GameStateEntity
import com.pushupRPG.app.data.db.LogEntryEntity
import com.pushupRPG.app.data.db.PushUpRecordEntity
import com.pushupRPG.app.utils.DateUtils
import com.pushupRPG.app.utils.GameCalculations
import com.pushupRPG.app.utils.ItemUtils
import kotlinx.coroutines.flow.Flow
import com.pushupRPG.app.data.model.EventType
import com.pushupRPG.app.utils.EventUtils
import com.pushupRPG.app.utils.MonsterUtils
import com.pushupRPG.app.utils.ShopUtils
import com.pushupRPG.app.data.model.PeriodStats
import com.pushupRPG.app.data.model.EnchantResult

class GameRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val dao = db.pushUpDao()

    // ==================== ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ====================

    // "boots_002_1234567890:1" -> "boots_002"
    private fun getBaseId(entry: String): String {
        val idPart = entry.split(":")[0]
        val parts = idPart.split("_")
        return if (parts.size > 2 &&
            parts.last().all { it.isDigit() } &&
            parts.last().length > 8) {
            parts.dropLast(1).joinToString("_")
        } else {
            idPart
        }
    }

    // "boots_002_1234567890:3" -> 3
    private fun getEnchantLevelFromEntry(entry: String): Int {
        return entry.split(":").getOrNull(1)?.toIntOrNull() ?: 0
    }

    // "boots_002_1234567890:3" -> "boots_002_1234567890"
    private fun getUniqueId(entry: String): String {
        return entry.split(":")[0]
    }

    // Разбить инвентарь на список entries
    private fun parseInventory(inventoryStr: String): MutableList<String> {
        return inventoryStr.split(",")
            .filter { it.isNotEmpty() }
            .toMutableList()
    }

    // Собрать инвентарь из списка entries
    private fun buildInventory(entries: List<String>): String {
        return entries.filter { it.isNotEmpty() }.joinToString(",")
    }

    // ==================== GAMESTATE ====================

    fun getGameStateFlow(): Flow<GameStateEntity?> {
        return dao.getGameStateFlow()
    }

    suspend fun getGameState(): GameStateEntity {
        return dao.getGameState() ?: createInitialGameState()
    }

    suspend fun saveGameState(state: GameStateEntity) {
        dao.saveGameState(state)
    }

    private suspend fun createInitialGameState(): GameStateEntity {
        val today = DateUtils.getTodayString()
        val initial = GameStateEntity(
            lastResetDate = today,
            lastLoginDate = today,
            lastBattleTick = System.currentTimeMillis(),
            characterBirthDate = today
        )
        dao.saveGameState(initial)
        return initial
    }

    // ==================== PUSH UPS ====================

    suspend fun addPushUps(count: Int) {
        val today = DateUtils.getTodayString()
        val state = getGameState()

        val newPushUpsToday = if (state.lastResetDate == today) {
            state.pushUpsToday + count
        } else {
            count
        }

        val newStreak = updateStreak(state)

        val activeEvent = EventUtils.getEventById(state.activeEventId)
        val xpMultiplier = if (activeEvent?.type == EventType.XP_BONUS &&
            EventUtils.isEventActive(state.eventEndTime)) 2 else 1
        val newTotalXp = state.totalXp + (count * xpMultiplier)
        val newLevel = GameCalculations.getLevelFromXp(newTotalXp)
        val oldLevel = GameCalculations.getLevelFromXp(state.totalXp)
        val leveledUp = newLevel > oldLevel
        val newStatPoints = if (leveledUp) {
            state.unspentStatPoints + GameCalculations.STAT_POINTS_PER_LEVEL
        } else {
            state.unspentStatPoints
        }

        val monsterUpdate = if (leveledUp) {
            val newMonster = MonsterUtils.rollNextMonster(newLevel)
            Triple(newMonster.name, newMonster.maxHp, newMonster.damage)
        } else {
            Triple(state.monsterName, state.monsterMaxHp, state.monsterDamage)
        }

        val maxHp = GameCalculations.getMaxHp(newLevel, state.baseHealth, 0)
        val newHp = if (state.isPlayerDead) maxHp else state.currentHp
        val wasRevived = state.isPlayerDead

        val updatedState = state.copy(
            pushUpsToday = newPushUpsToday,
            lastResetDate = today,
            totalXp = newTotalXp,
            playerLevel = newLevel,
            unspentStatPoints = newStatPoints,
            totalPushUpsAllTime = state.totalPushUpsAllTime + count,
            currentStreak = newStreak,
            longestStreak = maxOf(state.longestStreak, newStreak),
            lastLoginDate = today,
            currentHp = newHp,
            isPlayerDead = false,
            monsterName = monsterUpdate.first,
            monsterMaxHp = monsterUpdate.second,
            monsterDamage = monsterUpdate.third
        )
        dao.saveGameState(updatedState)

        dao.insertPushUpRecord(PushUpRecordEntity(date = today, count = count))

        if (wasRevived) {
            addLog("Hero was revived after push-ups!", "Герой воскрешён после отжиманий!")
        }
        if (leveledUp) {
            addLog("Level Up! Now level $newLevel!", "Повышение уровня! Теперь уровень $newLevel!")
        }
    }

    private fun updateStreak(state: GameStateEntity): Int {
        val today = DateUtils.getTodayString()
        return when {
            state.lastLoginDate == today -> state.currentStreak
            DateUtils.isYesterday(state.lastLoginDate) -> state.currentStreak + 1
            state.lastLoginDate.isEmpty() -> 1
            else -> 1
        }
    }

    suspend fun updateStreakOnLogin() {
        val state = getGameState()
        val today = DateUtils.getTodayString()
        if (state.lastLoginDate == today) return

        val newStreak = when {
            DateUtils.isYesterday(state.lastLoginDate) -> state.currentStreak + 1
            state.lastLoginDate.isEmpty() -> 1
            else -> 1
        }

        dao.saveGameState(state.copy(
            currentStreak = newStreak,
            longestStreak = maxOf(state.longestStreak, newStreak),
            lastLoginDate = today
        ))
    }

    // ==================== СТАТИСТИКА ====================

    suspend fun getStatsForPeriod(): PeriodStats {
        return PeriodStats(
            lastWeek = dao.getPushUpsSince(DateUtils.getDateStringDaysAgo(7)) ?: 0,
            lastMonth = dao.getPushUpsSince(DateUtils.getDateStringMonthsAgo(1)) ?: 0,
            lastQuarter = dao.getPushUpsSince(DateUtils.getDateStringMonthsAgo(3)) ?: 0,
            lastYear = dao.getPushUpsSince(DateUtils.getDateStringYearsAgo(1)) ?: 0,
            total = dao.getTotalPushUps() ?: 0
        )
    }

    suspend fun getLast7DaysStats() = dao.getLast7DaysStats()

    // ==================== ЛОГИ ====================

    fun getRecentLogsFlow(): Flow<List<LogEntryEntity>> = dao.getRecentLogs()
    fun getAllLogsFlow(): Flow<List<LogEntryEntity>> = dao.getAllLogs()

    suspend fun addLog(message: String, messageRu: String) {
        dao.insertLog(LogEntryEntity(message = message, messageRu = messageRu))
    }

    // ==================== ИНВЕНТАРЬ ====================

    suspend fun addItemToInventory(itemId: String) {
        val state = getGameState()
        val uniqueId = "${itemId}_${System.currentTimeMillis()}"
        val newEntry = "$uniqueId:0"

        val entries = parseInventory(state.inventoryItems)
        entries.add(newEntry)

        dao.saveGameState(state.copy(
            inventoryItems = buildInventory(entries),
            itemsCollected = state.itemsCollected + 1
        ))
    }

    // ==================== ЭКИПИРОВКА ====================

    suspend fun equipItem(itemId: String, slot: String) {
        val state = getGameState()
        val entries = parseInventory(state.inventoryItems)

        // Ищем entry по uniqueId
        val idx = entries.indexOfFirst { getUniqueId(it) == itemId }
        val itemEntry = if (idx >= 0) entries[idx] else "$itemId:0"

        // Убираем из инвентаря
        if (idx >= 0) entries.removeAt(idx)

        // Если в слоте уже что-то есть — возвращаем в инвентарь
        val currentSlotEntry = when (slot) {
            "head" -> state.equippedHead
            "necklace" -> state.equippedNecklace
            "weapon", "weapon1" -> state.equippedWeapon1
            "weapon2" -> state.equippedWeapon2
            "pants" -> state.equippedPants
            "boots" -> state.equippedBoots
            else -> ""
        }
        if (currentSlotEntry.isNotEmpty()) {
            entries.add(currentSlotEntry)
        }

        val newInventory = buildInventory(entries)

        val updatedState = when (slot) {
            "head" -> state.copy(equippedHead = itemEntry, inventoryItems = newInventory)
            "necklace" -> state.copy(equippedNecklace = itemEntry, inventoryItems = newInventory)
            "weapon" -> {
                if (state.equippedWeapon1.isEmpty())
                    state.copy(equippedWeapon1 = itemEntry, inventoryItems = newInventory)
                else if (state.equippedWeapon2.isEmpty())
                    state.copy(equippedWeapon2 = itemEntry, inventoryItems = newInventory)
                else
                    state.copy(equippedWeapon1 = itemEntry, inventoryItems = newInventory)
            }
            "weapon1" -> state.copy(equippedWeapon1 = itemEntry, inventoryItems = newInventory)
            "weapon2" -> state.copy(equippedWeapon2 = itemEntry, inventoryItems = newInventory)
            "pants" -> state.copy(equippedPants = itemEntry, inventoryItems = newInventory)
            "boots" -> state.copy(equippedBoots = itemEntry, inventoryItems = newInventory)
            else -> state
        }
        dao.saveGameState(updatedState)
    }

    suspend fun unequipItem(slot: String) {
        val state = getGameState()

        val slotEntry = when (slot) {
            "head" -> state.equippedHead
            "necklace" -> state.equippedNecklace
            "weapon1" -> state.equippedWeapon1
            "weapon2" -> state.equippedWeapon2
            "pants" -> state.equippedPants
            "boots" -> state.equippedBoots
            else -> ""
        }
        if (slotEntry.isEmpty()) return

        val fullEntry = if (slotEntry.contains(":")) slotEntry else "$slotEntry:0"

        val entries = parseInventory(state.inventoryItems)
        entries.add(fullEntry)

        val updatedState = when (slot) {
            "head" -> state.copy(equippedHead = "", inventoryItems = buildInventory(entries))
            "necklace" -> state.copy(equippedNecklace = "", inventoryItems = buildInventory(entries))
            "weapon1" -> state.copy(equippedWeapon1 = "", inventoryItems = buildInventory(entries))
            "weapon2" -> state.copy(equippedWeapon2 = "", inventoryItems = buildInventory(entries))
            "pants" -> state.copy(equippedPants = "", inventoryItems = buildInventory(entries))
            "boots" -> state.copy(equippedBoots = "", inventoryItems = buildInventory(entries))
            else -> state
        }
        dao.saveGameState(updatedState)
    }

    // ==================== ПРОДАЖА ====================

    suspend fun sellItem(itemId: String) {
        val state = getGameState()
        val entries = parseInventory(state.inventoryItems)

        val idx = entries.indexOfFirst { getUniqueId(it) == itemId }
        if (idx < 0) return

        val entry = entries[idx]
        val baseId = getBaseId(entry)
        val item = ItemUtils.getItemById(baseId)
        val rarity = item?.rarity ?: "common"
        val teethGained = GameCalculations.getTeethFromSell(rarity)

        entries.removeAt(idx)

        dao.saveGameState(state.copy(
            inventoryItems = buildInventory(entries),
            baseLuck = state.baseLuck + 0.05f,
            teeth = state.teeth + teethGained
        ))

        val itemName = item?.name_en ?: itemId
        val itemNameRu = item?.name_ru ?: itemId
        addLog(
            "Sold $itemName. +$teethGained 🦷 Luck increased!",
            "Продан $itemNameRu. +$teethGained 🦷 Удача увеличена!"
        )
    }

    // ==================== СБРОС ====================

    suspend fun resetAllProgress() {
        dao.deleteAllLogs()
        dao.deleteAllPushUpRecords()
        val today = DateUtils.getTodayString()
        val fresh = GameStateEntity(
            lastResetDate = today,
            lastLoginDate = today,
            lastBattleTick = System.currentTimeMillis(),
            characterBirthDate = today
        )
        dao.saveGameState(fresh)
    }

    // ==================== ОЧКИ ====================

    suspend fun spendStatPoint(stat: String) {
        val state = getGameState()
        if (state.unspentStatPoints <= 0) return

        val updatedState = when (stat) {
            "power" -> state.copy(
                basePower = state.basePower + 1,
                unspentStatPoints = state.unspentStatPoints - 1
            )
            "health" -> state.copy(
                baseHealth = state.baseHealth + 10,
                unspentStatPoints = state.unspentStatPoints - 1
            )
            "luck" -> state.copy(
                baseLuck = state.baseLuck + 0.1f,
                unspentStatPoints = state.unspentStatPoints - 1
            )
            else -> state
        }
        dao.saveGameState(updatedState)
    }

    suspend fun useFreePoints(): Boolean {
        val state = getGameState()
        if (state.freePointsUsedToday >= 2) return false

        dao.saveGameState(state.copy(
            unspentStatPoints = state.unspentStatPoints + 2,
            freePointsUsedToday = state.freePointsUsedToday + 1
        ))
        addLog("Free points: +2 stat points!", "Бесплатные очки: +2 очка характеристик!")
        return true
    }

    // ==================== СОБЫТИЯ ====================

    suspend fun checkAndUpdateEvent(): GameStateEntity {
        val state = getGameState()
        val now = System.currentTimeMillis()

        if (EventUtils.isEventActive(state.eventEndTime)) return state

        if (EventUtils.shouldStartNewEvent(state.lastEventTime, state.eventEndTime)) {
            val newEvent = EventUtils.rollRandomEvent()
            val newEndTime = now + EventUtils.EVENT_DURATION_MS
            val updatedState = state.copy(
                activeEventId = newEvent.id,
                eventStartTime = now,
                eventEndTime = newEndTime,
                lastEventTime = now
            )
            dao.saveGameState(updatedState)
            addLog(
                "Event started: ${newEvent.nameEn}! ${newEvent.descriptionEn}",
                "Событие началось: ${newEvent.nameRu}! ${newEvent.descriptionRu}"
            )
            return updatedState
        }

        if (state.activeEventId != 0 && !EventUtils.isEventActive(state.eventEndTime)) {
            val oldEvent = EventUtils.getEventById(state.activeEventId)
            val updatedState = state.copy(activeEventId = 0)
            dao.saveGameState(updatedState)
            if (oldEvent != null) {
                addLog(
                    "Event ended: ${oldEvent.nameEn}",
                    "Событие закончилось: ${oldEvent.nameRu}"
                )
            }
            return updatedState
        }

        return state
    }

    // ==================== ITEMS ====================

    fun loadItems() {
        ItemUtils.loadItems(context)
    }

    // ==================== МАГАЗИН ====================

    suspend fun getOrRefreshShop(): List<com.pushupRPG.app.data.model.Item> {
        val state = getGameState()
        val allItems = ItemUtils.loadItems(context)

        return if (ShopUtils.shouldRefreshShop(state.shopLastRefresh) ||
            state.shopItems.isEmpty()) {
            val newItems = ShopUtils.generateShopItems(allItems)
            val newItemsStr = ShopUtils.shopItemsToString(newItems)
            dao.saveGameState(state.copy(
                shopItems = newItemsStr,
                shopLastRefresh = System.currentTimeMillis()
            ))
            newItems
        } else {
            ShopUtils.shopItemsFromString(state.shopItems)
        }
    }

    suspend fun buyShopItem(itemId: String): Boolean {
        val state = getGameState()
        val item = ItemUtils.getItemById(itemId) ?: return false
        val price = ShopUtils.getBuyPrice(item.rarity)
        if (state.teeth < price) return false

        // Убираем из магазина
        val newShopItems = state.shopItems
            .split(",")
            .filter { it.isNotEmpty() && it != itemId }
            .joinToString(",")

        // Добавляем в инвентарь с уникальным ID и уровнем заточки 0
        val uniqueId = "${itemId}_${System.currentTimeMillis()}"
        val entries = parseInventory(state.inventoryItems)
        entries.add("$uniqueId:0")

        dao.saveGameState(state.copy(
            teeth = state.teeth - price,
            shopItems = newShopItems,
            inventoryItems = buildInventory(entries),
            itemsCollected = state.itemsCollected + 1
        ))

        addLog(
            "Bought ${item.name_en} for $price 🦷",
            "Куплено ${item.name_ru} за $price 🦷"
        )
        return true
    }

    suspend fun rerollShop(): Boolean {
        val state = getGameState()
        if (state.teeth < 1) return false

        val allItems = ItemUtils.loadItems(context)
        val newItems = ShopUtils.generateShopItems(allItems)
        val newItemsStr = ShopUtils.shopItemsToString(newItems)
        dao.saveGameState(state.copy(
            shopItems = newItemsStr,
            shopLastRefresh = System.currentTimeMillis(),
            teeth = state.teeth - 1
        ))
        return true
    }

    // ==================== КУЗНИЦА ====================

    suspend fun setForgeSlot(slot: Int, itemId: String) {
        val state = getGameState()
        val updatedState = if (slot == 1) {
            state.copy(forgeSlot1 = itemId)
        } else {
            state.copy(forgeSlot2 = itemId)
        }
        dao.saveGameState(updatedState)
    }

    suspend fun mergeItems(): com.pushupRPG.app.data.model.Item? {
        val state = getGameState()
        if (state.forgeSlot1.isEmpty() || state.forgeSlot2.isEmpty()) return null

        val entries = parseInventory(state.inventoryItems)

        val idx1 = entries.indexOfFirst { getUniqueId(it) == state.forgeSlot1 }
        val idx2 = entries.indexOfFirst { getUniqueId(it) == state.forgeSlot2 }

        if (idx1 < 0 || idx2 < 0) return null

        // Удаляем с большего индекса чтобы не сбить меньший
        val removeFirst = maxOf(idx1, idx2)
        val removeSecond = minOf(idx1, idx2)
        entries.removeAt(removeFirst)
        entries.removeAt(removeSecond)

        val allItems = ItemUtils.loadItems(context)
        val targetRarity = ShopUtils.rollForgeRarity()
        val eligible = allItems.filter { it.rarity == targetRarity }
        if (eligible.isEmpty()) return null

        val resultItem = eligible.random()
        val uniqueId = "${resultItem.id}_${System.currentTimeMillis()}"
        entries.add("$uniqueId:0")

        dao.saveGameState(state.copy(
            inventoryItems = buildInventory(entries),
            forgeSlot1 = "",
            forgeSlot2 = "",
            itemsCollected = state.itemsCollected + 1
        ))

        addLog(
            "Forge: created ${resultItem.name_en}!",
            "Кузница: создан ${resultItem.name_ru}!"
        )
        return resultItem
    }

    // ==================== CLOVER BOX ====================

    suspend fun checkAndResetDaily() {
        val state = getGameState()
        val today = DateUtils.getTodayString()
        if (state.lastDailyReset != today) {
            dao.saveGameState(state.copy(
                cloverBoxUsedToday = 0,
                freePointsUsedToday = 0,
                lastDailyReset = today
            ))
        }
    }

    suspend fun useCloverBox(): com.pushupRPG.app.data.model.Item? {
        val state = getGameState()
        if (state.cloverBoxUsedToday >= 2) return null

        val allItems = ItemUtils.loadItems(context)
        val targetRarity = ShopUtils.rollCloverBoxRarity()
        val eligible = allItems.filter { it.rarity == targetRarity }
        if (eligible.isEmpty()) return null

        val item = eligible.random()
        val uniqueId = "${item.id}_${System.currentTimeMillis()}"

        val entries = parseInventory(state.inventoryItems)
        entries.add("$uniqueId:0")

        dao.saveGameState(state.copy(
            inventoryItems = buildInventory(entries),
            cloverBoxUsedToday = state.cloverBoxUsedToday + 1,
            itemsCollected = state.itemsCollected + 1
        ))

        addLog(
            "Clover Box: got ${item.name_en}!",
            "Клеверная коробка: получен ${item.name_ru}!"
        )
        return item
    }

    // ==================== ЗАТОЧКА ====================

    fun calculateEnchantChance(luck: Float, streak: Int): Float {
        return minOf(75f, 7f + (luck * 3f) + (streak * 0.07f))
    }

    fun calculateEnchantCost(rarity: String, currentEnchantLevel: Int): Int {
        val basePrice = when (rarity) {
            "common" -> 1
            "uncommon" -> 2
            "rare" -> 3
            "epic" -> 5
            else -> 1
        }
        return basePrice * (currentEnchantLevel + 1)
    }

    suspend fun enchantItem(itemId: String): EnchantResult {
        val state = getGameState()
        val entries = parseInventory(state.inventoryItems)

        val idx = entries.indexOfFirst { getUniqueId(it) == itemId }
        if (idx < 0) return EnchantResult.FAILED

        val entry = entries[idx]
        val currentLevel = getEnchantLevelFromEntry(entry)
        if (currentLevel >= 9) return EnchantResult.MAX_LEVEL

        val baseId = getBaseId(entry)
        val item = ItemUtils.getItemById(baseId) ?: return EnchantResult.FAILED
        val cost = calculateEnchantCost(item.rarity, currentLevel)

        if (state.teeth < cost) return EnchantResult.NOT_ENOUGH_TEETH

        val chance = calculateEnchantChance(state.baseLuck, state.currentStreak)
        val success = kotlin.random.Random.nextFloat() * 100f < chance
        val newTeeth = state.teeth - cost

        return if (success) {
            val newLevel = currentLevel + 1
            entries[idx] = "${getUniqueId(entry)}:$newLevel"
            dao.saveGameState(state.copy(
                inventoryItems = buildInventory(entries),
                teeth = newTeeth
            ))
            addLog(
                "⚡ ${item.name_en} successfully enchanted to +$newLevel!",
                "⚡ ${item.name_ru} успешно заточен до +$newLevel!"
            )
            EnchantResult.SUCCESS
        } else {
            dao.saveGameState(state.copy(teeth = newTeeth))
            addLog(
                "💔 Enchanting ${item.name_en} failed...",
                "💔 Заточка ${item.name_ru} не удалась..."
            )
            EnchantResult.FAILED
        }
    }
}

// я удалил отсюда data class PeriodStats и enum class EnchantResult