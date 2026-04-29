package com.ninthbalcony.pushuprpg.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ninthbalcony.pushuprpg.data.db.GameStateEntity
import com.ninthbalcony.pushuprpg.data.db.LogEntryEntity
import com.ninthbalcony.pushuprpg.ui.theme.*
import com.ninthbalcony.pushuprpg.utils.EventUtils
import com.ninthbalcony.pushuprpg.utils.GameCalculations
import com.ninthbalcony.pushuprpg.utils.MonsterUtils
import com.ninthbalcony.pushuprpg.utils.DailyRewardUtils
import com.ninthbalcony.pushuprpg.ui.GameViewModel // ИСПРАВЛЕН ИМПОРТ
import com.ninthbalcony.pushuprpg.utils.AppStrings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset                  // + для крутого шрифта
import androidx.compose.ui.graphics.Shadow                  // + для крутого шрифта
import androidx.compose.ui.text.TextStyle                   // + для крутого шрифта
import androidx.compose.ui.tooling.preview.Preview
import com.ninthbalcony.pushuprpg.utils.SoundManager

@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    onNavigateToInventory: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToQuests: () -> Unit = {},
    onNavigateToProgress: () -> Unit = {},
    onNavigateToLeaderboard: () -> Unit = {}
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
    val showRateUsDialog by viewModel.showRateUsDialog.collectAsState(initial = false)
    val punchCooldownUntil by viewModel.punchCooldownUntil.collectAsState()
    val lastPunchDamage by viewModel.lastPunchDamage.collectAsState()
    val goblinTimeRemaining by viewModel.goblinTimeRemaining.collectAsState()
    val goblinEndTeeth by viewModel.goblinEndTeeth.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("pushup_prefs", android.content.Context.MODE_PRIVATE) }
    val soundEnabled = remember(prefs) { prefs.getBoolean("sounds_enabled", true) }
    val vibrationEnabled = remember(prefs) { prefs.getBoolean("vibration_enabled", true) }
    val achievementToast by viewModel.achievementToast.collectAsState()

    var showLevelUpFlash by remember { mutableStateOf(false) }
    LaunchedEffect(showLevelUpDialog) {
        if (showLevelUpDialog) {
            showLevelUpFlash = true
            SoundManager.playLevelUp(context, soundEnabled)
            kotlinx.coroutines.delay(1200)
            showLevelUpFlash = false
        }
    }

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
        // Visual top-to-bottom order: Battle → PushUps → Shop → Inventory → Logs → Quests
        targetRect.value = when (onboardingStep) {
            0 -> battleRect.value
            1 -> totalPushUpsRect.value
            2 -> shopRect.value
            3 -> inventoryRect.value
            4 -> logsRect.value
            5 -> questsRect.value
            else -> androidx.compose.ui.geometry.Rect.Zero
        }
        when (onboardingStep) {
            0 -> scrollState.animateScrollTo(0)                        // BattleArena — scroll to top
            5 -> scrollState.animateScrollTo(scrollState.maxValue)     // Quests — scroll to bottom
        }
    }

    val isInspection = LocalInspectionMode.current
    DisposableEffect(Unit) {
        if (!isInspection) {
            val enabled = context.getSharedPreferences("pushup_prefs", android.content.Context.MODE_PRIVATE)
                .getBoolean("sounds_enabled", true)
            SoundManager.playMusic(context, "music_main", enabled)
        }
        onDispose {}
    }
    if (!isInspection && (isLoading || gameState == null)) {
        Box(
            modifier = Modifier.fillMaxSize().background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = OrangeAccent)
        }
        return
    }

    val state = gameState ?: com.ninthbalcony.pushuprpg.data.db.GameStateEntity()

    LaunchedEffect(Unit) {
        viewModel.updateStreakOnLogin()
        viewModel.refreshSpinCounters()
        viewModel.triggerRealtimeTick()
        viewModel.claimDailyReward()
        while (true) {
            kotlinx.coroutines.delay(10_000L)
            viewModel.triggerRealtimeTick()
        }
    }

    // Initialize onboarding if first launch (only once)
    LaunchedEffect(state.isFirstLaunch) {
        if (state.isFirstLaunch && !isOnboardingComplete) {
            viewModel.initializeOnboarding(state)
        }
    }

    // Check and show Rate Us dialog
    LaunchedEffect(gameState) {
        if (gameState != null) {
            viewModel.checkAndShowRateUs(gameState!!)
        }
    }

    val language = state.language
    val statusBarHeightPx = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toFloat()
    }
    val maxHp = totalStats?.health ?: state.baseHealth

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

    goblinEndTeeth?.let { teeth ->
        GoblinEndDialog(
            teethEarned = teeth,
            language = language,
            onDismiss = { viewModel.clearGoblinEndTeeth() }
        )
    }

    // Show highlight onboarding if not complete
    if (state.isFirstLaunch && !isOnboardingComplete && onboardingStep < com.ninthbalcony.pushuprpg.managers.OnboardingManager.TOTAL_STEPS) {
        com.ninthbalcony.pushuprpg.ui.dialogs.HighlightTourGuideDialog(
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
            com.ninthbalcony.pushuprpg.managers.AdType.NONE -> {
                // Should not happen, but just in case show cooldown
                com.ninthbalcony.pushuprpg.ui.dialogs.AntiCheatWarningDialog(
                    remainingCooldownMs = cooldown.remainingMs,
                    onDismiss = { viewModel.clearAntiCheatCooldown() }
                )
            }
            com.ninthbalcony.pushuprpg.managers.AdType.SKIPPABLE -> {
                // Show rewarded ad with skip button (skip after 10s)
                com.ninthbalcony.pushuprpg.ui.dialogs.RewardedAdDialog(
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
            com.ninthbalcony.pushuprpg.managers.AdType.NO_SKIP -> {
                // Show rewarded ad without skip (force watch 20-30s)
                // For now, we'll show a warning that forces user to wait or watch ad
                com.ninthbalcony.pushuprpg.ui.dialogs.AntiCheatWarningDialog(
                    remainingCooldownMs = cooldown.remainingMs,
                    onDismiss = { viewModel.clearAntiCheatCooldown() }
                )
            }
        }
    }

    // Rate Us Dialog
    if (showRateUsDialog) {
        com.ninthbalcony.pushuprpg.ui.dialogs.RateUsDialog(
            onRate = {
                viewModel.rateUsAction(com.ninthbalcony.pushuprpg.data.repository.RateUsAction.RATE_NOW)
                val uri = android.net.Uri.parse("market://details?id=com.ninthbalcony.pushuprpg")
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
            },
            onRemindLater = {
                viewModel.rateUsAction(com.ninthbalcony.pushuprpg.data.repository.RateUsAction.REMIND_LATER)
            },
            onNeverAsk = {
                viewModel.rateUsAction(com.ninthbalcony.pushuprpg.data.repository.RateUsAction.NEVER_ASK)
            },
            onDismiss = {
                viewModel.dismissRateUsDialog()
            },
            title = AppStrings.t(language, "rate_us_title"),
            description = AppStrings.t(language, "rate_us_description"),
            buttonRate = AppStrings.t(language, "btn_rate_now"),
            buttonReminder = AppStrings.t(language, "btn_remind_later"),
            buttonNever = AppStrings.t(language, "btn_never_ask")
        )
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

            val battleAnim by viewModel.battleAnimation.collectAsState()
            BattleArena(
                state = state,
                maxHp = maxHp,
                punchesRemaining = viewModel.getPunchesRemaining(state),
                punchCooldownUntil = punchCooldownUntil,
                lastPunchDamage = lastPunchDamage,
                goblinTimeRemaining = goblinTimeRemaining,
                battleAnimation = battleAnim,
                soundEnabled = soundEnabled,
                onPunch = {
                    if (state.isGoldenGoblinActive) {
                        viewModel.performGoblinPunch()
                        if (vibrationEnabled) vibrate(context)
                    } else {
                        viewModel.performPunch()
                        SoundManager.playPunch(soundEnabled)
                        if (vibrationEnabled) vibrate(context)
                    }
                },
                modifier = Modifier
                    .heightIn(min = 220.dp)
                    .onGloballyPositioned { battleRect.value = it.boundsInWindow() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            PushUpCounter(
                state = state,
                inputValue = inputValue,
                language = language,
                onAddToInput = { viewModel.addToInput(it) },
                onReset = { viewModel.resetInput() },
                onSave = {
                    viewModel.savePushUps()
                    SoundManager.playSave(soundEnabled)
                    if (vibrationEnabled) vibrate(context)
                },
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
                totalCount = com.ninthbalcony.pushuprpg.utils.AchievementSystem.ALL.size,
                onClick = onNavigateToProgress
            )

            Spacer(modifier = Modifier.height(6.dp))

            LeaderboardShortcutButton(
                language = language,
                onClick = onNavigateToLeaderboard
            )
        }
    } // Column

    // Falling coins during Golden Goblin event
    if (state.isGoldenGoblinActive) {
        FallingCoinsOverlay(modifier = Modifier.matchParentSize())
    }

    // Achievement toast overlay
    achievementToast?.let { def ->
        AchievementToast(def = def, language = language, onDismiss = { viewModel.clearAchievementToast() })
    }

    // Level-up flash overlay
    AnimatedVisibility(
        visible = showLevelUpFlash,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(500)),
        modifier = Modifier.matchParentSize()
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0xFFFFD700).copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "LEVEL UP!",
                color = Color(0xFFFFD700),
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                style = TextStyle(shadow = Shadow(Color.Black, Offset(3f, 4f), 8f))
            )
        }
    }
    } // Box (фон)
}

