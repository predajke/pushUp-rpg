package com.ninthbalcony.pushuprpg.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninthbalcony.pushuprpg.R

/**
 * Reusable icon component backed by drawable PNGs.
 * Replaces emoji Text() across the app for consistent visual style.
 */
@Composable
fun GameIcon(
    @DrawableRes resId: Int,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    contentDescription: String? = null,
    tint: ColorFilter? = null,
) {
    Image(
        painter = painterResource(resId),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        colorFilter = tint,
    )
}

/** Mapping helpers for equipment slot → icon. */
fun equipSlotIcon(slot: String): Int = when (slot) {
    "weapon"   -> R.drawable.icon_power
    "head"     -> R.drawable.icon_helm
    "necklace" -> R.drawable.icon_neck
    "pants"    -> R.drawable.icon_pants
    "boots"    -> R.drawable.icon_boots
    else       -> R.drawable.icon_question
}
