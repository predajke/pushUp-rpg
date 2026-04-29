package com.ninthbalcony.pushuprpg.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninthbalcony.pushuprpg.data.db.GameStateEntity
import com.ninthbalcony.pushuprpg.ui.GameViewModel
import java.text.SimpleDateFormat
import java.util.Locale

// ── Design tokens (War Council dark-fantasy palette) ──────────────────────────

private val LbBgDeep        = Color(0xFF0E0A07)
private val LbBgCard        = Color(0xFF1A1310)
private val LbBgCardEnd     = Color(0xFF14100C)
private val LbBorderLeather = Color(0xFF2A1F17)
private val LbBorderPill    = Color(0xFF3A2C20)
private val LbGold          = Color(0xFFC9A35B)
private val LbGoldBright    = Color(0xFFF0C869)
private val LbGoldDim       = Color(0xFF8B6F3D)
private val LbGoldDarker    = Color(0xFF6B5430)
private val LbGoldDeepest   = Color(0xFF5A4525)
private val LbSilver        = Color(0xFFCFD1D4)
private val LbBronze        = Color(0xFFC8814A)
private val LbParchment     = Color(0xFFE8D9B3)
private val LbParchmentBright = Color(0xFFF5E8C8)
private val LbParchmentDim  = Color(0xFFBBA27A)
private val LbRowAlt        = Color(0x03FFFFFF)  // rgba(255,255,255,0.012)
private val LbMeBgStart     = Color(0x1AF0C869)
private val LbMeBgEnd       = Color(0x0AF0C869)

// Hoisted brushes — reused across all rows / recompositions to avoid per-frame allocations.
private val BrushMeRow       = Brush.horizontalGradient(listOf(LbMeBgStart, LbMeBgEnd))
private val BrushAltRow      = Brush.horizontalGradient(listOf(LbRowAlt, LbRowAlt))
private val BrushHeader      = Brush.verticalGradient(listOf(LbBgCard, LbBgCardEnd))
private val BrushStickyMe    = Brush.verticalGradient(listOf(LbBgCard, LbBgDeep))
private val BrushGoldRule    = Brush.horizontalGradient(listOf(Color.Transparent, LbGold, Color.Transparent))

// ── Data model ────────────────────────────────────────────────────────────────

data class LeaderboardPlayer(
    val rank: Int,
    val name: String,
    val country: String,
    val isFriend: Boolean = false,
    val isMe: Boolean = false,
    val res: Int = 0,
    val lvl: Int = 1,
    val totalPushUps: Int = 0,
    val power: Int = 0,
    val armor: Int = 0,
    val hp: Int = 0,
    val luck: Int = 0,
    val ageDays: Int = 1,
)

enum class LbScope { GLOBAL, COUNTRY, FRIENDS }
enum class LbPeriod { DAY, WEEK, MONTH, ALL }
enum class LbSortKey { TOTAL_PUSH_UPS, LVL, RES, POWER, ARMOR, HP, LUCK, AGE }
enum class LbSortDir { ASC, DESC }

data class ColDef(
    val key: String,
    val label: String,
    val widthDp: Dp,
    val align: Alignment.Horizontal,
    val icon: String? = null,
    val sortKey: LbSortKey? = null,
)

private val ALL_COLS = listOf(
    ColDef("rank",  "#",    30.dp,  Alignment.CenterHorizontally),
    ColDef("flag",  "",     22.dp,  Alignment.CenterHorizontally),
    ColDef("name",  "NAME", 120.dp, Alignment.Start),
    ColDef("res",   "RES",  36.dp,  Alignment.End,   "res",    LbSortKey.RES),
    ColDef("lvl",   "LVL",  34.dp,  Alignment.End,   "lvl",    LbSortKey.LVL),
    ColDef("push",  "PUSH", 54.dp,  Alignment.End,   "pushup", LbSortKey.TOTAL_PUSH_UPS),
    ColDef("power", "PWR",  46.dp,  Alignment.End,   "power",  LbSortKey.POWER),
    ColDef("armor", "ARM",  46.dp,  Alignment.End,   "armor",  LbSortKey.ARMOR),
    ColDef("hp",    "HP",   44.dp,  Alignment.End,   "hp",     LbSortKey.HP),
    ColDef("luck",  "LCK",  40.dp,  Alignment.End,   "luck",   LbSortKey.LUCK),
    ColDef("age",   "AGE",  48.dp,  Alignment.End,   "age",    LbSortKey.AGE),
)

