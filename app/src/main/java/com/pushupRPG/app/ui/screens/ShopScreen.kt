package com.pushupRPG.app.ui.screens

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pushupRPG.app.data.model.Item
import com.pushupRPG.app.data.model.EnchantResult
import com.pushupRPG.app.ui.theme.*
import com.pushupRPG.app.utils.ItemUtils
import com.pushupRPG.app.utils.ShopUtils
import com.pushupRPG.app.ui.GameViewModel


@Composable
fun ShopScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val shopItems by viewModel.shopItems.collectAsState()
    val language = gameState?.language ?: "en"
    val state = gameState ?: return

    var selectedShopItem by remember { mutableStateOf<Item?>(null) }
    var showForgeItemPicker by remember { mutableStateOf(0) }
    var resultMessage by remember { mutableStateOf("") }
    var showResultDialog by remember { mutableStateOf(false) }

    val forgeSlot1Item = remember(state.forgeSlot1) {
        if (state.forgeSlot1.isNotEmpty()) ItemUtils.getItemById(state.forgeSlot1) else null
    }
    val forgeSlot2Item = remember(state.forgeSlot2) {
        if (state.forgeSlot2.isNotEmpty()) ItemUtils.getItemById(state.forgeSlot2) else null
    }

    val selectedEnchantItem by viewModel.selectedEnchantItem.collectAsState()
    var showEnchantItemPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadShop()
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
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
                text = if (language == "ru") "Магазин" else "Shop",
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
                            if (language == "ru") "Недостаточно зубов!"
                            else "Not enough teeth!"
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
                    viewModel.mergeItems { result ->
                        resultMessage = if (result != null) {
                            if (language == "ru") "Создан: ${result.name_ru}!"
                            else "Created: ${result.name_en}!"
                        } else {
                            if (language == "ru") "Нужно 2 предмета в кузнице!"
                            else "Need 2 items in the forge!"
                        }
                        showResultDialog = true
                    }
                }
            )

            CloverBoxSection(
                cloverUsed = state.cloverBoxUsedToday,
                freePointsUsed = state.freePointsUsedToday,
                language = language,
                onCloverBox = {
                    viewModel.useCloverBox { result ->
                        resultMessage = if (result != null) {
                            if (language == "ru") "Получен: ${result.name_ru}!"
                            else "Got: ${result.name_en}!"
                        } else {
                            if (language == "ru") "Использовано на сегодня (макс. 2)"
                            else "Used up for today (max 2)"
                        }
                        showResultDialog = true
                    }
                },
                onFreePoints = {
                    viewModel.useFreePoints { success ->
                        resultMessage = if (success) {
                            if (language == "ru") "+2 очка характеристик!"
                            else "+2 stat points!"
                        } else {
                            if (language == "ru") "Использовано на сегодня (макс. 2)"
                            else "Used up for today (max 2)"
                        }
                        showResultDialog = true
                    }
                }
            )

            // --- Точильный камень ---
            GrindstoneSection(
                state = state,
                language = language,
                inventoryItems = viewModel.getInventoryItems(state),
                selectedEnchantItem = selectedEnchantItem,
                onSelectItem = { item -> viewModel.selectEnchantItem(item) },
                onEnchant = {
                    viewModel.enchantItem { result ->
                        resultMessage = when (result) {
                            EnchantResult.SUCCESS ->
                                if (language == "ru") "⚡ Заточка успешна!" else "⚡ Enchant successful!"
                            EnchantResult.FAILED ->
                                if (language == "ru") "💔 Заточка не удалась..." else "💔 Enchant failed..."
                            EnchantResult.MAX_LEVEL ->
                                if (language == "ru") "Максимальный уровень +9!" else "Maximum level +9!"
                            EnchantResult.NOT_ENOUGH_TEETH ->
                                if (language == "ru") "Недостаточно зубов!" else "Not enough teeth!"
                        }
                        showResultDialog = true
                    }
                },
                getEnchantInfo = { item -> viewModel.getEnchantInfo(state, item) },
                getEnchantLevel = { item -> viewModel.getEnchantLevel(state, item.id) }
            )
        }
    }
}

