package com.ninthbalcony.pushuprpg.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.ninthbalcony.pushuprpg.utils.ItemUtils
import com.ninthbalcony.pushuprpg.utils.ShopUtils
import com.ninthbalcony.pushuprpg.utils.SpinReward
import com.ninthbalcony.pushuprpg.utils.SpinResult
import com.ninthbalcony.pushuprpg.utils.SpinUtils
import com.ninthbalcony.pushuprpg.ui.GameViewModel
import com.ninthbalcony.pushuprpg.utils.SoundManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeIn
import androidx.compose.runtime.remember
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

/** Генерирует список иконок для ленты; winnerType фиксируется на позиции 22 */
private fun buildSpinRibbon(winnerType: String?): List<String> {
    val items = MutableList(60) { SPIN_WEIGHTED_TYPES.random() }
    if (winnerType != null) items[22] = winnerType
    return items
}

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

    var selectedShopItem by remember { mutableStateOf<Item?>(null) }
    var showForgeItemPicker by remember { mutableStateOf(0) }
    var resultMessage by remember { mutableStateOf("") }
    var showResultDialog by remember { mutableStateOf(false) }
    var showMergedDialog by remember { mutableStateOf(false) }
    var mergedItem by remember { mutableStateOf<Item?>(null) }
    var showMergeFailDialog by remember { mutableStateOf(false) }
    var showEnchantedDialog by remember { mutableStateOf(false) }
    var showCursedDialog by remember { mutableStateOf(false) }
    var showNoTeethDialog by remember { mutableStateOf(false) }

    // Daily Spin state
    var showSpinResultDialog by remember { mutableStateOf(false) }
    var spinResultToShow by remember { mutableStateOf<SpinResult?>(null) }
    var isSpinAnimating by remember { mutableStateOf(false) }
    // Лента генерируется в ShopScreen (гарантирует совпадение иконки и награды)
    var spinRibbonItems by remember { mutableStateOf(buildSpinRibbon(null)) }
    val spinResult by viewModel.spinResult.collectAsState()
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

    if (adRewardPending > 0) {
        com.ninthbalcony.pushuprpg.ui.dialogs.RewardedAdDialog(
            title = AppStrings.t(language, "ad_title"),
            description = AppStrings.t(language, "ad_reward_desc"),
            rewardText = "+$adRewardPending 🦷",
            onWatchAd = { (context as? android.app.Activity)?.let { viewModel.playRewardedAd(it) } },
            onDecline = { viewModel.dismissAdReward() },
            onDismiss = { viewModel.dismissAdReward() }
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
            onDismiss = { showResultDialog = false }
        )
    }

    if (showMergedDialog && mergedItem != null) {
        MergedDialog(
            item = mergedItem!!,
            language = language,
            onDismiss = { showMergedDialog = false; mergedItem = null }
        )
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
        CursedDialog(onDismiss = { showCursedDialog = false })
    }

    if (showNoTeethDialog) {
        NoTeethDialog(onDismiss = { showNoTeethDialog = false })
    }

    if (showSpinResultDialog && spinResultToShow != null) {
        SpinResultDialog(
            result = spinResultToShow!!.reward,
            wonItemIds = spinResultToShow!!.wonItemIds,
            language = language,
            onDismiss = {
                showSpinResultDialog = false
                spinResultToShow = null
                viewModel.clearSpinResult()
                viewModel.refreshSpinCounters()
            }
        )
    }

    if (showEnchantItemPicker) {
        EnchantItemPickerDialog(
            inventoryItems = viewModel.getInventoryItems(state),
            language = language,
            getEnchantLevel = { item -> viewModel.getEnchantLevel(state, item.id) },
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
                        resultMessage = if (success) {
                            if (language == "ru") "Куплено: ${item.name_ru}!"
                            else "Bought: ${item.name_en}!"
                        } else {
                            AppStrings.t(language, "insufficient_teeth")
                        }
                        showResultDialog = true
                        if (success) selectedShopItem = null
                    }
                },
                onReroll = { viewModel.rerollShop() }
            )

            ForgeSection(
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
            )

            CloverBoxSection(
                cloverUsed = state.cloverBoxUsedToday,
                freePointsUsed = state.freePointsUsedToday,
                adShopViewCount = state.adShopViewCount,
                adShopLastViewTime = state.adShopLastViewTime,
                language = language,
                onCloverBox = {
                    viewModel.useCloverBox { result ->
                        resultMessage = if (result != null) {
                            if (language == "ru") "Получен: ${result.name_ru}!"
                            else "Got: ${result.name_en}!"
                        } else {
                            AppStrings.t(language, "clover_limit")
                        }
                        showResultDialog = true
                    }
                },
                onFreePoints = {
                    viewModel.useFreePoints { success ->
                        resultMessage = if (success) {
                            AppStrings.t(language, "clover_bonus")
                        } else {
                            AppStrings.t(language, "clover_limit")
                        }
                        showResultDialog = true
                    }
                },
                onWatchAdReward = { viewModel.requestAdReward(20) }
            )

            // --- Daily Spin ---
            DailySpinSection(
                availableSpins = availableSpins,
                adViewsToday = adViewsToday,
                isSpinAnimating = isSpinAnimating,
                ribbonItems = spinRibbonItems,
                language = language,
                vibrationEnabled = shopVibrationEnabled,
                context = shopContext,
                onSpin = {
                    SoundManager.playSpin(shopSoundEnabled)
                    viewModel.performDailySpin()
                },
                onAdSpin = { viewModel.watchAdForSpin() },
                onAnimationEnd = {
                    isSpinAnimating = false
                    showSpinResultDialog = true
                }
            )

            // --- Точильный камень ---
            GrindstoneSection(
                state = state,
                language = language,
                inventoryItems = viewModel.getInventoryItems(state),
                selectedEnchantItem = selectedEnchantItem,
                shakeSuccess = enchantShake,
                vibrationEnabled = shopVibrationEnabled,
                context = shopContext,
                onSelectItem = { item -> viewModel.selectEnchantItem(item) },
                onEnchant = {
                    SoundManager.playEnchant(shopSoundEnabled)
                    if (shopVibrationEnabled && shopContext != null) vibrate(shopContext)
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
                                    resultMessage = AppStrings.t(language, "enchant_max")
                                    showResultDialog = true
                                }
                            }
                        }
                    }
                },
                getEnchantInfo = { item -> viewModel.getEnchantInfo(state, item) },
                getEnchantLevel = { item -> viewModel.getEnchantLevel(state, item.id) }
            )
        }
    } // Column
    } // Box (фон)
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
                        text = if (language == "ru")
                            "Обновление: ${ShopUtils.getTimeUntilRefresh(state.shopLastRefresh)}"
                        else
                            "Reset in: ${ShopUtils.getTimeUntilRefresh(state.shopLastRefresh)}",
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
                    (if (language == "ru") "Сброс стоимости через: $rerollResetLabel" else "Cost resets in: $rerollResetLabel")
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
                                .clickable(enabled = slot1Item != null && slot2Item != null) { onMerge() }
                                .padding(horizontal = 12.dp),  // внутренние отступы слева/справа
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
                                fontSize = 15.sp
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

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .padding(16.dp)
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

