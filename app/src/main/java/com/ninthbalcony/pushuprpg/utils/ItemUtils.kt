package com.ninthbalcony.pushuprpg.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ninthbalcony.pushuprpg.data.model.Item

object ItemUtils {

    private var allItems: List<Item> = emptyList()

    fun loadItems(context: Context): List<Item> {
        if (allItems.isNotEmpty()) return allItems
        return try {
            val json = context.assets.open("items.json")
                .bufferedReader()
                .use { it.readText() }
            val type = object : TypeToken<ItemsWrapper>() {}.type
            val wrapper: ItemsWrapper = Gson().fromJson(json, type)
            allItems = wrapper.items
            allItems
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getItemById(id: String): Item? {
        return allItems.find { it.id == getBaseItemId(id) }
    }

    fun getBaseItemId(id: String): String {
        // Убираем уровень заточки если есть
        val withoutLevel = id.split(":")[0]
        // Убираем timestamp суффикс если есть
        val parts = withoutLevel.split("_")
        return if (parts.size > 2 &&
            parts.last().all { it.isDigit() } &&
            parts.last().length > 8) {
            parts.dropLast(1).joinToString("_")
        } else {
            withoutLevel
        }
    }

    fun getItemsBySlot(slot: String): List<Item> {
        return allItems.filter { it.slot == slot }
    }

    fun getRandomItemByRarity(): Item? {
        if (allItems.isEmpty()) return null
        val rarity = rollRarity()
        val itemsOfRarity = allItems.filter { it.rarity == rarity }
        return if (itemsOfRarity.isNotEmpty()) {
            itemsOfRarity.random()
        } else {
            allItems.random()
        }
    }

    private fun rollRarity(): String {
        return when (Random.nextFloat() * 100f) {
            in 0f..50f  -> "common"
            in 50f..75f -> "uncommon"
            in 75f..90f -> "rare"
            in 90f..98f -> "epic"
            else        -> "legendary"
        }
    }

    fun getRarityColor(rarity: String): Long {
        return when (rarity) {
            "common"    -> 0xFF9E9E9E
            "uncommon"  -> 0xFF4CAF50
            "rare"      -> 0xFF2196F3
            "epic"      -> 0xFF9C27B0
            "legendary" -> 0xFFFFD700  // золотой
            else        -> 0xFF9E9E9E
        }
    }

    fun getSetBonuses(equippedItems: List<com.ninthbalcony.pushuprpg.data.model.Item>): SetBonuses {
        val setCounts = equippedItems.mapNotNull { it.setId }.groupingBy { it }.eachCount()
        var dmg = 0f; var armor = 0f; var drop = 0f; var xp = 0f; var enchant = 0f
        for ((setId, count) in setCounts) {
            when (setId) {
                // Сет-бонусы сбалансированы: снижены ~40-50% от оригинала
                "berserker" -> { dmg  += if (count >= 3) 0.20f else if (count >= 2) 0.10f else 0f }
                "guardian"  -> { armor += if (count >= 3) 0.25f else if (count >= 2) 0.12f else 0f }
                "shadow"    -> { drop  += if (count >= 3) 0.20f else if (count >= 2) 0.10f else 0f }
                "archon"    -> { xp    += if (count >= 3) 0.25f else if (count >= 2) 0.12f else 0f }
                "smith"     -> { enchant += if (count >= 3) 0.20f else if (count >= 2) 0.10f else 0f }
                "hellxdead" -> { dmg += if (count >= 3) 0.45f else if (count >= 2) 0.22f else 0f; drop += if (count >= 3) 0.15f else 0f }
                "singularity" -> {
                    dmg += if (count >= 4) 0.50f else if (count >= 3) 0.30f else if (count >= 2) 0.15f else 0f
                    armor += if (count >= 4) 0.25f else if (count >= 3) 0.10f else 0f
                }
                "void"      -> {
                    dmg += when {
                        count >= 5 -> 0.90f
                        count >= 4 -> 0.65f
                        count >= 3 -> 0.45f
                        count >= 2 -> 0.22f
                        else       -> 0f
                    }
                    drop += if (count >= 5) 0.50f else if (count >= 4) 0.30f else if (count >= 3) 0.15f else 0f
                    xp   += if (count >= 5) 0.30f else if (count >= 3) 0.12f else 0f
                }
                "scifi"     -> {
                    armor += if (count >= 3) 0.18f else if (count >= 2) 0.10f else 0f
                    dmg   += if (count >= 3) 0.15f else 0f
                }
                "post"      -> {
                    dmg += if (count >= 4) 1.00f else if (count >= 3) 0.55f else if (count >= 2) 0.28f else 0f
                }
                "elf"       -> {
                    dmg   += when { count >= 5 -> 1.20f; count >= 4 -> 0.70f; count >= 3 -> 0.45f; count >= 2 -> 0.22f; else -> 0f }
                    armor += when { count >= 5 -> 0.45f; count >= 4 -> 0.28f; count >= 3 -> 0.15f; else -> 0f }
                    drop  += if (count >= 5) 0.40f else 0f
                }
            }
        }
        return SetBonuses(damagePercent = dmg, armorPercent = armor, dropRatePercent = drop, xpPercent = xp, enchantPercent = enchant)
    }

    fun getItemName(item: Item, language: String, enchantLevel: Int = 0): String {
        val base = if (language == "ru") item.name_ru else item.name_en
        return if (enchantLevel > 0) "$base +$enchantLevel" else base
    }

    fun getItemDescription(item: Item, language: String): String {
        return if (language == "ru") item.description_ru else item.description_en
    }

    private data class ItemsWrapper(val items: List<com.ninthbalcony.pushuprpg.data.model.Item>)
}

data class SetBonuses(
    val damagePercent: Float = 0f,
    val armorPercent: Float = 0f,
    val dropRatePercent: Float = 0f,
    val xpPercent: Float = 0f,
    val enchantPercent: Float = 0f
)

private val Random = kotlin.random.Random