package com.ninthbalcony.pushuprpg.data.model

data class ItemStats(
    val power: Int = 0,
    val armor: Int = 0,
    val health: Int = 0,
    val luck: Float = 0f
)

data class Item(
    val id: String,
    val name_ru: String,
    val name_en: String,
    val slot: String,
    val rarity: String,
    val stats: ItemStats,
    val description_ru: String,
    val description_en: String,
    val image_id: String,
    val setId: String? = null
)