@Composable
fun MergedDialog(item: Item, language: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val bgResId = remember { context.resources.getIdentifier("bg_merged", "drawable", context.packageName) }
    val rarityColor = Color(ItemUtils.getRarityColor(item.rarity))
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
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
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MERGED!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeAccent
                )
                Spacer(modifier = Modifier.height(8.dp))
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
                ) {
                    Text(
                        text = if (language == "ru") item.name_ru else item.name_en,
                        fontSize = 16.sp,
                        color = rarityColor,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("YES!", fontWeight = FontWeight.Bold)
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
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "FAIL",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Blue
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Well", color = Color.White)
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
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("ENCHANTED!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("YES!", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CursedDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val bgResId = remember { context.resources.getIdentifier("bg_enc_no", "drawable", context.packageName) }
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
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Cursed...", fontSize = 20.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Well", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun NoTeethDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val bgResId = remember { context.resources.getIdentifier("bg_enc_noteeth", "drawable", context.packageName) }
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
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Come back later", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Not enough teeth...", fontSize = 14.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonGray),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("...", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ResultDialog(
    message: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .padding(24.dp),
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

// --- Точильный камень ---
@Composable
fun GrindstoneSection(
    state: com.ninthbalcony.pushuprpg.data.db.GameStateEntity,
    language: String,
    inventoryItems: List<Item>,
    selectedEnchantItem: Item?,
    shakeSuccess: Boolean = false,
    vibrationEnabled: Boolean = false,
    context: android.content.Context? = null,
    onSelectItem: (Item?) -> Unit,
    onEnchant: () -> Unit,
    getEnchantInfo: (Item) -> Pair<Float, Int>,
    getEnchantLevel: (Item) -> Int
) {
    val context = LocalContext.current
    val bgResId = remember {
        context.resources.getIdentifier("bg_grind", "drawable", context.packageName)
    }
    val grindstoneRes = remember {
        context.resources.getIdentifier("img_grindstone", "drawable", context.packageName)
    }

    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(shakeSuccess) {
        if (shakeSuccess) {
            repeat(4) {
                shakeOffset.animateTo(8f, tween(50))
                shakeOffset.animateTo(-8f, tween(50))
            }
            shakeOffset.animateTo(0f, tween(50))
        }
    }

    val enchantBorderColor by animateColorAsState(
        targetValue = if (shakeSuccess) Color(0xFFFFD700) else GoldAccent,
        animationSpec = tween(400),
        label = "enchantBorder"
    )

    var showItemPicker by remember { mutableStateOf(false) }

    if (showItemPicker) {
        EnchantItemPickerDialog(
            inventoryItems = inventoryItems,
            language = language,
            getEnchantLevel = getEnchantLevel,
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
                        val (chance, cost) = getEnchantInfo(selectedEnchantItem)
                        val currentLevel = getEnchantLevel(selectedEnchantItem)

                        Text(
                            text = "Chance = ${String.format("%.1f", chance)}%",
                            fontSize = 12.sp,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cost = $cost 🦷",
                            fontSize = 12.sp,
                            color = Color(0xFFE0E0E0)
                        )
                        Text(
                            text = AppStrings.t(language, "grindstone_effect"),
                            fontSize = 11.sp,
                            color = TextSecondary
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
                            text = AppStrings.t(language, "grindstone_effect"),
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Кнопка Заточить
                    Button(
                        onClick = onEnchant,
                        enabled = selectedEnchantItem != null &&
                                (getEnchantLevel(selectedEnchantItem) < 9),
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
    onSelect: (Item) -> Unit,
    onDismiss: () -> Unit,
    hasSelectedItem: Boolean = false,
    onRemove: () -> Unit = {}
) {
    // Исключаем предметы с максимальным уровнем +9
    val availableItems = inventoryItems.filter { getEnchantLevel(it) < 9 }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .padding(16.dp)
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
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                                    .border(
                                        1.5.dp,
                                        when {
                                            enchantLevel in 7..9 -> Color(0xFFFF4444)
                                            enchantLevel in 4..6 -> OrangeAccent
                                            enchantLevel in 1..3 -> GoldAccent
                                            else -> Color(ItemUtils.getRarityColor(item.rarity))
                                        },
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
                                if (enchantLevel > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(2.dp)
                                            .background(GoldAccent, RoundedCornerShape(3.dp))
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
                        text = if (language == "ru") "Снять" else "Remove",
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
    vibrationEnabled: Boolean = false,
    context: android.content.Context? = null,
    onSpin: () -> Unit,
    onAdSpin: () -> Unit,
    onAnimationEnd: () -> Unit
) {
    val context = LocalContext.current
    val bgResId = remember {
        context.resources.getIdentifier("bg_spin", "drawable", context.packageName)
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
                            if (vibrationEnabled && context != null) vibrate(context)
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
                            text = if (language == "ru") "Реклама" else "Watch AD",
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
                text = if (language == "ru")
                    "Leg 3% • Epic 12% • Rare 19% • Unc 20% • Зубы 21% • Com 25%"
                else
                    "Leg 3% • Epic 12% • Rare 19% • Unc 20% • Teeth 21% • Com 25%",
                color = TextSecondary.copy(alpha = 0.6f),
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
        "uncommon_spin" -> Color(0xFF4CAF50)
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
                    text = if (language == "ru") "Ты выиграл!" else "You Won!",
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
                    Text("OK", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                }
            }
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