// ── Formatters ────────────────────────────────────────────────────────────────

private fun fmt(n: Int): String = when {
    n >= 1_000_000 -> "%.1fM".format(n / 1_000_000.0).trimEnd('0').trimEnd('.')  + "M"
    n >= 10_000    -> "%.1fk".format(n / 1_000.0).trimEnd('0').trimEnd('.') + "k"
    else           -> "%,d".format(n)
}

private fun fmtAge(days: Int): String {
    if (days >= 365) {
        val y = days / 365
        val d = days % 365
        return "${y}y ${d}d"
    }
    return "${days}d"
}

private fun colValue(p: LeaderboardPlayer, key: String): String = when (key) {
    "res"   -> p.res.toString()
    "lvl"   -> p.lvl.toString()
    "push"  -> fmt(p.totalPushUps)
    "power" -> fmt(p.power)
    "armor" -> fmt(p.armor)
    "hp"    -> fmt(p.hp)
    "luck"  -> fmt(p.luck)
    "age"   -> fmtAge(p.ageDays)
    else    -> ""
}

// ── Filtering & sorting ───────────────────────────────────────────────────────

private fun applyFilters(
    players: List<LeaderboardPlayer>,
    me: LeaderboardPlayer,
    scope: LbScope,
    query: String,
): List<LeaderboardPlayer> {
    var list = players
    list = when (scope) {
        LbScope.COUNTRY -> list.filter { it.country == me.country || it.isMe }
        LbScope.FRIENDS -> list.filter { it.isFriend || it.isMe }
        LbScope.GLOBAL  -> list
    }
    if (query.isNotBlank()) list = list.filter { it.name.contains(query, ignoreCase = true) }
    return list
}

private fun applySort(list: List<LeaderboardPlayer>, key: LbSortKey, dir: LbSortDir): List<LeaderboardPlayer> {
    val base = compareBy<LeaderboardPlayer> { p ->
        when (key) {
            LbSortKey.TOTAL_PUSH_UPS -> p.totalPushUps
            LbSortKey.LVL   -> p.lvl
            LbSortKey.RES   -> p.res
            LbSortKey.POWER -> p.power
            LbSortKey.ARMOR -> p.armor
            LbSortKey.HP    -> p.hp
            LbSortKey.LUCK  -> p.luck
            LbSortKey.AGE   -> p.ageDays
        }
    }
    return list.sortedWith(if (dir == LbSortDir.DESC) base.reversed() else base)
}

// ── Flag (country code → emoji flag or color block) ──────────────────────────
// Using emoji flag characters (Unicode regional indicators) — supported on Android 7+

private fun countryToFlag(code: String): String {
    if (code.length != 2) return "🏳"
    val offset = 0x1F1E6 - 'A'.code
    return String(intArrayOf(code[0].uppercaseChar().code + offset, code[1].uppercaseChar().code + offset), 0, 2)
}

// ── Avatar monogram ───────────────────────────────────────────────────────────

