package com.ninthbalcony.pushuprpg.utils

import com.ninthbalcony.pushuprpg.data.db.GameStateEntity
import kotlin.random.Random

enum class NightStatBoostType { DMG_PERCENT, ARMOR_PERCENT, POWER_FLAT, ARMOR_FLAT, HP_FLAT }

sealed class NightSpinReward {
    object Nothing : NightSpinReward()
    data class StatBoost(val type: NightStatBoostType) : NightSpinReward()
    data class EnchantedItem(val tier: String, val itemIconRes: String = "", val enchantLevel: Int = 0, val itemNameEn: String = "", val itemNameRu: String = "") : NightSpinReward()
}

fun NightSpinReward.toRibbonType(): String = when (this) {
    is NightSpinReward.Nothing      -> "night_nothing"
    is NightSpinReward.StatBoost    -> "night_stat"
    is NightSpinReward.EnchantedItem -> when (tier) {
        "high" -> "night_high"
        "med"  -> "night_med"
        else   -> "night_low"
    }
}

fun generateNightSpinResult(): NightSpinReward {
    val roll = Random.nextFloat() * 100f
    return when {
        roll < 1f  -> NightSpinReward.StatBoost(NightStatBoostType.values().random())
        roll < 3f  -> NightSpinReward.EnchantedItem("high")
        roll < 6f  -> NightSpinReward.EnchantedItem("med")
        roll < 10f -> NightSpinReward.EnchantedItem("low")
        else       -> NightSpinReward.Nothing
    }
}

data class SpinReward(
    val type: String,        // "clover_box", "boss_cube", "boots_001", "teeth"
    val amount: Int = 0,     // for teeth
    val itemId: String = "", // for items
    val displayName: String  // for UI
)

data class SpinResult(
    val reward: SpinReward,
    val wonItemIds: List<String> = emptyList(),  // ID предметов которые выпали (для диалога)
    val animation_duration_ms: Int = 6000
)

object SpinUtils {

    const val MAX_DAILY_AD_VIEWS = 15
    const val TEETH_PER_SPIN = 25

    // Генерирует спин результат на основе вероятностей
    fun generateSpinResult(): SpinReward {
        val roll = Random.nextFloat() * 100f

        return when {
            roll < 3f  -> SpinReward(type = "boss_cube",     itemId = "boss_cube", displayName = "Boss Cube")
            roll < 15f -> SpinReward(type = "clover_box",    itemId = "clover_box", displayName = "Clover Box")
            roll < 34f -> SpinReward(type = "rare_spin",     displayName = "Rare Item")
            roll < 54f -> SpinReward(type = "uncommon_spin", displayName = "Uncommon Item")
            roll < 75f -> SpinReward(type = "teeth",         amount = TEETH_PER_SPIN, displayName = "$TEETH_PER_SPIN 🦷")
            else       -> SpinReward(type = "common_spin",   displayName = "Common Item")
        }
    }

    // Подсчитывает доступные спины на основе всех источников
    fun calculateAvailableSpins(
        state: GameStateEntity,
        pushUpsTodayForBonus: Boolean  // true если pushUpsToday >= 200
    ): Int {
        var spins = 0

        // 1 бесплатный спин в день
        if (state.dailySpinUsedToday == 0) spins += 1

        // Реклама: до 10 просмотров (каждый = 1 спин)
        spins += state.dailySpinAdViewsToday

        // Покупки: каждые 40 = +1 спин
        val purchaseBonus = state.totalShopPurchases / 40
        spins += purchaseBonus

        // Заточка: каждые 25 = +1 спин
        val enchantBonus = state.totalEnchantAttempts / 25
        spins += enchantBonus

        // Слияние: каждые 25 = +1 спин
        val mergeBonus = state.totalMergeAttempts / 25
        spins += mergeBonus

        // 200+ отжиманий в день: +2 спина
        if (pushUpsTodayForBonus) spins += 2

        // Level up дает +3 спина (будет добавлено в processAddXp)
        // (не отслеживается в state, а добавляется напрямую при levelUp)

        return spins
    }

    // Проверяет можно ли ещё просмотреть рекламу
    fun canWatchAd(state: GameStateEntity): Boolean {
        return state.dailySpinAdViewsToday < MAX_DAILY_AD_VIEWS
    }

    // ===== Night Spin =====
    private val NIGHT_SPIN_WEIGHTED_TYPES: List<String> = buildList {
        repeat(54) { add("night_nothing") }
        repeat(2)  { add("night_low") }
        repeat(2)  { add("night_med") }
        add("night_high")
        add("night_stat")
    }

    fun buildNightSpinRibbon(winnerType: String?): List<String> {
        val items = MutableList(60) { NIGHT_SPIN_WEIGHTED_TYPES.random() }
        if (winnerType != null) items[22] = winnerType
        return items
    }
}
