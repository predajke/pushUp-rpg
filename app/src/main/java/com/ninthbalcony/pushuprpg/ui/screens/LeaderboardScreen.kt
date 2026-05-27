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
import androidx.compose.foundation.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import com.ninthbalcony.pushuprpg.data.db.GameStateEntity
import com.ninthbalcony.pushuprpg.data.repository.LeaderboardEntry
import com.ninthbalcony.pushuprpg.ui.GameViewModel
import com.ninthbalcony.pushuprpg.ui.theme.*
import com.ninthbalcony.pushuprpg.utils.AppStrings
import com.ninthbalcony.pushuprpg.ui.components.clanTagColor
import com.ninthbalcony.pushuprpg.ui.util.rememberAvatarResId
import coil.compose.AsyncImage
import com.ninthbalcony.pushuprpg.utils.countryToFlag
import com.ninthbalcony.pushuprpg.R
import com.ninthbalcony.pushuprpg.ui.components.GameIcon
import java.text.SimpleDateFormat
import java.util.Locale

// Subtle 6%-white separator — единственный локальный токен, остальные — из темы
private val LbLine = Color(0x0FFFFFFF)

// Hoisted brushes (используют цвета темы)
private val BrushActiveTab = Brush.horizontalGradient(listOf(OrangeLight, OrangeAccent))
private val BrushStickyMe  = Brush.verticalGradient(listOf(OrangeAccent.copy(alpha = 0.05f), Color(0x66000000)))
private val BrushMeCard    = Brush.verticalGradient(listOf(OrangeAccent.copy(alpha = 0.13f), OrangeAccent.copy(alpha = 0.04f)))

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
    val clanTag: String = "",
    val clanTagColor: String = "default",
    val uid: String = "",
    val friendCode: String = "",
    val heroAvatar: String = "avatar_base",
    val gender: String = "male",
    val totalTeethEarned: Int = 0,
    val longestStreak: Int = 0,
    val lastUpdated: Long = 0L,
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
    period: LbPeriod,
    query: String,
): List<LeaderboardPlayer> {
    var list = players
    list = when (scope) {
        LbScope.COUNTRY -> list.filter { it.country == me.country || it.isMe }
        LbScope.FRIENDS -> list.filter { it.isFriend || it.isMe }
        LbScope.GLOBAL  -> list
    }
    if (period != LbPeriod.ALL) {
        val windowMs = when (period) {
            LbPeriod.DAY   -> 86_400_000L
            LbPeriod.WEEK  -> 7 * 86_400_000L
            LbPeriod.MONTH -> 30 * 86_400_000L
            LbPeriod.ALL   -> Long.MAX_VALUE
        }
        val cutoff = System.currentTimeMillis() - windowMs
        list = list.filter { it.isMe || it.lastUpdated == 0L || it.lastUpdated > cutoff }
    }
    if (query.isNotBlank()) list = list.filter { it.name.contains(query, ignoreCase = true) }
    return list
}

// ── Flag (country code → emoji flag) ─────────────────────────────────────────
// `countryToFlag` lives in utils/CountryUtils.kt — imported below.

// ── Avatar with rank-aware medal gradients ────────────────────────────────────