@Composable
private fun LbAvatar(name: String, isMe: Boolean, size: Dp) {
    val initial = remember(name) { (name.firstOrNull() ?: '?').uppercaseChar().toString() }
    val bg = remember(name) {
        val hue = name.fold(0) { acc, c -> (acc * 31 + c.code) % 360 }.toFloat()
        Color.hsl(hue, 0.30f, 0.18f)
    }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bg)
            .border(
                width = if (isMe) 1.5.dp else 1.dp,
                color = if (isMe) LbGoldBright else LbGoldDeepest,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = if (isMe) LbGoldBright else LbParchment,
            fontSize = (size.value * 0.40f).sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ── Rank cell ─────────────────────────────────────────────────────────────────

@Composable
private fun RankCell(rank: Int, isMe: Boolean) {
    val medalColor = when (rank) {
        1 -> LbGoldBright
        2 -> LbSilver
        3 -> LbBronze
        else -> null
    }
    if (medalColor != null) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("●", color = medalColor, fontSize = 9.sp)
            Text(
                text = rank.toString(),
                color = medalColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    } else {
        Text(
            text = rank.toString(),
            color = if (isMe) LbGoldBright else LbGoldDim,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ── Column header icon (drawn as text glyphs) ────────────────────────────────

@Composable
private fun StatIconText(kind: String, color: Color, size: Int = 9) {
    val glyph = when (kind) {
        "pushup" -> "💪"
        "power"  -> "⚔"
        "armor"  -> "🛡"
        "hp"     -> "❤"
        "luck"   -> "🍀"
        "age"    -> "⌛"
        "lvl"    -> "★"
        "res"    -> "↺"
        else     -> ""
    }
    Text(glyph, color = color, fontSize = size.sp, lineHeight = size.sp)
}

// ── Column header row ─────────────────────────────────────────────────────────

@Composable
private fun LbColumnHeader(
    sortKey: LbSortKey,
    sortDir: LbSortDir,
    onSort: (LbSortKey) -> Unit,
    hScroll: ScrollState,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrushHeader)
            .border(width = 0.5.dp, color = LbBorderLeather)
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(hScroll)
                .padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ALL_COLS.forEach { col ->
                val active = col.sortKey == sortKey
                val color = if (active) LbGoldBright else LbGoldDarker
                Row(
                    modifier = Modifier
                        .width(col.widthDp)
                        .then(if (col.sortKey != null) Modifier.clickable { onSort(col.sortKey) } else Modifier)
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = when (col.align) {
                        Alignment.End -> Arrangement.End
                        Alignment.CenterHorizontally -> Arrangement.Center
                        else -> Arrangement.Start
                    },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (col.icon != null) {
                        StatIconText(col.icon, color, 9)
                        Spacer(Modifier.width(2.dp))
                    }
                    Text(
                        text = col.label,
                        color = color,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.12.sp,
                    )
                    if (active) {
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = if (sortDir == LbSortDir.ASC) "▲" else "▼",
                            color = LbGoldBright,
                            fontSize = 8.sp,
                        )
                    }
                }
            }
        }
    }
}

// ── Single player row ─────────────────────────────────────────────────────────

@Composable
private fun LbPlayerRow(
    p: LeaderboardPlayer,
    alt: Boolean,
    compact: Boolean,
    hScroll: ScrollState,
) {
    val padV = if (compact) 5.dp else 8.dp
    val avatarSize = if (compact) 16.dp else 20.dp
    val isMe = p.isMe

    val rowMod = when {
        isMe -> Modifier.background(BrushMeRow)
        alt  -> Modifier.background(BrushAltRow)
        else -> Modifier
    }

    Box(modifier = Modifier.fillMaxWidth().then(rowMod)) {
        // Gold left rail for "me" — pinned to the visible left edge (does not scroll)
        if (isMe) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(LbGoldBright)
                    .align(Alignment.CenterStart)
            )
        }

        Row(
            modifier = Modifier
                .horizontalScroll(hScroll)
                .padding(horizontal = 8.dp, vertical = padV),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ALL_COLS.forEach { col ->
                Box(
                    modifier = Modifier.width(col.widthDp).padding(horizontal = 2.dp),
                    contentAlignment = when (col.align) {
                        Alignment.End -> Alignment.CenterEnd
                        Alignment.CenterHorizontally -> Alignment.Center
                        else -> Alignment.CenterStart
                    },
                ) {
                    when (col.key) {
                        "rank" -> RankCell(rank = p.rank, isMe = isMe)
                        "flag" -> Text(countryToFlag(p.country), fontSize = 11.sp, lineHeight = 11.sp)
                        "name" -> Row(verticalAlignment = Alignment.CenterVertically) {
                            LbAvatar(name = p.name, isMe = isMe, size = avatarSize)
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = p.name,
                                color = if (isMe) LbGoldBright else LbParchment,
                                fontSize = 11.sp,
                                fontWeight = if (isMe) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            if (p.isFriend && !isMe) {
                                Spacer(Modifier.width(3.dp))
                                Text("•", color = LbGoldDim, fontSize = 8.sp)
                            }
                        }
                        else -> Text(
                            text = colValue(p, col.key),
                            color = if (isMe) LbParchmentBright else LbParchmentDim,
                            fontSize = 11.sp,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }

    // Divider
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(LbBorderLeather.copy(alpha = 0.45f))
    )
}

// ── Scope tab ─────────────────────────────────────────────────────────────────

@Composable
private fun ScopeTab(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(3.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (selected) BrushHeader else SolidColor(LbBgDeep), shape)
            .border(
                width = if (selected) 1.dp else 0.5.dp,
                color = if (selected) LbGold else LbBorderLeather,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) LbGoldBright else LbGoldDim,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.12.sp,
        )
    }
}