@Composable
fun ShopSection(
    shopItems: List<Item>,
    selectedItem: Item?,
    state: com.pushupRPG.app.data.db.GameStateEntity,
    language: String,
    onSelectItem: (Item) -> Unit,
    onBuy: (Item) -> Unit,
    onReroll: () -> Unit
) {
    val context = LocalContext.current
    val bgResId = remember {
        context.resources.getIdentifier("bg_shop", "drawable", context.packageName)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == "ru") "Магазин" else "Shop",
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
                            text = if (language == "ru") "Обновить" else "Reroll",
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
                        text = if (language == "ru") "Магазин пуст" else "Shop is empty",
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
                            text = if (language == "ru") "Купить" else "Buy",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Reroll cost = 1 🦷",
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
    onSlot1Click: () -> Unit,
    onSlot2Click: () -> Unit,
    onMerge: () -> Unit
) {
    val context = LocalContext.current
    val bgResId = remember {
        context.resources.getIdentifier("bg_forge", "drawable", context.packageName)
    }
    val mergeBtnBg = remember {
        context.resources.getIdentifier("bg_merge_btn", "drawable", context.packageName)
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
                text = if (language == "ru") "Кузница" else "Forge",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

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

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .background(
                                if (slot1Item != null && slot2Item != null) OrangeAccent
                                else ButtonGray,
                                RoundedCornerShape(8.dp)
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = slot1Item != null && slot2Item != null) { onMerge() },
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
                            text = if (language == "ru") "Сплавить" else "Merge",
                            fontWeight = FontWeight.Bold,
                            color = Color.Yellow,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Common 65% • Uncommon 25% • Rare 10%",
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
    language: String,
    onCloverBox: () -> Unit,
    onFreePoints: () -> Unit
) {
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
                text = if (language == "ru") "Клеверная коробка" else "Clover Box",
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
                            modifier = Modifier.fillMaxSize().padding(6.dp),
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
                                text = if (language == "ru") "Бесплатный предмет" else "Free item",
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
                                text = if (language == "ru") "Получить" else "Get",
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
                                text = if (language == "ru") "Бесплатные очки" else "Free points",
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
                                text = if (language == "ru") "Получить" else "Get",
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
                text = if (language == "ru") "Выбери предмет" else "Select item",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (availableItems.isEmpty()) {
                Text(
                    text = if (language == "ru") "Нет доступных предметов" else "No available items",
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
                    text = if (language == "ru") "Отмена" else "Cancel",
                    color = TextSecondary
                )
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
    state: com.pushupRPG.app.data.db.GameStateEntity,
    language: String,
    inventoryItems: List<Item>,
    selectedEnchantItem: Item?,
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
            onDismiss = { showItemPicker = false }
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
                text = if (language == "ru") "Точильный камень" else "Grindstone",
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
                            .background(DarkSurfaceVariant, RoundedCornerShape(10.dp))
                            .border(
                                2.dp,
                                if (selectedEnchantItem != null) GoldAccent
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
                            text = if (language == "ru")
                                "Вещь +1 к статам"
                            else
                                "Item +1 to all stats",
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
                            text = if (language == "ru") "Вещь +1 к статам" else "Item +1 to all stats",
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
                            text = if (language == "ru") "Заточить" else "Enchant",
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
    onDismiss: () -> Unit
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
                text = if (language == "ru") "Выбери предмет" else "Select item",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (availableItems.isEmpty()) {
                Text(
                    text = if (language == "ru")
                        "Нет доступных предметов"
                    else
                        "No available items",
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

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (language == "ru") "Отмена" else "Cancel",
                    color = TextSecondary
                )
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