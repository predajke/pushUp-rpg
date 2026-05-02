package com.ninthbalcony.pushuprpg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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

// ── Design tokens (Variant A) ─────────────────────────────────────────────────

private val LbBg       = Color(0xFF100F12)
private val LbPanel    = Color(0xFF1C1A1E)
private val LbLine     = Color(0x0FFFFFFF)   // rgba(255,255,255,0.06)
private val LbText     = Color(0xFFECE6D8)
private val LbTextDim  = Color(0xFF9B9389)
private val LbTextMute = Color(0xFF6E675F)
private val LbOrange   = Color(0xFFFF8A2A)
private val LbOrange2  = Color(0xFFFFB152)
private val LbGreen    = Color(0xFF34C759)
private val LbGreen2   = Color(0xFF5FDF7A)
private val LbGold     = Color(0xFFF3C969)
private val LbSilver   = Color(0xFFCFD2D6)
private val LbBronze   = Color(0xFFCD8C4A)

// Hoisted brushes
private val BrushBg        = Brush.verticalGradient(listOf(Color(0xFF131115), Color(0xFF0C0B0E)))
private val BrushActiveTab = Brush.horizontalGradient(listOf(LbOrange2, LbOrange))
private val BrushStickyMe  = Brush.verticalGradient(listOf(Color(0x0DFF8A2A), Color(0x66000000)))
private val BrushMeCard    = Brush.verticalGradient(listOf(Color(0x21FF8A2A), Color(0x0AFF8A2A)))

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

// ── Formatters ────────────────────────────────────────────────────────────────

