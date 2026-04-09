package com.ninthbalcony.pushuprpg.data.model

data class Monster(
    val id: Int,
    val name: String,
    val nameRu: String,
    val level: Int,
    val maxHp: Int,
    val damage: Int,
    val dropRate: Float,
    val imageRes: String,
    val isBoss: Boolean = false,
    val dropRarityMin: String = "common"  // боссы гарантируют rare+
)