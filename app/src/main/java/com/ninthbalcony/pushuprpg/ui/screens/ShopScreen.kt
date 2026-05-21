package com.ninthbalcony.pushuprpg.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ninthbalcony.pushuprpg.data.model.EnchantResult
import com.ninthbalcony.pushuprpg.data.model.ForgeResult
import com.ninthbalcony.pushuprpg.data.model.Item
import com.ninthbalcony.pushuprpg.ui.theme.*
import com.ninthbalcony.pushuprpg.utils.AppStrings
import com.ninthbalcony.pushuprpg.utils.EventUtils
import com.ninthbalcony.pushuprpg.utils.ItemUtils
import com.ninthbalcony.pushuprpg.utils.ShopUtils
import com.ninthbalcony.pushuprpg.utils.NightSpinReward
import com.ninthbalcony.pushuprpg.utils.NightStatBoostType
import com.ninthbalcony.pushuprpg.utils.SpinReward
import com.ninthbalcony.pushuprpg.utils.SpinResult
import com.ninthbalcony.pushuprpg.utils.SpinUtils
import com.ninthbalcony.pushuprpg.utils.toRibbonType
import com.ninthbalcony.pushuprpg.ui.GameViewModel
import com.ninthbalcony.pushuprpg.utils.SoundManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeIn
import androidx.compose.ui.tooling.preview.Preview
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.ninthbalcony.pushuprpg.ui.preview.FakeGameRepository

// Веса иконок для ленты (отражают вероятности наград)
private val SPIN_WEIGHTED_TYPES = listOf(
    "common_spin",   "common_spin",   "common_spin",   "common_spin",   "common_spin",
    "teeth",         "teeth",         "teeth",         "teeth",
    "uncommon_spin", "uncommon_spin", "uncommon_spin", "uncommon_spin",
    "rare_spin",     "rare_spin",     "rare_spin",     "rare_spin",
    "clover_box",    "clover_box",
    "boss_cube"
)

private val NIGHT_GRINDSTONE_EVENT_IDS = setOf(6, 9, 10, 11)

/** Генерирует список иконок для ленты; winnerType фиксируется на позиции 22 */
private fun buildSpinRibbon(winnerType: String?): List<String> {
    val items = MutableList(60) { SPIN_WEIGHTED_TYPES.random() }
    if (winnerType != null) items[22] = winnerType
    return items
}

private fun buildNightSpinRibbon(winnerType: String?): List<String> =
    SpinUtils.buildNightSpinRibbon(winnerType)

