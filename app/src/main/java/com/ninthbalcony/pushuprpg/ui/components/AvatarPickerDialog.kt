package com.ninthbalcony.pushuprpg.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ninthbalcony.pushuprpg.ui.theme.*
import com.ninthbalcony.pushuprpg.ui.util.rememberAvatarResId
import com.ninthbalcony.pushuprpg.utils.AppStrings
import com.ninthbalcony.pushuprpg.utils.AvatarSystem

/**
 * 4×2 grid avatar picker. Uses [AvatarSystem.AVATARS] + [rememberAvatarResId] so
 * unlock conditions, gender suffixes, and locked states stay consistent with
 * what was already in SettingsScreen.
 */
@Composable
fun AvatarPickerDialog(
    currentAvatar: String,
    currentGender: String,
    unlockedIds: Set<String>,
    language: String,
    onPick: (avatarId: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val bgResId = remember { context.resources.getIdentifier("bg_fight_3", "drawable", context.packageName) }
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
        ) {
            if (bgResId != 0) Image(
                painter = painterResource(id = bgResId),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.25f
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
            Text(
                text = AppStrings.t(language, "choose_avatar"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(12.dp))

            AvatarSystem.AVATARS.chunked(4).forEach { rowAvatars ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    rowAvatars.forEach { def ->
                        val isUnlocked = def.id in unlockedIds
                        val isSelected = currentAvatar == def.id
                        val resId = rememberAvatarResId(def.id, currentGender)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(
                                    when {
                                        isSelected -> OrangeAccent.copy(alpha = 0.2f)
                                        isUnlocked -> DarkSurfaceVariant
                                        else       -> DarkSurfaceVariant.copy(alpha = 0.5f)
                                    },
                                    RoundedCornerShape(10.dp)
                                )
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) OrangeAccent else if (isUnlocked) TextMuted else TextMuted.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .then(
                                    if (isUnlocked) Modifier.clickable { onPick(def.id) }
                                    else Modifier
                                )
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isUnlocked) {
                                if (resId != 0) {
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = def.id,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("🔒", fontSize = 18.sp)
                                    val cond = if (language == "ru") def.conditionRu else def.conditionEn
                                    Text(
                                        text = cond,
                                        fontSize = 9.sp,
                                        color = TextMuted,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 10.sp,
                                    )
                                }
                            }
                        }
                    }
                    repeat(4 - rowAvatars.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(AppStrings.t(language, "close"), color = TextSecondary)
            }
        }
        }
    }
}
