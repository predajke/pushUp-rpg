package com.pushupRPG.app.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pushupRPG.app.data.model.Item

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
        // Убираем timestamp суффикс если есть
        val cleanId = run {
            val parts = id.split("_")
            if (parts.size > 2 && parts.last().all { it.isDigit() } && parts.last().length > 8) {
                parts.dropLast(1).joinToString("_")
            } else {
                id
            }
        }
        return allItems.find { it.id == cleanId }
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
            in 0f..55f -> "common"
            in 55f..80f -> "uncommon"
            in 80f..95f -> "rare"
            else -> "epic"
        }
    }

    fun getRarityColor(rarity: String): Long {
        return when (rarity) {
            "common" -> 0xFF9E9E9E     // серый
            "uncommon" -> 0xFF4CAF50   // зелёный
            "rare" -> 0xFF2196F3       // синий
            "epic" -> 0xFF9C27B0       // фиолетовый
            else -> 0xFF9E9E9E
        }
    }

    fun getItemName(item: Item, language: String): String {
        return if (language == "ru") item.name_ru else item.name_en
    }

    fun getItemDescription(item: Item, language: String): String {
        return if (language == "ru") item.description_ru else item.description_en
    }

    private data class ItemsWrapper(val items: List<Item>)
}

private val Random = kotlin.random.Random