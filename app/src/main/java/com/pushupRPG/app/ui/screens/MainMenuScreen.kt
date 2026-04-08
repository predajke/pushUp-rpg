package com.pushupRPG.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pushupRPG.app.data.db.GameStateEntity
import com.pushupRPG.app.data.db.LogEntryEntity
import com.pushupRPG.app.ui.theme.*
import com.pushupRPG.app.utils.EventUtils
import com.pushupRPG.app.utils.GameCalculations
import com.pushupRPG.app.utils.MonsterUtils
import com.pushupRPG.app.utils.DailyRewardUtils
import com.pushupRPG.app.ui.GameViewModel // ИСПРАВЛЕН ИМПОРТ
import com.pushupRPG.app.utils.AppStrings

@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    onNavigateToInventory: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToQuests: () -> Unit = {},
    onNavigateToProgress: () -> Unit = {}
) {
    // ИСПРАВЛЕНО: Добавлены начальные значения (initial) и заменен logs на recentLogs
    val gameState by viewModel.gameState.collectAsState(initial = null)
    val logs by viewModel.recentLogs.collectAsState(initial = emptyList())
    val inputValue by viewModel.inputValue.collectAsState(initial = 0)
    val showLevelUpDialog by viewModel.showLevelUpDialog.collectAsState(initial = false)
    val newLevel by viewModel.newLevel.collectAsState(initial = 0)
    val totalStats by viewModel.totalStats.collectAsState(initial = null)
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    val activeEvent by viewModel.activeEvent.collectAsState(initial = null)
    val showDailyReward by viewModel.showDailyReward.collectAsState(initial = false)
    val pendingDailyReward by viewModel.pendingDailyReward.collectAsState(initial = null)
    val onboardingStep by viewModel.onboardingStep.collectAsState(initial = 0)
    val isOnboardingComplete by viewModel.isOnboardingComplete.collectAsState(initial = false)
    val antiCheatCooldown by viewModel.antiCheatCooldown.collectAsState(initial = null)

    // Scroll state extracted here so it can be used in LaunchedEffect (before early return)
    val scrollState = rememberScrollState()

    // Track coordinates for tour guide - declared before early return so remember works correctly
    val targetRect = remember { mutableStateOf(androidx.compose.ui.geometry.Rect.Zero) }
    val totalPushUpsRect = remember { mutableStateOf(androidx.compose.ui.geometry.Rect.Zero) }
    val inventoryRect = remember { mutableStateOf(androidx.compose.ui.geometry.Rect.Zero) }
    val shopRect = remember { mutableStateOf(androidx.compose.ui.geometry.Rect.Zero) }
    val battleRect = remember { mutableStateOf(androidx.compose.ui.geometry.Rect.Zero) }
    val logsRect = remember { mutableStateOf(androidx.compose.ui.geometry.Rect.Zero) }
    val questsRect = remember { mutableStateOf(androidx.compose.ui.geometry.Rect.Zero) }

    // Update target rect when step or any measured rect changes
    LaunchedEffect(onboardingStep, totalPushUpsRect.value, inventoryRect.value, shopRect.value, battleRect.value, logsRect.value, questsRect.value) {
        targetRect.value = when (onboardingStep) {
            0 -> totalPushUpsRect.value
            1 -> inventoryRect.value
            2 -> shopRect.value
            3 -> battleRect.value
            4 -> logsRect.value
            5 -> questsRect.value
            else -> androidx.compose.ui.geometry.Rect.Zero
        }
        // Step 5 (Quests) is near bottom of scroll — scroll down so the element is visible
        if (onboardingStep == 5) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    if (isLoading || gameState == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = OrangeAccent)
        }
        return
    }

    val state = gameState!!

    LaunchedEffect(Unit) {
        viewModel.triggerRealtimeTick()
        viewModel.claimDailyReward()
        while (true) {
            kotlinx.coroutines.delay(10_000L)
            viewModel.triggerRealtimeTick()
        }
    }

    // Initialize onboarding if first launch
    LaunchedEffect(gameState) {
        if (gameState != null && gameState!!.isFirstLaunch && !isOnboardingComplete) {
            viewModel.initializeOnboarding(gameState!!)
        }
    }

    val language = state.language
    val statusBarHeightPx = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toFloat()
    }
    val maxHp = GameCalculations.getMaxHp(
        state.playerLevel,
        state.baseHealth,
        viewModel.getEquippedItems(state).sumOf { it.stats.health }
    )

    if (showLevelUpDialog) {
        LevelUpDialog(
            newLevel = newLevel,
            unspentPoints = state.unspentStatPoints,
            language = language,
            onSpendPoint = { stat -> viewModel.spendStatPoint(stat) },
            onDismiss = { viewModel.dismissLevelUpDialog() }
        )
    }

    if (showDailyReward && pendingDailyReward != null) {
        DailyRewardDialog(
            reward = pendingDailyReward!!,
            language = language,
            onDismiss = { viewModel.dismissDailyReward() }
        )
    }

    // Show highlight onboarding if not complete
    if (!isOnboardingComplete && onboardingStep < com.pushupRPG.app.managers.OnboardingManager.TOTAL_STEPS) {
        com.pushupRPG.app.ui.dialogs.HighlightTourGuideDialog(
            currentStep = onboardingStep,
            onboardingManager = viewModel.getOnboardingManager(),
            language = language,
            targetRect = targetRect.value,
            statusBarHeightPx = statusBarHeightPx,
            onNext = { viewModel.nextOnboardingStep() },
            onSkip = { viewModel.skipOnboarding(gameState) },
            onComplete = { viewModel.completeOnboarding(gameState) }
        )
    }

    // Anti-cheat: Show cooldown dialog or ad
    antiCheatCooldown?.let { cooldown ->
        when (cooldown.adType) {
            com.pushupRPG.app.managers.AdType.NONE -> {
                // Should not happen, but just in case show cooldown
                com.pushupRPG.app.ui.dialogs.AntiCheatWarningDialog(
                    remainingCooldownMs = cooldown.remainingMs,
                    onDismiss = { viewModel.clearAntiCheatCooldown() }
                )
            }
            com.pushupRPG.app.managers.AdType.SKIPPABLE -> {
                // Show rewarded ad with skip button (skip after 10s)
                com.pushupRPG.app.ui.dialogs.RewardedAdDialog(
                    title = AppStrings.t(language, "ad_title"),
                    description = AppStrings.t(language, "ad_description_cheat"),
                    rewardText = AppStrings.t(language, "ad_button_watch"),
                    onWatchAd = {
                        // TODO: Show rewarded ad here when AdManager is integrated
                        viewModel.clearAntiCheatCooldown()
                    },
                    onDecline = {
                        // Show cooldown instead
                        // User chose not to watch ad, show cooldown
                    },
                    onDismiss = {
                        // Dialog dismissed, show cooldown
                    }
                )
            }
            com.pushupRPG.app.managers.AdType.NO_SKIP -> {
                // Show rewarded ad without skip (force watch 20-30s)
                // For now, we'll show a warning that forces user to wait or watch ad
                com.pushupRPG.app.ui.dialogs.AntiCheatWarningDialog(
                    remainingCooldownMs = cooldown.remainingMs,
                    onDismiss = { viewModel.clearAntiCheatCooldown() }
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        ScreenBackground("bg_mainmenu_overall")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        TopBar(
            state = state,
            maxHp = maxHp,
            onSettingsClick = onNavigateToSettings
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 16.dp)
                .navigationBarsPadding()
        ) {
            EventBanner(
                language = language,
                activeEvent = activeEvent,
                eventEndTime = state.eventEndTime
            )

            Spacer(modifier = Modifier.height(10.dp))

            PushUpCounter(
                state = state,
                inputValue = inputValue,
                language = language,
                onAddToInput = { viewModel.addToInput(it) },
                onReset = { viewModel.resetInput() },
                onSave = { viewModel.savePushUps() },
                onTotalClick = onNavigateToStatistics,
                onShopClick = onNavigateToShop,
                modifier = Modifier.onGloballyPositioned { totalPushUpsRect.value = it.boundsInWindow() },
                onShopPositioned = { shopRect.value = it }
            )

            Spacer(modifier = Modifier.height(10.dp))

            StatsPanel(
                state = state,
                totalStats = totalStats,
                onClick = onNavigateToInventory,
                modifier = Modifier.onGloballyPositioned { inventoryRect.value = it.boundsInWindow() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            BattleArena(
                state = state,
                maxHp = maxHp,
                modifier = Modifier
                    .heightIn(min = 220.dp)
                    .onGloballyPositioned { battleRect.value = it.boundsInWindow() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            MiniLog(
                logs = logs,
                language = language,
                onClick = onNavigateToLogs,
                modifier = Modifier.onGloballyPositioned { logsRect.value = it.boundsInWindow() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            QuestShortcutButton(
                language = language,
                quests = viewModel.getActiveQuests(state),
                onClick = onNavigateToQuests,
                modifier = Modifier.onGloballyPositioned { questsRect.value = it.boundsInWindow() }
            )

            Spacer(modifier = Modifier.height(6.dp))

            ProgressShortcutButton(
                language = language,
                unlockedCount = viewModel.getUnlockedAchievements(state).size,
                totalCount = com.pushupRPG.app.utils.AchievementSystem.ALL.size,
                onClick = onNavigateToProgress
            )
        }
    } // Column
    } // Box (фон)
}

@Composable
fun QuestShortcutButton(
    language: String,
    quests: List<com.pushupRPG.app.utils.ActiveQuest>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bgResId = remember {
        context.resources.getIdentifier("bg_quest_button", "drawable", context.packageName)
    }
    val readyCount = quests.count { it.isCompleted && !it.claimed }
    val label = AppStrings.t(language, "quests")
    val badge = if (readyCount > 0) " ($readyCount ✓)" else ""

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        if (bgResId != 0) {
            Image(
                painter = painterResource(id = bgResId),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.40f)))
        } else {
            Box(modifier = Modifier.matchParentSize().background(DarkSurface))
        }
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📋", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "$label$badge",
                color = if (readyCount > 0) GoldAccent else TextPrimary,
                fontWeight = if (readyCount > 0) FontWeight.Bold else FontWeight.Normal,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text("›", color = TextMuted, fontSize = 18.sp)
        }
    }
}

@Composable
fun ProgressShortcutButton(
    language: String,
    unlockedCount: Int,
    totalCount: Int,
    onClick: () -> Unit
) {
    val label = AppStrings.t(language, "progress")
    val badge = "$unlockedCount / $totalCount"
    val context = LocalContext.current
    val bgResId = remember {
        context.resources.getIdentifier("bg_progress_button", "drawable", context.packageName)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        if (bgResId != 0) {
            Image(
                painter = painterResource(id = bgResId),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.35f
            )
            Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.35f)))
        } else {
            Box(modifier = Modifier.matchParentSize().background(DarkSurface))
        }
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🏆", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                color = TextPrimary,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(badge, color = GoldAccent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(8.dp))
            Text("›", color = TextMuted, fontSize = 18.sp)
        }
    }
}

