package com.ninthbalcony.pushuprpg.data.model

/**
 * Один удар в серии при Save отжиманий. Переносится через смерти монстров —
 * `monsterName`/`monsterMaxHp` могут отличаться внутри одного chain'а.
 */
data class BattleHit(
    val damage: Int,
    val isCrit: Boolean,
    val monsterName: String,
    val monsterMaxHp: Int,
    val monsterImageRes: String,
    val monsterHpAfter: Int,
    val killed: Boolean
)

/** Последовательность ударов от одного Save'а отжиманий. */
data class BattleChain(
    val hits: List<BattleHit>
)
