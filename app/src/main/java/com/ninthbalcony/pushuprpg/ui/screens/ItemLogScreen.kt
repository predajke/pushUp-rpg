package com.ninthbalcony.pushuprpg.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.ninthbalcony.pushuprpg.data.model.Item
import com.ninthbalcony.pushuprpg.ui.GameViewModel
import com.ninthbalcony.pushuprpg.ui.theme.*
import com.ninthbalcony.pushuprpg.utils.AppStrings
import com.ninthbalcony.pushuprpg.utils.ItemUtils
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.ninthbalcony.pushuprpg.ui.preview.FakeGameRepository

// common = 0, legendary = 4 — ascending order для сортировки
private val RARITY_ORDER = listOf("common", "uncommon", "rare", "epic", "legendary")

private val SET_NAMES = mapOf(
    "berserker"   to Pair("Berserker",       "Берсерк"),
    "guardian"    to Pair("Guardian",         "Страж"),
    "shadow"      to Pair("Shadow",           "Тень"),
    "archon"      to Pair("Archon",           "Архонт"),
    "smith"       to Pair("Smith",            "Кузнец"),
    "hellxdead"   to Pair("Hell & Dead",      "Ад Мертвецов"),
    "singularity" to Pair("Singularity",      "Сингулярность"),
    "void"        to Pair("Void",             "Пустота"),
    "scifi"       to Pair("Sci-Fi",           "Sci-Fi"),
    "post"        to Pair("Post Apocalypse",  "Постапок"),
    "elf"         to Pair("Elf",              "Эльф"),
)

private fun rarityIndex(rarity: String) =
    RARITY_ORDER.indexOf(rarity).let { if (it < 0) Int.MAX_VALUE else it }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemLogScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState(initial = null)
    val state = gameState ?: return
    val language = state.language ?: "en"
    val context = LocalContext.current

    val allItems = remember { ItemUtils.loadItems(context) }
    val collectedIds = remember(state.itemLogJson, state.inventoryItems) {
        viewModel.getCollectedItemBaseIds(state)
    }
    val collectedCount = remember(collectedIds, allItems) { allItems.count { it.id in collectedIds } }

    // Обычные предметы (без сета), common → legendary, потом по имени
    val individualItems = remember(allItems, language) {
        allItems
            .filter { it.setId.isNullOrBlank() }
            .sortedWith(compareBy({ rarityIndex(it.rarity) }, { if (language == "ru") it.name_ru else it.name_en }))
    }

    // Сеты: Map<setId, List<Item>>, сортируем сеты по редкости первого предмета
    val setGroups: List<Pair<String, List<Item>>> = remember(allItems, language) {
        allItems
            .filter { !it.setId.isNullOrBlank() }
            .groupBy { it.setId!! }
            .map { (setId, items) ->
                val sorted = items.sortedWith(compareBy({ rarityIndex(it.rarity) }, { if (language == "ru") it.name_ru else it.name_en }))
                setId to sorted
            }
            .sortedBy { (_, items) -> rarityIndex(items.first().rarity) }
    }

    // Все предвычисленные resource ID — вне LazyVerticalGrid (избегаем remember внутри items())
    val imageResIds: Map<String, Int> = remember(allItems) {
        allItems.associate { item ->
            item.id to context.resources.getIdentifier(item.image_id, "drawable", context.packageName)
        }
    }

    var selectedItem by remember { mutableStateOf<Item?>(null) }

    selectedItem?.let { item ->
        ItemDetailDialog(
            item = item,
            imgResId = imageResIds[item.id] ?: 0,
            language = language,
            onDismiss = { selectedItem = null }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBackground)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "${AppStrings.t(language, "item_log")} ($collectedCount/${allItems.size})",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier.fillMaxSize().navigationBarsPadding(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // ── Обычные предметы ──
            items(individualItems, key = { "item_${it.id}" }) { item ->
                ItemCell(item, collectedIds, imageResIds, language, onClick = { selectedItem = item })
            }

            // ── Заголовок секции Сеты ──
            if (setGroups.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "header_sets") {
                    SectionHeader(if (language == "ru") "Сеты" else "Sets")
                }
            }

            // ── Каждый сет со своим заголовком ──
            setGroups.forEach { (setId, items) ->
                val setName = SET_NAMES[setId]?.let { if (language == "ru") it.second else it.first } ?: setId
                val setRarityColor = Color(ItemUtils.getRarityColor(items.first().rarity))

                item(span = { GridItemSpan(maxLineSpan) }, key = "header_$setId") {
                    SetHeader(name = setName, rarityColor = setRarityColor)
                }
                items(items, key = { "set_${it.id}" }) { item ->
                    ItemCell(item, collectedIds, imageResIds, language, onClick = { selectedItem = item })
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp, start = 4.dp)
    )
}

@Composable
private fun SetHeader(name: String, rarityColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp, start = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(14.dp)
                .background(rarityColor, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = name,
            color = rarityColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ItemCell(
    item: Item,
    collectedIds: Set<String>,
    imageResIds: Map<String, Int>,
    language: String,
    onClick: () -> Unit
) {
    val collected = item.id in collectedIds
    val rarityColor = Color(ItemUtils.getRarityColor(item.rarity))
    val imgResId = imageResIds[item.id] ?: 0

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkCard)
            .border(
                width = if (collected) 1.5.dp else 0.5.dp,
                color = if (collected) rarityColor else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .then(if (collected) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (imgResId != 0) {
            Image(
                painter = painterResource(id = imgResId),
                contentDescription = if (language == "ru") item.name_ru else item.name_en,
                modifier = Modifier
                    .size(48.dp)
                    .alpha(if (collected) 1f else 0.2f),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(rarityColor.copy(alpha = if (collected) 0.8f else 0.15f), RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun ItemDetailDialog(
    item: Item,
    imgResId: Int,
    language: String,
    onDismiss: () -> Unit
) {
    val rarityColor = Color(ItemUtils.getRarityColor(item.rarity))
    val name = if (language == "ru") item.name_ru else item.name_en
    val description = if (language == "ru") item.description_ru else item.description_en
    val rarityLabel = when (item.rarity) {
        "common" -> if (language == "ru") "Обычный" else "Common"
        "uncommon" -> if (language == "ru") "Необычный" else "Uncommon"
        "rare" -> if (language == "ru") "Редкий" else "Rare"
        "epic" -> if (language == "ru") "Эпический" else "Epic"
        "legendary" -> if (language == "ru") "Легендарный" else "Legendary"
        else -> item.rarity
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkCard, RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (imgResId != 0) {
                Image(
                    painter = painterResource(id = imgResId),
                    contentDescription = name,
                    modifier = Modifier.size(72.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(name, color = rarityColor, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(rarityLabel, color = rarityColor.copy(alpha = 0.7f), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (item.stats.power > 0)  StatChip("⚔", item.stats.power.toString())
                if (item.stats.armor > 0)  StatChip("🛡", item.stats.armor.toString())
                if (item.stats.health > 0) StatChip("❤", item.stats.health.toString())
                if (item.stats.luck > 0f)  StatChip("🍀", item.stats.luck.toInt().toString())
            }
            if (description.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    description,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (language == "ru") "Закрыть" else "Close", color = TextPrimary)
            }
        }
    }
}

@Composable
private fun StatChip(icon: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 18.sp)
        Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 920)
@Composable
private fun ItemLogScreenPreview() {
    val vm = remember { GameViewModel(FakeGameRepository()) }
    ItemLogScreen(viewModel = vm, onBack = {})
}
