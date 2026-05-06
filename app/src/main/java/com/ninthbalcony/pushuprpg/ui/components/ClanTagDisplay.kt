package com.ninthbalcony.pushuprpg.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Color tokens for clan-tag accents. Keys must match `GameViewModel.CLAN_TAG_COLORS`. */
val CLAN_TAG_COLOR_MAP: Map<String, Color> = mapOf(
    "default" to Color(0xFF8A8E96),
    "blue"    to Color(0xFF3FA9F5),
    "green"   to Color(0xFF34C759),
    "red"     to Color(0xFFFF453A),
    "yellow"  to Color(0xFFFFCC00),
)

fun clanTagColor(key: String): Color = CLAN_TAG_COLOR_MAP[key] ?: CLAN_TAG_COLOR_MAP.getValue("default")

/**
 * Inline `[TAG]` chip used in profile header / friend rows / leaderboard rows.
 * Returns nothing if the tag is blank — callers can call unconditionally.
 */
@Composable
fun ClanTagText(
    tag: String,
    colorKey: String,
    fontSize: Int = 13,
    bold: Boolean = true,
    modifier: Modifier = Modifier,
) {
    if (tag.isBlank()) return
    Text(
        text = "[$tag]",
        color = clanTagColor(colorKey),
        fontSize = fontSize.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        modifier = modifier,
    )
}

/** Pill-shaped clan tag with background — used in profile header. */
@Composable
fun ClanTagPill(
    tag: String,
    colorKey: String,
    modifier: Modifier = Modifier,
) {
    val color = clanTagColor(colorKey)
    val display = if (tag.isBlank()) "[----]" else "[$tag]"
    Text(
        text = display,
        color = if (tag.isBlank()) color.copy(alpha = 0.6f) else color,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}
