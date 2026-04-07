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
import com.pushupRPG.app.utils.AppStrings
import com.pushupRPG.app.utils.GameCalculations
import com.pushupRPG.app.utils.ItemUtils

// --- Экипировка ---
@Composable
private fun EquipmentSection(
    state: GameStateEntity,
    language: String,
    onSlotClick: (String) -> Unit,
    onNavigateToShop: () -> Unit,
    viewModel: GameViewModel,
    onNavigateToAchievements: () -> Unit
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
                    label = AppStrings.t(language, "slot_head"),
                    itemId = state.equippedHead,
                    onClick = { onSlotClick("head") }
                )
                EquipSlot(
                    label = AppStrings.t(language, "slot_necklace"),
                    itemId = state.equippedNecklace,
                    onClick = { onSlotClick("necklace") }
                )
                EquipSlot(
                    label = AppStrings.t(language, "slot_weapon1"),
                    itemId = state.equippedWeapon1,
                    onClick = { onSlotClick("weapon1") }
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.width(130.dp)
            ) {
                val heroAvatar = state.heroAvatar.ifEmpty { "hero_1" }
                val heroResId = remember(heroAvatar) {
                    context.resources.getIdentifier(heroAvatar, "drawable", context.packageName)
                }

                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
                        .border(
                            1.dp,
                            OrangeAccent.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        ),
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

                ActiveAchievementsPanel(
                    state = state,
                    language = language,
                    viewModel = viewModel,
                    onClick = onNavigateToAchievements
                )

                Box(
                    modifier = Modifier
                        .width(135.dp)
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
                    label = AppStrings.t(language, "slot_pants"),
                    itemId = state.equippedPants,
                    onClick = { onSlotClick("pants") }
                )
                EquipSlot(
                    label = AppStrings.t(language, "slot_boots"),
                    itemId = state.equippedBoots,
                    onClick = { onSlotClick("boots") }
                )
                EquipSlot(
                    label = AppStrings.t(language, "slot_weapon2"),
                    itemId = state.equippedWeapon2,
                    onClick = { onSlotClick("weapon2") }
                )
            }
        }
    }
}

