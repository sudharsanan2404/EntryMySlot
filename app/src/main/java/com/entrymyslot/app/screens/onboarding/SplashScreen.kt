package com.entrymyslot.app.screens.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.entrymyslot.app.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    var visible by remember {
        mutableStateOf(false)
    }

    // ---------------------------------------------------------
    // ENTRY SCALE
    // ---------------------------------------------------------
    val entryScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.92f,
        animationSpec = tween(
            durationMillis = 700,
            easing = LinearOutSlowInEasing
        ),
        label = "entry-scale"
    )

    // ---------------------------------------------------------
    // FADE
    // ---------------------------------------------------------
    val logoAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = LinearOutSlowInEasing
        ),
        label = "logo-alpha"
    )

    val pulse = rememberInfiniteTransition(label = "splash-logo-pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo-zoom"
    )

    // ---------------------------------------------------------
    // SPLASH TIMER
    // ---------------------------------------------------------
    LaunchedEffect(Unit) {
        visible = true

        delay(2200)

        onFinished()
    }

    // ---------------------------------------------------------
    // SCREEN
    // ---------------------------------------------------------
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A35A5),
                        Color(0xFF071F5A),
                        Color(0xFF041329)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // -----------------------------------------------------
        // LOGO CONTAINER
        // -----------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = painterResource(
                    id = R.drawable.entrymyslotlogopcg
                ),
                contentDescription = "EntryMySlot",

                modifier = Modifier
                    .width(240.dp)
                    .alpha(logoAlpha)
                    .graphicsLayer {

                        scaleX = entryScale * pulseScale
                        scaleY = entryScale * pulseScale

                        // Explicitly disable clipping
                        clip = false
                    },

                contentScale = ContentScale.Fit
            )
        }
    }
}
