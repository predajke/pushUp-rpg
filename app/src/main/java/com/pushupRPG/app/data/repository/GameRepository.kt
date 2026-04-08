package com.pushupRPG.app.data.repository

import android.content.Context
import com.pushupRPG.app.data.db.AppDatabase
import com.pushupRPG.app.data.db.GameStateEntity
import com.pushupRPG.app.data.db.LogEntryEntity
import com.pushupRPG.app.data.db.PushUpRecordEntity
import com.pushupRPG.app.data.db.entity.MaxPushUpsAttemptEntity
import com.pushupRPG.app.data.exception.CheatCooldownException
import com.pushupRPG.app.utils.DateUtils
import com.pushupRPG.app.utils.GameCalculations
import com.pushupRPG.app.utils.ItemUtils
import com.pushupRPG.app.managers.AntiCheatManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.pushupRPG.app.data.model.EventType
import com.pushupRPG.app.utils.EventUtils
import com.pushupRPG.app.utils.MonsterUtils
import com.pushupRPG.app.utils.ShopUtils
import com.pushupRPG.app.data.model.PeriodStats
import com.pushupRPG.app.data.model.EnchantResult
import com.pushupRPG.app.data.model.ForgeResult
import com.pushupRPG.app.utils.BossUtils
import com.pushupRPG.app.utils.DailyRewardUtils
import com.pushupRPG.app.utils.QuestSystem
import com.pushupRPG.app.utils.QuestType
import com.pushupRPG.app.utils.AchievementSystem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GameRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val dao = db.pushUpDao()
    private val maxPushUpsDao = db.maxPushUpsDao()
    private val antiCheatManager = AntiCheatManager()
    private val saveMutex = Mutex()

    init {
        // Загружаем все предметы сразу — иначе getItemById() вернёт null до первого действия
        ItemUtils.loadItems(context)
    }

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
        saveMutex.withLock {
            dao.saveGameState(state)
        }
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

    /** Возвращает новый уровень если произошёл level-up, иначе 0 */
    suspend fun addPushUps(count: Int): Int {
        val today = DateUtils.getTodayString()

        // Anti-cheat: проверка максимума 99 отжиманий
        if (count == 99) {
            val lastMaxAttempt = maxPushUpsDao.getLastAttemptForDate(today)
            val attemptNumber = maxPushUpsDao.getAttemptsCountForDate(today) + 1

            if (lastMaxAttempt != null) {
                val remainingCooldown = antiCheatManager.getRemainingCooldownMsForMaxAttempt(
                    lastMaxAttempt.timestamp,
                    lastMaxAttempt.attemptNumber
                )

                if (remainingCooldown > 0) {
                    val adType = antiCheatManager.getRequiredAd(attemptNumber)
                    addLog("⚠️ Anti-cheat cooldown", "⚠️ Кулдаун после максимума")
                    throw CheatCooldownException(remainingCooldown, adType, attemptNumber)
                }
            }

            // Сохраняем попытку в БД
            maxPushUpsDao.insertAttempt(MaxPushUpsAttemptEntity(date = today, attemptNumber = attemptNumber))
        }

        val state = getGameState()
        val wasRevived = state.isPlayerDead

        val newPushUpsToday = if (state.lastResetDate == today) state.pushUpsToday + count else count
        val newStreak = updateStreak(state)

        val activeEvent = EventUtils.getEventById(state.activeEventId)
        val eventMult = if (activeEvent?.type == EventType.XP_BONUS &&
            EventUtils.isEventActive(state.eventEndTime)) 2f else 1f
        val streakBonus = GameCalculations.getStreakXpBonus(newStreak)
        val achBonuses = AchievementSystem.getActiveBonuses(state.activeAchievementIds)
        val equippedForBonuses = getEquippedItemObjects(state)
        val setBonuses = ItemUtils.getSetBonuses(equippedForBonuses)
        val xpBonus = achBonuses.xpPercent + setBonuses.xpPercent
        val xpGained = ((count * eventMult) * (1f + streakBonus + xpBonus)).toInt()
        val newTotalXp = state.totalXp + xpGained
        val newLevel = GameCalculations.getLevelFromXp(newTotalXp)
        val oldLevel = GameCalculations.getLevelFromXp(state.totalXp)
        val leveledUp = newLevel > oldLevel
        val newStatPoints = if (leveledUp)
            state.unspentStatPoints + GameCalculations.STAT_POINTS_PER_LEVEL
        else state.unspentStatPoints

        val maxHp = GameCalculations.getMaxHp(newLevel, state.baseHealth, 0)

        // Базовое состояние после XP/уровня
        var workingState = state.copy(
            pushUpsToday = newPushUpsToday,
            lastResetDate = today,
            totalXp = newTotalXp,
            playerLevel = newLevel,
            unspentStatPoints = newStatPoints,
            totalPushUpsAllTime = state.totalPushUpsAllTime + count,
            currentStreak = newStreak,
            longestStreak = maxOf(state.longestStreak, newStreak),
            lastLoginDate = today,
            currentHp = if (wasRevived) maxHp else state.currentHp,
            isPlayerDead = false,
            bestSingleSession = maxOf(state.bestSingleSession, count)
        )

        // Level-up: спавним нового монстра с полным HP
        if (leveledUp) {
            val newMonster = MonsterUtils.rollNextMonster(newLevel)
            workingState = workingState.copy(
                monsterName = newMonster.name,
                monsterLevel = newMonster.level,
                monsterMaxHp = newMonster.maxHp,
                monsterCurrentHp = newMonster.maxHp,
                monsterDamage = newMonster.damage
            )
        }

        // Боевая обработка отжиманий (пропускаем если только что воскресли)
        if (!wasRevived) {
            workingState = processPushUpCombat(workingState, count)
        }

        // Квест-прогресс
        var questsForPushups = QuestSystem.deserialize(workingState.activeQuestsJson)
        questsForPushups = QuestSystem.addProgress(questsForPushups, QuestType.PUSHUPS_DAY, count)
        questsForPushups = QuestSystem.addProgress(questsForPushups, QuestType.PUSHUPS_SESSION, count)
        // Combo-квест: порог 50 отжиманий
        val prevPushUps = if (state.lastResetDate == today) state.pushUpsToday else 0
        if (prevPushUps < 50 && newPushUpsToday >= 50) {
            questsForPushups = QuestSystem.addProgress(questsForPushups, QuestType.PUSHUPS_AND_KILLS, 1)
        }
        workingState = workingState.copy(activeQuestsJson = QuestSystem.serialize(questsForPushups))

        // Проверка достижений — ach_berserker (50+ за сессию), time-based
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        workingState = AchievementSystem.checkAndUnlock(
            workingState.copy(
                // Помечаем условия для checkAndUnlock через totalPushUpsAllTime — уже обновлено выше
            ), today
        )
        // Ночная / утренняя сессия — логика через час
        if (hour >= 23 || hour < 7) {
            val nightId = if (hour >= 23) "ach_night_shift" else "ach_early_bird"
            val unlocked = AchievementSystem.getUnlocked(workingState.achievementsJson)
            if (unlocked.none { it.defId == nightId }) {
                val newUnlocked = unlocked + com.pushupRPG.app.utils.UnlockedAchievement(nightId, today)
                workingState = workingState.copy(
                    achievementsJson = AchievementSystem.serializeUnlocked(newUnlocked)
                )
            }
        }

        try {
            dao.saveGameState(workingState)
            dao.insertPushUpRecord(PushUpRecordEntity(date = today, count = count))
        } catch (e: Exception) {
            addLog("❌ Error saving progress", "❌ Ошибка сохранения прогресса")
            throw e
        }

        if (wasRevived) addLog("🌅 Hero was revived after push-ups!", "🌅 Герой воскрешён после отжиманий!")
        if (leveledUp) addLog("⬆️ Level Up! Now level $newLevel!", "⬆️ Повышение уровня! Теперь уровень $newLevel!")
        return if (leveledUp) newLevel else 0
    }

    // ==================== БОЕВАЯ СИСТЕМА ====================

    /** Обрабатывает урон от отжиманий + контратаку монстра */
    private suspend fun processPushUpCombat(state: GameStateEntity, count: Int): GameStateEntity {
        val (equippedItems, enchantLevels) = getEquippedWithEnchant(state)
        val totalStats = GameCalculations.calculateTotalStats(state, equippedItems, enchantLevels)

        val isBurst = GameCalculations.isBurstAttack(count)
        val burstMult = if (isBurst) GameCalculations.BURST_MULTIPLIER else 1

        // Считаем суммарный урон от count отжиманий (макс 50 итераций, масштабируем)
        val iterations = count.coerceAtMost(50)
        var totalDmg = 0
        var maxSingleHit = 0
        var critCount = 0
        repeat(iterations) {
            val isCrit = GameCalculations.isCriticalHit(totalStats.luck)
            val dmg = GameCalculations.calculatePlayerDamage(totalStats.power * burstMult, isCrit)
            totalDmg += dmg
            if (isCrit) critCount++
            if (dmg > maxSingleHit) maxSingleHit = dmg
        }
        if (count > 50) totalDmg = (totalDmg.toFloat() * count / 50f).toInt()

        val burstText = if (isBurst) " 💥 BURST!" else ""
        val critText = if (critCount > 0) " ($critCount×CRIT)" else ""
        addLog(
            "💪 $count push-ups → -$totalDmg to ${state.monsterName}!$burstText$critText",
            "💪 $count отжиманий → -$totalDmg ${state.monsterName}!$burstText$critText"
        )

        val newMonsterHp = (state.monsterCurrentHp - totalDmg).coerceAtLeast(0)

        // Монстр убит — спавним следующего
        if (newMonsterHp <= 0) {
            return handleMonsterKill(state.copy(
                totalDamageDealt = state.totalDamageDealt + totalDmg,
                highestDamage = maxOf(state.highestDamage, maxSingleHit),
                totalCriticalHits = state.totalCriticalHits + critCount
            ))
        }

        // Монстр жив — бьёт в ответ
        val monsterDmg = GameCalculations.calculateDamageTaken(state.monsterDamage, totalStats.armor)
        val newPlayerHp = (state.currentHp - monsterDmg).coerceAtLeast(0)
        val playerDied = newPlayerHp <= 0

        addLog(
            "⚔️ ${state.monsterName} strikes back: -$monsterDmg HP",
            "⚔️ ${state.monsterName} бьёт в ответ: -$monsterDmg HP"
        )

        val teethOnHit = if (GameCalculations.isTeethDropped()) 1 else 0
        val afterCombat = state.copy(
            monsterCurrentHp = newMonsterHp,
            currentHp = newPlayerHp,
            isPlayerDead = playerDied,
            teeth = state.teeth + teethOnHit,
            totalDamageDealt = state.totalDamageDealt + totalDmg,
            highestDamage = maxOf(state.highestDamage, maxSingleHit),
            totalCriticalHits = state.totalCriticalHits + critCount,
            totalTeethEarned = state.totalTeethEarned + teethOnHit
        )

        if (playerDied) {
            val regen = state.monsterMaxHp / 2
            addLog("💀 ${state.playerName} defeated! Monster recovered $regen HP!",
                "💀 ${state.playerName} побеждён! Монстр восстановил $regen HP!")
            return afterCombat.copy(monsterCurrentHp = (newMonsterHp + regen).coerceAtMost(state.monsterMaxHp))
        }
        return afterCombat
    }

    /** Авто-бой: один тик каждые 5 минут */
    suspend fun processBattleTick() {
        try {
            val state = getGameState()
            val now = System.currentTimeMillis()

            if (state.isPlayerDead) {
                dao.saveGameState(state.copy(lastBattleTick = now))
                return
            }

            val elapsed = now - state.lastBattleTick
            val tickMs = 5 * 60 * 1000L  // 5 минут
            if (elapsed < tickMs) return

            val ticks = (elapsed / tickMs).toInt().coerceAtMost(3)
            var current = state
            repeat(ticks) {
                if (!current.isPlayerDead) {
                    current = processAutoAttackTick(current)
                }
            }
            dao.saveGameState(current.copy(lastBattleTick = now))
        } catch (e: Exception) {
            android.util.Log.e("GameRepo", "processBattleTick failed", e)
        }
    }

    /** Один раунд авто-боя: игрок атакует → монстр отвечает → реген HP */
    private suspend fun processAutoAttackTick(state: GameStateEntity): GameStateEntity {
        val (equippedItems, enchantLevels) = getEquippedWithEnchant(state)
        val totalStats = GameCalculations.calculateTotalStats(state, equippedItems, enchantLevels)

        // Авто-атака игрока
        val isCrit = GameCalculations.isCriticalHit(totalStats.luck)
        val activeEvent = EventUtils.getEventById(state.activeEventId)
        val powerMult = if (activeEvent?.type == EventType.POWER_BONUS &&
            EventUtils.isEventActive(state.eventEndTime)) 1.5f else 1f
        val playerDmg = (GameCalculations.calculatePlayerDamage(totalStats.power, isCrit) * powerMult).toInt()

        val critText = if (isCrit) " 💫 CRIT!" else ""
        addLog(
            "⚔️ Auto-attack: -$playerDmg to ${state.monsterName}$critText",
            "⚔️ Авто-атака: -$playerDmg ${state.monsterName}$critText"
        )

        val newMonsterHp = (state.monsterCurrentHp - playerDmg).coerceAtLeast(0)
        if (newMonsterHp <= 0) {
            return handleMonsterKill(state.copy(
                totalDamageDealt = state.totalDamageDealt + playerDmg,
                highestDamage = maxOf(state.highestDamage, playerDmg),
                totalCriticalHits = state.totalCriticalHits + (if (isCrit) 1 else 0)
            ))
        }

        // Монстр атакует + реген игрока за 5 минут
        val monsterDmg = GameCalculations.calculateDamageTaken(state.monsterDamage, totalStats.armor)
        val maxHp = GameCalculations.getMaxHp(state.playerLevel, state.baseHealth,
            equippedItems.sumOf { it.stats.health })
        val regenMult = if (activeEvent?.type == EventType.REGEN_BONUS &&
            EventUtils.isEventActive(state.eventEndTime)) 2f else 1f
        val hpRegen = (GameCalculations.calculateHpRegen(maxHp, 5L) * regenMult).toInt()
        val hpAfterRegen = (state.currentHp + hpRegen).coerceAtMost(maxHp)
        val newPlayerHp = (hpAfterRegen - monsterDmg).coerceAtLeast(0)
        val playerDied = newPlayerHp <= 0

        val teethOnHit = if (GameCalculations.isTeethDropped()) 1 else 0
        addLog(
            "🐾 ${state.monsterName} attacks: -$monsterDmg HP | Regen: +$hpRegen HP",
            "🐾 ${state.monsterName} атакует: -$monsterDmg HP | Реген: +$hpRegen HP"
        )

        val afterTick = state.copy(
            monsterCurrentHp = newMonsterHp,
            currentHp = newPlayerHp,
            isPlayerDead = playerDied,
            teeth = state.teeth + teethOnHit,
            totalDamageDealt = state.totalDamageDealt + playerDmg,
            highestDamage = maxOf(state.highestDamage, playerDmg),
            totalCriticalHits = state.totalCriticalHits + (if (isCrit) 1 else 0),
            totalTeethEarned = state.totalTeethEarned + teethOnHit
        )

        if (playerDied) {
            val regen = state.monsterMaxHp / 2
            addLog("💀 ${state.playerName} defeated! Monster recovered $regen HP!",
                "💀 ${state.playerName} побеждён! Монстр восстановил $regen HP!")
            return afterTick.copy(monsterCurrentHp = (newMonsterHp + regen).coerceAtMost(state.monsterMaxHp))
        }
        return afterTick
    }

    /** Обрабатывает смерть монстра: дроп лута/зубов, спавн нового */
    private suspend fun handleMonsterKill(state: GameStateEntity): GameStateEntity {
        val teethFromKill = GameCalculations.getTeethFromMonster(state.monsterLevel)
        val monster = MonsterUtils.getMonsterByLevel(state.monsterLevel)

        // Бонус дропа от достижений и сетов
        val achBonuses = AchievementSystem.getActiveBonuses(state.activeAchievementIds)
        val equippedItems = getEquippedItemObjects(state)
        val setBonuses = ItemUtils.getSetBonuses(equippedItems)
        val dropBonus = achBonuses.dropRatePercent + setBonuses.dropRatePercent
        val isItemDropped = GameCalculations.isItemDropped(state.baseLuck, monster.dropRate * (1f + dropBonus))

        var newInventory = state.inventoryItems
        var newItemLog = state.itemLogJson
        var itemsCollectedAdd = 0
        var droppedRarity = ""
        if (isItemDropped) {
            ItemUtils.loadItems(context)
            val dropped = ItemUtils.getRandomItemByRarity()
            if (dropped != null) {
                val uniqueId = "${dropped.id}_${System.currentTimeMillis()}"
                val entries = parseInventory(state.inventoryItems).toMutableList()
                entries.add("$uniqueId:0")
                newInventory = buildInventory(entries)
                itemsCollectedAdd = 1
                droppedRarity = dropped.rarity
                // Обновляем itemLog (храним последние 50 uniqueId)
                val logList: MutableList<String> = try {
                    Gson().fromJson<List<String>>(newItemLog,
                        object : TypeToken<List<String>>() {}.type)?.toMutableList() ?: mutableListOf()
                } catch (e: Exception) { mutableListOf() }
                logList.add(0, uniqueId)
                if (logList.size > 50) logList.removeAt(logList.lastIndex)
                newItemLog = Gson().toJson(logList)
                addLog(
                    "🎁 ${state.monsterName} dropped ${dropped.name_en}!",
                    "🎁 ${state.monsterName} дроп: ${dropped.name_ru}!"
                )
            }
        }

        val newKillCount = state.monstersKilled + 1
        val wasCurrentBoss = state.isCurrentBoss

        // Обновляем бестиарий
        val bestiaryMap: MutableMap<String, Int> = try {
            Gson().fromJson<Map<String, Int>>(state.bestiaryJson,
                object : TypeToken<Map<String, Int>>() {}.type)?.toMutableMap() ?: mutableMapOf()
        } catch (e: Exception) { mutableMapOf() }
        bestiaryMap[state.monsterName] = (bestiaryMap[state.monsterName] ?: 0) + 1
        val newBestiaryJson = Gson().toJson(bestiaryMap)

        // Обновляем убийства боссов
        var newBossKillsJson = state.bossKillsJson
        if (state.isCurrentBoss && state.currentBossId != 0) {
            val bossKillsMap: MutableMap<String, Int> = try {
                Gson().fromJson<Map<String, Int>>(state.bossKillsJson,
                    object : TypeToken<Map<String, Int>>() {}.type)?.toMutableMap() ?: mutableMapOf()
            } catch (e: Exception) { mutableMapOf() }
            bossKillsMap[state.monsterName] = (bossKillsMap[state.monsterName] ?: 0) + 1
            newBossKillsJson = Gson().toJson(bossKillsMap)
        }

        addLog(
            "💀 ${state.monsterName} (${state.monsterLevel} lvl) defeated! +$teethFromKill 🦷",
            "💀 ${state.monsterName} (${state.monsterLevel} lvl) повержен! +$teethFromKill 🦷"
        )

        // Квест-прогресс за убийство
        var quests = QuestSystem.deserialize(state.activeQuestsJson)
        val killsBefore = QuestSystem.getDailyKillsFromQuests(quests)
        quests = QuestSystem.addProgress(quests, QuestType.KILLS_DAY, 1)
        if (wasCurrentBoss) quests = QuestSystem.addProgress(quests, QuestType.BOSS_KILLS, 1)
        // Combo-квест: порог 5 убийств сегодня
        val killsAfter = QuestSystem.getDailyKillsFromQuests(quests)
        if (killsBefore < 5 && killsAfter >= 5) {
            quests = QuestSystem.addProgress(quests, QuestType.PUSHUPS_AND_KILLS, 1)
        }

        // Спавн следующего монстра / босса
        val spawnBoss = !wasCurrentBoss && BossUtils.shouldSpawnBoss(newKillCount)
        val next = if (spawnBoss) {
            BossUtils.getBossForKillCount(newKillCount).also {
                addLog("⚠️ BOSS appeared: ${it.name}!", "⚠️ БОСС появился: ${it.nameRu}!")
            }
        } else {
            MonsterUtils.rollNextMonster(state.playerLevel)
        }

        val today = DateUtils.getTodayString()
        var updated = state.copy(
            monsterName = next.name,
            monsterLevel = next.level,
            monsterMaxHp = next.maxHp,
            monsterCurrentHp = next.maxHp,
            monsterDamage = next.damage,
            isCurrentBoss = spawnBoss,
            currentBossId = if (spawnBoss) next.id else 0,
            monstersKilled = newKillCount,
            teeth = state.teeth + teethFromKill,
            inventoryItems = newInventory,
            itemsCollected = state.itemsCollected + itemsCollectedAdd,
            highestMonsterLevelKilled = maxOf(state.highestMonsterLevelKilled, state.monsterLevel),
            totalTeethEarned = state.totalTeethEarned + teethFromKill,
            activeQuestsJson = QuestSystem.serialize(quests),
            bestiaryJson = newBestiaryJson,
            itemLogJson = newItemLog,
            bossKillsJson = newBossKillsJson
        )

        // Достижения за дроп редкости
        val unlocked = AchievementSystem.getUnlocked(updated.achievementsJson).toMutableList()
        if (droppedRarity == "legendary" && unlocked.none { it.defId == "ach_legendary_catch" })
            unlocked.add(com.pushupRPG.app.utils.UnlockedAchievement("ach_legendary_catch", today))
        if ((droppedRarity == "epic" || droppedRarity == "legendary") && unlocked.none { it.defId == "ach_epic_catch" })
            unlocked.add(com.pushupRPG.app.utils.UnlockedAchievement("ach_epic_catch", today))
        updated = updated.copy(achievementsJson = AchievementSystem.serializeUnlocked(unlocked))

        return AchievementSystem.checkAndUnlock(updated, today)
    }

    /** Возвращает экипированные предметы как объекты Item */
    private fun getEquippedItemObjects(state: GameStateEntity): List<com.pushupRPG.app.data.model.Item> {
        return listOf(
            state.equippedHead, state.equippedNecklace, state.equippedWeapon1,
            state.equippedWeapon2, state.equippedPants, state.equippedBoots
        ).filter { it.isNotEmpty() }.mapNotNull { entry -> ItemUtils.getItemById(entry) }
    }

    private fun getEquippedWithEnchant(state: GameStateEntity): Pair<List<com.pushupRPG.app.data.model.Item>, List<Int>> {
        val slots = listOf(
            state.equippedHead, state.equippedNecklace, state.equippedWeapon1,
            state.equippedWeapon2, state.equippedPants, state.equippedBoots
        ).filter { it.isNotEmpty() }
        val items  = slots.mapNotNull { ItemUtils.getItemById(it) }
        val levels = slots.map { it.split(":").getOrNull(1)?.toIntOrNull() ?: 0 }
        return items to levels
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

        // Определяем реальный слот (для "weapon" — ищем первый свободный)
        val actualSlot = when (slot) {
            "weapon" -> when {
                state.equippedWeapon1.isEmpty() -> "weapon1"
                state.equippedWeapon2.isEmpty() -> "weapon2"
                else -> "weapon1"  // оба заняты — заменяем weapon1
            }
            else -> slot
        }

        // Если в целевом слоте уже что-то есть — возвращаем в инвентарь
        val currentSlotEntry = when (actualSlot) {
            "head" -> state.equippedHead
            "necklace" -> state.equippedNecklace
            "weapon1" -> state.equippedWeapon1
            "weapon2" -> state.equippedWeapon2
            "pants" -> state.equippedPants
            "boots" -> state.equippedBoots
            else -> ""
        }
        if (currentSlotEntry.isNotEmpty()) {
            entries.add(currentSlotEntry)
        }

        val newInventory = buildInventory(entries)

        val updatedState = when (actualSlot) {
            "head" -> state.copy(equippedHead = itemEntry, inventoryItems = newInventory)
            "necklace" -> state.copy(equippedNecklace = itemEntry, inventoryItems = newInventory)
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
            teeth = state.teeth + teethGained,
            totalTeethEarned = state.totalTeethEarned + teethGained
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
        return saveMutex.withLock {
            val state = getGameState()
            val item = ItemUtils.getItemById(itemId) ?: return@withLock false
            val price = ShopUtils.getBuyPrice(item.rarity)
            if (state.teeth < price) return@withLock false

            val newShopItems = state.shopItems
                .split(",")
                .filter { it.isNotEmpty() && it != itemId }
                .joinToString(",")

            val uniqueId = "${itemId}_${System.currentTimeMillis()}"
            val entries = parseInventory(state.inventoryItems)
            entries.add("$uniqueId:0")

            var buyQuests = QuestSystem.deserialize(state.activeQuestsJson)
            buyQuests = QuestSystem.addProgress(buyQuests, QuestType.BUY_ITEM, 1)
            buyQuests = QuestSystem.addProgress(buyQuests, QuestType.TEETH_SPENT, price)
            dao.saveGameState(state.copy(
                teeth = state.teeth - price,
                shopItems = newShopItems,
                inventoryItems = buildInventory(entries),
                itemsCollected = state.itemsCollected + 1,
                totalTeethSpent = state.totalTeethSpent + price,
                activeQuestsJson = QuestSystem.serialize(buyQuests)
            ))

            addLog(
                "Bought ${item.name_en} for $price 🦷",
                "Куплено ${item.name_ru} за $price 🦷"
            )
            true
        }
    }

    suspend fun rerollShop(): Boolean {
        val state = getGameState()
        if (state.teeth < 1) return false

        val allItems = ItemUtils.loadItems(context)
        val baseItems = ShopUtils.generateShopItems(allItems).toMutableList()
        val activeEvent = EventUtils.getEventById(state.activeEventId)
        if (activeEvent?.type == EventType.ENCHANTERS_LUCK &&
            EventUtils.isEventActive(state.eventEndTime) &&
            kotlin.random.Random.nextFloat() < 0.03f) {
            allItems.filter { it.rarity == "legendary" }.randomOrNull()?.let { baseItems.add(it) }
        }
        val newItemsStr = ShopUtils.shopItemsToString(baseItems)
        var rerollQuests = QuestSystem.deserialize(state.activeQuestsJson)
        rerollQuests = QuestSystem.addProgress(rerollQuests, QuestType.TEETH_SPENT, 1)
        dao.saveGameState(state.copy(
            shopItems = newItemsStr,
            shopLastRefresh = System.currentTimeMillis(),
            teeth = state.teeth - 1,
            totalTeethSpent = state.totalTeethSpent + 1,
            activeQuestsJson = QuestSystem.serialize(rerollQuests)
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

    suspend fun mergeItems(): ForgeResult {
        return saveMutex.withLock {
            val state = getGameState()
            if (state.forgeSlot1.isEmpty() || state.forgeSlot2.isEmpty()) return@withLock ForgeResult.NoItems

            val entries = parseInventory(state.inventoryItems)

            val idx1 = entries.indexOfFirst { getUniqueId(it) == state.forgeSlot1 }
            val idx2 = entries.indexOfFirst { getUniqueId(it) == state.forgeSlot2 }

            if (idx1 < 0 || idx2 < 0) return@withLock ForgeResult.NoItems

            // Проверяем редкость ДО удаления
            val rarity1 = ItemUtils.getItemById(getBaseId(entries[idx1]))?.rarity
            val rarity2 = ItemUtils.getItemById(getBaseId(entries[idx2]))?.rarity

            // Удаляем с большего индекса чтобы не сбить меньший
            val removeFirst = maxOf(idx1, idx2)
            val removeSecond = minOf(idx1, idx2)
            entries.removeAt(removeFirst)
            entries.removeAt(removeSecond)

            val allItems = ItemUtils.loadItems(context)
            val targetRarity = if (rarity1 == "epic" && rarity2 == "epic" && kotlin.random.Random.nextFloat() < 0.25f)
                "legendary" else ShopUtils.rollForgeRarity()

            if (targetRarity == null) {
                // FAIL — оба предмета уже удалены, просто сохраняем без нового
                val forgeQuests = QuestSystem.serialize(
                    QuestSystem.addProgress(QuestSystem.deserialize(state.activeQuestsJson), QuestType.FORGE, 1)
                )
                dao.saveGameState(state.copy(
                    inventoryItems = buildInventory(entries),
                    forgeSlot1 = "",
                    forgeSlot2 = "",
                    totalItemsMerged = state.totalItemsMerged + 1,
                    activeQuestsJson = forgeQuests
                ))
                addLog("Forge: FAIL! Both items destroyed.", "Кузница: FAIL! Оба предмета уничтожены.")
                return@withLock ForgeResult.Fail
            }

            val eligible = allItems.filter { it.rarity == targetRarity }
            if (eligible.isEmpty()) return@withLock ForgeResult.NoItems

            val resultItem = eligible.random()
            val uniqueId = "${resultItem.id}_${System.currentTimeMillis()}"
            entries.add("$uniqueId:0")

            val forgeQuests = QuestSystem.serialize(
                QuestSystem.addProgress(QuestSystem.deserialize(state.activeQuestsJson), QuestType.FORGE, 1)
            )
            dao.saveGameState(state.copy(
                inventoryItems = buildInventory(entries),
                forgeSlot1 = "",
                forgeSlot2 = "",
                itemsCollected = state.itemsCollected + 1,
                totalItemsMerged = state.totalItemsMerged + 1,
                activeQuestsJson = forgeQuests
            ))

            addLog(
                "Forge: created ${resultItem.name_en}!",
                "Кузница: создан ${resultItem.name_ru}!"
            )
            ForgeResult.Success(resultItem)
        }
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

    fun calculateEnchantChance(luck: Float, streak: Int, achBonus: Float = 0f): Float {
        return minOf(90f, 7f + (luck * 3f) + (streak * 0.07f) + achBonus)
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
        return saveMutex.withLock {
            val state = getGameState()
            val entries = parseInventory(state.inventoryItems)

            val idx = entries.indexOfFirst { getUniqueId(it) == itemId }
            if (idx < 0) return@withLock EnchantResult.FAILED

            val entry = entries[idx]
            val currentLevel = getEnchantLevelFromEntry(entry)
            if (currentLevel >= 9) return@withLock EnchantResult.MAX_LEVEL

            val baseId = getBaseId(entry)
            val item = ItemUtils.getItemById(baseId) ?: return@withLock EnchantResult.FAILED
            val cost = calculateEnchantCost(item.rarity, currentLevel)

            if (state.teeth < cost) return@withLock EnchantResult.NOT_ENOUGH_TEETH

            val achBonuses = AchievementSystem.getActiveBonuses(state.activeAchievementIds)
            val equippedForEnchant = getEquippedItemObjects(state)
            val setBonuses = ItemUtils.getSetBonuses(equippedForEnchant)
            val enchantBonus = (achBonuses.enchantFlat + setBonuses.enchantPercent) * 100f
            val activeEvent = EventUtils.getEventById(state.activeEventId)
            val eventBonus = if (activeEvent?.type == EventType.ENCHANTERS_LUCK &&
                EventUtils.isEventActive(state.eventEndTime)) 5f else 0f
            val chance = calculateEnchantChance(state.baseLuck, state.currentStreak, enchantBonus + eventBonus)
            val success = kotlin.random.Random.nextFloat() * 100f < chance
            val newTeeth = state.teeth - cost

            val today = DateUtils.getTodayString()
            return@withLock if (success) {
                val newLevel = currentLevel + 1
                entries[idx] = "${getUniqueId(entry)}:$newLevel"
                var enchantQuestsList = QuestSystem.deserialize(state.activeQuestsJson)
                enchantQuestsList = QuestSystem.addProgress(enchantQuestsList, QuestType.ENCHANT, 1)
                enchantQuestsList = QuestSystem.addProgress(enchantQuestsList, QuestType.TEETH_SPENT, cost)
                val enchantQuests = QuestSystem.serialize(enchantQuestsList)
                // Достижение ach_master_enchant — вещь заточена до +9
                var afterEnchant = state.copy(
                    inventoryItems = buildInventory(entries),
                    teeth = newTeeth,
                    totalTeethSpent = state.totalTeethSpent + cost,
                    totalEnchantmentsSuccess = state.totalEnchantmentsSuccess + 1,
                    activeQuestsJson = enchantQuests
                )
                if (newLevel >= 9) {
                    val ul = AchievementSystem.getUnlocked(afterEnchant.achievementsJson).toMutableList()
                    if (ul.none { it.defId == "ach_master_enchant" }) {
                        ul.add(com.pushupRPG.app.utils.UnlockedAchievement("ach_master_enchant", today))
                        afterEnchant = afterEnchant.copy(achievementsJson = AchievementSystem.serializeUnlocked(ul))
                    }
                }
                afterEnchant = AchievementSystem.checkAndUnlock(afterEnchant, today)
                dao.saveGameState(afterEnchant)
                addLog(
                    "⚡ ${item.name_en} successfully enchanted to +$newLevel!",
                    "⚡ ${item.name_ru} успешно заточен до +$newLevel!"
                )
                EnchantResult.SUCCESS
            } else {
                var failQuests = QuestSystem.deserialize(state.activeQuestsJson)
                failQuests = QuestSystem.addProgress(failQuests, QuestType.TEETH_SPENT, cost)
                dao.saveGameState(state.copy(
                    teeth = newTeeth,
                    totalTeethSpent = state.totalTeethSpent + cost,
                    activeQuestsJson = QuestSystem.serialize(failQuests)
                ))
                addLog(
                    "💔 Enchanting ${item.name_en} failed...",
                    "💔 Заточка ${item.name_ru} не удалась..."
                )
                EnchantResult.FAILED
            }
        }
    }

    suspend fun setActiveAchievements(ids: List<String>) {
        val state = getGameState()
        dao.saveGameState(state.copy(activeAchievementIds = ids.take(3).joinToString(",")))
    }

    // ==================== КВЕСТЫ ====================

    suspend fun checkAndRefreshQuests() {
        val state = getGameState()
        val today = DateUtils.getTodayString()
        var quests = QuestSystem.deserialize(state.activeQuestsJson)
        var refreshDate = state.lastQuestRefreshDate
        var changed = false

        // Ежедневный сброс квестов
        if (refreshDate != today) {
            val weeklyQuest = quests.firstOrNull { QuestSystem.getDefById(it.defId)?.isWeekly == true }
            val currentWeek = QuestSystem.getIsoWeekNumber(today)
            val lastWeek = QuestSystem.getIsoWeekNumber(refreshDate)
            val newWeekly = if (weeklyQuest == null || currentWeek != lastWeek) {
                QuestSystem.rollWeeklyQuest()
            } else weeklyQuest
            quests = QuestSystem.rollDailyQuests() + newWeekly
            refreshDate = today
            changed = true
        }

        if (changed) {
            dao.saveGameState(state.copy(
                activeQuestsJson = QuestSystem.serialize(quests),
                lastQuestRefreshDate = refreshDate
            ))
        }
    }

    suspend fun claimQuestReward(defId: String): Boolean {
        return saveMutex.withLock {
            val state = getGameState()
            val quests = QuestSystem.deserialize(state.activeQuestsJson).toMutableList()
            val idx = quests.indexOfFirst { it.defId == defId && it.isCompleted && !it.claimed }
            if (idx < 0) return@withLock false

            val def = QuestSystem.getDefById(defId) ?: return@withLock false
            quests[idx] = quests[idx].copy(claimed = true)

            var newState = state.copy(
                activeQuestsJson = QuestSystem.serialize(quests),
                teeth = state.teeth + def.rewardTeeth,
                totalTeethEarned = state.totalTeethEarned + def.rewardTeeth
            )

            if (def.rewardItemRarity != null) {
                val allItems = ItemUtils.loadItems(context)
                val eligible = allItems.filter { it.rarity == def.rewardItemRarity }
                val item = eligible.randomOrNull()
                if (item != null) {
                    val uniqueId = "${item.id}_${System.currentTimeMillis()}"
                    val entries = parseInventory(newState.inventoryItems)
                    entries.add("$uniqueId:0")
                    newState = newState.copy(
                        inventoryItems = buildInventory(entries),
                        itemsCollected = newState.itemsCollected + 1
                    )
                    addLog("🏆 Quest reward: ${item.name_en}!", "🏆 Награда квеста: ${item.name_ru}!")
                }
            }
            if (def.rewardTeeth > 0) {
                addLog("🏆 Quest reward: +${def.rewardTeeth} 🦷", "🏆 Награда квеста: +${def.rewardTeeth} 🦷")
            }

            dao.saveGameState(newState)
            true
        }
    }

    // ==================== ЕЖЕДНЕВНАЯ НАГРАДА ====================

    suspend fun claimDailyReward(): DailyRewardUtils.DailyReward? {
        return try {
            val state = getGameState()
            val today = DateUtils.getTodayString()
            if (!DailyRewardUtils.needsReward(state.lastDailyRewardDate, today)) return null

            val reward = DailyRewardUtils.getRewardForDay(state.dailyRewardDay)
            var newState = state.copy(
                lastDailyRewardDate = today,
                dailyRewardDay = DailyRewardUtils.nextDay(state.dailyRewardDay),
                teeth = state.teeth + reward.teeth,
                totalTeethEarned = state.totalTeethEarned + reward.teeth
            )

            if (reward.isCloverBox) {
                val allItems = ItemUtils.loadItems(context)
                val targetRarity = ShopUtils.rollCloverBoxRarity()
                val item = allItems.filter { it.rarity == targetRarity }.randomOrNull()
                if (item != null) {
                    val uniqueId = "${item.id}_${System.currentTimeMillis()}"
                    val entries = parseInventory(newState.inventoryItems)
                    entries.add("$uniqueId:0")
                    newState = newState.copy(
                        inventoryItems = buildInventory(entries),
                        itemsCollected = newState.itemsCollected + 1
                    )
                }
            } else if (reward.itemRarity != null) {
                val allItems = ItemUtils.loadItems(context)
                val item = allItems.filter { it.rarity == reward.itemRarity }.randomOrNull()
                if (item != null) {
                    val uniqueId = "${item.id}_${System.currentTimeMillis()}"
                    val entries = parseInventory(newState.inventoryItems)
                    entries.add("$uniqueId:0")
                    newState = newState.copy(
                        inventoryItems = buildInventory(entries),
                        itemsCollected = newState.itemsCollected + 1
                    )
                }
            }

            addLog("🎁 Daily reward claimed: Day ${reward.day}", "🎁 Ежедневная награда: День ${reward.day}")
            dao.saveGameState(newState)
            reward
        } catch (e: Exception) {
            android.util.Log.e("GameRepo", "claimDailyReward failed", e)
            null
        }
    }

    // ==================== DEBUG / TEST HELPERS ====================

    /**
     * Добавляет 10 тестовых вещей (по 2 каждого слота) и 100000 зубов.
     * Использовать ТОЛЬКО для тестирования!
     */
    suspend fun addDebugItemsForTest() {
        val allItems = ItemUtils.loadItems(context)
        val slots = listOf("head", "necklace", "weapon", "pants", "boots")
        val entries = mutableListOf<String>()
        for (slot in slots) {
            val candidates = allItems.filter { it.slot == slot }
            if (candidates.isEmpty()) continue
            repeat(2) { i ->
                val item = candidates[i % candidates.size]
                val uniqueId = "${item.id}_${System.currentTimeMillis() + entries.size}"
                entries.add("$uniqueId:0")
            }
        }
        val state = getGameState()
        val existing = parseInventory(state.inventoryItems)
        existing.addAll(entries)
        dao.saveGameState(state.copy(
            inventoryItems = buildInventory(existing),
            teeth = state.teeth + 100000
        ))
    }
}

// я удалил отсюда data class PeriodStats и enum class EnchantResult