@Composable
fun DailyRewardDialog(
    reward: DailyRewardUtils.DailyReward,
    language: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = AppStrings.t(language, "daily_reward"),
                color = GoldAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (language == "ru") "День ${reward.day} / 7" else "Day ${reward.day} / 7",
                color = TextMuted,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (language == "ru") reward.descriptionRu() else reward.descriptionEn(),
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
            ) {
                Text(AppStrings.t(language, "btn_claim"), color = Color.White)
            }
        }
    }
}

// --- Шапка ---
@Composable
fun TopBar(
    state: GameStateEntity,
    maxHp: Int,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(OrangeAccent, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Lvl ${state.playerLevel}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            LinearProgressIndicator(
                progress = { GameCalculations.getXpProgress(state.totalXp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = OrangeAccent,
                trackColor = DarkSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🔥 Streak ${state.currentStreak} days",
                    fontSize = 11.sp,
                    color = OrangeLight
                )
                Text(
                    text = "${GameCalculations.getXpForNextLevel(state.totalXp)} xp to next",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { onSettingsClick() }
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
                    .border(2.dp, OrangeAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👤", fontSize = 20.sp)
            }
            Text(
                text = state.playerName,
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// --- Баннер события ---
@Composable
fun EventBanner(
    language: String,
    activeEvent: com.pushupRPG.app.data.model.GameEvent?,
    eventEndTime: Long
) {
    val context = LocalContext.current
    val borderColor = if (activeEvent != null) GoldAccent else OrangeAccent.copy(alpha = 0.3f)

    val bgImageName = activeEvent?.let {
        when (it.type) {
            com.pushupRPG.app.data.model.EventType.DROP_RATE_BONUS,
            com.pushupRPG.app.data.model.EventType.RARE_DROP_BONUS -> "event_bg_luck"
            com.pushupRPG.app.data.model.EventType.POWER_BONUS,
            com.pushupRPG.app.data.model.EventType.BERSERKER,
            com.pushupRPG.app.data.model.EventType.BATTLE_SPEED_BONUS -> "event_bg_power"
            com.pushupRPG.app.data.model.EventType.ARMOR_BONUS,
            com.pushupRPG.app.data.model.EventType.HEALTH_BONUS -> "event_bg_defense"
            com.pushupRPG.app.data.model.EventType.REGEN_BONUS,
            com.pushupRPG.app.data.model.EventType.XP_BONUS -> "event_bg_spirit"
            com.pushupRPG.app.data.model.EventType.NIGHTMARE -> "event_bg_nightmare"
            else -> null
        }
    }
    val bgResId = remember(bgImageName) {
        if (bgImageName != null)
            context.resources.getIdentifier(bgImageName, "drawable", context.packageName)
        else 0
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
    ) {
        // Слой 1: фон (картинка или цвет)
        if (bgResId != 0) {
            Image(
                painter = painterResource(id = bgResId),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.50f)))
        } else {
            Box(modifier = Modifier.matchParentSize()
                .background(if (activeEvent != null) Color(0xFF1A1500) else DarkSurfaceVariant))
        }

        // Слой 2: контент
        Box(modifier = Modifier.padding(10.dp)) {
            if (activeEvent == null) {
                Text(
                    text = if (language == "ru")
                        "Событий пока нет. Продолжай тренироваться!"
                    else
                        "No events right now. Keep training!",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = activeEvent.icon, fontSize = 20.sp)
                        Column {
                            Text(
                                text = EventUtils.getEventName(activeEvent, language),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                            Text(
                                text = EventUtils.getEventDescription(activeEvent, language),
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Text(
                        text = EventUtils.getRemainingTime(eventEndTime),
                        fontSize = 11.sp,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- Счётчик отжиманий ---
@Composable
fun PushUpCounter(
    state: GameStateEntity,
    inputValue: Int,
    language: String,
    onAddToInput: (Int) -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    onTotalClick: () -> Unit,
    onShopClick: () -> Unit,
    modifier: Modifier = Modifier,
    onShopPositioned: ((androidx.compose.ui.geometry.Rect) -> Unit)? = null
) {
    val context = LocalContext.current
    val bgResId = remember {
        context.resources.getIdentifier("bg_pushups", "drawable", context.packageName)
    }
    val shopBtnBg = remember {
        context.resources.getIdentifier("bg_shop_btn", "drawable", context.packageName)
    }
    val bgSave = remember {
        context.resources.getIdentifier("bg_save", "drawable", context.packageName)
    }
    val bgStats = remember {
        context.resources.getIdentifier("bg_stats", "drawable", context.packageName)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .background(DarkCard, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
    ) {
        if (bgResId != 0) {
            Image(
                painter = painterResource(id = bgResId),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.15f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = AppStrings.t(language, "counter_today"),
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier.fillMaxWidth().clickable { onTotalClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${state.pushUpsToday}",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeAccent,
                    lineHeight = 48.sp
                )
            }

            // Сетка 3×3: каждая колонка имеет одинаковый вес
            // Левый: Reset / -1 / Shop
            // Центр: +0 (2 строки) / Save
            // Правый: +10 / +1 / > (0.5)
            val btnH = 36.dp
            val gap = 8.dp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap)
            ) {
                // --- Левый Column ---
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(gap)
                ) {
                    Button(
                        onClick = onReset,
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonGray),
                        modifier = Modifier.fillMaxWidth().height(btnH),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp),
                        border = BorderStroke(1.dp, Color.Black)
                    ) {
                        Text(text = AppStrings.t(language, "btn_reset"), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onAddToInput(-1) },
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonRed),
                        modifier = Modifier.fillMaxWidth().height(btnH),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp),
                        border = BorderStroke(1.dp, Color.Black)
                    ) {
                        Text(text = "-1", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(btnH)
                            .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                            .background(Color(0xFF1A3A2A), RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onShopClick() }
                            .onGloballyPositioned { coordinates ->
                                onShopPositioned?.invoke(coordinates.boundsInWindow())
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (shopBtnBg != 0) {
                            Image(
                                painter = painterResource(id = shopBtnBg),
                                contentDescription = null,
                                modifier = Modifier.matchParentSize(),
                                contentScale = ContentScale.Crop,
                                alpha = 0.4f
                            )
                        }
                        Text(
                            text = AppStrings.t(language, "shop"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthColor
                        )
                    }
                }

                // --- Центральный Column ---
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(gap)
                ) {
                    // +0 занимает высоту двух кнопок + один gap
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(btnH * 2 + gap)
                            .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                            .border(1.dp, OrangeAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+$inputValue",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangeAccent,
                            maxLines = 1
                        )
                    }
                    Button(
                        onClick = onSave,
                        modifier = Modifier.fillMaxWidth().height(btnH),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp),
                        border = BorderStroke(1.dp, Color.Black)
                    ) {
                        // Контейнер для относительного позиционирования
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Фоновая картинка (если указана)
                            if (shopBtnBg != 0) {
                                Image(
                                    painter = painterResource(id = bgSave),
                                    contentDescription = null,
                                    modifier = Modifier.matchParentSize(),
                                    contentScale = ContentScale.Crop,
                                    alpha = 0.3f
                                )
                            }

                            // Текст поверх картинки
                            Text(
                                text = AppStrings.t(language, "btn_save"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                // --- Правый Column ---
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(gap)
                ) {
                    Button(
                        onClick = { onAddToInput(10) },
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
                        modifier = Modifier.fillMaxWidth().height(btnH),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp),
                        border = BorderStroke(1.dp, Color.Black)
                    ) {
                        Text(text = "+10", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onAddToInput(1) },
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
                        modifier = Modifier.fillMaxWidth().height(btnH),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp),
                        border = BorderStroke(1.dp, Color.Black)
                    ) {
                        Text(text = "+1", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    // > кнопка — 0.5 ширины колонки, по центру, была, сейчас будет как и все и не > а Stats
                    Box(
                        modifier = Modifier.fillMaxWidth().height(btnH),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(btnH),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .border(          // ← border нужно ДО button
                                        width = 1.dp,
                                        color = Color.Black,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                                    .border(1.dp, OrangeAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .clickable { onTotalClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                // Внутренний Box для картинки и текста
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Фоновая картинка
                                    if (shopBtnBg != 0) {
                                        Image(
                                            painter = painterResource(id = bgStats),
                                            contentDescription = null,
                                            modifier = Modifier.matchParentSize(),
                                            contentScale = ContentScale.Crop,
                                            alpha = 0.3f
                                        )
                                    }

                                    // Текст поверх
                                    Text(
                                        text = "Stats",
                                        fontSize = 14.sp,
                                        color = OrangeAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Панель статов ---
@Composable
fun StatsPanel(
    state: GameStateEntity,
    totalStats: com.pushupRPG.app.utils.TotalStats?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val power = totalStats?.power ?: state.basePower
    val armor = totalStats?.armor ?: state.baseArmor
    val health = totalStats?.health ?: state.baseHealth
    val luck = totalStats?.luck ?: state.baseLuck

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .background(DarkCard, RoundedCornerShape(12.dp))
            .border(1.dp, OrangeAccent.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(icon = "⚔️", value = "$power", color = PowerColor)
        StatItem(icon = "🛡️", value = "$armor", color = ArmorColor)
        StatItem(icon = "❤️", value = "$health", color = HealthColor)
        StatItem(icon = "🍀", value = String.format("%.1f", luck), color = LuckColor)
        StatItem(icon = "🦷", value = "${state.teeth}", color = Color(0xFFE0E0E0))

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = "›",
            fontSize = 24.sp,
            color = OrangeAccent.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatItem(icon: String, value: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = icon, fontSize = 16.sp)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

// --- Арена ---
@Composable
fun BattleArena(
    state: GameStateEntity,
    maxHp: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bgResId = remember {
        val index = (1..5).random()
        context.resources.getIdentifier("bg_fight_$index", "drawable", context.packageName)
    }

    val heroImageRes = state.heroAvatar.ifEmpty { MonsterUtils.getHeroImageRes(state.playerLevel) }
    val monsterImageRes = remember(state.monsterName) {
        MonsterUtils.getImageResByName(state.monsterName)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .then(modifier)
            .background(DarkCard, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
    ) {
        if (bgResId != 0) {
            Image(
                painter = painterResource(id = bgResId),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.2f
            )
        }

        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val hpPercent = if (maxHp > 0) state.currentHp.toFloat() / maxHp else 0f
                    val hpColor = when {
                        hpPercent > 0.6f -> HpBarFull
                        hpPercent > 0.3f -> HpBarMid
                        else -> HpBarLow
                    }
                    Text(text = "${state.currentHp}/${maxHp} HP", fontSize = 11.sp, color = hpColor)
                    LinearProgressIndicator(
                        progress = { hpPercent.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = hpColor,
                        trackColor = HpBarBackground
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (state.isPlayerDead) "💀" else "⚔️", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val monsterHpPercent = if (state.monsterMaxHp > 0)
                        state.monsterCurrentHp.toFloat() / state.monsterMaxHp else 0f
                    Text(
                        text = "${state.monsterCurrentHp}/${state.monsterMaxHp} HP",
                        fontSize = 11.sp,
                        color = HpBarLow,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                    LinearProgressIndicator(
                        progress = { monsterHpPercent.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = HpBarLow,
                        trackColor = HpBarBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    DrawableImage(
                        name = heroImageRes,
                        modifier = Modifier
                            .size(110.dp)
                            .alpha(if (state.isPlayerDead) 0.4f else 1f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.playerName,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text(
                    text = "VS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeAccent
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    DrawableImage(
                        name = monsterImageRes,
                        modifier = Modifier.size(110.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val monster = MonsterUtils.getMonsterByLevel(state.monsterLevel)
                    val monsterName = MonsterUtils.getMonsterName(monster, state.language)
                    Text(
                        text = "$monsterName (${state.monsterLevel} lvl)",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// --- Мини-лог ---
@Composable
fun MiniLog(
    logs: List<LogEntryEntity>,
    language: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .background(LogBackground, RoundedCornerShape(12.dp))
            .border(1.dp, LogText.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        val recentLogs = logs.take(4)

        if (recentLogs.isEmpty()) {
            Text(
                text = AppStrings.t(language, "battle_soon"),
                fontSize = 13.sp,
                color = LogText.copy(alpha = 0.6f)
            )
        } else {
            recentLogs.forEach { log ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                ) {
                    Text(
                        text = formatTimestamp(log.timestamp),
                        fontSize = 12.sp,
                        color = LogText.copy(alpha = 0.6f),
                        modifier = Modifier.width(44.dp)
                    )
                    Text(
                        text = if (language == "ru") log.messageRu else log.message,
                        fontSize = 12.sp,
                        color = LogText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = AppStrings.t(language, "view_all_logs"),
            fontSize = 11.sp,
            color = LogText.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.End)
        )
    }
}

// --- Level Up диалог ---
@Composable
fun LevelUpDialog(
    newLevel: Int,
    unspentPoints: Int,
    language: String,
    onSpendPoint: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val bgResId = remember {
        context.resources.getIdentifier("bg_levelup", "drawable", context.packageName)
    }
    val iconResId = remember {
        context.resources.getIdentifier("lvlup", "drawable", context.packageName)
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
        ) {
            if (bgResId != 0) {
                Image(
                    painter = painterResource(id = bgResId),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.25f
                )
            }

            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (iconResId != 0) {
                    Image(
                        painter = painterResource(id = iconResId),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(text = "⬆️", fontSize = 48.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = AppStrings.t(language, "levelup_title"),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
                Text(
                    text = if (language == "ru") "Уровень $newLevel" else "Level $newLevel",
                    fontSize = 16.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (language == "ru")
                        "Очков для распределения: $unspentPoints"
                    else
                        "Points to spend: $unspentPoints",
                    fontSize = 14.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatPointButton(
                        icon = "⚔️",
                        label = AppStrings.t(language, "stat_power"),
                        enabled = unspentPoints > 0,
                        onClick = { onSpendPoint("power") }
                    )
                    StatPointButton(
                        icon = "❤️",
                        label = AppStrings.t(language, "stat_health"),
                        enabled = unspentPoints > 0,
                        onClick = { onSpendPoint("health") }
                    )
                    StatPointButton(
                        icon = "🍀",
                        label = AppStrings.t(language, "stat_luck"),
                        enabled = unspentPoints > 0,
                        onClick = { onSpendPoint("luck") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (unspentPoints <= 0) OrangeAccent else GoldAccent
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (unspentPoints <= 0) {
                            AppStrings.t(language, "btn_continue")
                        } else {
                            AppStrings.t(language, "btn_later")
                        },
                        fontWeight = FontWeight.Bold,
                        color = if (unspentPoints <= 0) Color.White else Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun StatPointButton(
    icon: String,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = DarkSurfaceVariant,
            disabledContainerColor = DarkSurfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(72.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = icon, fontSize = 24.sp)
            Text(
                text = label,
                fontSize = 10.sp,
                color = if (enabled) TextPrimary else TextMuted
            )
        }
    }
}

@Composable
fun DrawableImage(
    name: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resId = remember(name) {
        context.resources.getIdentifier(name, "drawable", context.packageName)
    }

    if (resId != 0) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = name,
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    } else {
        Box(
            modifier = modifier.background(DarkSurfaceVariant, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "?", fontSize = 32.sp, color = TextMuted)
        }
    }
}

@Composable
fun ScreenBackground(name: String) {
    val context = LocalContext.current
    val resId = remember(name) {
        context.resources.getIdentifier(name, "drawable", context.packageName)
    }
    if (resId != 0) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.25f
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}