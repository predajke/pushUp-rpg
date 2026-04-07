package com.pushupRPG.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pushupRPG.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "splash_alpha"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(2000)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha)
        ) {
            val context = LocalContext.current
            val logoResId = remember {
                context.resources.getIdentifier("logo", "drawable", context.packageName)
            }

            if (logoResId != 0) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = logoResId),
                    contentDescription = "Logo",
                    modifier = Modifier.size(220.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(text = "💪", fontSize = 80.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Push UP RPG",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = OrangeAccent
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Train your body. Level up your hero.",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
}