@Composable
fun ShopScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState(initial = null)
    val shopItems by viewModel.shopItems.collectAsState(initial = emptyList())
    val language = gameState?.language ?: "en"
    val shopContext = LocalContext.current
    val isInspection = androidx.compose.ui.platform.LocalInspectionMode.current
    DisposableEffect(Unit) {
        if (!isInspection) {
            val enabled = shopContext.getSharedPreferences("pushup_prefs", android.content.Context.MODE_PRIVATE)
                .getBoolean("sounds_enabled", true)
            SoundManager.playMusic(shopContext, "music_shop", enabled)
        }
        onDispose {
            if (!isInspection) {
                val enabled = shopContext.getSharedPreferences("pushup_prefs", android.content.Context.MODE_PRIVATE)
                    .getBoolean("sounds_enabled", true)
                SoundManager.playMusic(shopContext, "music_main", enabled)
            }
        }
    }
    val shopSoundEnabled = remember {
        if (isInspection) true
        else shopContext.getSharedPreferences("pushup_prefs", android.content.Context.MODE_PRIVATE)
            .getBoolean("sounds_enabled", true)
    }
    val shopVibrationEnabled = remember {
        if (isInspection) false
        else shopContext.getSharedPreferences("pushup_prefs", android.content.Context.MODE_PRIVATE)
            .getBoolean("vibration_enabled", true)
    }
    val state = gameState ?: return
    val isNightGrindstone = state.activeEventId in NIGHT_GRINDSTONE_EVENT_IDS &&
        EventUtils.isEventActive(state.eventEndTime)
    val isNightSpin = isNightGrindstone
    val grindstoneMaxEnchant = if (isNightGrindstone) 25 else 9

    var selectedShopItem by remember { mutableStateOf<Item?>(null) }
    var showForgeItemPicker by remember { mutableStateOf(0) }
    var resultMessage by remember { mutableStateOf("") }
    var showResultDialog by remember { mutableStateOf(false) }
    var resultDialogCloverBg by remember { mutableStateOf(false) }
    var boughtItem by remember { mutableStateOf<Item?>(null) }
    var cloverBoxItem by remember { mutableStateOf<Item?>(null) }
    var showFreePointsDialog by remember { mutableStateOf(false) }
    var showMergedDialog by remember { mutableStateOf(false) }
    var mergedItem by remember { mutableStateOf<Item?>(null) }
    var showMergeFailDialog by remember { mutableStateOf(false) }
    var showEnchantedDialog by remember { mutableStateOf(false) }
    var showCursedDialog by remember { mutableStateOf(false) }
    var showNoTeethDialog by remember { mutableStateOf(false) }

    // Daily Spin state
    var showSpinResultDialog by remember { mutableStateOf(false) }
    var spinResultToShow by remember { mutableStateOf<SpinResult?>(null) }
    var showNightSpinResultDialog by remember { mutableStateOf(false) }
    var nightSpinResultToShow by remember { mutableStateOf<NightSpinReward?>(null) }
    var isSpinAnimating by remember { mutableStateOf(false) }
    // Лента генерируется в ShopScreen (гарантирует совпадение иконки и награды)
    var spinRibbonItems by remember { mutableStateOf(buildSpinRibbon(null)) }
    val spinResult by viewModel.spinResult.collectAsState()
    val nightSpinResult by viewModel.nightSpinResult.collectAsState()
    val availableSpins by viewModel.availableSpins.collectAsState()
    val adViewsToday by viewModel.adViewsToday.collectAsState()

    // --- Forge flash animation trigger ---
    var forgeFlash by remember { mutableStateOf(false) }
    LaunchedEffect(forgeFlash) {
        if (forgeFlash) { kotlinx.coroutines.delay(600); forgeFlash = false }
    }

    // --- Enchant shake animation trigger ---
    var enchantShake by remember { mutableStateOf(false) }
    LaunchedEffect(enchantShake) {
        if (enchantShake) { kotlinx.coroutines.delay(500); enchantShake = false }
    }

    // --- Spin loop sound ---
    LaunchedEffect(isSpinAnimating) {
        if (isSpinAnimating) SoundManager.playSpinLoop(shopSoundEnabled)
        else SoundManager.stopSpinLoop()
    }

    val forgeSlot1Item = remember(state.forgeSlot1) {
        if (state.forgeSlot1.isNotEmpty()) ItemUtils.getItemById(state.forgeSlot1) else null
    }
    val forgeSlot2Item = remember(state.forgeSlot2) {
        if (state.forgeSlot2.isNotEmpty()) ItemUtils.getItemById(state.forgeSlot2) else null
    }

    val selectedEnchantItem by viewModel.selectedEnchantItem.collectAsState(initial = null)
    var showEnchantItemPicker by remember { mutableStateOf(false) }

    val adRewardPending by viewModel.adRewardPending.collectAsState()
    val adSpinPending by viewModel.adSpinPending.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadShop()
        viewModel.refreshSpinCounters()
    }

    LaunchedEffect(state.shopLastRefresh) {
        val remaining = com.ninthbalcony.pushuprpg.utils.ShopUtils.SHOP_REFRESH_INTERVAL_MS -
            (System.currentTimeMillis() - state.shopLastRefresh)
        if (remaining > 0) {
            kotlinx.coroutines.delay(remaining)
            viewModel.loadShop()
        }
    }

    // Когда результат пришёл: строим ленту с победителем на поз.22, ждём recomposition, затем анимация
    LaunchedEffect(spinResult) {
        val result = spinResult ?: return@LaunchedEffect
        if (isSpinAnimating || showSpinResultDialog) return@LaunchedEffect
        // 1. Строим ленту с правильным типом на позиции 22 (до старта анимации)
        spinRibbonItems = buildSpinRibbon(result.reward.type)
        spinResultToShow = result
        // 2. Ждём recomposition (~4 кадра = 64ms) — гарантирует что лента перестроилась
        kotlinx.coroutines.delay(64)
        isSpinAnimating = true
    }

    LaunchedEffect(nightSpinResult) {
        val result = nightSpinResult ?: return@LaunchedEffect
        if (isSpinAnimating || showNightSpinResultDialog) return@LaunchedEffect
        spinRibbonItems = buildNightSpinRibbon(result.toRibbonType())
        nightSpinResultToShow = result
        kotlinx.coroutines.delay(64)
        isSpinAnimating = true
    }

    if (adRewardPending > 0) {
        com.ninthbalcony.pushuprpg.ui.dialogs.RewardedAdDialog(
            title = AppStrings.t(language, "ad_title"),
            description = AppStrings.t(language, "ad_reward_desc"),
            rewardText = "+$adRewardPending 🦷",
            language = language,
            onWatchAd = { (context as? android.app.Activity)?.let { viewModel.playRewardedAd(it) } },
            onDecline = { viewModel.dismissAdReward() },
            onDismiss = { viewModel.dismissAdReward() }
        )
    }

    if (adSpinPending) {
        com.ninthbalcony.pushuprpg.ui.dialogs.RewardedAdDialog(
            title = AppStrings.t(language, "ad_title"),
            description = AppStrings.t(language, "ad_reward_desc"),
            rewardText = "+1 🎰",
            language = language,
            onWatchAd = { (context as? android.app.Activity)?.let { viewModel.playAdSpin(it) } },
            onDecline = { viewModel.dismissAdSpin() },
            onDismiss = { viewModel.dismissAdSpin() }
        )
    }

    if (showForgeItemPicker > 0) {
        ForgeItemPickerDialog(
            inventoryItems = viewModel.getInventoryItems(state),
            excludeItemId = if (showForgeItemPicker == 1) state.forgeSlot2 else state.forgeSlot1,
            language = language,
            onSelect = { item ->
                viewModel.setForgeSlot(showForgeItemPicker, item.id)
                showForgeItemPicker = 0
            },
            onDismiss = { showForgeItemPicker = 0 }
        )
    }

    if (showResultDialog) {
        ResultDialog(
            message = resultMessage,
            showBg = resultDialogCloverBg,
            onDismiss = { showResultDialog = false; resultDialogCloverBg = false }
        )
    }

    boughtItem?.let { item ->
        BuySuccessDialog(
            item = item,
            language = language,
            onDismiss = { boughtItem = null }
        )
    }

    cloverBoxItem?.let { item ->
        CloverBoxResultDialog(
            item = item,
            language = language,
            onDismiss = { cloverBoxItem = null }
        )
    }

    if (showFreePointsDialog) {
        FreePointsResultDialog(
            language = language,
            onDismiss = { showFreePointsDialog = false }
        )
    }

    mergedItem?.let { item ->
        if (showMergedDialog) {
            MergedDialog(
                item = item,
                language = language,
                onDismiss = { showMergedDialog = false; mergedItem = null }
            )
        }
    }

    if (showMergeFailDialog) {
        MergeFailDialog(
            language = language,
            onDismiss = { showMergeFailDialog = false }
        )
    }

    if (showEnchantedDialog) {
        EnchantedDialog(onDismiss = { showEnchantedDialog = false })
    }

    if (showCursedDialog) {
        CursedDialog(language = language, onDismiss = { showCursedDialog = false })
    }

    if (showNoTeethDialog) {
        NoTeethDialog(language = language, onDismiss = { showNoTeethDialog = false })
    }

    spinResultToShow?.let { spin ->
        if (showSpinResultDialog) {
            SpinResultDialog(
                result = spin.reward,
                wonItemIds = spin.wonItemIds,
                language = language,
                onDismiss = {
                    showSpinResultDialog = false
                    spinResultToShow = null
                    viewModel.clearSpinResult()
                    viewModel.refreshSpinCounters()
                }
            )
        }
    }

    nightSpinResultToShow?.let { reward ->
        if (showNightSpinResultDialog) {
            NightSpinResultDialog(
                reward = reward,
                language = language,
                onDismiss = {
                    showNightSpinResultDialog = false
                    nightSpinResultToShow = null
                    viewModel.clearNightSpinResult()
                    viewModel.refreshSpinCounters()
                }
            )
        }
    }

    if (showEnchantItemPicker) {
        EnchantItemPickerDialog(
            inventoryItems = viewModel.getInventoryItems(state),
            language = language,
            getEnchantLevel = { item -> viewModel.getEnchantLevel(state, item.id) },
            maxEnchant = grindstoneMaxEnchant,
            onSelect = { item ->
                viewModel.selectEnchantItem(item)
                showEnchantItemPicker = false
            },
            onDismiss = { showEnchantItemPicker = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        ScreenBackground("bg_shop_overall")
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Text(
                text = AppStrings.t(language, "shop"),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Text(text = "🦷", fontSize = 16.sp)
                Text(
                    text = " ${state.teeth}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE0E0E0)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
                .navigationBarsPadding(), // добавили
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShopSection(
                shopItems = shopItems,
                selectedItem = selectedShopItem,
                state = state,
                language = language,
                onSelectItem = { item ->
                    selectedShopItem = if (selectedShopItem?.id == item.id) null else item
                },
                onBuy = { item ->
                    viewModel.buyShopItem(item.id) { success ->
                        if (success) {
                            boughtItem = item
                            selectedShopItem = null
                        } else {
                            resultMessage = AppStrings.t(language, "insufficient_teeth")
                            showResultDialog = true
                        }
                    }
                },
                onReroll = { viewModel.rerollShop() }
            )

            if (state.playerLevel >= 4 || state.prestigeLevel >= 1) ForgeSection(
                slot1Item = forgeSlot1Item,
                slot2Item = forgeSlot2Item,
                language = language,
                flashSuccess = forgeFlash,
                vibrationEnabled = shopVibrationEnabled,
                context = shopContext,
                onSlot1Click = {
                    if (state.forgeSlot1.isNotEmpty()) {
                        // Если слот занят — очищаем его
                        viewModel.setForgeSlot(1, "")
                    } else {
                        showForgeItemPicker = 1
                    }
                },
                onSlot2Click = {
                    if (state.forgeSlot2.isNotEmpty()) {
                        // Если слот занят — очищаем его
                        viewModel.setForgeSlot(2, "")
                    } else {
                        showForgeItemPicker = 2
                    }
                },
                onMerge = {
                    SoundManager.playMerge(shopSoundEnabled)
                    if (shopVibrationEnabled) vibrate(shopContext)
                    viewModel.mergeItems { result ->
                        when (result) {
                            is ForgeResult.Success -> {
                                mergedItem = result.item
                                showMergedDialog = true
                                forgeFlash = true
                            }
                            is ForgeResult.Fail -> showMergeFailDialog = true
                            is ForgeResult.NoItems -> {
                                resultMessage = AppStrings.t(language, "forge_need_two")
                                showResultDialog = true
                            }
                        }
                    }
                },
                onRecycle = { viewModel.recycleForgeSlots() }
            ) else LockedSection(
                name = AppStrings.t(language, "forge"),
                unlockLevel = 4,
                backgroundKey = "bg_forge_locked",
                language = language
            )

            CloverBoxSection(
                cloverUsed = state.cloverBoxUsedToday,
                freePointsUsed = state.freePointsUsedToday,
                adShopViewCount = state.adShopViewCount,
                adShopLastViewTime = state.adShopLastViewTime,
                language = language,
                onCloverBox = {
                    viewModel.useCloverBox { result ->
                        if (result != null) {
                            cloverBoxItem = result
                        } else {
                            resultMessage = AppStrings.t(language, "clover_limit")
                            resultDialogCloverBg = true
                            showResultDialog = true
                        }
                    }
                },
                onFreePoints = {
                    viewModel.useFreePoints { success ->
                        if (success) {
                            showFreePointsDialog = true
                        } else {
                            resultMessage = AppStrings.t(language, "clover_limit")
                            resultDialogCloverBg = true
                            showResultDialog = true
                        }
                    }
                },
                onWatchAdReward = { viewModel.requestAdReward(25) }
            )

            // --- Daily Spin ---
            DailySpinSection(
                availableSpins = availableSpins,
                adViewsToday = adViewsToday,
                isSpinAnimating = isSpinAnimating,
                ribbonItems = spinRibbonItems,
                language = language,
                isNight = isNightSpin,
                vibrationEnabled = shopVibrationEnabled,
                context = shopContext,
                onSpin = {
                    SoundManager.playSpin(shopSoundEnabled)
                    if (isNightSpin) viewModel.performNightDailySpin()
                    else viewModel.performDailySpin()
                },
                onAdSpin = { viewModel.requestAdSpin() },
                onAnimationEnd = {
                    isSpinAnimating = false
                    if (isNightSpin) showNightSpinResultDialog = true
                    else showSpinResultDialog = true
                }
            )

            // --- Точильный камень ---
            val enchantInfo = selectedEnchantItem?.let { viewModel.getEnchantInfo(state, it) }
            if (state.playerLevel >= 6 || state.prestigeLevel >= 1) GrindstoneSection(
                state = state,
                language = language,
                inventoryItems = viewModel.getInventoryItems(state),
                selectedEnchantItem = selectedEnchantItem,
                isNightMode = isNightGrindstone,
                maxEnchant = grindstoneMaxEnchant,
                shakeSuccess = enchantShake,
                vibrationEnabled = shopVibrationEnabled,
                context = shopContext,
                onSelectItem = { item -> viewModel.selectEnchantItem(item) },
                onEnchant = {
                    SoundManager.playEnchant(shopSoundEnabled)
                    if (shopVibrationEnabled) vibrate(shopContext)
                    val item = selectedEnchantItem
                    if (item != null) {
                        val uniqueId = state.inventoryItems.split(",")
                            .filter { it.isNotEmpty() }
                            .firstOrNull { entry -> entry.split(":")[0].contains(item.id) }
                            ?.split(":")?.get(0) ?: item.id
                        viewModel.enchantItemWithCallback(uniqueId) { result ->
                            when (result) {
                                EnchantResult.SUCCESS        -> { showEnchantedDialog = true; enchantShake = true }
                                EnchantResult.FAILED         -> showCursedDialog = true
                                EnchantResult.NOT_ENOUGH_TEETH -> showNoTeethDialog = true
                                EnchantResult.MAX_LEVEL      -> {
                                    resultMessage = if (isNightGrindstone) {
                                        AppStrings.t(language, "night_mode_max")
                                    } else AppStrings.t(language, "enchant_max")
                                    showResultDialog = true
                                }
                            }
                        }
                    }
                },
                enchantChance = enchantInfo?.first ?: 0f,
                enchantCost = enchantInfo?.second ?: 0,
                getEnchantLevel = { item -> viewModel.getEnchantLevel(state, item.id) }
            ) else LockedSection(
                name = AppStrings.t(language, "grindstone"),
                unlockLevel = 6,
                backgroundKey = "bg_grind_locked",
                language = language
            )
        }
    } // Column
    } // Box (фон)
}

@Composable
private fun LockedSection(name: String, unlockLevel: Int, backgroundKey: String, language: String) {
    val context = LocalContext.current
    val bgResId = remember(backgroundKey) {
        context.resources.getIdentifier(backgroundKey, "drawable", context.packageName)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, TextMuted.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
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
            modifier = Modifier.padding(vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = name, color = TextMuted, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${AppStrings.t(language, "unlocks_at_level")} $unlockLevel",
                color = TextMuted.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun ShopSection(
    shopItems: List<Item>,
    selectedItem: Item?,
    state: com.ninthbalcony.pushuprpg.data.db.GameStateEntity,
    language: String,
    onSelectItem: (Item) -> Unit,
    onBuy: (Item) -> Unit,
    onReroll: () -> Unit
) {
    val context = LocalContext.current
    val bgResId = remember {
        context.resources.getIdentifier("bg_shop", "drawable", context.packageName)
    }

    var currentTimeMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1_000L)
            currentTimeMs = System.currentTimeMillis()
        }
    }
    val resetIntervalMs = 5L * 60 * 1000
    val effectiveCount = if (currentTimeMs - state.shopRerollResetTime >= resetIntervalMs) 0 else state.shopRerollCount
    val rerollCost = (effectiveCount + 1) * 3
    val rerollResetLabel: String? = if (effectiveCount > 0) {
        val remaining = resetIntervalMs - (currentTimeMs - state.shopRerollResetTime)
        if (remaining > 0) "${remaining / 60000}m ${(remaining % 60000) / 1000}s" else null
    } else null

    Box(
        modifier = Modifier
            .fillMaxWidth()
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
                Text(
                    text = AppStrings.t(language, "shop"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${AppStrings.t(language, "shop_update_label")} ${ShopUtils.getTimeUntilRefresh(state.shopLastRefresh)}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Button(
                        onClick = onReroll,
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonGray),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "${AppStrings.t(language, "btn_reroll")} $rerollCost 🦷",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (shopItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = AppStrings.t(language, "shop_empty"),
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    shopItems.forEach { item ->
                        ShopItemCell(
                            item = item,
                            isSelected = selectedItem?.id == item.id,
                            language = language,
                            onClick = { onSelectItem(item) },
                            modifier = Modifier.width(72.dp)
                        )
                    }
                    repeat(ShopUtils.SHOP_SLOTS - shopItems.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(72.dp)
                                .aspectRatio(1f)
                                .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                                .border(1.dp, TextMuted.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        )
                    }
                }
            }

            if (selectedItem != null) {
                Spacer(modifier = Modifier.height(10.dp))
                val price = ShopUtils.getBuyPrice(selectedItem.rarity)
                val canAfford = state.teeth >= price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = ItemUtils.getItemName(selectedItem, language),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(ItemUtils.getRarityColor(selectedItem.rarity))
                        )
                        Text(
                            text = "🦷 $price",
                            fontSize = 13.sp,
                            color = if (canAfford) Color(0xFFE0E0E0) else HpBarLow
                        )
                    }
                    Button(
                        onClick = { onBuy(selectedItem) },
                        enabled = canAfford,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ButtonGreen,
                            disabledContainerColor = ButtonGray
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = AppStrings.t(language, "btn_buy"),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (rerollResetLabel != null)
                    "${AppStrings.t(language, "reroll_reset_in")} $rerollResetLabel"
                else
                    (if (language == "ru") "Стоимость рерола: $rerollCost 🦷 (×3 за каждый рерол)" else "Reroll cost: $rerollCost 🦷 (×3 each reroll)"),
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ShopItemCell(
    item: Item,
    isSelected: Boolean,
    language: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rarityColor = Color(ItemUtils.getRarityColor(item.rarity))
    val context = LocalContext.current
    val resId = remember(item.image_id) {
        context.resources.getIdentifier(item.image_id, "drawable", context.packageName)
    }
    val price = ShopUtils.getBuyPrice(item.rarity)

    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                .border(
                    width = if (isSelected) 2.dp else 1.5.dp,
                    color = if (isSelected) GoldAccent else rarityColor,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (resId != 0) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = item.name_en,
                    modifier = Modifier.fillMaxSize().padding(6.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(text = getItemEmojiForShop(item.slot), fontSize = 24.sp)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "🦷 $price",
            fontSize = 11.sp,
            color = Color(0xFFE0E0E0),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ForgeSection(
    slot1Item: Item?,
    slot2Item: Item?,
    language: String,
    flashSuccess: Boolean = false,
    vibrationEnabled: Boolean = false,
    context: android.content.Context? = null,
    onSlot1Click: () -> Unit,
    onSlot2Click: () -> Unit,
    onMerge: () -> Unit,
    onRecycle: () -> Unit
) {
    val context = LocalContext.current
    val bgResId = remember {
        context.resources.getIdentifier("bg_forge", "drawable", context.packageName)
    }
    val mergeBtnBg = remember {
        context.resources.getIdentifier("bg_merge_btn", "drawable", context.packageName)
    }
    val mergeBorderColor by animateColorAsState(
        targetValue = if (flashSuccess) Color(0xFFFFD700) else Color(0xFFBB6307),
        animationSpec = tween(300),
        label = "forgeBorder"
    )

    var mergeSparkActive by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
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

        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = AppStrings.t(language, "forge"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(DarkSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val blacksmithRes = remember {
                        context.resources.getIdentifier("img_blacksmith", "drawable", context.packageName)
                    }
                    if (blacksmithRes != 0) {
                        Image(
                            painter = painterResource(id = blacksmithRes),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(text = "⚒️", fontSize = 40.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ForgeSlot(item = slot1Item, label = "1", onClick = onSlot1Click)
                        ForgeSlot(item = slot2Item, label = "2", onClick = onSlot2Click)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Ширина строки = 2 слота: (64+8+64)=136dp = Merge(86) + gap(6) + Recycle(44)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Merge button
                        Box(
                            modifier = Modifier
                                .width(92.dp)
                                .height(44.dp)
                                .background(
                                    color = Color(0xFF0b0f02),  // новый цвет фона
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 2.dp,
                                    color = mergeBorderColor,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = slot1Item != null && slot2Item != null) {
                                    mergeSparkActive = true
                                    onMerge()
                                }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (mergeBtnBg != 0 && slot1Item != null && slot2Item != null) {
                                Image(
                                    painter = painterResource(id = mergeBtnBg),
                                    contentDescription = null,
                                    modifier = Modifier.matchParentSize(),
                                    contentScale = ContentScale.Crop,
                                    alpha = 0.3f
                                )
                            }
                            Text(
                                text = AppStrings.t(language, "btn_merge"),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC34017),  // новый цвет текста
                                fontSize = 15.sp,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            SparkBurst(
                                active = mergeSparkActive,
                                modifier = Modifier.matchParentSize(),
                                onComplete = { mergeSparkActive = false }
                            )
                        }
                        // Recycle button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF003300), RoundedCornerShape(8.dp))
                                .border(width = 2.dp,color = Color(0xFF145727), shape = RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onRecycle() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("♻", fontSize = 26.sp, color = Color(0xFF90EE90))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Com 50% • Unc 20% • Rare 10% • Epic 4% • Leg 1% • Fail 15%",
                fontSize = 11.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ForgeSlot(
    item: Item?,
    label: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(DarkSurfaceVariant, RoundedCornerShape(10.dp))
            .border(
                2.dp,
                if (item != null) Color(ItemUtils.getRarityColor(item.rarity))
                else OrangeAccent.copy(alpha = 0.4f),
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (item != null) {
            val resId = remember(item.image_id) {
                context.resources.getIdentifier(item.image_id, "drawable", context.packageName)
            }
            if (resId != 0) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = item.name_en,
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(text = getItemEmojiForShop(item.slot), fontSize = 32.sp)
            }
        } else {
            Text(
                text = "+",
                fontSize = 28.sp,
                color = OrangeAccent.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun CloverBoxSection(
    cloverUsed: Int,
    freePointsUsed: Int,
    adShopViewCount: Int,
    adShopLastViewTime: Long,
    language: String,
    onCloverBox: () -> Unit,
    onFreePoints: () -> Unit,
    onWatchAdReward: () -> Unit
) {
    var adCurrentTimeMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(adShopLastViewTime) {
        while (true) {
            kotlinx.coroutines.delay(1_000L)
            adCurrentTimeMs = System.currentTimeMillis()
        }
    }
    val adCooldownMs = minOf(30_000L, adShopViewCount.toLong() * 5_000L)
    val adCooldownRemaining = (adCooldownMs - (adCurrentTimeMs - adShopLastViewTime)).coerceAtLeast(0L)
    val onAdCooldown = adCooldownRemaining > 0

    val context = LocalContext.current
    val bgResId = remember {
        context.resources.getIdentifier("bg_clover", "drawable", context.packageName)
    }
    val cloverBoxRes = remember {
        context.resources.getIdentifier("img_clover_box", "drawable", context.packageName)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
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
            Text(
                text = AppStrings.t(language, "clover_box"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(DarkSurfaceVariant, RoundedCornerShape(10.dp))
                        .border(1.dp, HealthColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (cloverBoxRes != 0) {
                        Image(
                            painter = painterResource(id = cloverBoxRes),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(1.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(text = "📦", fontSize = 40.sp)
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = AppStrings.t(language, "clover_free_item"),
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Text(text = "${cloverUsed}/2", fontSize = 11.sp, color = TextMuted)
                        }
                        Button(
                            onClick = onCloverBox,
                            enabled = cloverUsed < 2,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ButtonGreen,
                                disabledContainerColor = ButtonGray
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text(
                                text = AppStrings.t(language, "btn_get"),
                                fontSize = 13.sp
                            )
                        }
                    }

                    HorizontalDivider(color = DarkSurfaceVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = AppStrings.t(language, "clover_free_pts"),
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Text(text = "${freePointsUsed}/2", fontSize = 11.sp, color = TextMuted)
                        }
                        Button(
                            onClick = onFreePoints,
                            enabled = freePointsUsed < 2,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ButtonGreen,
                                disabledContainerColor = ButtonGray
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text(
                                text = AppStrings.t(language, "btn_get"),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Common 70% • Uncommon 20% • Rare 10%",
                fontSize = 11.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = DarkSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎬", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(AppStrings.t(language, "ad_reward_title"), fontSize = 13.sp, color = TextPrimary)
                        Text("+20 🦷", fontSize = 12.sp, color = TextSecondary)
                        if (onAdCooldown) {
                            Text("${adCooldownRemaining / 1000}s", fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
                Button(
                    onClick = onWatchAdReward,
                    enabled = !onAdCooldown,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangeAccent,
                        disabledContainerColor = ButtonGray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(AppStrings.t(language, "btn_watch_ad"), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun ForgeItemPickerDialog(
    inventoryItems: List<Item>,
    excludeItemId: String = "",
    language: String,
    onSelect: (Item) -> Unit,
    onDismiss: () -> Unit
) {
    val availableItems = if (excludeItemId.isNotEmpty()) {
        inventoryItems.filter { it.id != excludeItemId }
    } else {
        inventoryItems
    }
    val context = LocalContext.current
    val bgResId = remember { context.resources.getIdentifier("bg_actach_panel", "drawable", context.packageName) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
        ) {
            if (bgResId != 0) Image(
                painter = painterResource(id = bgResId),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.25f
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = AppStrings.t(language, "item_picker_title"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (availableItems.isEmpty()) {
                    Text(
                        text = AppStrings.t(language, "item_picker_empty"),
                        color = TextMuted,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    Column(modifier = Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState())) {
                        val rows = availableItems.chunked(4)
                        rows.forEach { rowItems ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                rowItems.forEach { item ->
                                    val context = LocalContext.current
                                    val resId = remember(item.image_id) {
                                        context.resources.getIdentifier(
                                            item.image_id, "drawable", context.packageName
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                                            .border(
                                                1.5.dp,
                                                Color(ItemUtils.getRarityColor(item.rarity)),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { onSelect(item) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (resId != 0) {
                                            Image(
                                                painter = painterResource(id = resId),
                                                contentDescription = item.name_en,
                                                modifier = Modifier.fillMaxSize().padding(4.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                        } else {
                                            Text(text = getItemEmojiForShop(item.slot), fontSize = 24.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = AppStrings.t(language, "btn_cancel"),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun MergedDialog(item: Item, language: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val bgResId = remember { context.resources.getIdentifier("bg_merged", "drawable", context.packageName) }
    val itemResId = remember(item.image_id) {
        context.resources.getIdentifier(item.image_id, "drawable", context.packageName)
    }
    val rarityColor = Color(ItemUtils.getRarityColor(item.rarity))

    val lottieAnimName = when (item.rarity) {
        "legendary" -> "anim_legendary"
        "epic"      -> "anim_epic"
        else        -> null
    }
    val lottieResId = remember(lottieAnimName) {
        if (lottieAnimName != null) context.resources.getIdentifier(lottieAnimName, "raw", context.packageName) else 0
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
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
            if (lottieResId != 0) {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieResId))
                val progress by animateLottieCompositionAsState(composition, iterations = 1)
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.matchParentSize()
                )
            }
            Column(
                modifier = Modifier.padding(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MERGED!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeAccent,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
                        .border(2.dp, rarityColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (itemResId != 0) {
                        Image(
                            painter = painterResource(id = itemResId),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(6.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(text = "?", fontSize = 36.sp, color = TextMuted)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (language == "ru") item.name_ru else item.name_en,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = rarityColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(0.65f).height(40.dp)
                ) {
                    Text("YES!", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun MergeFailDialog(language: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val bgResId = remember { context.resources.getIdentifier("bg_merged_fail", "drawable", context.packageName) }
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
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
                modifier = Modifier.padding(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "FAIL",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Blue,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(0.65f).height(40.dp)
                ) {
                    Text(AppStrings.t(language, "btn_well"), color = Color.White, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun EnchantedDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val bgResId = remember { context.resources.getIdentifier("bg_enc_yes", "drawable", context.packageName) }
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
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
                modifier = Modifier.padding(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ENCHANTED!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeAccent,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(0.65f).height(40.dp)
                ) {
                    Text("YES!", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun CursedDialog(language: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val bgResId = remember { context.resources.getIdentifier("bg_enc_no", "drawable", context.packageName) }
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
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
                modifier = Modifier.padding(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Cursed...",
                    fontSize = 24.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(0.65f).height(40.dp)
                ) {
                    Text(AppStrings.t(language, "btn_well"), color = Color.White, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun NoTeethDialog(language: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val bgResId = remember { context.resources.getIdentifier("bg_enc_noteeth", "drawable", context.packageName) }
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
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
                modifier = Modifier.padding(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(AppStrings.t(language, "no_teeth_title"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                Text(AppStrings.t(language, "no_teeth_body"), fontSize = 16.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonGray),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text("...", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ResultDialog(
    message: String,
    onDismiss: () -> Unit,
    showBg: Boolean = false
) {
    val context = LocalContext.current
    val bgResId = remember(showBg) {
        if (showBg) context.resources.getIdentifier("event_bg_spirit", "drawable", context.packageName)
        else context.resources.getIdentifier("bg_fight_3", "drawable", context.packageName)
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
                    alpha = 0.20f
                )
            }
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "✨", fontSize = 40.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    fontSize = 16.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("OK")
                }
            }
        }
    }
}

@Composable
fun BuySuccessDialog(
    item: Item,
    language: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val bgResId = remember {
        context.resources.getIdentifier("bg_clover", "drawable", context.packageName)
    }
    val itemResId = remember(item.image_id) {
        context.resources.getIdentifier(item.image_id, "drawable", context.packageName)
    }
    val rarityColor = Color(ItemUtils.getRarityColor(item.rarity))
    val itemName = ItemUtils.getItemName(item, language)

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
                Text(
                    text = AppStrings.t(language, "purchased_label"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
                        .border(2.dp, rarityColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (itemResId != 0) {
                        Image(
                            painter = painterResource(id = itemResId),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(6.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(text = "?", fontSize = 36.sp, color = TextMuted)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = itemName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = rarityColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp)
                ) {
                    Text(AppStrings.t(language, "btn_ok"), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CloverBoxResultDialog(
    item: Item,
    language: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val bgResId = remember {
        context.resources.getIdentifier("bg_clover", "drawable", context.packageName)
    }
    val itemResId = remember(item.image_id) {
        context.resources.getIdentifier(item.image_id, "drawable", context.packageName)
    }
    val rarityColor = Color(ItemUtils.getRarityColor(item.rarity))
    val itemName = ItemUtils.getItemName(item, language)
    val lottieAnimName = when (item.rarity) {
        "legendary" -> "anim_legendary"
        "epic"      -> "anim_epic"
        else        -> null
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
            if (lottieAnimName != null) {
                val lottieResId = remember(lottieAnimName) {
                    context.resources.getIdentifier(lottieAnimName, "raw", context.packageName)
                }
                if (lottieResId != 0) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieResId))
                    val progress by animateLottieCompositionAsState(composition, iterations = 1)
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.matchParentSize()
                    )
                }
            }
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = AppStrings.t(language, "free_item_label"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
                        .border(2.dp, rarityColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (itemResId != 0) {
                        Image(
                            painter = painterResource(id = itemResId),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(6.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(text = "?", fontSize = 36.sp, color = TextMuted)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = itemName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = rarityColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp)
                ) {
                    Text(AppStrings.t(language, "btn_ok"), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FreePointsResultDialog(
    language: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val bgResId = remember {
        context.resources.getIdentifier("bg_record_popup", "drawable", context.packageName)
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
                    alpha = 0.35f
                )
            }
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = AppStrings.t(language, "free_points_label"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
                        .border(2.dp, GoldAccent, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+2",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = AppStrings.t(language, "clover_bonus"),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp)
                ) {
                    Text(AppStrings.t(language, "btn_ok"), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- Точильный камень ---
@Composable
fun GrindstoneSection(
    state: com.ninthbalcony.pushuprpg.data.db.GameStateEntity,
    language: String,
    inventoryItems: List<Item>,
    selectedEnchantItem: Item?,
    isNightMode: Boolean = false,
    maxEnchant: Int = 9,
    shakeSuccess: Boolean = false,
    vibrationEnabled: Boolean = false,
    context: android.content.Context? = null,
    onSelectItem: (Item?) -> Unit,
    onEnchant: () -> Unit,
    enchantChance: Float,
    enchantCost: Int,
    getEnchantLevel: (Item) -> Int
) {
    val context = LocalContext.current
    val bgKey = if (isNightMode) "bg_grind_night" else "bg_grind"
    val iconKey = if (isNightMode) "img_grindstone_night" else "img_grindstone"
    val bgResId = remember(bgKey) {
        context.resources.getIdentifier(bgKey, "drawable", context.packageName)
    }
    val grindstoneRes = remember(iconKey) {
        context.resources.getIdentifier(iconKey, "drawable", context.packageName)
    }

    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(shakeSuccess) {
        if (shakeSuccess) {
            repeat(4) {
                shakeOffset.animateTo(4f, tween(50))
                shakeOffset.animateTo(-4f, tween(50))
            }
            shakeOffset.animateTo(0f, tween(50))
        }
    }

    val enchantBorderColor by animateColorAsState(
        targetValue = if (shakeSuccess) Color(0xFFFFD700) else GoldAccent,
        animationSpec = tween(400),
        label = "enchantBorder"
    )

    var enchantSwirlActive by remember { mutableStateOf(false) }
    var showItemPicker by remember { mutableStateOf(false) }

    if (showItemPicker) {
        EnchantItemPickerDialog(
            inventoryItems = inventoryItems,
            language = language,
            getEnchantLevel = getEnchantLevel,
            maxEnchant = maxEnchant,
            onSelect = { item ->
                onSelectItem(item)
                showItemPicker = false
            },
            onDismiss = { showItemPicker = false },
            hasSelectedItem = selectedEnchantItem != null,
            onRemove = { onSelectItem(null) }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
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

        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = AppStrings.t(language, "grindstone"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (isNightMode) {
                Text(
                    text = "${AppStrings.t(language, "night_mode_enchant")}$maxEnchant",
                    fontSize = 11.sp,
                    color = Color(0xFFBB86FC)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Картинка точильщика слева
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(DarkSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (grindstoneRes != 0) {
                        Image(
                            painter = painterResource(id = grindstoneRes),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(text = "⚡", fontSize = 40.sp)
                    }
                    MagicSwirl(
                        active = enchantSwirlActive,
                        modifier = Modifier.matchParentSize(),
                        onComplete = { enchantSwirlActive = false }
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Правая часть
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Слот для предмета
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .graphicsLayer { translationX = shakeOffset.value }
                            .background(DarkSurfaceVariant, RoundedCornerShape(10.dp))
                            .border(
                                2.dp,
                                if (selectedEnchantItem != null) enchantBorderColor
                                else OrangeAccent.copy(alpha = 0.4f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { showItemPicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedEnchantItem != null) {
                            val resId = remember(selectedEnchantItem.id) {
                                context.resources.getIdentifier(
                                    ItemUtils.getBaseItemId(selectedEnchantItem.id),
                                    "drawable",
                                    context.packageName
                                )
                            }
                            if (resId != 0) {
                                Image(
                                    painter = painterResource(id = resId),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().padding(6.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Text(text = getItemEmojiForShop(selectedEnchantItem.slot), fontSize = 28.sp)
                            }
                            // Уровень заточки на предмете
                            val currentLevel = getEnchantLevel(selectedEnchantItem)
                            if (currentLevel > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(2.dp)
                                        .background(GoldAccent, RoundedCornerShape(3.dp))
                                        .padding(horizontal = 2.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "+$currentLevel",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        } else {
                            Text(text = "+", fontSize = 28.sp, color = OrangeAccent.copy(alpha = 0.5f))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Информация о шансе и стоимости
                    if (selectedEnchantItem != null) {
                        val currentLevel = getEnchantLevel(selectedEnchantItem)

                        Text(
                            text = "Chance = ${String.format("%.1f", enchantChance)}%",
                            fontSize = 12.sp,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cost = $enchantCost 🦷",
                            fontSize = 12.sp,
                            color = Color(0xFFE0E0E0)
                        )
                        Text(
                            text = if (isNightMode) {
                                if (language == "ru") "От +19: +2 Сила/Броня/HP"
                                else "From +19: +2 Power/Armor/HP"
                            } else AppStrings.t(language, "grindstone_effect"),
                            fontSize = 11.sp,
                            color = if (isNightMode) Color(0xFFBB86FC) else TextSecondary
                        )
                    } else {
                        Text(
                            text = if (language == "ru") "Chance % =" else "Chance % =",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "Cost =",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Text(
                            text = if (isNightMode) {
                                if (language == "ru") "От +19: +2 Сила/Броня/HP"
                                else "From +19: +2 Power/Armor/HP"
                            } else AppStrings.t(language, "grindstone_effect"),
                            fontSize = 11.sp,
                            color = if (isNightMode) Color(0xFFBB86FC).copy(alpha = 0.6f) else TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Кнопка Заточить
                    Button(
                        onClick = {
                            enchantSwirlActive = true
                            onEnchant()
                        },
                        enabled = selectedEnchantItem != null &&
                                (getEnchantLevel(selectedEnchantItem) < maxEnchant),
                        modifier = Modifier.fillMaxWidth(0.8f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4A1A8A), // фиолетовый
                            disabledContainerColor = ButtonGray
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = AppStrings.t(language, "btn_enchant"),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// --- Диалог выбора предмета для заточки ---
@Composable
fun EnchantItemPickerDialog(
    inventoryItems: List<Item>,
    language: String,
    getEnchantLevel: (Item) -> Int,
    maxEnchant: Int = 9,
    onSelect: (Item) -> Unit,
    onDismiss: () -> Unit,
    hasSelectedItem: Boolean = false,
    onRemove: () -> Unit = {}
) {
    val availableItems = inventoryItems.filter { getEnchantLevel(it) < maxEnchant }
    val context = LocalContext.current
    val bgResId = remember { context.resources.getIdentifier("bg_actach_panel", "drawable", context.packageName) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
        ) {
            if (bgResId != 0) Image(
                painter = painterResource(id = bgResId),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.25f
            )
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = AppStrings.t(language, "item_picker_title"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (availableItems.isEmpty()) {
                Text(
                    text = AppStrings.t(language, "item_picker_empty"),
                    color = TextMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                Column(modifier = Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState())) {
                val rows = availableItems.chunked(4)
                rows.forEach { rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        rowItems.forEach { item ->
                            val context = LocalContext.current
                            val resId = remember(item.id) {
                                context.resources.getIdentifier(
                                    ItemUtils.getBaseItemId(item.id),
                                    "drawable",
                                    context.packageName
                                )
                            }
                            val enchantLevel = getEnchantLevel(item)
                            val borderStroke = rememberEnchantBorder(
                                enchantLevel,
                                Color(ItemUtils.getRarityColor(item.rarity)),
                                1.5.dp
                            )
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                                    .border(borderStroke, RoundedCornerShape(8.dp))
                                    .clickable { onSelect(item) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (resId != 0) {
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = item.name_en,
                                        modifier = Modifier.fillMaxSize().padding(4.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Text(text = getItemEmojiForShop(item.slot), fontSize = 24.sp)
                                }
                                if (enchantLevel > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(2.dp)
                                            .background(getEnchantBadgeColor(enchantLevel), RoundedCornerShape(3.dp))
                                            .padding(horizontal = 2.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "+$enchantLevel",
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                } // scrollable Column
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (hasSelectedItem) {
                Button(
                    onClick = { onRemove(); onDismiss() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1A1A))
                ) {
                    Text(
                        text = AppStrings.t(language, "btn_unequip"),
                        color = Color.White
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = AppStrings.t(language, "btn_cancel"),
                        color = TextSecondary
                    )
                }
            }
        }
        } // Box
    }
}

private fun getItemEmojiForShop(slot: String): String {
    return when (slot) {
        "weapon" -> "⚔️"
        "head" -> "⛑️"
        "necklace" -> "📿"
        "pants" -> "👖"
        "boots" -> "👟"
        else -> "📦"
    }
}

// ==================== DAILY SPIN ====================

@Composable
private fun DailySpinSection(
    availableSpins: Int,
    adViewsToday: Int,
    isSpinAnimating: Boolean,
    ribbonItems: List<String>,
    language: String,
    isNight: Boolean = false,
    vibrationEnabled: Boolean = false,
    context: android.content.Context? = null,
    onSpin: () -> Unit,
    onAdSpin: () -> Unit,
    onAnimationEnd: () -> Unit
) {
    val context = LocalContext.current
    val bgKey = if (isNight) "bg_spin_night" else "bg_spin"
    val bgResId = remember(bgKey) {
        context.resources.getIdentifier(bgKey, "drawable", context.packageName)
    }
    val canWatchAd = adViewsToday < SpinUtils.MAX_DAILY_AD_VIEWS
    val canSpin = availableSpins > 0 && !isSpinAnimating

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        if (bgResId != 0) {
            Image(
                painter = painterResource(id = bgResId),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.5f
            )
            Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.4f)))
        } else {
            Box(modifier = Modifier.matchParentSize().background(DarkCard))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Заголовок убран — он уже нарисован на картинке bg_spin
            Spacer(modifier = Modifier.height(4.dp))

            Text("▼", color = Color(0xFFFFD700), fontSize = 14.sp)

            SpinRibbon(
                isSpinAnimating = isSpinAnimating,
                ribbonItems = ribbonItems,
                onAnimationEnd = onAnimationEnd,
                modifier = Modifier.fillMaxWidth().height(80.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // SPIN и Watch AD по центру рядом
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SPIN с бейджем
                Box(modifier = Modifier.width(120.dp)) {
                    Button(
                        onClick = {
                            if (vibrationEnabled) vibrate(context)
                            onSpin()
                        },
                        enabled = canSpin,
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1e6303),
                            disabledContainerColor = Color(0xFF1B5E20)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SPIN", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    if (availableSpins > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 6.dp, y = (-6).dp)
                                .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.Red)
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$availableSpins", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Watch AD
                Button(
                    onClick = onAdSpin,
                    enabled = !isSpinAnimating && canWatchAd,
                    modifier = Modifier.width(68.dp).height(34.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFb39500),
                        contentColor = Color.Black,
                        disabledContainerColor = Color(0xFF9ea605),
                        disabledContentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = AppStrings.t(language, "watch_ad_btn"),
                            fontSize = 12.sp, fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$adViewsToday/${SpinUtils.MAX_DAILY_AD_VIEWS}",
                            fontSize = 9.sp
                        )
                    }
                }
            }

            // Вероятности наград
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isNight) {
                    if (language == "ru")
                        "Стат 1% • Зач+++ 2% • Зач++ 3% • Зач+ 4% • Ничего 90%"
                    else
                        "Stat 1% • Ench+++ 2% • Ench++ 3% • Ench+ 4% • Nothing 90%"
                } else {
                    if (language == "ru")
                        "Leg 3% • Epic 12% • Rare 19% • Unc 20% • Зубы 21% • Com 25%"
                    else
                        "Leg 3% • Epic 12% • Rare 19% • Unc 20% • Teeth 21% • Com 25%"
                },
                color = if (isNight) Color(0xFFBB86FC).copy(alpha = 0.8f) else TextSecondary.copy(alpha = 0.6f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Лента с анимацией вращения
@Composable
private fun SpinRibbon(
    isSpinAnimating: Boolean,
    ribbonItems: List<String>,  // передаётся снаружи, winner на позиции 22
    onAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val itemWidthDp = 72.dp
    val itemWidthPx = with(density) { itemWidthDp.toPx() }
    val scrollItems = 22

    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(isSpinAnimating) {
        if (isSpinAnimating) {
            offsetX.snapTo(0f)
            offsetX.animateTo(
                targetValue = scrollItems.toFloat() * itemWidthPx,
                animationSpec = tween(durationMillis = 7000, easing = FastOutSlowInEasing)
            )
            onAnimationEnd()
        } else {
            offsetX.snapTo(0f)
        }
    }

    var containerWidthPx by remember { mutableStateOf(0) }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .onSizeChanged { containerWidthPx = it.width }
    ) {
        val centeringOffset = containerWidthPx.toFloat() / 2f - itemWidthPx / 2f

        // ВАЖНО: align=Start, иначе дефолтный CenterHorizontally центрирует
        // Row (шириной 60*72=4320dp) внутри Box и ломает расчёт centeringOffset
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .wrapContentWidth(align = Alignment.Start, unbounded = true)
                .graphicsLayer { translationX = centeringOffset - offsetX.value },
            verticalAlignment = Alignment.CenterVertically
        ) {
            ribbonItems.forEach { type ->
                SpinRibbonIcon(type = type, context = context, sizeDp = itemWidthDp)
            }
        }

        // Золотой маркер по центру
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(itemWidthDp)
                .fillMaxHeight()
                .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(4.dp))
        )
    }
}

// Иконка на ленте
@Composable
private fun SpinRibbonIcon(
    type: String,
    context: android.content.Context,
    sizeDp: androidx.compose.ui.unit.Dp
) {
    val iconResId = remember(type) {
        when (type) {
            "clover_box"    -> context.resources.getIdentifier("img_clover_box", "drawable", context.packageName)
            "boss_cube"     -> context.resources.getIdentifier("boss_cube", "drawable", context.packageName)
            "teeth"         -> context.resources.getIdentifier("teeth_bag", "drawable", context.packageName)
            "uncommon_spin" -> context.resources.getIdentifier("weapon_009", "drawable", context.packageName)
            "rare_spin"     -> context.resources.getIdentifier("weapon_011", "drawable", context.packageName)
            "common_spin"   -> context.resources.getIdentifier("boots_001", "drawable", context.packageName)
            // Night spin icons
            "night_stat"    -> context.resources.getIdentifier("set_hellxdead_head", "drawable", context.packageName)
            "night_high"    -> context.resources.getIdentifier("weapon_035", "drawable", context.packageName)
            "night_med"     -> context.resources.getIdentifier("set_shadow_dagger", "drawable", context.packageName)
            "night_low"     -> context.resources.getIdentifier("weapon_022", "drawable", context.packageName)
            "night_nothing" -> context.resources.getIdentifier("weapon_008", "drawable", context.packageName)
            else            -> 0
        }
    }
    Box(
        modifier = Modifier.size(sizeDp),
        contentAlignment = Alignment.Center
    ) {
        if (iconResId != 0) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                modifier = Modifier.size(sizeDp - 12.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(text = "❓", fontSize = 24.sp)
        }
    }
}

// Диалог результата спина
@Composable
private fun SpinResultDialog(
    result: SpinReward,
    wonItemIds: List<String>,
    language: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val bgDrawable = when (result.type) {
        "boss_cube"     -> "bg_pop_cube"
        "clover_box"    -> "bg_pop_epic"
        "rare_spin",
        "uncommon_spin",
        "common_spin"   -> "bg_pop_boot"
        else            -> "bg_pop_teeth"
    }
    val bgResId = remember(bgDrawable) {
        context.resources.getIdentifier(bgDrawable, "drawable", context.packageName)
    }

    val addedToInv = if (language == "ru") "Добавлен в инвентарь!" else "Added to inventory!"
    val (title, desc) = when (result.type) {
        "boss_cube"     -> Pair(if (language == "ru") "Легендарный предмет!" else "Legendary Item!", addedToInv)
        "clover_box"    -> Pair(if (language == "ru") "Эпический предмет!" else "Epic Item!", addedToInv)
        "rare_spin"     -> Pair(if (language == "ru") "Редкий предмет!" else "Rare Item!", addedToInv)
        "uncommon_spin" -> Pair(if (language == "ru") "Необычный предмет!" else "Uncommon Item!", addedToInv)
        "common_spin"   -> Pair(if (language == "ru") "Обычный предмет!" else "Common Item!", addedToInv)
        else            -> Pair("${result.amount} 🦷", if (language == "ru") "Зубы добавлены к балансу!" else "Teeth added to your balance!")
    }

    val rewardColor = when (result.type) {
        "boss_cube"     -> Color(0xFFFFD700)
        "clover_box"    -> Color(0xFF9C27B0)
        "rare_spin"     -> Color(0xFF2196F3)
        "uncommon_spin" -> UncommonColor
        "common_spin"   -> Color(0xFFE0E0E0)
        else            -> Color(0xFFE0E0E0)
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
        ) {
            // Фоновая картинка
            if (bgResId != 0) {
                Image(
                    painter = painterResource(id = bgResId),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.45f)))
            } else {
                Box(modifier = Modifier.matchParentSize().background(DarkCard))
            }

            // Lottie overlay for legendary/epic wins
            val lottieAnimName = when (result.type) {
                "boss_cube"  -> "anim_legendary"
                "clover_box" -> "anim_epic"
                else         -> null
            }
            if (lottieAnimName != null) {
                val resId = remember(lottieAnimName) {
                    context.resources.getIdentifier(lottieAnimName, "raw", context.packageName)
                }
                if (resId != 0) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resId))
                    val progress by animateLottieCompositionAsState(composition, iterations = 1)
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.matchParentSize()
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                // Заголовок по центру
                Text(
                    text = AppStrings.t(language, "you_won_label"),
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Иконки выпавших предметов (только для item-наград)
                if (wonItemIds.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        wonItemIds.forEach { itemId ->
                            val resId = remember(itemId) {
                                context.resources.getIdentifier(itemId, "drawable", context.packageName)
                            }
                            Box(
                                modifier = Modifier.size(72.dp).padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (resId != 0) {
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Text("❓", fontSize = 36.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    color = rewardColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Кнопка OK — узкая, по центру
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.width(80.dp).height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(AppStrings.t(language, "btn_ok"), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun NightSpinResultDialog(
    reward: NightSpinReward,
    language: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val (iconRes, title, desc) = when (reward) {
        is NightSpinReward.Nothing -> Triple(
            "weapon_008",
            if (language == "ru") "Ничего..." else "Nothing...",
            if (language == "ru") "В следующий раз повезёт!" else "Better luck next time!"
        )
        is NightSpinReward.StatBoost -> {
            val (icon, titleStr, descStr) = when (reward.type) {
                NightStatBoostType.DMG_PERCENT   -> Triple("set_hellxdead_head", if (language == "ru") "+1% к урону!" else "+1% Damage!", if (language == "ru") "Постоянный бонус добавлен." else "Permanent bonus added.")
                NightStatBoostType.ARMOR_PERCENT -> Triple("set_hellxdead_head", if (language == "ru") "+1% к броне!" else "+1% Armor!", if (language == "ru") "Постоянный бонус добавлен." else "Permanent bonus added.")
                NightStatBoostType.POWER_FLAT    -> Triple("set_hellxdead_head", if (language == "ru") "+5 Силы!" else "+5 Power!", if (language == "ru") "Постоянный бонус добавлен." else "Permanent bonus added.")
                NightStatBoostType.ARMOR_FLAT    -> Triple("set_hellxdead_head", if (language == "ru") "+5 Брони!" else "+5 Armor!", if (language == "ru") "Постоянный бонус добавлен." else "Permanent bonus added.")
                NightStatBoostType.HP_FLAT       -> Triple("set_hellxdead_head", if (language == "ru") "+15 HP!" else "+15 HP!", if (language == "ru") "Постоянный бонус добавлен." else "Permanent bonus added.")
            }
            Triple(icon, titleStr, descStr)
        }
        is NightSpinReward.EnchantedItem -> when (reward.tier) {
            "high" -> Triple("weapon_035", if (language == "ru") "Высокая заточка!" else "High Enchant!", if (language == "ru") "+12–25 добавлен в инвентарь." else "+12–25 added to inventory.")
            "med"  -> Triple("set_shadow_dagger", if (language == "ru") "Средняя заточка!" else "Mid Enchant!", if (language == "ru") "+5–11 добавлен в инвентарь." else "+5–11 added to inventory.")
            else   -> Triple("weapon_022", if (language == "ru") "Лёгкая заточка!" else "Low Enchant!", if (language == "ru") "+1–4 добавлен в инвентарь." else "+1–4 added to inventory.")
        }
    }

    val rewardColor = when (reward) {
        is NightSpinReward.Nothing       -> TextMuted
        is NightSpinReward.StatBoost     -> Color(0xFFBB86FC)
        is NightSpinReward.EnchantedItem -> when (reward.tier) {
            "high" -> Color(0xFFFFD700)
            "med"  -> Color(0xFF2196F3)
            else   -> UncommonColor
        }
    }

    val iconResId = remember(iconRes) {
        context.resources.getIdentifier(iconRes, "drawable", context.packageName)
    }
    val bgResId = remember {
        context.resources.getIdentifier("bg_spin_night", "drawable", context.packageName)
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))) {
            if (bgResId != 0) {
                Image(painter = painterResource(id = bgResId), contentDescription = null,
                    modifier = Modifier.matchParentSize(), contentScale = ContentScale.Crop)
                Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.55f)))
            } else {
                Box(modifier = Modifier.matchParentSize().background(DarkCard))
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Text(
                    text = AppStrings.t(language, "you_won_label"),
                    color = Color(0xFFBB86FC),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (iconResId != 0) {
                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (reward !is NightSpinReward.Nothing) {
                            NightSpinGlow(color = rewardColor, modifier = Modifier.matchParentSize())
                        }
                        Image(painter = painterResource(id = iconResId), contentDescription = null,
                            modifier = Modifier.size(72.dp), contentScale = ContentScale.Fit)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Text(text = title, color = rewardColor, fontSize = 16.sp,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = desc, color = TextSecondary, fontSize = 13.sp,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onDismiss, modifier = Modifier.width(80.dp).height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EA)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(AppStrings.t(language, "btn_ok"), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}

/** Радиальный взрыв искр (анвил-эффект для Merge). */
@Composable
private fun SparkBurst(
    active: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFFC107),
    durationMs: Int = 500,
    onComplete: () -> Unit = {}
) {
    if (!active) return
    val progress = remember { Animatable(0f) }
    val particles = remember {
        List(14) { i ->
            val angle = (i * (360f / 14f) + (-12..12).random()) * (PI.toFloat() / 180f)
            cos(angle) to sin(angle)
        }
    }
    LaunchedEffect(Unit) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(durationMs, easing = LinearEasing))
        onComplete()
    }
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val maxR = size.minDimension * 0.95f
        val p = progress.value
        val alpha = (1f - p).coerceIn(0f, 1f)
        particles.forEach { (cosA, sinA) ->
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = 3.5.dp.toPx() * (1f - p * 0.5f),
                center = Offset(cx + cosA * maxR * p, cy + sinA * maxR * p)
            )
        }
    }
}

/** Магическое завихрение (Enchant): частицы по спирали стягиваются к центру. */
@Composable
private fun MagicSwirl(
    active: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFBB86FC),
    durationMs: Int = 800,
    onComplete: () -> Unit = {}
) {
    if (!active) return
    val progress = remember { Animatable(0f) }
    val startAngles = remember { List(10) { it * 36f } }
    LaunchedEffect(Unit) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(durationMs, easing = LinearEasing))
        onComplete()
    }
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val maxR = size.minDimension * 0.45f
        val p = progress.value
        startAngles.forEach { startAngle ->
            val angle = (startAngle + p * 540f) * (PI.toFloat() / 180f)
            val r = maxR * (1f - p * 0.85f)
            val alpha = ((1f - p) * 0.9f).coerceIn(0f, 1f)
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = 4.dp.toPx(),
                center = Offset(cx + cos(angle) * r, cy + sin(angle) * r)
            )
        }
    }
}

/** Пульсирующее свечение + орбита для рарного результата ночного спина. */
@Composable
private fun NightSpinGlow(
    color: Color,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "nightSpinGlow")
    val pulse by infinite.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(3000, easing = LinearEasing)),
        label = "rotation"
    )
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val baseR = size.minDimension * 0.5f
        drawCircle(color = color.copy(alpha = pulse * 0.35f), radius = baseR * 0.85f, center = Offset(cx, cy))
        drawCircle(color = color.copy(alpha = pulse * 0.15f), radius = baseR * 1.10f, center = Offset(cx, cy))
        val particleCount = 8
        for (i in 0 until particleCount) {
            val angle = (rotation + i * 360f / particleCount) * (PI.toFloat() / 180f)
            val r = baseR * 0.85f
            drawCircle(
                color = color.copy(alpha = 0.9f),
                radius = 4.dp.toPx(),
                center = Offset(cx + cos(angle) * r, cy + sin(angle) * r)
            )
        }
    }
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
private fun ShopScreenPreview() {
    val vm = remember { GameViewModel(FakeGameRepository()) }
    ShopScreen(viewModel = vm, onBack = {})
}