@Composable
private fun EquipSlot(label: String, itemId: String, onClick: () -> Unit) {
    val enchantLevel = itemId.split(":").getOrNull(1)?.toIntOrNull() ?: 0
    val item = if (itemId.isNotEmpty()) ItemUtils.getItemById(itemId) else null
    val borderColor = when {
        enchantLevel in 7..9 -> Color(0xFFFF4444)
        enchantLevel in 4..6 -> OrangeAccent
        enchantLevel in 1..3 -> GoldAccent
        item != null -> Color(ItemUtils.getRarityColor(item.rarity))
        else -> TextMuted
    }
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
                    context.resources.getIdentifier(
                        item.image_id,
                        "drawable",
                        context.packageName
                    )
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
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
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
private fun InventoryGrid(
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
            text = "${AppStrings.t(language, "inventory")} (${items.size})",
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
                    text = AppStrings.t(language, "inventory_empty"),
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
private fun InventoryItemCell(
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
private fun ItemInfoSection(
    selectedItem: Item?,
    state: GameStateEntity,
    language: String,
    onEquip: (Item) -> Unit,
    onUnequip: (String) -> Unit,
    onSell: (Item) -> Unit,
    getEnchantLevel: (Item) -> Int = { 0 }
) {
    val equippedSlot = if (selectedItem != null) {
        // Сравниваем unique ID (без суффикса заточки ":N")
        val itemUniqueId = selectedItem.id
        when (itemUniqueId) {
            state.equippedHead.split(":")[0] -> "head"
            state.equippedNecklace.split(":")[0] -> "necklace"
            state.equippedWeapon1.split(":")[0] -> "weapon1"
            state.equippedWeapon2.split(":")[0] -> "weapon2"
            state.equippedPants.split(":")[0] -> "pants"
            state.equippedBoots.split(":")[0] -> "boots"
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
                    text = AppStrings.t(language, "item_select_prompt"),
                    fontSize = 13.sp,
                    color = TextMuted
                )
            } else {
                val name =
                    ItemUtils.getItemName(selectedItem, language, getEnchantLevel(selectedItem))
                val description = ItemUtils.getItemDescription(selectedItem, language)
                val rarityText = when (selectedItem.rarity) {
                    "common" -> AppStrings.t(language, "rarity_common")
                    "uncommon" -> AppStrings.t(language, "rarity_uncommon")
                    "rare" -> AppStrings.t(language, "rarity_rare")
                    "epic" -> AppStrings.t(language, "rarity_epic")
                    else -> selectedItem.rarity
                }

                Text(
                    text = name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = rarityColor
                )
                Text(
                    text = if (isEquipped) {
                        "$rarityText • ${AppStrings.t(language, "status_equipped")}"
                    } else rarityText,
                    fontSize = 11.sp,
                    color = rarityColor.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))

                val stats = selectedItem.stats
                val ench = getEnchantLevel(selectedItem)
                val bonuses = buildList {
                    if (stats.power > 0 || ench > 0) add("⚔️ +${stats.power + ench}")
                    if (stats.armor > 0 || ench > 0) add("🛡️ +${stats.armor + ench}")
                    if (stats.health > 0 || ench > 0) add("❤️ +${stats.health + ench}")
                    if (stats.luck > 0f) add("🍀 +${stats.luck + ench}")
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
                            text = AppStrings.t(language, "btn_unequip"),
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
                            text = AppStrings.t(language, "btn_equip"),
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
                            text = AppStrings.t(language, "btn_sell"),
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
private fun InventoryStatsPanel(
    state: GameStateEntity,
    totalStats: com.pushupRPG.app.utils.TotalStats?,
    language: String,
    onSpendPoint: (String) -> Unit
) {
    val totalPower = totalStats?.power ?: state.basePower
    val totalArmor = totalStats?.armor ?: state.baseArmor
    val totalHealth = totalStats?.health ?: state.baseHealth
    val totalLuck = totalStats?.luck ?: state.baseLuck

    val hasPoints = state.unspentStatPoints > 0

    val context = LocalContext.current
    val statsBgResId = remember {
        context.resources.getIdentifier("bg_stats_panel", "drawable", context.packageName)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        if (statsBgResId != 0) {
            Image(
                painter = painterResource(id = statsBgResId),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier.matchParentSize()
                    .background(Color.Black.copy(alpha = 0.60f))
            )
        } else {
            Box(modifier = Modifier.matchParentSize().background(DarkCard))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.t(language, "stat_title"),
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
                label = AppStrings.t(language, "stat_power"),
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
                    text = AppStrings.t(language, "stat_armor"),
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
                label = AppStrings.t(language, "stat_health"),
                value = "$totalHealth",
                color = HealthColor,
                hasPoints = hasPoints,
                onSpend = { onSpendPoint("health") }
            )

            InventoryStatRowWithButton(
                icon = "🍀",
                label = AppStrings.t(language, "stat_luck"),
                value = String.format("%.1f", totalLuck),
                color = LuckColor,
                hasPoints = hasPoints,
                onSpend = { onSpendPoint("luck") }
            )
        } // Column
    } // Box (фон)
}

@Composable
private fun InventoryStatRowWithButton(
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

// --- Активные достижения ---
@Composable
private fun ActiveAchievementsPanel(
    state: GameStateEntity,
    language: String,
    viewModel: GameViewModel,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val activeIds = viewModel.getActiveAchievementIds(state)
    val defs = activeIds.mapNotNull { com.pushupRPG.app.utils.AchievementSystem.getDefById(it) }
    val panelBg = remember {
        context.resources.getIdentifier("bg_actach_panel", "drawable", context.packageName)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .background(DarkCard)
    ) {
        if (panelBg != 0) {
            Image(
                painter = painterResource(id = panelBg),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            defs.forEach { def ->
                val resId = remember(def.imageRes) {
                    context.resources.getIdentifier(
                        def.imageRes,
                        "drawable",
                        context.packageName
                    )
                }
                if (resId != 0) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = resId),
                        contentDescription = def.nameRu,
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier.size(36.dp)
                            .background(DarkSurfaceVariant, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("★", color = GoldAccent, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

private fun getItemEmoji(slot: String): String {
    return when (slot) {
        "weapon" -> "⚔️"
        "head" -> "⛑️"
        "necklace" -> "📿"
        "pants" -> "👖"
        "boots" -> "👟"
        else -> "📦"
    }
}

@Composable
fun InventoryScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToAchievements: () -> Unit = {}
) {
    // ИСПРАВЛЕНО: добавлено initial = null
    val gameState by viewModel.gameState.collectAsState(initial = null)
    val selectedItem by viewModel.selectedInventoryItem.collectAsState(initial = null)
    val totalStats by viewModel.totalStats.collectAsState(initial = null)

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

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        ScreenBackground("bg_inventory_overall")
        Column(modifier = Modifier.fillMaxSize()) {
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
                    text = AppStrings.t(language, "inventory"),
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
                    .navigationBarsPadding()
            ) {
                // Экипировка персонажа
                EquipmentSection(
                    state = state,
                    language = language,
                    onSlotClick = { slot ->
                        val equippedEntry = when (slot) {
                            "head" -> state.equippedHead
                            "necklace" -> state.equippedNecklace
                            "weapon1" -> state.equippedWeapon1
                            "weapon2" -> state.equippedWeapon2
                            "pants" -> state.equippedPants
                            "boots" -> state.equippedBoots
                            else -> ""
                        }
                        if (equippedEntry.isNotEmpty()) {
                            val uniqueId = equippedEntry.split(":")[0]
                            val item = ItemUtils.getItemById(equippedEntry)?.copy(id = uniqueId)
                            viewModel.selectInventoryItem(item)
                        }
                    },
                    onNavigateToShop = onNavigateToShop,
                    viewModel = viewModel,
                    onNavigateToAchievements = onNavigateToAchievements
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
                    onSell = { item -> viewModel.sellItem(item.id) },
                    getEnchantLevel = { item -> viewModel.getEnchantLevel(state, item.id) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Статы
                InventoryStatsPanel(
                    state = state,
                    totalStats = totalStats,
                    language = language,
                    onSpendPoint = { stat -> viewModel.spendStatPoint(stat) }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
