package com.pushupRPG.app.utils

import com.pushupRPG.app.data.model.Item
import kotlin.random.Random

object ShopUtils {

    const val SHOP_REFRESH_INTERVAL_MS = 6L * 60 * 60 * 1000 // 6 часов
    const val SHOP_SLOTS = 4

    // Цены покупки в магазине
    fun getBuyPrice(rarity: String): Int {
        return when (rarity) {
            "common" -> 2
            "uncommon" -> 4
            "rare" -> 10
            "legendary" -> 50
            else -> 2
        }
    }

    // Генерируем товары для магазина (только common, uncommon, rare)
    fun generateShopItems(allItems: List<Item>): List<Item> {
        val eligible = allItems.filter { it.rarity != "epic" && it.rarity != "legendary" }
        if (eligible.isEmpty()) return emptyList()

        val result = mutableListOf<Item>()
        val itemCount = Random.nextInt(1, SHOP_SLOTS) // 1-3 предмета

        repeat(itemCount) {
            result.add(eligible.random())
        }
        return result
    }

    // Сохраняем товары как строку ID
    fun shopItemsToString(items: List<Item>): String {
        return items.joinToString(",") { it.id }
    }

    // Читаем товары из строки ID
    fun shopItemsFromString(str: String): List<Item> {
        if (str.isEmpty()) return emptyList()
        return str.split(",")
            .filter { it.isNotEmpty() }
            .mapNotNull { ItemUtils.getItemById(it) }
    }

    fun shouldRefreshShop(lastRefresh: Long): Boolean {
        return System.currentTimeMillis() - lastRefresh >= SHOP_REFRESH_INTERVAL_MS
    }

    fun getTimeUntilRefresh(lastRefresh: Long): String {
        val remaining = SHOP_REFRESH_INTERVAL_MS - (System.currentTimeMillis() - lastRefresh)
        if (remaining <= 0) return "Now"
        val hours = remaining / 3_600_000
        val minutes = (remaining % 3_600_000) / 60_000
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }

    // Шансы кузницы. null = FAIL (15%)
    fun rollForgeRarity(): String? {
        return when (Random.nextFloat() * 100f) {
            in 0f..50f  -> "common"
            in 50f..70f -> "uncommon"
            in 70f..80f -> "rare"
            in 80f..84f -> "epic"
            in 84f..85f -> "legendary"
            else        -> null
        }
    }

    // Шансы Clover Box
    fun rollCloverBoxRarity(): String {
        return when (Random.nextFloat() * 100f) {
            in 0f..70f -> "common"
            in 70f..90f -> "uncommon"
            else -> "rare"
        }
    }
}