private fun fmt(n: Int): String = when {
    n >= 1_000_000 -> "%.1fM".format(n / 1_000_000.0).trimEnd('0').trimEnd('.') + "M"
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

// ── Filtering ─────────────────────────────────────────────────────────────────

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

// ── Flag (country code → emoji flag) ─────────────────────────────────────────

private fun countryToFlag(code: String): String {
    if (code.length != 2) return "🏳"
    val offset = 0x1F1E6 - 'A'.code
    return String(intArrayOf(code[0].uppercaseChar().code + offset, code[1].uppercaseChar().code + offset), 0, 2)
}

// ── Avatar with rank-aware medal gradients ────────────────────────────────────

@Composable
private fun LbAvatar(
    name: String,
    rank: Int,
    size: Dp = 26.dp,
    borderColor: Color? = null,
    bgBrush: Brush? = null,
) {
    val initial = remember(name) { (name.firstOrNull() ?: '?').uppercaseChar().toString() }
    val derivedBorder = when (rank) {
        1    -> Color(0x66F3C969)
        2    -> Color(0x59CFD2D6)
        3    -> Color(0x66CD8C4A)
        else -> Color(0x332A2428)
    }
    val derivedBg: Brush = when (rank) {
        1    -> Brush.radialGradient(listOf(Color(0xFF6C4D18), Color(0xFF1A1719)))
        2    -> Brush.radialGradient(listOf(Color(0xFF4A4D54), Color(0xFF1A1719)))
        3    -> Brush.radialGradient(listOf(Color(0xFF5A3A1C), Color(0xFF1A1719)))
        else -> Brush.radialGradient(listOf(Color(0xFF2A2428), Color(0xFF1A1719)))
    }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bgBrush ?: derivedBg)
            .border(1.dp, borderColor ?: derivedBorder, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            color = LbText,
            fontSize = (size.value * 0.40f).sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ── Column header (5 fixed cols, no scroll) ───────────────────────────────────

@Composable
private fun LbColumnHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "#",
            color = LbTextMute,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(36.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "NAME",
            color = LbTextMute,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.14.sp,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "LVL",
            color = LbTextMute,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.14.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.width(44.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "PUSH",
            color = LbOrange,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.14.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.width(38.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "PWR",
            color = LbGreen,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.14.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.width(50.dp),
        )
    }
}

// ── Player row (5 fixed cols, no scroll) ─────────────────────────────────────

@Composable
private fun LbPlayerRow(p: LeaderboardPlayer) {
    val rankColor = when (p.rank) {
        1    -> LbGold
        2    -> LbSilver
        3    -> LbBronze
        else -> LbTextMute
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = p.rank.toString(),
            color = rankColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(36.dp),
        )
        Spacer(Modifier.width(6.dp))
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LbAvatar(name = p.name, rank = p.rank)
            Spacer(Modifier.width(7.dp))
            Text(
                text = p.name,
                color = if (p.isMe) Color.White else LbText,
                fontSize = 13.sp,
                fontWeight = if (p.isMe) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = p.lvl.toString(),
            color = LbTextDim,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(44.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = fmt(p.totalPushUps),
            color = LbOrange2,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(38.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = fmt(p.power),
            color = LbGreen2,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(50.dp),
        )
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0x09FFFFFF)))
}

// ── Unified tab pill (scope + time tabs) ─────────────────────────────────────

@Composable
private fun TabPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    verticalPadding: Dp,
    cornerRadius: Dp,
    fontSize: Float,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (selected) Modifier.background(BrushActiveTab)
                else Modifier.background(Color(0x08FFFFFF), shape)
            )
            .border(1.dp, if (selected) Color(0x99FF8A2A) else LbLine, shape)
            .clickable(onClick = onClick)
            .padding(vertical = verticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) Color(0xFF15110A) else LbTextDim,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.SemiBold,
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

    var scope  by remember { mutableStateOf(LbScope.GLOBAL) }
    var period by remember { mutableStateOf(LbPeriod.ALL) }
    var query  by remember { mutableStateOf("") }

    val mockRoster = remember { generateMockRoster() }
    val me = remember(gameState) { buildMePlayer(gameState) }
    val players = remember(mockRoster, me) {
        mockRoster.toMutableList().also { it[me.rank - 1] = me }
    }
    val filtered = remember(players, scope, query) { applyFilters(players, me, scope, query) }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrushBg)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ───────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 10.dp),
            ) {
                Text(
                    text = "‹",
                    color = LbTextDim,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable(onClick = onBack),
                )
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("✦", color = LbGold.copy(alpha = 0.85f), fontSize = 12.sp)
                    Text(
                        text = "LEADERBOARD",
                        color = LbGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.22.sp,
                    )
                    Text("✦", color = LbGold.copy(alpha = 0.85f), fontSize = 12.sp)
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(LbLine))

            // ── Scope tabs ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                scopes.forEach { (s, label) ->
                    TabPill(
                        label = label,
                        selected = scope == s,
                        onClick = { scope = s },
                        verticalPadding = 9.dp,
                        cornerRadius = 10.dp,
                        fontSize = 12f,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // ── Time tabs ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                periods.forEach { (p, label) ->
                    TabPill(
                        label = label,
                        selected = period == p,
                        onClick = { period = p },
                        verticalPadding = 6.dp,
                        cornerRadius = 8.dp,
                        fontSize = 11f,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // ── Search bar ───────────────────────────────────────────────────
            val searchShape = RoundedCornerShape(10.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 10.dp)
                    .background(Color(0x0AFFFFFF), searchShape)
                    .border(1.dp, LbLine, searchShape)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("🔍", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                Spacer(Modifier.width(6.dp))
                val placeholder = if (language == "ru") "Фильтр по имени…" else "Filter by name…"
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    cursorBrush = SolidColor(LbOrange),
                    textStyle = TextStyle(color = LbText, fontSize = 12.sp),
                    decorationBox = { inner ->
                        if (query.isEmpty()) Text(placeholder, color = LbTextMute, fontSize = 12.sp)
                        inner()
                    }
                )
            }

            // ── Column header with top+bottom dividers ────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x04FFFFFF))
            ) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(LbLine).align(Alignment.TopCenter))
                LbColumnHeader()
                Box(Modifier.fillMaxWidth().height(1.dp).background(LbLine).align(Alignment.BottomCenter))
            }

            // ── Scrollable list + sticky "Your Standing" ──────────────────────
            Box(modifier = Modifier.weight(1f)) {
                if (filtered.isEmpty()) {
                    val emptyMsg = if (language == "ru") "Чемпионов не найдено." else "No champions match."
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(emptyMsg, color = LbTextMute, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 90.dp),
                    ) {
                        itemsIndexed(filtered, key = { _, p -> "${p.rank}-${p.name}" }) { _, p ->
                            LbPlayerRow(p = p)
                        }
                    }
                }

                StickyMeRow(
                    me = me,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}

// ── Sticky "Your Standing" card ───────────────────────────────────────────────

@Composable
private fun StickyMeRow(
    me: LeaderboardPlayer,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(10.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BrushStickyMe)
            .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 14.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(LbLine))
        Spacer(Modifier.height(10.dp))
        Text(
            text = "YOUR STANDING",
            color = LbOrange,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.18.sp,
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(BrushMeCard)
                .border(1.dp, Color(0x66FF8A2A), cardShape)
                .padding(horizontal = 8.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "#${me.rank}",
                color = LbOrange2,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(36.dp),
            )
            Spacer(Modifier.width(6.dp))
            LbAvatar(
                name = me.name,
                rank = me.rank,
                borderColor = Color(0x8CFF8A2A),
                bgBrush = Brush.radialGradient(listOf(Color(0xFF4A2A10), Color(0xFF1A1108))),
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = me.name,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = me.lvl.toString(),
                color = LbTextDim,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                modifier = Modifier.width(44.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = fmt(me.totalPushUps),
                color = LbOrange2,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                modifier = Modifier.width(38.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = fmt(me.power),
                color = LbGreen2,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                modifier = Modifier.width(50.dp),
            )
        }
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