// ── Period pill ───────────────────────────────────────────────────────────────

@Composable
private fun PeriodPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) LbGold.copy(alpha = 0.10f) else Color.Transparent)
            .border(
                width = if (selected) 1.dp else 0.5.dp,
                color = if (selected) LbGold else LbBorderPill,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) LbGoldBright else LbGoldDim,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.05.sp,
        )
    }
}

// ── Main screen ───────────────────────────────────────────────────────────────

@Composable
fun LeaderboardScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
) {
    val gameState by viewModel.gameState.collectAsState(initial = null)
    val language  = gameState?.language ?: "en"

    // --- State ---
    var scope    by remember { mutableStateOf(LbScope.GLOBAL) }
    var period   by remember { mutableStateOf(LbPeriod.ALL) }
    var query    by remember { mutableStateOf("") }
    var sortKey  by remember { mutableStateOf(LbSortKey.TOTAL_PUSH_UPS) }
    var sortDir  by remember { mutableStateOf(LbSortDir.DESC) }
    var compact  by remember { mutableStateOf(false) }
    val hScroll = rememberScrollState()

    // Mock 500-player roster is deterministic — generate once. Replace with real API later.
    val mockRoster = remember { generateMockRoster() }
    val me = remember(gameState) { buildMePlayer(gameState) }
    val players = remember(mockRoster, me) {
        // Slot "me" at rank 364 (per design spec) without disturbing surrounding ranks.
        mockRoster.toMutableList().also { it[me.rank - 1] = me }
    }

    val filtered = remember(players, scope, query) { applyFilters(players, me, scope, query) }
    val sorted = remember(filtered, sortKey, sortDir) { applySort(filtered, sortKey, sortDir) }

    val onSortTap: (LbSortKey) -> Unit = { key ->
        if (sortKey == key) {
            sortDir = if (sortDir == LbSortDir.ASC) LbSortDir.DESC else LbSortDir.ASC
        } else {
            sortKey = key
            sortDir = LbSortDir.DESC
        }
    }

    // ── Layout ──────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LbBgDeep)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // ── Header band ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LbBgCard)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("❖", color = LbGold, fontSize = 14.sp)
            Spacer(Modifier.width(10.dp))
            Text(
                text = "LEADERBOARD",
                color = LbGoldBright,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.34.sp,
            )
            Spacer(Modifier.width(10.dp))
            Text("❖", color = LbGold, fontSize = 14.sp)
        }
        // Gold rule under header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BrushGoldRule)
        )

        val scopes = remember(language) {
            listOf(
                LbScope.GLOBAL  to if (language == "ru") "Глобально" else "Global",
                LbScope.COUNTRY to if (language == "ru") "Страна"    else "Country",
                LbScope.FRIENDS to if (language == "ru") "Друзья"    else "Friends",
            )
        }
        val periods = remember(language) {
            listOf(
                LbPeriod.DAY   to if (language == "ru") "День"   else "Day",
                LbPeriod.WEEK  to if (language == "ru") "Неделя" else "Week",
                LbPeriod.MONTH to if (language == "ru") "Месяц"  else "Month",
                LbPeriod.ALL   to if (language == "ru") "За всё" else "All Time",
            )
        }

        // ── Scope tabs ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            scopes.forEach { (s, label) ->
                ScopeTab(label, selected = scope == s, onClick = { scope = s }, modifier = Modifier.weight(1f))
            }
        }

        // ── Period pills ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            periods.forEach { (p, label) ->
                PeriodPill(label, selected = period == p, onClick = { period = p })
            }
        }

        // ── Search ───────────────────────────────────────────────────────────
        val searchShape = RoundedCornerShape(3.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 6.dp)
                .background(LbBgCard, searchShape)
                .border(0.5.dp, LbBorderLeather, searchShape)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("🔍", color = LbGold, fontSize = 12.sp)
            Spacer(Modifier.width(6.dp))
            val placeholder = if (language == "ru") "Фильтр по имени…" else "Filter by name…"
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                cursorBrush = SolidColor(LbGold),
                textStyle = TextStyle(color = LbParchment, fontSize = 12.sp),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(placeholder, color = LbGoldDim, fontSize = 12.sp)
                    }
                    inner()
                }
            )
        }

        // ── Column header (sticky via Box structure, shares horizontal scroll) ─
        LbColumnHeader(sortKey = sortKey, sortDir = sortDir, onSort = onSortTap, hScroll = hScroll)

        // ── Scrolling list + sticky-you ──────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            if (sorted.isEmpty()) {
                val emptyMsg = if (language == "ru") "Чемпионов не найдено." else "No champions match."
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(emptyMsg, color = LbGoldDeepest, fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp),
                ) {
                    itemsIndexed(sorted, key = { _, p -> "${p.rank}-${p.name}" }) { idx, p ->
                        LbPlayerRow(p = p, alt = idx % 2 == 1, compact = compact, hScroll = hScroll)
                    }
                }
            }

            // Sticky me row pinned at bottom
            StickyMeRow(
                me = me,
                sortedList = sorted,
                compact = compact,
                hScroll = hScroll,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

// ── Sticky me row with modifier support ──────────────────────────────────────

@Composable
private fun StickyMeRow(
    me: LeaderboardPlayer,
    sortedList: List<LeaderboardPlayer>,
    compact: Boolean,
    hScroll: ScrollState,
    modifier: Modifier = Modifier,
) {
    val meIdx = sortedList.indexOfFirst { it.isMe }
    val above = if (meIdx > 0) sortedList[meIdx - 1] else null
    val below = if (meIdx in 0 until sortedList.size - 1) sortedList[meIdx + 1] else null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BrushStickyMe)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, LbGold, Color.Transparent)
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "YOUR STANDING",
                color = LbGoldDarker,
                fontSize = 9.sp,
                letterSpacing = 0.18.sp,
            )
            val ctx = buildString {
                above?.let { append("↑ #${it.rank} ${it.name.take(10)}") }
                if (above != null && below != null) append("  ·  ")
                below?.let { append("↓ #${it.rank} ${it.name.take(10)}") }
            }
            if (ctx.isNotBlank()) {
                Text(text = ctx, color = LbGoldDarker, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        LbPlayerRow(p = me, alt = false, compact = compact, hScroll = hScroll)
    }
}

