package com.ninthbalcony.pushuprpg.data.repository

import android.content.Context
import com.ninthbalcony.pushuprpg.data.db.AppDatabase
import com.ninthbalcony.pushuprpg.data.db.GameStateEntity
import com.ninthbalcony.pushuprpg.data.db.LogEntryEntity
import com.ninthbalcony.pushuprpg.data.db.PushUpRecordEntity
import com.ninthbalcony.pushuprpg.data.db.entity.MaxPushUpsAttemptEntity
import com.ninthbalcony.pushuprpg.data.exception.CheatCooldownException
import com.ninthbalcony.pushuprpg.data.model.BattleChain
import com.ninthbalcony.pushuprpg.data.model.BattleHit
import com.ninthbalcony.pushuprpg.utils.DateUtils
import com.ninthbalcony.pushuprpg.utils.GameCalculations
import com.ninthbalcony.pushuprpg.utils.ItemUtils
import com.ninthbalcony.pushuprpg.managers.AntiCheatManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.ninthbalcony.pushuprpg.data.model.EventType
import com.ninthbalcony.pushuprpg.utils.EventUtils
import com.ninthbalcony.pushuprpg.utils.MonsterUtils
import com.ninthbalcony.pushuprpg.utils.ShopUtils
import com.ninthbalcony.pushuprpg.utils.SpinUtils
import com.ninthbalcony.pushuprpg.utils.SpinReward
import com.ninthbalcony.pushuprpg.utils.SpinResult
import com.ninthbalcony.pushuprpg.data.model.PeriodStats
import com.ninthbalcony.pushuprpg.data.model.EnchantResult
import com.ninthbalcony.pushuprpg.data.model.ForgeResult
import com.ninthbalcony.pushuprpg.utils.BossUtils
import com.ninthbalcony.pushuprpg.utils.DailyRewardUtils
import com.ninthbalcony.pushuprpg.utils.QuestSystem
import com.ninthbalcony.pushuprpg.utils.QuestType
import com.ninthbalcony.pushuprpg.utils.AchievementSystem
import com.ninthbalcony.pushuprpg.utils.AvatarSystem
import com.google.gson.Gson
import kotlin.math.roundToInt
import com.google.gson.reflect.TypeToken

class GameRepository(private val context: Context) : IGameRepository {

    private val db = AppDatabase.getDatabase(context)
    private val dao = db.pushUpDao()
    private val maxPushUpsDao = db.maxPushUpsDao()
    private val antiCheatManager = AntiCheatManager()
    private var playGamesManager: com.ninthbalcony.pushuprpg.managers.PlayGamesManager? = null
    private val saveMutex = Mutex()

    private val _battleChain = MutableSharedFlow<BattleChain>(extraBufferCapacity = 4)
    override val battleChain: SharedFlow<BattleChain> = _battleChain

    init {
        // Загружаем все предметы сразу — иначе getItemById() вернёт null до первого действия
        ItemUtils.loadItems(context)
    }

