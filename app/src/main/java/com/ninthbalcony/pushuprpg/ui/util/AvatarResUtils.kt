package com.ninthbalcony.pushuprpg.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.ninthbalcony.pushuprpg.utils.AvatarSystem

/**
 * Resolves the drawable resId for the player's selected avatar, taking gender into account.
 * Falls back to legacy `hero_1` / `hero_2` if the gendered drawable is missing.
 *
 * Cached per (avatarId, gender) — `getIdentifier` is reflective and slow.
 */
@Composable
fun rememberAvatarResId(avatarId: String, gender: String): Int {
    val context = LocalContext.current
    return remember(avatarId, gender) {
        val name = AvatarSystem.drawableId(avatarId, gender)
        val direct = context.resources.getIdentifier(name, "drawable", context.packageName)
        if (direct != 0) direct else context.resources.getIdentifier(
            if (gender == "female") "hero_2" else "hero_1",
            "drawable", context.packageName
        )
    }
}