// ── Mock data generator (replace with real API / Firebase call) ───────────────

private const val MY_RANK = 364
private const val MY_COUNTRY = "UA"

private val MOCK_COUNTRIES = listOf(
    "US","UA","PL","DE","FR","ES","IT","GB","BR","AR","MX","JP","KR","CN","TH",
    "VN","ID","PH","IN","TR","EG","ZA","NG","SE","NO","FI","DK","NL","BE","CZ",
    "SK","HU","RO","GR","PT","IE","AT","CH","AU","NZ","CA","CL","CO","RU","BY",
    "KZ","GE","IL","SA","AE","SG","MY","TW","HK","LT","LV","EE","BG","HR","RS"
)
private val MOCK_FIRST = listOf("Krak","Vor","Mor","Drag","Tor","Gar","Bran","Ash","Ven","Dur","Hex","Ulf","Kael","Fen","Iron","Stone","Steel","Blood","Storm","Frost","Shadow","Wolf","Hawk","Bone","Dawn","Night","Grim","Red")
private val MOCK_LAST  = listOf("mar","grim","heart","fang","blade","born","wolf","crow","axe","rune","bane","wind","hammer","shade","horn","fist","thorn","dawn","star")
private val MOCK_FRIENDS = setOf(2, 17, 44, 88, 121, 199, 247, 312, 333, 360, 363, 365, 388, 401, 442, 477)

