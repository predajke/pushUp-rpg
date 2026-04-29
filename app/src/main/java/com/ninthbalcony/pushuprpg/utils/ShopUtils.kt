package com.ninthbalcony.pushuprpg.utils

import com.ninthbalcony.pushuprpg.data.model.Item
import kotlin.random.Random

object ShopUtils {

    const val SHOP_REFRESH_INTERVAL_MS = 10L * 60 * 1000 // 10 минут
    const val SHOP_SLOTS = 4

    // Цены покупки в магазине
    fun getBuyPrice(rarity: String): Int {
        return when (rarity) {
            "common" -> 5
            "uncommon" -> 10
            "rare" -> 20
            "legendary" -> 50
            else -> 5
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
        val minutes = remaining / 60_000
        val seconds = (remaining % 60_000) / 1_000
        return "${minutes}m ${seconds}s"
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