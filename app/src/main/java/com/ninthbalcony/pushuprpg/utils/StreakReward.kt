package com.ninthbalcony.pushuprpg.utils

/**
 * Видимые награды за длительный streak. Дополняет существующий скрытый
 * [GameCalculations.getStreakXpBonus] явной таблицей milestone'ов, которые
 * игрок видит и может claim'нуть один раз за milestone.
 *
 * Балансировка: награды растут нелинейно — день 7 даёт уверенный «вау»,
 * день 30 — серьёзная мотивация, день 90 — почти trophy.
 */
data class StreakMilestone(
    val day: Int,
    val teeth: Int,
    val statPoints: Int,
    val spinTokens: Int,
    /** "common" / "uncommon" / "rare" / "epic" / "legendary"; null = без предмета. */
    val itemRarity: String?,
)

object StreakRewards {

    val MILESTONES: List<StreakMilestone> = listOf(
        StreakMilestone(day = 3,  teeth = 20,   statPoints = 0,  spinTokens = 0, itemRarity = null),
        StreakMilestone(day = 7,  teeth = 50,   statPoints = 1,  spinTokens = 0, itemRarity = "common"),
        StreakMilestone(day = 14, teeth = 100,  statPoints = 2,  spinTokens = 1, itemRarity = "uncommon"),
        StreakMilestone(day = 21, teeth = 200,  statPoints = 3,  spinTokens = 1, itemRarity = "rare"),
        StreakMilestone(day = 30, teeth = 500,  statPoints = 5,  spinTokens = 2, itemRarity = "epic"),
        StreakMilestone(day = 60, teeth = 1000, statPoints = 8,  spinTokens = 3, itemRarity = "epic"),
        StreakMilestone(day = 90, teeth = 2000, statPoints = 12, spinTokens = 5, itemRarity = "legendary"),
    )

    /** Ближайший unclaimed milestone впереди, или null если все достигнуты. */
    fun nextMilestone(currentStreak: Int): StreakMilestone? =
        MILESTONES.firstOrNull { it.day > currentStreak }

    /** Самый высокий уже-достижимый milestone, который ещё не выдан. */
    fun pendingClaim(currentStreak: Int, lastClaimedDay: Int): StreakMilestone? =
        MILESTONES.lastOrNull { it.day <= currentStreak && it.day > lastClaimedDay }
}
