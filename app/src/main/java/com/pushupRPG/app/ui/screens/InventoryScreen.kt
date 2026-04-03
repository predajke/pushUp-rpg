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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pushupRPG.app.data.db.GameStateEntity
import com.pushupRPG.app.data.model.Item
import com.pushupRPG.app.ui.GameViewModel
import com.pushupRPG.app.ui.theme.*
import com.pushupRPG.app.utils.GameCalculations
import com.pushupRPG.app.utils.ItemUtils

@Composable
fun InventoryScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onNavigateToShop: () -> Unit
) {
    // ИСПРАВЛЕНО: добавлено initial = null
    val gameState by viewModel.gameState.collectAsState(initial = null)
    val selectedItem by viewModel.selectedInventoryItem.collectAsState(initial = null)

    if (gameState == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = OrangeAccent)
        }
        return
    }

    val state = gameState!!
    val language = state.language
    val inventoryItems = viewModel.getInventoryItems(state)
    val equippedItems = viewModel.getEquippedItems(state)

    Column(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        // Топбар
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
                text = if (language == "ru") "Инвентарь" else "Inventory",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🦷 ${state.teeth}",
                    fontSize = 14.sp,
                    color = Color(0xFFE0E0E0),
                    fontWeight = FontWeight.Bold
                )
                Text(text = state.playerName, fontSize = 14.sp, color = TextSecondary)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Экипировка персонажа
            EquipmentSection(
                state = state,
                language = language,
                onSlotClick = { slot ->
                    val equippedId = when (slot) {
                        "head" -> state.equippedHead
                        "necklace" -> state.equippedNecklace
                        "weapon1" -> state.equippedWeapon1
                        "weapon2" -> state.equippedWeapon2
                        "pants" -> state.equippedPants
                        "boots" -> state.equippedBoots
                        else -> ""
                    }
                    if (equippedId.isNotEmpty()) {
                        val item = ItemUtils.getItemById(equippedId)
                        viewModel.selectInventoryItem(item)
                    }
                },
                onNavigateToShop = onNavigateToShop
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Инвентарь
            InventoryGrid(
                items = inventoryItems,
                selectedItem = selectedItem,
                language = language,
                onItemClick = { item ->
                    viewModel.selectInventoryItem(
                        if (selectedItem?.id == item.id) null else item
                    )
                },
                onNavigateToShop = onNavigateToShop,
                getEnchantLevel = { item -> viewModel.getEnchantLevel(state, item.id) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Описание предмета
            ItemInfoSection(
                selectedItem = selectedItem,
                state = state,
                language = language,
                onEquip = { item -> viewModel.equipItem(item.id, item.slot) },
                onUnequip = { slot -> viewModel.unequipItem(slot) },
                onSell = { item -> viewModel.sellItem(item.id) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Статы
            InventoryStatsPanel(
                state = state,
                equippedItems = equippedItems,
                language = language,
                onSpendPoint = { stat -> viewModel.spendStatPoint(stat) }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// --- Экипировка ---
@Composable
fun EquipmentSection(
    state: GameStateEntity,
    language: String,
    onSlotClick: (String) -> Unit,
    onNavigateToShop: () -> Unit
) {
    val context = LocalContext.current
    val forgeBtnBg = remember {
        context.resources.getIdentifier("bg_forgeshop_btn", "drawable", context.packageName)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EquipSlot(
                    label = if (language == "ru") "Голова" else "Head",
                    itemId = state.equippedHead,
                    onClick = { onSlotClick("head") }
                )
                EquipSlot(
                    label = if (language == "ru") "Ожерелие" else "Necklace",
                    itemId = state.equippedNecklace,
                    onClick = { onSlotClick("necklace") }
                )
                EquipSlot(
                    label = if (language == "ru") "Оружие 1" else "Weapon 1",
                    itemId = state.equippedWeapon1,
                    onClick = { onSlotClick("weapon1") }
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val heroAvatar = state.heroAvatar.ifEmpty { "hero_1" }
                val heroResId = remember(heroAvatar) {
                    context.resources.getIdentifier(heroAvatar, "drawable", context.packageName)
                }

                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
                        .border(1.dp, OrangeAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (heroResId != 0) {
                        Image(
                            painter = painterResource(id = heroResId),
                            contentDescription = "Hero",
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(text = "🥊", fontSize = 56.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .height(36.dp)
                        .background(Color(0xFF2A1A00), RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNavigateToShop() },
                    contentAlignment = Alignment.Center
                ) {
                    if (forgeBtnBg != 0) {
                        Image(
                            painter = painterResource(id = forgeBtnBg),
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop,
                            alpha = 0.35f
                        )
                    }
                    Text(
                        text = "⚒️ Shop",
                        color = OrangeLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                EquipSlot(
                    label = if (language == "ru") "Штаны" else "Pants",
                    itemId = state.equippedPants,
                    onClick = { onSlotClick("pants") }
                )
                EquipSlot(
                    label = if (language == "ru") "Обувь" else "Boots",
                    itemId = state.equippedBoots,
                    onClick = { onSlotClick("boots") }
                )
                EquipSlot(
                    label = if (language == "ru") "Оружие 2" else "Weapon 2",
                    itemId = state.equippedWeapon2,
                    onClick = { onSlotClick("weapon2") }
                )
            }
        }
    }
}

@Composable
fun EquipSlot(label: String, itemId: String, onClick: () -> Unit) {
    val item = if (itemId.isNotEmpty()) ItemUtils.getItemById(itemId) else null
    val borderColor = if (item != null) Color(ItemUtils.getRarityColor(item.rarity)) else TextMuted
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                .border(2.dp, borderColor, RoundedCornerShape(8.dp)),
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
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(text = getItemEmoji(item.slot), fontSize = 28.sp)
                }
            } else {
                Text(text = "·", fontSize = 24.sp, color = TextMuted)
            }
        }
        Text(
            text = label,
            fontSize = 9.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

// --- Сетка инвентаря ---
@Composable
fun InventoryGrid(
    items: List<Item>,
    selectedItem: Item?,
    language: String,
    onItemClick: (Item) -> Unit,
    onNavigateToShop: () -> Unit,
    getEnchantLevel: (Item) -> Int = { 0 }
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .background(DarkCard, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = if (language == "ru") "Инвентарь (${items.size})" else "Inventory (${items.size})",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (language == "ru")
                        "Инвентарь пуст.\nПобеди монстров чтобы получить предметы!"
                    else
                        "Inventory is empty.\nDefeat monsters to get items!",
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val rows = items.chunked(5)
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowItems.forEach { item ->
                        InventoryItemCell(
                            item = item,
                            isSelected = selectedItem?.id == item.id,
                            onClick = { onItemClick(item) },
                            enchantLevel = getEnchantLevel(item),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(5 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryItemCell(
    item: Item,
    isSelected: Boolean,
    onClick: () -> Unit,
    enchantLevel: Int = 0,
    modifier: Modifier = Modifier
) {
    val rarityColor = Color(ItemUtils.getRarityColor(item.rarity))

    val borderColor = when {
        isSelected -> GoldAccent
        enchantLevel in 7..9 -> Color(0xFFFF4444)
        enchantLevel in 4..6 -> OrangeAccent
        enchantLevel in 1..3 -> GoldAccent
        else -> rarityColor
    }
    val borderWidth = if (isSelected || enchantLevel > 0) 2.dp else 1.5.dp

    val context = LocalContext.current
    val resId = remember(item.id) {
        context.resources.getIdentifier(
            ItemUtils.getBaseItemId(item.id),
            "drawable",
            context.packageName
        )
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(DarkSurfaceVariant, RoundedCornerShape(6.dp))
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(6.dp))
            .clickable { onClick() },
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
            Text(text = getItemEmoji(item.slot), fontSize = 24.sp)
        }

        if (enchantLevel > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(2.dp)
                    .background(
                        color = when {
                            enchantLevel in 7..9 -> Color(0xFFFF4444)
                            enchantLevel in 4..6 -> OrangeAccent
                            else -> GoldAccent
                        },
                        shape = RoundedCornerShape(3.dp)
                    )
                    .padding(horizontal = 2.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "+$enchantLevel",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// --- Информация о предмете ---
@Composable
fun ItemInfoSection(
    selectedItem: Item?,
    state: GameStateEntity,
    language: String,
    onEquip: (Item) -> Unit,
    onUnequip: (String) -> Unit,
    onSell: (Item) -> Unit
) {
    val equippedSlot = if (selectedItem != null) {
        when (selectedItem.id) {
            state.equippedHead -> "head"
            state.equippedNecklace -> "necklace"
            state.equippedWeapon1 -> "weapon1"
            state.equippedWeapon2 -> "weapon2"
            state.equippedPants -> "pants"
            state.equippedBoots -> "boots"
            else -> null
        }
    } else null
    val isEquipped = equippedSlot != null

    val rarityColor = if (selectedItem != null) {
        Color(ItemUtils.getRarityColor(selectedItem.rarity))
    } else TextMuted

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .background(DarkCard, RoundedCornerShape(12.dp))
            .border(1.dp, rarityColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (selectedItem == null) {
                Text(
                    text = if (language == "ru") "Выбери предмет для просмотра"
                    else "Select an item to view info",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            } else {
                val name = ItemUtils.getItemName(selectedItem, language)
                val description = ItemUtils.getItemDescription(selectedItem, language)
                val rarityText = when (selectedItem.rarity) {
                    "common" -> if (language == "ru") "Обычный" else "Common"
                    "uncommon" -> if (language == "ru") "Необычный" else "Uncommon"
                    "rare" -> if (language == "ru") "Редкий" else "Rare"
                    "epic" -> if (language == "ru") "Эпический" else "Epic"
                    else -> selectedItem.rarity
                }

                Text(text = name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = rarityColor)
                Text(
                    text = if (isEquipped) {
                        if (language == "ru") "$rarityText • Надето" else "$rarityText • Equipped"
                    } else rarityText,
                    fontSize = 11.sp,
                    color = rarityColor.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))

                val stats = selectedItem.stats
                val bonuses = buildList {
                    if (stats.power > 0) add("⚔️ +${stats.power}")
                    if (stats.armor > 0) add("🛡️ +${stats.armor}")
                    if (stats.health > 0) add("❤️ +${stats.health}")
                    if (stats.luck > 0) add("🍀 +${stats.luck}")
                }
                Text(text = bonuses.joinToString("  "), fontSize = 13.sp, color = TextPrimary)

                val sellPrice = GameCalculations.getTeethFromSell(selectedItem.rarity)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(text = "Sell:", fontSize = 12.sp, color = TextMuted)
                    Text(
                        text = "🦷 $sellPrice",
                        fontSize = 12.sp,
                        color = Color(0xFFE57373),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (selectedItem != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (isEquipped) {
                    Button(
                        onClick = { onUnequip(equippedSlot!!) },
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonGray),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = if (language == "ru") "Снять" else "Unequip",
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Button(
                        onClick = { onEquip(selectedItem) },
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = if (language == "ru") "Надеть" else "Equip",
                            fontSize = 13.sp
                        )
                    }
                    Button(
                        onClick = { onSell(selectedItem) },
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonRed),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = if (language == "ru") "Продать" else "Sell",
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// --- Статы в инвентаре ---
@Composable
fun InventoryStatsPanel(
    state: GameStateEntity,
    equippedItems: List<Item>,
    language: String,
    onSpendPoint: (String) -> Unit
) {
    val itemPower = equippedItems.sumOf { it.stats.power }
    val itemArmor = equippedItems.sumOf { it.stats.armor }
    val itemHealth = equippedItems.sumOf { it.stats.health }
    val itemLuck = equippedItems.sumOf { it.stats.luck.toDouble() }.toFloat()

    val totalPower = state.basePower + itemPower
    val totalArmor = state.baseArmor + itemArmor
    val totalHealth = state.baseHealth + itemHealth
    val totalLuck = state.baseLuck + itemLuck

    val hasPoints = state.unspentStatPoints > 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .background(DarkCard, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (language == "ru") "Характеристики" else "Stats",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            if (hasPoints) {
                Text(
                    text = if (language == "ru")
                        "У вас ${state.unspentStatPoints} очков"
                    else
                        "You have ${state.unspentStatPoints} points",
                    fontSize = 12.sp,
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        InventoryStatRowWithButton(
            icon = "⚔️",
            label = if (language == "ru") "Сила" else "Power",
            value = "$totalPower",
            color = PowerColor,
            hasPoints = hasPoints,
            onSpend = { onSpendPoint("power") }
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🛡️", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (language == "ru") "Броня" else "Armor",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$totalArmor",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ArmorColor,
                modifier = Modifier.width(60.dp),
                textAlign = TextAlign.End
            )
            Spacer(modifier = Modifier.width(36.dp))
        }

        InventoryStatRowWithButton(
            icon = "❤️",
            label = if (language == "ru") "Здоровье" else "Health",
            value = "$totalHealth",
            color = HealthColor,
            hasPoints = hasPoints,
            onSpend = { onSpendPoint("health") }
        )

        InventoryStatRowWithButton(
            icon = "🍀",
            label = if (language == "ru") "Удача" else "Luck",
            value = String.format("%.1f", totalLuck),
            color = LuckColor,
            hasPoints = hasPoints,
            onSpend = { onSpendPoint("luck") }
        )
    }
}

@Composable
fun InventoryStatRowWithButton(
    icon: String,
    label: String,
    value: String,
    color: Color,
    hasPoints: Boolean,
    onSpend: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color,
            modifier = Modifier.width(60.dp),
            textAlign = TextAlign.End
        )
        Box(modifier = Modifier.width(36.dp)) {
            if (hasPoints) {
                Text(
                    text = "+",
                    fontSize = 26.sp,
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable { onSpend() }
                        .padding(4.dp)
                )
            }
        }
    }
}

fun getItemEmoji(slot: String): String {
    return when (slot) {
        "weapon" -> "⚔️"
        "head" -> "⛑️"
        "necklace" -> "📿"
        "pants" -> "👖"
        "boots" -> "👟"
        else -> "📦"
    }
}