@Composable
private fun AchievementToast(
    def: com.ninthbalcony.pushuprpg.utils.AchievementDef,
    language: String,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(def.id) {
        visible = true
        kotlinx.coroutines.delay(2800)
        visible = false
        kotlinx.coroutines.delay(400)
        onDismiss()
    }
    val context = LocalContext.current
    val resId = remember(def.imageRes) {
        context.resources.getIdentifier(def.imageRes, "drawable", context.packageName)
    }
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier.fillMaxSize().padding(top = 56.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(78.dp)
                    .clip(shape)
                    .background(Color(0xCC1A1310))
                    .border(2.dp, GoldAccent, shape)
            ) {
                // Background image of the achievement — semi-transparent.
                if (resId != 0) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.30f
                    )
                }
                // Dark gradient overlay for text legibility.
                Box(
                    modifier = Modifier.matchParentSize().background(
                        Brush.horizontalGradient(
                            listOf(Color(0xCC000000), Color(0x55000000), Color(0xCC000000))
                        )
                    )
                )
                // Foreground content.
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (resId != 0) {
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = null,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, GoldAccent, RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (language == "ru") "🏆 ДОСТИЖЕНИЕ" else "🏆 ACHIEVEMENT UNLOCKED",
                            color = GoldAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (language == "ru") def.nameRu else def.nameEn,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(shadow = Shadow(Color.Black, Offset(1f, 1f), 3f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuestShortcutButton(
    language: String,
    quests: List<com.ninthbalcony.pushuprpg.utils.ActiveQuest>,
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

    val infiniteTransition = rememberInfiniteTransition(label = "questPulse")
    val pulseDot by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "questDot"
    )

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
            if (readyCount > 0) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(GoldAccent.copy(alpha = pulseDot), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
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
fun LeaderboardShortcutButton(
    language: String,
    onClick: () -> Unit
) {
    val label = if (language == "ru") "Таблица лидеров" else "Leaderboard"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🏅", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                color = TextPrimary,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.weight(1f))
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
            if (state.prestigeLevel > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-8).dp)
                        .size(20.dp)
                        .background(GoldAccent, CircleShape)
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.prestigeLevel.toString(),
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
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
    activeEvent: com.ninthbalcony.pushuprpg.data.model.GameEvent?,
    eventEndTime: Long
) {
    val context = LocalContext.current
    val borderColor = if (activeEvent != null) GoldAccent else OrangeAccent.copy(alpha = 0.3f)

    val bgImageName = activeEvent?.let {
        when (it.type) {
            com.ninthbalcony.pushuprpg.data.model.EventType.DROP_RATE_BONUS,
            com.ninthbalcony.pushuprpg.data.model.EventType.RARE_DROP_BONUS -> "event_bg_luck"
            com.ninthbalcony.pushuprpg.data.model.EventType.POWER_BONUS,
            com.ninthbalcony.pushuprpg.data.model.EventType.BERSERKER,
            com.ninthbalcony.pushuprpg.data.model.EventType.BATTLE_SPEED_BONUS -> "event_bg_power"
            com.ninthbalcony.pushuprpg.data.model.EventType.ARMOR_BONUS,
            com.ninthbalcony.pushuprpg.data.model.EventType.HEALTH_BONUS -> "event_bg_defense"
            com.ninthbalcony.pushuprpg.data.model.EventType.REGEN_BONUS,
            com.ninthbalcony.pushuprpg.data.model.EventType.XP_BONUS -> "event_bg_spirit"
            com.ninthbalcony.pushuprpg.data.model.EventType.NIGHTMARE -> "event_bg_nightmare"
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
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier.fillMaxWidth().clickable { onTotalClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${state.pushUpsToday}",
                    fontSize = 45.sp,
                    color = OrangeAccent,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(3f, 4f),
                        blurRadius = 3f
                        )
                    )
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
                        Text(text = "-1", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                                fontSize = 16.sp,
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
                        Text(text = "+1", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
    totalStats: com.ninthbalcony.pushuprpg.utils.TotalStats?,
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
    punchesRemaining: Int = 25,
    punchCooldownUntil: Long = 0L,
    lastPunchDamage: Int? = null,
    goblinTimeRemaining: Long = 0L,
    battleAnimation: com.ninthbalcony.pushuprpg.data.model.BattleHit? = null,
    soundEnabled: Boolean = true,
    onPunch: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bgResId = remember {
        val index = (1..5).random()
        context.resources.getIdentifier("bg_fight_$index", "drawable", context.packageName)
    }

    // Анимация урона от Punch
    var showDmg by remember { mutableStateOf(false) }
    var dmgVal by remember { mutableStateOf(0) }
    val dmgAlpha by animateFloatAsState(if (showDmg) 1f else 0f, tween(200), label = "da")
    val dmgOffsetY by animateFloatAsState(if (showDmg) -36f else 0f, tween(700), label = "do")
    LaunchedEffect(lastPunchDamage) {
        if (lastPunchDamage != null && lastPunchDamage > 0) {
            dmgVal = lastPunchDamage; showDmg = true
            kotlinx.coroutines.delay(800L); showDmg = false
        }
    }

    // Сотрясение экрана
    val shakeX = remember { Animatable(0f) }
    LaunchedEffect(lastPunchDamage) {
        if (lastPunchDamage != null && lastPunchDamage > 0) {
            val mag = if (lastPunchDamage > 50) 6f else 3f
            repeat(3) { shakeX.animateTo(mag, tween(40)); shakeX.animateTo(-mag, tween(40)) }
            shakeX.animateTo(0f, tween(40))
        }
    }
    LaunchedEffect(state.monstersKilled) {
        if (state.monstersKilled > 0) {
            repeat(5) { shakeX.animateTo(8f, tween(40)); shakeX.animateTo(-8f, tween(40)) }
            shakeX.animateTo(0f, tween(40))
        }
    }

    // Живой счётчик кулдауна (обновляется каждые 100ms)
    val now by produceState(System.currentTimeMillis()) {
        while (true) { kotlinx.coroutines.delay(100L); value = System.currentTimeMillis() }
    }
    val cooldownSec = ((punchCooldownUntil - now) / 1000L + 1).coerceAtLeast(0)

    val heroResId = com.ninthbalcony.pushuprpg.ui.util.rememberAvatarResId(
        avatarId = state.heroAvatar.ifEmpty { "avatar_base" },
        gender = state.playerGender
    )

    // ── Battle chain animation (Save → серия ударов) ─────────────────────────
    // Когда battleAnimation != null — показываем damage number и HP-bar монстра
    // в анимированном состоянии. Звук удара играем на каждом новом хите.
    // Когда chain заканчивается (null) — гасим damage-number через 200ms.
    LaunchedEffect(battleAnimation) {
        val hit = battleAnimation
        if (hit == null) {
            kotlinx.coroutines.delay(200L)
            showDmg = false
        } else {
            SoundManager.playPunch(soundEnabled)
            dmgVal = hit.damage
            showDmg = true
            val mag = if (hit.isCrit) 6f else 2f
            shakeX.animateTo(mag, tween(30))
            shakeX.animateTo(-mag, tween(30))
            shakeX.animateTo(0f, tween(30))
        }
    }

    // ── Countdown до следующего auto-tick'а (5 минут) ─────────────────────────
    val nextTickAt = state.lastBattleTick + 5 * 60 * 1000L
    val ticksLeftSec = ((nextTickAt - now) / 1000L).coerceAtLeast(0L)
    val countdownText = if (state.isPlayerDead) "—:—" else "%d:%02d".format(ticksLeftSec / 60, ticksLeftSec % 60)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .then(modifier)
            .graphicsLayer { translationX = shakeX.value }
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
                    if (state.isGoldenGoblinActive) {
                        Text(
                            text = "∞ HP",
                            fontSize = 11.sp, color = GoldAccent,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        LinearProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = GoldAccent,
                            trackColor = HpBarBackground
                        )
                    } else {
                        // Если идёт chain-анимация — показываем "анимированное" HP монстра
                        // (включая переход на нового монстра при killed-хите).
                        val displayHp = battleAnimation?.monsterHpAfter ?: state.monsterCurrentHp
                        val displayMaxHp = battleAnimation?.monsterMaxHp ?: state.monsterMaxHp
                        val monsterHpPercent = if (displayMaxHp > 0)
                            displayHp.toFloat() / displayMaxHp else 0f
                        Text(
                            text = "$displayHp/$displayMaxHp HP",
                            fontSize = 11.sp, color = HpBarLow,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        LinearProgressIndicator(
                            progress = { monsterHpPercent.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = HpBarLow,
                            trackColor = HpBarBackground
                        )
                        // Countdown до следующей атаки монстра в auto-tick.
                        Text(
                            text = if (state.language == "ru") "След. атака: $countdownText"
                                   else "Next attack: $countdownText",
                            fontSize = 10.sp,
                            color = TextMuted,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                        )
                    }
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
                    if (heroResId != 0) {
                        Image(
                            painter = painterResource(id = heroResId),
                            contentDescription = "Hero",
                            modifier = Modifier
                                .size(110.dp)
                                .alpha(if (state.isPlayerDead) 0.4f else 1f),
                            contentScale = ContentScale.Fit
                        )
                    }
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
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFCC00),
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Red,
                            offset = Offset(3f, 4f),
                            blurRadius = 4f
                        )
                    )
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    if (state.isGoldenGoblinActive) {
                        val secondsLeft = (goblinTimeRemaining / 1000L).coerceAtLeast(0L)
                        Text(
                            text = "⏱ ${secondsLeft}s",
                            fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                        DrawableImage(name = "monster_goblin_gold", modifier = Modifier.size(110.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (state.language == "ru") "Золотой Гоблин" else "Golden Goblin",
                            fontSize = 12.sp, color = GoldAccent,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        val monster = MonsterUtils.getMonsterByLevel(state.monsterLevel)
                        // Во время chain-анимации показываем имя/картинку из текущего хита —
                        // если убили на 17-м хите, кадры 18..30 покажут уже нового монстра.
                        val monsterName = battleAnimation?.monsterName ?: MonsterUtils.getMonsterName(monster, state.language)
                        Box(contentAlignment = Alignment.TopCenter) {
                            val imageRes = battleAnimation?.monsterImageRes
                                ?: state.monsterImageRes.takeIf { it.isNotBlank() && it != "monster_01" }
                                ?: monster.imageRes
                            DrawableImage(name = imageRes, modifier = Modifier.size(110.dp))
                            if (dmgAlpha > 0f) {
                                val isCritHit = battleAnimation?.isCrit == true
                                Text(
                                    text = if (isCritHit) "💥 -$dmgVal CRIT!" else "-$dmgVal",
                                    color = (if (isCritHit) Color(0xFFFFCC00) else Color(0xFFFF4444))
                                        .copy(alpha = dmgAlpha),
                                    fontSize = if (isCritHit) 24.sp else 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.offset(y = dmgOffsetY.dp),
                                    style = TextStyle(shadow = Shadow(Color.Black, Offset(1f, 2f), 4f))
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$monsterName (${state.monsterLevel} lvl)",
                            fontSize = 12.sp, color = TextSecondary,
                            textAlign = TextAlign.Center, maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Кнопка Punch
            Spacer(modifier = Modifier.height(0.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onPunch,
                    enabled = if (state.isGoldenGoblinActive) !state.isPlayerDead
                              else punchesRemaining > 0 && cooldownSec == 0L && !state.isPlayerDead,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isGoldenGoblinActive) GoldAccent else ButtonRed,
                        disabledContainerColor = ButtonGray
                    ),
                    modifier = Modifier.height(34.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = when {
                            state.isPlayerDead -> "💀 Dead"
                            state.isGoldenGoblinActive -> "👊 PUNCH!"
                            punchesRemaining == 0 -> "No punches"
                            cooldownSec > 0L -> "⏳ ${cooldownSec}s"
                            else -> "👊 Punch"
                        },
                        fontSize = if (state.isGoldenGoblinActive) 15.sp else 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.isGoldenGoblinActive) Color.Black else Color.White
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                if (state.isGoldenGoblinActive) {
                    val t = (state.goldenGoblinPunchCount / 60f).coerceIn(0f, 1f)
                    val counterColor = Color(red = 1f, green = 1f - t, blue = 1f - t)
                    val counterScale = 1f + (state.goldenGoblinPunchCount / 10) * 0.01f
                    Text(
                        text = "${state.goldenGoblinPunchCount}",
                        color = counterColor,
                        fontSize = (14f * counterScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "$punchesRemaining/25",
                        color = if (punchesRemaining > 0) TextSecondary else TextMuted,
                        fontSize = 8.sp
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
            .padding(6.dp)
    ) {
        val recentLogs = logs.take(4)

        if (recentLogs.isEmpty()) {
            Text(
                text = AppStrings.t(language, "battle_soon"),
                fontSize = 12.sp,
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

        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = AppStrings.t(language, "view_all_logs"),
            fontSize = 10.sp,
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

@Composable
private fun GoblinEndDialog(teethEarned: Int, language: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val bgResId = remember {
        context.resources.getIdentifier("bg_goblin_escape", "drawable", context.packageName)
    }
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
        ) {
            if (bgResId != 0) {
                Image(
                    painter = painterResource(id = bgResId),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.3f
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Text(
                text = if (language == "ru") "Гоблин сбежал!" else "Goblin escaped!",
                color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (language == "ru") "Ты получил $teethEarned 🦷!" else "You earned $teethEarned 🦷!",
                color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(AppStrings.t(language, "btn_continue"), color = Color.Black, fontWeight = FontWeight.Bold)
            }
            }
        }
    }
}

@Composable
private fun FallingCoinsOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "coins")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "coinPhase"
    )
    // x position (0-1), phase offset (0-1), coin radius px
    val coinData = remember {
        List(14) {
            Triple(
                kotlin.random.Random.nextFloat(),
                kotlin.random.Random.nextFloat(),
                7f + kotlin.random.Random.nextFloat() * 9f
            )
        }
    }
    Canvas(modifier = modifier) {
        coinData.forEach { (x, offset, radius) ->
            val y = ((phase + offset) % 1f) * size.height
            drawCircle(
                color = androidx.compose.ui.graphics.Color(0xFFFFD700).copy(alpha = 0.75f),
                radius = radius,
                center = androidx.compose.ui.geometry.Offset(x * size.width, y)
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

private fun vibrate(context: android.content.Context) {
    val vib = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val vm = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE)
                as android.os.VibratorManager
        vm.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
    }
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        vib.vibrate(android.os.VibrationEffect.createOneShot(80, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vib.vibrate(80)
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 920)
@Composable
private fun MainMenuScreenPreview() {
    val vm = remember { GameViewModel(com.ninthbalcony.pushuprpg.ui.preview.FakeGameRepository()) }
    MainMenuScreen(
        viewModel = vm,
        onNavigateToInventory = {},
        onNavigateToLogs = {},
        onNavigateToStatistics = {},
        onNavigateToSettings = {},
        onNavigateToShop = {},
        onNavigateToQuests = {},
        onNavigateToProgress = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun BattleArenaPreview() {
    val mockState = GameStateEntity(
        playerLevel = 2,
        currentHp = 97,
        baseHealth = 120,
        monsterLevel = 1,
        monsterCurrentHp = 55,
        monsterMaxHp = 61,
        isPlayerDead = false,
        playerName = "Hero",
        heroAvatar = "",
        language = "en"
    )
    BattleArena(state = mockState, maxHp = 120)
}

@Preview(showBackground = true)
@Composable
private fun PushUpCounterPreview() {
    val mockState = GameStateEntity(
        pushUpsToday = 297,
        language = "en"
    )
    PushUpCounter(
        state = mockState,
        inputValue = 0,
        language = "en",
        onAddToInput = {},
        onReset = {},
        onSave = {},
        onTotalClick = {},
        onShopClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun StatsPanelPreview() {
    val mockState = GameStateEntity(
        basePower = 5,
        baseArmor = 0,
        baseHealth = 120,
        baseLuck = 0.3f,
        teeth = 8
    )
    StatsPanel(
        state = mockState,
        totalStats = null,
        onClick = {}
    )
}