private fun generateMockRoster(): List<LeaderboardPlayer> {
    var seed = 20260428
    fun nextFloat(): Float {
        seed = (seed * 1664525 + 1013904223).toInt()
        return ((seed ushr 8) and 0xFFFFFF).toFloat() / 0xFFFFFF
    }
    fun nextInt(lo: Int, hi: Int) = lo + (nextFloat() * (hi - lo + 1)).toInt().coerceAtMost(hi - lo)
    fun pick(list: List<String>) = list[(nextFloat() * list.size).toInt().coerceAtMost(list.size - 1)]

    val log500 = Math.log(500.0)
    val list = (1..500).map { rank ->
        val t = 1f - (Math.log(rank.toDouble()) / log500).toFloat()
        fun tPow(exp: Float) = Math.pow(t.toDouble(), exp.toDouble()).toFloat()
        LeaderboardPlayer(
            rank = rank,
            name = pick(MOCK_FIRST) + pick(MOCK_LAST),
            country = pick(MOCK_COUNTRIES),
            totalPushUps = (180000 * tPow(1.6f) + 4500).toInt().coerceAtLeast(1),
            lvl   = (99 * tPow(0.85f) + nextInt(0, 4)).toInt().coerceIn(1, 99),
            res   = (42 * tPow(1.2f)  + nextInt(0, 2) - 1).toInt().coerceAtLeast(0),
            power = (9999 * tPow(1.1f) + 120).toInt().coerceAtLeast(50),
            armor = (7400 * tPow(1.15f) + 80).toInt().coerceAtLeast(20),
            hp    = (12000 * tPow(1.05f) + 220).toInt().coerceAtLeast(100),
            luck  = (640 * tPow(0.9f) + 12).toInt().coerceAtLeast(1),
            ageDays = (900 * tPow(0.6f) + nextInt(0, 30)).toInt().coerceAtLeast(1),
        )
    }.sortedByDescending { it.totalPushUps }
        .mapIndexed { i, p -> p.copy(rank = i + 1, isFriend = i in MOCK_FRIENDS) }

    return list
}

private val BIRTH_DATE_FMT = SimpleDateFormat("yyyy-MM-dd", Locale.US)

private fun buildMePlayer(gs: GameStateEntity?): LeaderboardPlayer {
    if (gs == null) {
        return LeaderboardPlayer(
            rank = MY_RANK, name = "YouHero", country = MY_COUNTRY, isMe = true,
        )
    }
    val ageDays = gs.characterBirthDate.takeIf { it.isNotBlank() }?.let { dateStr ->
        runCatching { BIRTH_DATE_FMT.parse(dateStr) }.getOrNull()
            ?.let { ((System.currentTimeMillis() - it.time) / 86_400_000L).toInt().coerceAtLeast(1) }
    } ?: 1
    return LeaderboardPlayer(
        rank = MY_RANK,
        name = gs.playerName,
        country = MY_COUNTRY,
        isMe = true,
        res = gs.prestigeLevel,
        lvl = gs.playerLevel,
        totalPushUps = gs.totalPushUpsAllTime,
        power = gs.basePower,
        armor = gs.baseArmor,
        hp = gs.baseHealth,
        luck = gs.baseLuck.toInt(),
        ageDays = ageDays,
    )
}