    override fun setPlayGamesManager(manager: com.ninthbalcony.pushuprpg.managers.PlayGamesManager) {
        playGamesManager = manager
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

    override fun getGameStateFlow(): Flow<GameStateEntity?> {
        return dao.getGameStateFlow()
    }

    override suspend fun getGameState(): GameStateEntity {
        return dao.getGameState() ?: createInitialGameState()
    }

    override suspend fun saveGameState(state: GameStateEntity) {
        saveMutex.withLock {
            // isFirstLaunch переходит только в одну сторону: true → false.
            // Если в БД уже false — сохраняем false независимо от того,
            // какое значение несёт stale-копия state (race condition protection).
            val existing = dao.getGameState()
            val toSave = if (existing?.isFirstLaunch == false) state.copy(isFirstLaunch = false) else state
            dao.saveGameState(toSave)
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
    override suspend fun addPushUps(count: Int): Int {
        val today = DateUtils.getTodayString()

        // Anti-cheat: кулдаун при быстром повторном сохранении (любое кол-во < 99)
        if (count in 1..98) {
            val remaining = antiCheatManager.checkGeneralCooldown(count)
            if (remaining > 0) {
                addLog("⚠️ Save too fast", "⚠️ Слишком быстрое сохранение")
                throw CheatCooldownException(remaining, antiCheatManager.generalAdType(count), 0)
            }
        }

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
        val didPrestige = newLevel >= 50
        val actualPrestige = if (didPrestige) state.prestigeLevel + 1 else state.prestigeLevel
        val actualXp = if (didPrestige) 0 else newTotalXp
        val actualLevel = if (didPrestige) 1 else newLevel
        val pointsPerLevel = GameCalculations.STAT_POINTS_PER_LEVEL * (1 + state.prestigeLevel)
        val prestigeBonus = if (didPrestige) 10 else 0
        val newStatPoints = if (leveledUp)
            state.unspentStatPoints + pointsPerLevel + prestigeBonus
        else state.unspentStatPoints

        val maxHp = GameCalculations.getMaxHp(newLevel, state.baseHealth, 0)

        // Базовое состояние после XP/уровня
        var workingState = state.copy(
            pushUpsToday = newPushUpsToday,
            lastResetDate = today,
            totalXp = actualXp,
            playerLevel = actualLevel,
            unspentStatPoints = newStatPoints,
            prestigeLevel = actualPrestige,
            totalPushUpsAllTime = state.totalPushUpsAllTime + count,
            currentStreak = newStreak,
            longestStreak = maxOf(state.longestStreak, newStreak),
            lastLoginDate = today,
            currentHp = if (wasRevived || didPrestige) maxHp else state.currentHp,
            isPlayerDead = false,
            bestSingleSession = maxOf(state.bestSingleSession, count)
        )

        // Level-up или Prestige: спавним нового монстра с полным HP
        if (leveledUp || didPrestige) {
            val spawnLevel = if (didPrestige) 1 else actualLevel
            val newMonster = MonsterUtils.rollNextMonster(spawnLevel)
            val prestigeMult = 1 + actualPrestige
            if (didPrestige) addLog("🏅 Prestige $actualPrestige! Monsters are now ${prestigeMult}x stronger.", "🏅 Prestige $actualPrestige! Монстры теперь в ${prestigeMult}x сильнее.")

            // Add 3 spin tokens for level up
            val spinBonus = 3
            workingState = workingState.copy(
                monsterName = newMonster.name,
                monsterLevel = newMonster.level,
                monsterImageRes = newMonster.imageRes,
                monsterMaxHp = newMonster.maxHp * prestigeMult,
                monsterCurrentHp = newMonster.maxHp * prestigeMult,
                monsterDamage = newMonster.damage * prestigeMult,
                spinTokens = workingState.spinTokens + spinBonus
            )
            if (leveledUp && !didPrestige) {
                addLog("⬆️ Level Up! +3 Spins", "⬆️ Уровень выше! +3 Вращения")
            }
        }

        // Боевая обработка отжиманий (пропускаем если только что воскресли).
        // Эмитим chain в SharedFlow ниже после успешного save — чтобы UI не показал
        // анимацию для прогресса, который не сохранился из-за ошибки.
        var combatHits: List<BattleHit> = emptyList()
        if (!wasRevived) {
            val (newState, hits) = processPushUpCombat(workingState, count)
            workingState = newState
            combatHits = hits
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
        workingState = AvatarSystem.checkAndUnlock(workingState)
        // Ночная / утренняя сессия — логика через час
        if (hour >= 23 || hour < 7) {
            val nightId = if (hour >= 23) "ach_night_shift" else "ach_early_bird"
            val unlocked = AchievementSystem.getUnlocked(workingState.achievementsJson)
            if (unlocked.none { it.defId == nightId }) {
                val newUnlocked = unlocked + com.ninthbalcony.pushuprpg.utils.UnlockedAchievement(nightId, today)
                workingState = workingState.copy(
                    achievementsJson = AchievementSystem.serializeUnlocked(newUnlocked)
                )
            }
        }

        // Play Games achievements
        val pushupIncrease = count
        if ((state.totalPushUpsAllTime + pushupIncrease) % 100 >= state.totalPushUpsAllTime % 100) {
            val milestones = (state.totalPushUpsAllTime + pushupIncrease) / 100 - state.totalPushUpsAllTime / 100
            if (milestones > 0) {
                playGamesManager?.incrementAchievementMasterPushups(milestones)
            }
        }
        if (workingState.teeth >= 5000 && state.teeth < 5000) {
            playGamesManager?.unlockAchievementRich()
        }

        try {
            dao.saveGameState(workingState)
            dao.insertPushUpRecord(PushUpRecordEntity(date = today, count = count))
        } catch (e: Exception) {
            addLog("❌ Error saving progress", "❌ Ошибка сохранения прогресса")
            throw e
        }

        antiCheatManager.recordGeneralSave()
        if (wasRevived) addLog("🌅 Hero was revived after push-ups!", "🌅 Герой воскрешён после отжиманий!")
        if (leveledUp) addLog("⬆️ Level Up! Now level $newLevel!", "⬆️ Повышение уровня! Теперь уровень $newLevel!")
        if (combatHits.isNotEmpty()) _battleChain.tryEmit(BattleChain(combatHits))
        return if (leveledUp) newLevel else 0
    }

    // ==================== БОЕВАЯ СИСТЕМА ====================

    /**
     * Серия N независимых ударов от Save отжиманий. Каждый удар — отдельный roll
     * на crit. При смерти монстра оставшиеся удары переносятся на следующего.
     * Контр-атаки монстра здесь нет — только в auto-tick (5 мин), чтобы Save'ы
     * были чистым "output spectacle".
     *
     * Возвращает финальный state и список хитов для UI-анимации.
     */
    private suspend fun processPushUpCombat(state: GameStateEntity, count: Int): Pair<GameStateEntity, List<BattleHit>> {
        val (equippedItems, enchantLevels) = getEquippedWithEnchant(state)
        val achBonuses = AchievementSystem.getActiveBonuses(state.activeAchievementIds)
        val setBonuses = ItemUtils.getSetBonuses(equippedItems)
        val totalStats = GameCalculations.calculateTotalStats(state, equippedItems, enchantLevels, achBonuses, setBonuses)

        val isBurst = GameCalculations.isBurstAttack(count)
        val burstMult = if (isBurst) GameCalculations.BURST_MULTIPLIER else 1

        val activeEvent = EventUtils.getEventById(state.activeEventId)
        val powerEventMult = if (activeEvent?.type == EventType.POWER_BONUS &&
            EventUtils.isEventActive(state.eventEndTime)) 1.5f else 1f

        val hits = mutableListOf<BattleHit>()
        var current = state
        var totalDmg = 0
        var critCount = 0
        var maxSingleHit = 0
        var monstersKilledInChain = 0

        repeat(count) {
            val isCrit = GameCalculations.isCriticalHit(totalStats.luck)
            val rawDmg = GameCalculations.calculatePlayerDamage(totalStats.power * burstMult, isCrit)
            val dmg = (rawDmg * powerEventMult).toInt()

            val newMonsterHp = (current.monsterCurrentHp - dmg).coerceAtLeast(0)
            val killed = newMonsterHp <= 0

            hits += BattleHit(
                damage = dmg,
                isCrit = isCrit,
                monsterName = current.monsterName,
                monsterMaxHp = current.monsterMaxHp,
                monsterImageRes = current.monsterImageRes,
                monsterHpAfter = newMonsterHp,
                killed = killed
            )

            totalDmg += dmg
            if (isCrit) critCount++
            if (dmg > maxSingleHit) maxSingleHit = dmg

            current = if (killed) {
                monstersKilledInChain++
                handleMonsterKill(current.copy(monsterCurrentHp = 0))
            } else {
                current.copy(monsterCurrentHp = newMonsterHp)
            }
        }

        val burstText = if (isBurst) " 💥 BURST!" else ""
        val critText = if (critCount > 0) " ($critCount×CRIT)" else ""
        val killedText = if (monstersKilledInChain > 0) " 💀×$monstersKilledInChain" else ""
        addLog(
            "💪 $count push-ups → -$totalDmg total!$burstText$critText$killedText",
            "💪 $count отжиманий → -$totalDmg урона!$burstText$critText$killedText"
        )

        return current.copy(
            totalDamageDealt = state.totalDamageDealt + totalDmg,
            highestDamage = maxOf(state.highestDamage, maxSingleHit),
            totalCriticalHits = state.totalCriticalHits + critCount
        ) to hits
    }

    /** Авто-бой: один тик каждые 5 минут */
    override suspend fun processBattleTick() {
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

            val ticks = (elapsed / tickMs).toInt().coerceAtMost(288)
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
        val achBonuses = AchievementSystem.getActiveBonuses(state.activeAchievementIds)
        val setBonuses = ItemUtils.getSetBonuses(equippedItems)
        val totalStats = GameCalculations.calculateTotalStats(state, equippedItems, enchantLevels, achBonuses, setBonuses)

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
        val maxHp = totalStats.health
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

    private fun addItemToLog(state: GameStateEntity, uniqueId: String): String {
        val logList: MutableList<String> = try {
            Gson().fromJson<List<String>>(state.itemLogJson,
                object : TypeToken<List<String>>() {}.type)?.toMutableList() ?: mutableListOf()
        } catch (e: Exception) { mutableListOf() }
        logList.add(0, uniqueId)
        if (logList.size > 50) logList.removeAt(logList.lastIndex)
        return Gson().toJson(logList)
    }

    // Минимум 5 убийств, шанс появления начинается с 10-го (+5% за каждое)
    private fun shouldSpawnGoblin(killsSince: Int): Boolean {
        if (killsSince < 10) return false
        val chance = (killsSince - 9) * 5
        return kotlin.random.Random.nextInt(100) < chance.coerceAtMost(100)
    }

    /** Обрабатывает смерть монстра: дроп лута/зубов, спавн нового */
    private suspend fun handleMonsterKill(state: GameStateEntity): GameStateEntity {
        val baseTeeth = GameCalculations.getTeethFromMonster(state.monsterLevel)
        val bossMult = if (state.isCurrentBoss) kotlin.random.Random.nextInt(2, 5) else 1
        val teethFromKill = baseTeeth * bossMult
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
                newItemLog = addItemToLog(state, uniqueId)
                val legTag = if (dropped.rarity == "legendary") " ★LEGENDARY★" else ""
                addLog(
                    "🎁 ${state.monsterName} dropped ${dropped.name_en}!$legTag",
                    "🎁 ${state.monsterName} дроп: ${dropped.name_ru}!$legTag"
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

        // Спавн следующего монстра / босса / гоблина
        val newKillsSinceGoblin = state.killsSinceLastGoblin + 1
        val spawnBoss = !wasCurrentBoss && BossUtils.shouldSpawnBoss(newKillCount)
        val spawnGoblin = !wasCurrentBoss && !spawnBoss && shouldSpawnGoblin(newKillsSinceGoblin)
        val next = when {
            spawnBoss -> BossUtils.getBossForKillCount(newKillCount).also {
                addLog("⚠️ BOSS appeared: ${it.name}!", "⚠️ БОСС появился: ${it.nameRu}!")
            }
            else -> MonsterUtils.rollNextMonster(state.playerLevel)
        }
        if (spawnGoblin) {
            addLog("🟡 A Golden Goblin appeared! Tap Punch as fast as you can!",
                   "🟡 Появился Золотой Гоблин! Жми Punch как можно быстрее!")
        }

        val prestigeMult = 1 + state.prestigeLevel
        val today = DateUtils.getTodayString()
        var updated = state.copy(
            monsterName = if (spawnGoblin) "Golden Goblin" else next.name,
            monsterLevel = if (spawnGoblin) state.playerLevel else next.level,
            monsterImageRes = if (spawnGoblin) "monster_goblin_gold" else next.imageRes,
            monsterMaxHp = if (spawnGoblin) 10_000_000 else next.maxHp * prestigeMult,
            monsterCurrentHp = if (spawnGoblin) 10_000_000 else next.maxHp * prestigeMult,
            monsterDamage = if (spawnGoblin) 1 else next.damage * prestigeMult,
            isCurrentBoss = spawnBoss,
            currentBossId = if (spawnBoss) next.id else 0,
            isGoldenGoblinActive = spawnGoblin,
            goldenGoblinEndTime = if (spawnGoblin) System.currentTimeMillis() + 60_000L else state.goldenGoblinEndTime,
            goldenGoblinPunchCount = if (spawnGoblin) 0 else state.goldenGoblinPunchCount,
            killsSinceLastGoblin = if (spawnGoblin) 0 else newKillsSinceGoblin,
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
            unlocked.add(com.ninthbalcony.pushuprpg.utils.UnlockedAchievement("ach_legendary_catch", today))
        if ((droppedRarity == "epic" || droppedRarity == "legendary") && unlocked.none { it.defId == "ach_epic_catch" })
            unlocked.add(com.ninthbalcony.pushuprpg.utils.UnlockedAchievement("ach_epic_catch", today))
        updated = updated.copy(achievementsJson = AchievementSystem.serializeUnlocked(unlocked))

        // Play Games achievements
        if (state.monstersKilled == 0) {
            playGamesManager?.unlockAchievementFirstFight()
        }
        if (droppedRarity == "epic") {
            playGamesManager?.unlockAchievementEpicCatch()
        }
        if (droppedRarity == "legendary") {
            playGamesManager?.unlockAchievementLegendaryCatch()
        }

        val afterAchievements = AchievementSystem.checkAndUnlock(updated, today)
        return AvatarSystem.checkAndUnlock(afterAchievements)
    }

    /** Возвращает экипированные предметы как объекты Item */
    private fun getEquippedItemObjects(state: GameStateEntity): List<com.ninthbalcony.pushuprpg.data.model.Item> {
        return listOf(
            state.equippedHead, state.equippedNecklace, state.equippedWeapon1,
            state.equippedWeapon2, state.equippedPants, state.equippedBoots
        ).filter { it.isNotEmpty() }.mapNotNull { entry -> ItemUtils.getItemById(entry) }
    }

    private fun getEquippedWithEnchant(state: GameStateEntity): Pair<List<com.ninthbalcony.pushuprpg.data.model.Item>, List<Int>> {
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

    override suspend fun updateStreakOnLogin() {
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

    override suspend fun getStatsForPeriod(): PeriodStats {
        return PeriodStats(
            lastWeek = dao.getPushUpsSince(DateUtils.getDateStringDaysAgo(7)) ?: 0,
            lastMonth = dao.getPushUpsSince(DateUtils.getDateStringMonthsAgo(1)) ?: 0,
            lastQuarter = dao.getPushUpsSince(DateUtils.getDateStringMonthsAgo(3)) ?: 0,
            lastYear = dao.getPushUpsSince(DateUtils.getDateStringYearsAgo(1)) ?: 0,
            total = dao.getTotalPushUps() ?: 0
        )
    }

    override suspend fun getLast7DaysStats() = dao.getLast7DaysStats()

    override suspend fun getLast12MonthsStats() = dao.getLast12MonthsStats()

    // ==================== ЛОГИ ====================

    override fun getRecentLogsFlow(): Flow<List<LogEntryEntity>> = dao.getRecentLogs()
    override fun getAllLogsFlow(): Flow<List<LogEntryEntity>> = dao.getAllLogs()

    override suspend fun addLog(message: String, messageRu: String) {
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

    override suspend fun equipItem(itemId: String, slot: String) {
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

        // Play Games: Full Wardrobe achievement
        if (updatedState.equippedHead.isNotEmpty() &&
            updatedState.equippedNecklace.isNotEmpty() &&
            updatedState.equippedWeapon1.isNotEmpty() &&
            updatedState.equippedWeapon2.isNotEmpty() &&
            updatedState.equippedPants.isNotEmpty() &&
            updatedState.equippedBoots.isNotEmpty()) {
            playGamesManager?.unlockAchievementFullWardrobe()
        }

        dao.saveGameState(AvatarSystem.checkAndUnlock(updatedState))
    }

    override suspend fun unequipItem(slot: String) {
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

    override suspend fun sellItem(itemId: String) {
        val state = getGameState()
        val entries = parseInventory(state.inventoryItems)

        val idx = entries.indexOfFirst { getUniqueId(it) == itemId }
        if (idx < 0) return

        val entry = entries[idx]
        val baseId = getBaseId(entry)
        val item = ItemUtils.getItemById(baseId)
        val rarity = item?.rarity ?: "common"
        val teethGained = GameCalculations.getTeethFromSell(rarity)
        val luckGained = GameCalculations.getLuckFromSell(rarity)

        entries.removeAt(idx)

        dao.saveGameState(state.copy(
            inventoryItems = buildInventory(entries),
            baseLuck = state.baseLuck + luckGained,
            teeth = state.teeth + teethGained,
            totalTeethEarned = state.totalTeethEarned + teethGained
        ))

        val itemName = item?.name_en ?: itemId
        val itemNameRu = item?.name_ru ?: itemId
        if (luckGained > 0f) {
            addLog(
                "Sold $itemName. +$teethGained 🦷 +Luck",
                "Продан $itemNameRu. +$teethGained 🦷 +Удача"
            )
        } else {
            addLog(
                "Sold $itemName. +$teethGained 🦷",
                "Продан $itemNameRu. +$teethGained 🦷"
            )
        }
    }

    // ==================== СБРОС ====================

    override suspend fun resetAllProgress() {
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

    override suspend fun spendStatPoint(stat: String) {
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

    override suspend fun useFreePoints(): Boolean {
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

    override suspend fun checkAndUpdateEvent(): GameStateEntity {
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

    override suspend fun getOrRefreshShop(): List<com.ninthbalcony.pushuprpg.data.model.Item> {
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

    override suspend fun buyShopItem(itemId: String): Boolean {
        return saveMutex.withLock {
            val state = getGameState()
            val item = ItemUtils.getItemById(itemId) ?: return@withLock false
            val price = ShopUtils.getBuyPrice(item.rarity)
            if (state.teeth < price) return@withLock false

            val shopList = state.shopItems.split(",").filter { it.isNotEmpty() }.toMutableList()
            val removeIndex = shopList.indexOf(itemId)
            if (removeIndex >= 0) shopList.removeAt(removeIndex)
            val newShopItems = shopList.joinToString(",")

            val uniqueId = "${itemId}_${System.currentTimeMillis()}"
            val entries = parseInventory(state.inventoryItems)
            entries.add("$uniqueId:0")

            var buyQuests = QuestSystem.deserialize(state.activeQuestsJson)
            buyQuests = QuestSystem.addProgress(buyQuests, QuestType.BUY_ITEM, 1)
            buyQuests = QuestSystem.addProgress(buyQuests, QuestType.TEETH_SPENT, price)
            val newItemLog = addItemToLog(state, uniqueId)
            dao.saveGameState(state.copy(
                teeth = state.teeth - price,
                shopItems = newShopItems,
                inventoryItems = buildInventory(entries),
                itemsCollected = state.itemsCollected + 1,
                totalTeethSpent = state.totalTeethSpent + price,
                totalShopPurchases = state.totalShopPurchases + 1,
                itemLogJson = newItemLog,
                activeQuestsJson = QuestSystem.serialize(buyQuests)
            ))

            addLog(
                "Bought ${item.name_en} for $price 🦷",
                "Куплено ${item.name_ru} за $price 🦷"
            )
            true
        }
    }

    override suspend fun addTeeth(amount: Int) {
        saveMutex.withLock {
            val state = getGameState()
            dao.saveGameState(state.copy(
                teeth = state.teeth + amount,
                teethFromAds = state.teethFromAds + amount,
                adShopViewCount = state.adShopViewCount + 1,
                adShopLastViewTime = System.currentTimeMillis()
            ))
            addLog("🎬 Ad reward: +$amount 🦷", "🎬 Награда за рекламу: +$amount 🦷")
        }
    }

    override suspend fun rerollShop(): Boolean {
        val state = getGameState()
        val now = System.currentTimeMillis()
        val resetIntervalMs = 5L * 60 * 1000
        val currentCount = if (now - state.shopRerollResetTime >= resetIntervalMs) 0 else state.shopRerollCount
        val cost = (currentCount + 1) * 3
        if (state.teeth < cost) return false

        val allItems = ItemUtils.loadItems(context)
        val baseItems = ShopUtils.generateShopItems(allItems).toMutableList()
        val activeEvent = EventUtils.getEventById(state.activeEventId)
        if (activeEvent?.type == EventType.ENCHANTERS_LUCK &&
            EventUtils.isEventActive(state.eventEndTime) &&
            kotlin.random.Random.nextFloat() < 0.03f) {
            allItems.filter { it.rarity == "epic" }.randomOrNull()?.let { baseItems.add(it) }
        }
        val newItemsStr = ShopUtils.shopItemsToString(baseItems)
        var rerollQuests = QuestSystem.deserialize(state.activeQuestsJson)
        rerollQuests = QuestSystem.addProgress(rerollQuests, QuestType.TEETH_SPENT, cost)
        dao.saveGameState(state.copy(
            shopItems = newItemsStr,
            shopLastRefresh = now,
            teeth = state.teeth - cost,
            totalTeethSpent = state.totalTeethSpent + cost,
            shopRerollCount = currentCount + 1,
            shopRerollResetTime = if (currentCount == 0) now else state.shopRerollResetTime,
            activeQuestsJson = QuestSystem.serialize(rerollQuests)
        ))
        return true
    }

    // ==================== КУЗНИЦА ====================

    override suspend fun setForgeSlot(slot: Int, itemId: String) {
        val state = getGameState()
        val updatedState = if (slot == 1) {
            state.copy(forgeSlot1 = itemId)
        } else {
            state.copy(forgeSlot2 = itemId)
        }
        dao.saveGameState(updatedState)
    }

    /** Вставляет случайные вещи наименьшей редкости (кроме epic/legendary) в пустые слоты Forge */
    override suspend fun recycleToForgeSlots() {
        val state = getGameState()
        val entries = parseInventory(state.inventoryItems)
        val occupied = setOf(state.forgeSlot1, state.forgeSlot2).filter { it.isNotEmpty() }
        val rarityPriority = listOf("common", "uncommon", "rare")

        // Записи инвентаря по редкости, исключая уже занятые слоты и epic/legendary
        val eligible = entries.filter { entry ->
            val uniqueId = getUniqueId(entry)
            if (uniqueId in occupied) return@filter false
            val item = ItemUtils.getItemById(getBaseId(entry)) ?: return@filter false
            item.rarity in rarityPriority
        }
        if (eligible.isEmpty()) return

        val lowestRarity = rarityPriority.firstOrNull { rarity ->
            eligible.any { entry ->
                ItemUtils.getItemById(getBaseId(entry))?.rarity == rarity
            }
        } ?: return

        val pool = eligible.filter { entry ->
            ItemUtils.getItemById(getBaseId(entry))?.rarity == lowestRarity
        }.shuffled()

        var poolIdx = 0
        var newSlot1 = state.forgeSlot1
        var newSlot2 = state.forgeSlot2

        if (newSlot1.isEmpty() && poolIdx < pool.size) {
            newSlot1 = getUniqueId(pool[poolIdx++])
        }
        if (newSlot2.isEmpty() && poolIdx < pool.size) {
            newSlot2 = getUniqueId(pool[poolIdx])
        }

        dao.saveGameState(state.copy(forgeSlot1 = newSlot1, forgeSlot2 = newSlot2))
    }

    override suspend fun mergeItems(): ForgeResult {
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
                    totalMergeAttempts = state.totalMergeAttempts + 1,
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
            val newItemLog = addItemToLog(state, uniqueId)
            dao.saveGameState(state.copy(
                inventoryItems = buildInventory(entries),
                forgeSlot1 = "",
                forgeSlot2 = "",
                itemsCollected = state.itemsCollected + 1,
                totalItemsMerged = state.totalItemsMerged + 1,
                totalMergeAttempts = state.totalMergeAttempts + 1,
                itemLogJson = newItemLog,
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

    override suspend fun checkAndResetDaily() {
        val state = getGameState()
        val today = DateUtils.getTodayString()
        if (state.lastDailyReset != today) {
            dao.saveGameState(state.copy(
                cloverBoxUsedToday = 0,
                freePointsUsedToday = 0,
                adShopViewCount = 0,

                // ===== Daily Spin Reset =====
                dailySpinUsedToday = 0,
                dailySpinAdViewsToday = 0,
                lastDailySpinReset = today,
                spinTokens = state.spinTokens + 1,  // +1 бесплатный спин каждый день

                lastDailyReset = today
            ))
        }
    }

    // ==================== HOURLY SPIN ACCUMULATION ====================

    /** Начисляет +1 спин за каждые 3 часа отсутствия (макс 5). Возвращает количество начисленных спинов. */
    override suspend fun checkAndGrantHourlySpins(): Int {
        return saveMutex.withLock {
            val state = getGameState()
            val now = System.currentTimeMillis()
            val threeHoursMs = 3L * 60L * 60L * 1000L
            val lastGrant = if (state.lastHourlySpinGrantTime == 0L) now else state.lastHourlySpinGrantTime
            val elapsedMs = now - lastGrant
            val blocks = (elapsedMs / threeHoursMs).toInt().coerceIn(0, 5)
            if (blocks > 0) {
                dao.saveGameState(state.copy(
                    spinTokens = state.spinTokens + blocks,
                    lastHourlySpinGrantTime = lastGrant + blocks * threeHoursMs
                ))
                addLog("+$blocks hourly spin(s)", "+$blocks часовой(-ых) спин(-ов)")
            }
            blocks
        }
    }

    // ==================== DAILY SPIN ====================

    /** Тратит 1 спин-токен, генерирует награду и сохраняет её в инвентарь */
    override suspend fun performDailySpin(): SpinResult? {
        return saveMutex.withLock {
            val state = getGameState()
            if (state.spinTokens <= 0) {
                addLog("❌ No spins available", "❌ Спинов больше нет")
                return@withLock null
            }

            val reward = SpinUtils.generateSpinResult()
            var updatedState = state.copy(spinTokens = state.spinTokens - 1)
            var newItemLog = state.itemLogJson
            val wonItemIds = mutableListOf<String>()

            when (reward.type) {
                "clover_box" -> {
                    val allItems = ItemUtils.loadItems(context)
                    val epicItem = allItems.filter { it.rarity == "epic" }.randomOrNull()
                    if (epicItem != null) {
                        val uid = "${epicItem.id}_${System.currentTimeMillis()}"
                        val entries = parseInventory(updatedState.inventoryItems)
                        entries.add("$uid:0")
                        newItemLog = addItemToLog(updatedState.copy(itemLogJson = newItemLog), uid)
                        updatedState = updatedState.copy(
                            inventoryItems = buildInventory(entries),
                            itemsCollected = updatedState.itemsCollected + 1,
                            itemsFromSpin = updatedState.itemsFromSpin + 1
                        )
                        wonItemIds.add(epicItem.id)
                        addLog("🎁 Won: ${epicItem.name_en} (Epic)!", "🎁 Выиграл: ${epicItem.name_ru} (Epic)!")
                    }
                }
                "boss_cube" -> {
                    val allItems = ItemUtils.loadItems(context)
                    val legendaryItem = allItems.filter { it.rarity == "legendary" }.randomOrNull()
                    if (legendaryItem != null) {
                        val uid = "${legendaryItem.id}_${System.currentTimeMillis()}"
                        val entries = parseInventory(updatedState.inventoryItems)
                        entries.add("$uid:0")
                        newItemLog = addItemToLog(updatedState.copy(itemLogJson = newItemLog), uid)
                        updatedState = updatedState.copy(
                            inventoryItems = buildInventory(entries),
                            itemsCollected = updatedState.itemsCollected + 1,
                            itemsFromSpin = updatedState.itemsFromSpin + 1
                        )
                        wonItemIds.add(legendaryItem.id)
                        addLog("🎁 Won: ${legendaryItem.name_en} (Legendary)!", "🎁 Выиграл: ${legendaryItem.name_ru} (Legendary)!")
                    } else {
                        // Fallback: добавляем boss_cube как предмет
                        val uid = "boss_cube_${System.currentTimeMillis()}"
                        val entries = parseInventory(updatedState.inventoryItems)
                        entries.add("$uid:0")
                        newItemLog = addItemToLog(updatedState.copy(itemLogJson = newItemLog), uid)
                        updatedState = updatedState.copy(
                            inventoryItems = buildInventory(entries),
                            itemsCollected = updatedState.itemsCollected + 1,
                            itemsFromSpin = updatedState.itemsFromSpin + 1
                        )
                        wonItemIds.add("boss_cube")
                        addLog("🎁 Won: Boss Cube (Legendary)!", "🎁 Выиграл: Boss Cube (Legendary)!")
                    }
                }
                "rare_spin", "uncommon_spin", "common_spin" -> {
                    val allItems = ItemUtils.loadItems(context)
                    val rarity = when (reward.type) {
                        "rare_spin"     -> "rare"
                        "uncommon_spin" -> "uncommon"
                        else            -> "common"
                    }
                    val item = allItems.filter { it.rarity == rarity }.randomOrNull()
                    if (item != null) {
                        val uid = "${item.id}_${System.currentTimeMillis()}"
                        val entries = parseInventory(updatedState.inventoryItems)
                        entries.add("$uid:0")
                        newItemLog = addItemToLog(updatedState.copy(itemLogJson = newItemLog), uid)
                        updatedState = updatedState.copy(
                            inventoryItems = buildInventory(entries),
                            itemsCollected = updatedState.itemsCollected + 1,
                            itemsFromSpin = updatedState.itemsFromSpin + 1
                        )
                        wonItemIds.add(item.id)
                        addLog("🎁 Won: ${item.name_en} (${item.rarity})!", "🎁 Выиграл: ${item.name_ru} (${item.rarity})!")
                    }
                }
                "teeth" -> {
                    updatedState = updatedState.copy(
                        teeth = updatedState.teeth + reward.amount,
                        totalTeethEarned = updatedState.totalTeethEarned + reward.amount,
                        teethFromSpin = updatedState.teethFromSpin + reward.amount
                    )
                    addLog("🎁 Won: ${reward.amount} 🦷", "🎁 Выиграл: ${reward.amount} 🦷")
                }
            }

            dao.saveGameState(updatedState.copy(itemLogJson = newItemLog))
            SpinResult(reward = reward, wonItemIds = wonItemIds)
        }
    }

    /** Смотрит рекламу → добавляет 1 спин-токен (не запускает спин) */
    override suspend fun addSpinFromAd(): Boolean {
        return saveMutex.withLock {
            val state = getGameState()
            if (!SpinUtils.canWatchAd(state)) return@withLock false
            dao.saveGameState(state.copy(
                dailySpinAdViewsToday = state.dailySpinAdViewsToday + 1,
                spinTokens = state.spinTokens + 1
            ))
            addLog("🎬 Watched ad: +1 Spin token", "🎬 Реклама просмотрена: +1 Спин")
            true
        }
    }

    /** Возвращает количество доступных спинов (= spinTokens) */
    override suspend fun getAvailableSpins(): Int = getGameState().spinTokens

    override suspend fun useCloverBox(): com.ninthbalcony.pushuprpg.data.model.Item? {
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

        val newItemLog = addItemToLog(state, uniqueId)
        dao.saveGameState(state.copy(
            inventoryItems = buildInventory(entries),
            cloverBoxUsedToday = state.cloverBoxUsedToday + 1,
            itemsCollected = state.itemsCollected + 1,
            itemLogJson = newItemLog
        ))

        addLog(
            "Clover Box: got ${item.name_en}!",
            "Клеверная коробка: получен ${item.name_ru}!"
        )
        return item
    }

    // ==================== ЗАТОЧКА ====================

    override fun calculateEnchantChance(luck: Float, streak: Int, achBonus: Float, isNight: Boolean): Float {
        val base = minOf(90f, 7f + (luck * 3f) + (streak * 0.07f) + achBonus)
        return if (isNight) base / 2f else base
    }

    override fun calculateEnchantCost(rarity: String, currentEnchantLevel: Int, isNight: Boolean): Int {
        val basePrice = when (rarity) {
            "common" -> 1
            "uncommon" -> 2
            "rare" -> 3
            "epic" -> 8
            "legendary" -> 14
            else -> 1
        }
        val base = basePrice * (currentEnchantLevel + 1)
        if (!isNight) return base
        val nightBase = base * 5
        if (currentEnchantLevel < 19) return nightBase
        val multiplier = 2.0f + (currentEnchantLevel - 19) * 0.5f
        return (nightBase * multiplier).roundToInt()
    }

    private val nightEnchantEventIds = setOf(6, 9, 10, 11)

    override suspend fun enchantItem(itemId: String): EnchantResult {
        return saveMutex.withLock {
            val state = getGameState()
            val entries = parseInventory(state.inventoryItems)

            val idx = entries.indexOfFirst { getUniqueId(it) == itemId }
            if (idx < 0) return@withLock EnchantResult.FAILED

            val entry = entries[idx]
            val currentLevel = getEnchantLevelFromEntry(entry)
            val isNight = state.activeEventId in nightEnchantEventIds &&
                EventUtils.isEventActive(state.eventEndTime)
            val maxEnchant = if (isNight) 25 else 9
            if (currentLevel >= maxEnchant) return@withLock EnchantResult.MAX_LEVEL

            val baseId = getBaseId(entry)
            val item = ItemUtils.getItemById(baseId) ?: return@withLock EnchantResult.FAILED
            val cost = calculateEnchantCost(item.rarity, currentLevel, isNight)

            if (state.teeth < cost) return@withLock EnchantResult.NOT_ENOUGH_TEETH

            val achBonuses = AchievementSystem.getActiveBonuses(state.activeAchievementIds)
            val equippedForEnchant = getEquippedItemObjects(state)
            val setBonuses = ItemUtils.getSetBonuses(equippedForEnchant)
            val enchantBonus = (achBonuses.enchantFlat + setBonuses.enchantPercent) * 100f
            val activeEvent = EventUtils.getEventById(state.activeEventId)
            val eventBonus = if (activeEvent?.type == EventType.ENCHANTERS_LUCK &&
                EventUtils.isEventActive(state.eventEndTime)) 5f else 0f
            val chance = calculateEnchantChance(state.baseLuck, state.currentStreak, enchantBonus + eventBonus, isNight)
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
                    totalEnchantAttempts = state.totalEnchantAttempts + 1,
                    activeQuestsJson = enchantQuests
                )
                if (newLevel >= 9) {
                    val ul = AchievementSystem.getUnlocked(afterEnchant.achievementsJson).toMutableList()
                    if (ul.none { it.defId == "ach_master_enchant" }) {
                        ul.add(com.ninthbalcony.pushuprpg.utils.UnlockedAchievement("ach_master_enchant", today))
                        afterEnchant = afterEnchant.copy(achievementsJson = AchievementSystem.serializeUnlocked(ul))
                    }
                }
                afterEnchant = AchievementSystem.checkAndUnlock(afterEnchant, today)
                dao.saveGameState(afterEnchant)
                addLog(
                    "⚡ ${item.name_en} successfully enchanted to +$newLevel!",
                    "⚡ ${item.name_ru} успешно заточен до +$newLevel!"
                )
                playGamesManager?.incrementAchievementAlchemist(1)
                EnchantResult.SUCCESS
            } else {
                var failQuests = QuestSystem.deserialize(state.activeQuestsJson)
                failQuests = QuestSystem.addProgress(failQuests, QuestType.TEETH_SPENT, cost)
                dao.saveGameState(state.copy(
                    teeth = newTeeth,
                    totalTeethSpent = state.totalTeethSpent + cost,
                    totalEnchantAttempts = state.totalEnchantAttempts + 1,
                    activeQuestsJson = QuestSystem.serialize(failQuests)
                ))
                addLog(
                    "💔 Enchanting ${item.name_en} failed...",
                    "💔 Заточка ${item.name_ru} не удалась..."
                )
                playGamesManager?.incrementAchievementFailedEnchants(1)
                EnchantResult.FAILED
            }
        }
    }

    override suspend fun setActiveAchievements(ids: List<String>) {
        val state = getGameState()
        dao.saveGameState(state.copy(activeAchievementIds = ids.take(3).joinToString(",")))
    }

    // ==================== КВЕСТЫ ====================

    override suspend fun checkAndRefreshQuests() {
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

    override suspend fun claimQuestReward(defId: String): Boolean {
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
                totalTeethEarned = state.totalTeethEarned + def.rewardTeeth,
                teethFromQuests = state.teethFromQuests + def.rewardTeeth
            )

            var newItemLog = state.itemLogJson
            if (def.rewardItemRarity != null) {
                val allItems = ItemUtils.loadItems(context)
                val eligible = allItems.filter { it.rarity == def.rewardItemRarity }
                val item = eligible.randomOrNull()
                if (item != null) {
                    val uniqueId = "${item.id}_${System.currentTimeMillis()}"
                    val entries = parseInventory(newState.inventoryItems)
                    entries.add("$uniqueId:0")
                    newItemLog = addItemToLog(newState.copy(itemLogJson = newItemLog), uniqueId)
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

            dao.saveGameState(newState.copy(itemLogJson = newItemLog))
            true
        }
    }

    override suspend fun adRerollDailyQuests(): Boolean {
        val state = getGameState()
        val today = DateUtils.getTodayString()
        if (state.lastAdQuestRerollDate == today) return false
        val quests = QuestSystem.deserialize(state.activeQuestsJson)
        val weekly = quests.firstOrNull { QuestSystem.getDefById(it.defId)?.isWeekly == true }
        val updated = QuestSystem.rollDailyQuests() + listOfNotNull(weekly)
        dao.saveGameState(state.copy(
            activeQuestsJson = QuestSystem.serialize(updated),
            lastAdQuestRerollDate = today
        ))
        return true
    }

    // ==================== ЕЖЕДНЕВНАЯ НАГРАДА ====================

    override suspend fun claimDailyReward(): DailyRewardUtils.DailyReward? {
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
            var newItemLog = state.itemLogJson

            if (reward.isCloverBox) {
                val allItems = ItemUtils.loadItems(context)
                val targetRarity = ShopUtils.rollCloverBoxRarity()
                val item = allItems.filter { it.rarity == targetRarity }.randomOrNull()
                if (item != null) {
                    val uniqueId = "${item.id}_${System.currentTimeMillis()}"
                    val entries = parseInventory(newState.inventoryItems)
                    entries.add("$uniqueId:0")
                    newItemLog = addItemToLog(newState.copy(itemLogJson = newItemLog), uniqueId)
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
                    newItemLog = addItemToLog(newState.copy(itemLogJson = newItemLog), uniqueId)
                    newState = newState.copy(
                        inventoryItems = buildInventory(entries),
                        itemsCollected = newState.itemsCollected + 1
                    )
                }
            }

            addLog("🎁 Daily reward claimed: Day ${reward.day}", "🎁 Ежедневная награда: День ${reward.day}")
            dao.saveGameState(newState.copy(itemLogJson = newItemLog))
            reward
        } catch (e: Exception) {
            android.util.Log.e("GameRepo", "claimDailyReward failed", e)
            null
        }
    }

    // ==================== DEBUG / TEST HELPERS ====================

    // ===== PUNCH =====
    override suspend fun performPunch(): Int = saveMutex.withLock {
        val state = getGameState()
        if (state.isPlayerDead) return@withLock 0
        if (state.isGoldenGoblinActive) return@withLock 0  // Только performGoblinPunch во время события

        val today = DateUtils.getTodayString()
        val usedToday = if (state.lastPunchDate == today) state.punchesUsedToday else 0
        if (usedToday >= 25) return@withLock -1

        val (equippedItems, enchantLevels) = getEquippedWithEnchant(state)
        val achBonuses = AchievementSystem.getActiveBonuses(state.activeAchievementIds)
        val setBonuses = ItemUtils.getSetBonuses(equippedItems)
        val totalStats = GameCalculations.calculateTotalStats(state, equippedItems, enchantLevels, achBonuses, setBonuses)

        val isCrit = GameCalculations.isCriticalHit(totalStats.luck)
        val dmg = GameCalculations.calculatePlayerDamage(totalStats.power, isCrit)
        val newMonsterHp = (state.monsterCurrentHp - dmg).coerceAtLeast(0)

        val newState = if (newMonsterHp <= 0) {
            val newKillsSince = state.killsSinceLastGoblin + 1
            val spawnGoblin = shouldSpawnGoblin(newKillsSince)
            if (spawnGoblin) {
                state.copy(
                    punchesUsedToday = usedToday + 1, lastPunchDate = today,
                    monstersKilled = state.monstersKilled + 1,
                    highestMonsterLevelKilled = maxOf(state.highestMonsterLevelKilled, state.monsterLevel),
                    monsterName = "Golden Goblin", monsterLevel = state.playerLevel,
                    monsterImageRes = "monster_goblin_gold",
                    monsterMaxHp = 10_000_000, monsterCurrentHp = 10_000_000,
                    monsterDamage = 1,
                    isGoldenGoblinActive = true,
                    goldenGoblinEndTime = System.currentTimeMillis() + 60_000L,
                    goldenGoblinPunchCount = 0,
                    killsSinceLastGoblin = 0,
                    totalDamageDealt = state.totalDamageDealt + dmg
                )
            } else {
                val monster = MonsterUtils.rollNextMonster(state.playerLevel)
                state.copy(
                    punchesUsedToday = usedToday + 1, lastPunchDate = today,
                    monstersKilled = state.monstersKilled + 1,
                    highestMonsterLevelKilled = maxOf(state.highestMonsterLevelKilled, state.monsterLevel),
                    monsterName = monster.name, monsterLevel = monster.level,
                    monsterImageRes = monster.imageRes,
                    monsterMaxHp = monster.maxHp, monsterCurrentHp = monster.maxHp,
                    monsterDamage = monster.damage,
                    isCurrentBoss = false,
                    currentBossId = 0,
                    killsSinceLastGoblin = newKillsSince,
                    totalDamageDealt = state.totalDamageDealt + dmg
                )
            }
        } else {
            state.copy(
                punchesUsedToday = usedToday + 1, lastPunchDate = today,
                monsterCurrentHp = newMonsterHp,
                totalDamageDealt = state.totalDamageDealt + dmg
            )
        }
        dao.saveGameState(newState)
        dmg
    }

    /**
     * Добавляет 10 тестовых вещей (по 2 каждого слота) и 100000 зубов.
     * Использовать ТОЛЬКО для тестирования!
     */
    override suspend fun addDebugItemsForTest() {
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

    // ==================== GOLDEN GOBLIN ====================

    override suspend fun performGoblinPunch(): Int = saveMutex.withLock {
        val state = getGameState()
        if (!state.isGoldenGoblinActive) return@withLock 0
        if (System.currentTimeMillis() >= state.goldenGoblinEndTime) return@withLock 0
        val newCount = state.goldenGoblinPunchCount + 1
        dao.saveGameState(state.copy(goldenGoblinPunchCount = newCount))
        newCount
    }

    override suspend fun endGoldenGoblin(): Int = saveMutex.withLock {
        val state = getGameState()
        if (!state.isGoldenGoblinActive) return@withLock 0
        val teethEarned = state.goldenGoblinPunchCount
        val monster = MonsterUtils.rollNextMonster(state.playerLevel)
        val prestigeMult = 1 + state.prestigeLevel
        dao.saveGameState(state.copy(
            isGoldenGoblinActive = false,
            goldenGoblinEndTime = 0L,
            goldenGoblinPunchCount = 0,
            teeth = state.teeth + teethEarned,
            totalTeethEarned = state.totalTeethEarned + teethEarned,
            monsterName = monster.name,
            monsterLevel = monster.level,
            monsterImageRes = monster.imageRes,
            monsterMaxHp = monster.maxHp * prestigeMult,
            monsterCurrentHp = monster.maxHp * prestigeMult,
            monsterDamage = monster.damage * prestigeMult
        ))
        addLog(
            "🟡 Golden Goblin escaped! You earned $teethEarned 🦷",
            "🟡 Золотой Гоблин сбежал! Ты получил $teethEarned 🦷"
        )
        teethEarned
    }

    // ==================== RATE US ====================

    override suspend fun updateRateUsState(action: RateUsAction) {
        val state = getGameState()
        val newState = when (action) {
            RateUsAction.RATE_NOW -> {
                state.copy(rateUsLastShowDate = System.currentTimeMillis())
            }
            RateUsAction.REMIND_LATER -> {
                state.copy(rateUsLastShowDate = System.currentTimeMillis())
            }
            RateUsAction.NEVER_ASK -> {
                state.copy(rateUsDoNotShowAgain = true)
            }
        }
        saveGameState(newState)
    }
}

enum class RateUsAction {
    RATE_NOW, REMIND_LATER, NEVER_ASK
}

// я удалил отсюда data class PeriodStats и enum class EnchantResult