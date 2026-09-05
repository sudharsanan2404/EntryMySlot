package com.entrymyslot.app.screens.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.entrymyslot.app.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "logo-pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val entryScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.7f,
        animationSpec = tween(800, easing = LinearOutSlowInEasing),
        label = "entry-scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f, 
        animationSpec = tween(700), 
        label = "logo-alpha"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(2200)
        onFinished()
    }

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF0A35A5), Color(0xFF071F5A), Color(0xFF041329)))
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alpha)
                .scale(entryScale * pulseScale)
        ) {
            Image(
                painterResource(R.drawable.entrymyslotlogopcg),
                "EntryMySlot",
                modifier = Modifier.size(240.dp)
            )
        }
    }
}