@Composable
private fun LbAvatar(
    name: String,
    rank: Int,
    size: Dp = 26.dp,
    borderColor: Color? = null,
    bgBrush: Brush? = null,
    avatarUrl: String? = null,
) {
    val initial = remember(name) { (name.firstOrNull() ?: '?').uppercaseChar().toString() }
    val derivedBorder = when (rank) {
        1    -> Color(0x66F3C969)
        2    -> Color(0x59CFD2D6)
        3    -> Color(0x66CD8C4A)
        else -> Color(0x332A2428)
    }
    val derivedBg: Brush = when (rank) {
        1    -> Brush.radialGradient(listOf(Color(0xFF6C4D18), DarkCard))
        2    -> Brush.radialGradient(listOf(Color(0xFF4A4D54), DarkCard))
        3    -> Brush.radialGradient(listOf(Color(0xFF5A3A1C), DarkCard))
        else -> Brush.radialGradient(listOf(DarkSurfaceVariant, DarkCard))
    }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bgBrush ?: derivedBg)
            .border(1.dp, borderColor ?: derivedBorder, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = name,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = initial,
                color = TextPrimary,
                fontSize = (size.value * 0.40f).sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ── Column header (5 fixed cols, no scroll) ───────────────────────────────────

@Composable
private fun LbColumnHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "#",
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.14.sp,
            textAlign = TextAlign.Start,
            modifier = Modifier.width(22.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "NAME",
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.14.sp,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "LVL",
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.14.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.width(44.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "PUSH",
            color = OrangeAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.14.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.width(38.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "PWR",
            color = UncommonColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.14.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.width(50.dp),
        )
    }
}

// ── Player row (5 fixed cols, no scroll) ─────────────────────────────────────

@Composable
private fun LbPlayerRow(p: LeaderboardPlayer, avatarUrl: String? = null, onClick: (() -> Unit)? = null) {
    val rankColor = when (p.rank) {
        1    -> GoldAccent
        2    -> PodiumSilver
        3    -> PodiumBronze
        else -> TextMuted
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(start = 8.dp, end = 14.dp, top = 9.dp, bottom = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = p.rank.toString(),
            color = rankColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Start,
            modifier = Modifier.width(22.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = countryToFlag(p.country),
            fontSize = 14.sp,
        )
        Spacer(Modifier.width(5.dp))
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LbAvatar(name = p.name, rank = p.rank, avatarUrl = if (p.isMe) avatarUrl else null)
            Spacer(Modifier.width(6.dp))
            if (p.clanTag.isNotBlank()) {
                Text(
                    text = "[${p.clanTag}]",
                    color = com.ninthbalcony.pushuprpg.ui.components.clanTagColor(p.clanTagColor),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = p.name,
                color = if (p.isMe) Color.White else TextPrimary,
                fontSize = 13.sp,
                fontWeight = if (p.isMe) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = p.lvl.toString(),
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(44.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = fmt(p.totalPushUps),
            color = OrangeLight,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(38.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = fmt(p.power),
            color = HealthColor,
            fontSize = 12.sp,
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
            .border(1.dp, if (selected) OrangeAccent.copy(alpha = 0.6f) else LbLine, shape)
            .clickable(onClick = onClick)
            .padding(vertical = verticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) DarkBackground else TextSecondary,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ── Main screen ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
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
    var selectedPlayer by remember { mutableStateOf<LeaderboardPlayer?>(null) }

    // Live data from RTDB (populated by VM via LeaderboardRepository).
    val liveEntries by viewModel.leaderboardEntries.collectAsState()
    val friends     by viewModel.friendsList.collectAsState()
    val isLoading   by viewModel.leaderboardLoading.collectAsState()
    val isOnline    by viewModel.isOnline.collectAsState()

    // Trigger leaderboard fetch on first composition. Friends are kept live by
    // the real-time friendsFlow subscribed in GameViewModel.init.
    LaunchedEffect(Unit) { viewModel.refreshLeaderboard() }

    val friendUids = remember(friends) { friends.map { it.uid }.toSet() }
    val myAvatarUrl by viewModel.playerIconUri.collectAsState()
    val me = remember(gameState) { buildMePlayer(gameState) }
    // Merge top-100 + friends (friends might be outside top-100), dedup, sort by pushUps,
    // assign rank, mark isMe / isFriend.
    val players = remember(liveEntries, friends, me, friendUids) {
        val merged = (liveEntries + friends).distinctBy { it.uid }
        mapEntriesToPlayers(merged, me, friendUids)
    }
    val filtered = remember(players, me, scope, period, query) { applyFilters(players, me, scope, period, query) }

    val scopes = remember(language) {
        listOf(
            LbScope.GLOBAL  to AppStrings.t(language, "lb_global"),
            LbScope.COUNTRY to AppStrings.t(language, "lb_country"),
            LbScope.FRIENDS to AppStrings.t(language, "lb_friends"),
        )
    }
    val periods = remember(language) {
        listOf(
            LbPeriod.DAY   to AppStrings.t(language, "lb_period_day"),
            LbPeriod.WEEK  to AppStrings.t(language, "lb_period_week"),
            LbPeriod.MONTH to AppStrings.t(language, "lb_period_month"),
            LbPeriod.ALL   to AppStrings.t(language, "lb_period_all"),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        ScreenBackground("bg_mainmenu_overall")
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ───────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 10.dp),
            ) {
                Text(
                    text = "‹",
                    color = TextSecondary,
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
                    Text("✦", color = GoldAccent.copy(alpha = 0.85f), fontSize = 12.sp)
                    Text(
                        text = "LEADERBOARD",
                        color = GoldAccent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.22.sp,
                    )
                    Text("✦", color = GoldAccent.copy(alpha = 0.85f), fontSize = 12.sp)
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(LbLine))

            // ── Offline banner ───────────────────────────────────────────────
            if (!isOnline) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x33FF453A))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = if (language == "ru")
                            "⚠ Нет подключения — данные могут быть устаревшими"
                        else
                            "⚠ Offline — data may be stale",
                        color = ButtonRedLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

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
                GameIcon(R.drawable.icon_search, size = 12.dp)
                Spacer(Modifier.width(6.dp))
                val placeholder = AppStrings.t(language, "lb_filter_name")
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    cursorBrush = SolidColor(OrangeAccent),
                    textStyle = TextStyle(color = TextPrimary, fontSize = 12.sp),
                    decorationBox = { inner ->
                        if (query.isEmpty()) Text(placeholder, color = TextMuted, fontSize = 12.sp)
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
            PullToRefreshBox(
                modifier = Modifier.weight(1f),
                isRefreshing = isLoading,
                onRefresh = { viewModel.refreshLeaderboard(force = true) },
            ) {
                if (!isLoading && filtered.isEmpty()) {
                    val emptyMsg = AppStrings.t(language, "lb_empty")
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(emptyMsg, color = TextMuted, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 140.dp),
                    ) {
                        itemsIndexed(filtered, key = { _, p -> "${p.rank}-${p.name}" }) { _, p ->
                            LbPlayerRow(
                                p = p,
                                avatarUrl = if (p.isMe) myAvatarUrl else null,
                                onClick = if (!p.isMe) { { selectedPlayer = p } } else null,
                            )
                        }
                    }
                }

                StickyMeRow(
                    me = me,
                    avatarUrl = myAvatarUrl,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding(),
                )
            }
        }

        // Mini-profile dialog (other players)
        selectedPlayer?.let { p ->
            PlayerProfileDialog(
                player = p,
                isAlreadyFriend = viewModel.isFriend(p.uid),
                language = language,
                onAddFriend = {
                    viewModel.addFriendByUid(p.uid)
                    selectedPlayer = null
                },
                onDismiss = { selectedPlayer = null },
            )
        }
    }
}

// ── Sticky "Your Standing" card ───────────────────────────────────────────────

@Composable
private fun StickyMeRow(
    me: LeaderboardPlayer,
    avatarUrl: String? = null,
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
            color = OrangeAccent,
            fontSize = 10.sp,
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
                color = OrangeLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                modifier = Modifier.width(34.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = countryToFlag(me.country),
                fontSize = 14.sp,
            )
            Spacer(Modifier.width(5.dp))
            LbAvatar(
                name = me.name,
                rank = me.rank,
                borderColor = Color(0x8CFF8A2A),
                bgBrush = Brush.radialGradient(listOf(Color(0xFF4A2A10), Color(0xFF1A1108))),
                avatarUrl = avatarUrl,
            )
            Spacer(Modifier.width(6.dp))
            if (me.clanTag.isNotBlank()) {
                Text(
                    text = "[${me.clanTag}]",
                    color = com.ninthbalcony.pushuprpg.ui.components.clanTagColor(me.clanTagColor),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Spacer(Modifier.width(4.dp))
            }
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
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                modifier = Modifier.width(44.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = fmt(me.totalPushUps),
                color = OrangeLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                modifier = Modifier.width(38.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = fmt(me.power),
                color = HealthColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                modifier = Modifier.width(50.dp),
            )
        }
    }
}

// ── Mock data generator (replace with real API / Firebase call) ───────────────

private const val MY_RANK = 1                // overwritten by mapEntriesToPlayers
private const val MY_COUNTRY = "XX"  // pirate flag until player picks a country

/**
 * Convert RTDB entries to the legacy `LeaderboardPlayer` shape used by the
 * rendering code. Strategy:
 *  - Sort by totalPushUps desc
 *  - If the local user appears in the list (matches by name+country) we
 *    upgrade that row with full local stats; otherwise we splice `me` in at
 *    the top so they always see their rank
 *  - Stats not stored on the server (power/armor/hp/luck/ageDays) default to 0
 */
private fun mapEntriesToPlayers(
    entries: List<LeaderboardEntry>,
    me: LeaderboardPlayer,
    friendUids: Set<String> = emptySet(),
): List<LeaderboardPlayer> {
    val sorted = entries.sortedByDescending { it.totalPushUps }
    val mapped = sorted.mapIndexed { idx, e ->
        val isMe = e.name.equals(me.name, ignoreCase = true) && e.country == me.country
        LeaderboardPlayer(
            rank = idx + 1,
            name = e.name,
            country = e.country.ifBlank { MY_COUNTRY },
            isFriend = e.uid in friendUids,
            isMe = isMe,
            res = e.prestige,
            lvl = e.level,
            totalPushUps = e.totalPushUps,
            power = if (isMe) me.power else e.power,
            armor = if (isMe) me.armor else 0,
            hp    = if (isMe) me.hp    else 0,
            luck  = if (isMe) me.luck  else 0,
            ageDays = if (isMe) me.ageDays else 1,
            clanTag = e.clanTag,
            clanTagColor = e.clanTagColor,
            uid = e.uid,
            friendCode = e.friendCode,
            heroAvatar = e.heroAvatar,
            gender = e.gender,
            totalTeethEarned = e.totalTeethEarned,
            longestStreak = e.longestStreak,
            lastUpdated = e.lastUpdated,
        )
    }
    // If we didn't see ourselves in the snapshot yet (sign-in just finished, no
    // push has propagated), splice in synthetic "me" so the sticky row + filters
    // still work.
    return if (mapped.any { it.isMe }) mapped
    else (mapped + me.copy(rank = mapped.size + 1)).sortedByDescending { it.totalPushUps }
        .mapIndexed { i, p -> p.copy(rank = i + 1) }
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
        country = gs.playerCountry.ifBlank { MY_COUNTRY },
        isMe = true,
        res = gs.prestigeLevel,
        lvl = gs.playerLevel,
        totalPushUps = gs.totalPushUpsAllTime,
        power = gs.basePower,
        armor = gs.baseArmor,
        hp = gs.baseHealth,
        luck = gs.baseLuck.toInt(),
        ageDays = ageDays,
        clanTag = gs.clanTag,
        clanTagColor = gs.clanTagColor,
        heroAvatar = gs.heroAvatar,
        gender = gs.playerGender,
        totalTeethEarned = gs.totalTeethEarned,
        longestStreak = gs.longestStreak,
    )
}

// ── Mini player profile dialog ────────────────────────────────────────────────

@Composable
private fun PlayerProfileDialog(
    player: LeaderboardPlayer,
    isAlreadyFriend: Boolean,
    language: String,
    onAddFriend: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val clipboard = remember {
        context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
    }
    val avatarResId = rememberAvatarResId(player.heroAvatar, player.gender)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkCard, RoundedCornerShape(16.dp))
                .border(1.dp, Color(0x33FF8A2A), RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (avatarResId != 0) {
                    Image(
                        painter = painterResource(id = avatarResId),
                        contentDescription = player.name,
                        modifier = Modifier.fillMaxSize().padding(6.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Flag + clan + name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = countryToFlag(player.country), fontSize = 18.sp)
                Spacer(Modifier.width(6.dp))
                if (player.clanTag.isNotBlank()) {
                    Text(
                        text = "[${player.clanTag}]",
                        color = clanTagColor(player.clanTagColor),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = player.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(2.dp))

            // Level / prestige
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${AppStrings.t(language, "stat_level")} ${player.lvl}",
                    color = OrangeAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (player.res > 0) {
                    Spacer(Modifier.width(8.dp))
                    Text(text = "·", color = TextMuted, fontSize = 13.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${AppStrings.t(language, "lb_prestige")} ${player.res}",
                        color = Color(0xFF3FA9F5),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Stats block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x14FFFFFF), RoundedCornerShape(10.dp))
                    .padding(12.dp),
            ) {
                StatLine(
                    label = AppStrings.t(language, "lb_push_ups_header"),
                    value = fmt(player.totalPushUps),
                    valueColor = OrangeLight,
                )
                Spacer(Modifier.height(6.dp))
                StatLine(
                    label = AppStrings.t(language, "lb_streak_header"),
                    value = "${player.longestStreak}d",
                    valueColor = HealthColor,
                )
                Spacer(Modifier.height(6.dp))
                StatLine(
                    label = AppStrings.t(language, "lb_teeth_header"),
                    value = fmt(player.totalTeethEarned),
                    valueColor = GoldAccent,
                )
            }

            Spacer(Modifier.height(14.dp))

            // Friend code + copy
            if (player.friendCode.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = AppStrings.t(language, "lb_code"),
                        color = TextMuted,
                        fontSize = 12.sp,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = player.friendCode,
                        color = OrangeAccent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(OrangeAccent.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = {
                        clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("friend code", player.friendCode))
                        android.widget.Toast.makeText(
                            context,
                            AppStrings.t(language, "copied"),
                            android.widget.Toast.LENGTH_SHORT,
                        ).show()
                    }) {
                        Text(AppStrings.t(language, "btn_copy"), color = OrangeAccent, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        AppStrings.t(language, "close"),
                        color = TextMuted,
                    )
                }
                if (isAlreadyFriend) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UncommonColor.copy(alpha = 0.18f)),
                        enabled = false,
                    ) {
                        Text(
                            text = AppStrings.t(language, "lb_already_friend"),
                            color = UncommonColor,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                } else {
                    Button(
                        onClick = onAddFriend,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    ) {
                        Text(
                            text = AppStrings.t(language, "lb_add_friend"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.14.sp,
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
