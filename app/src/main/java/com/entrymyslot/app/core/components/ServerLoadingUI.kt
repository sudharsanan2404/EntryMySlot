package com.entrymyslot.app.core.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PremiumOrange = Color(0xFFFA580B)
private val PremiumSurface = Color(0xFF0B274F)
private val PremiumText = Color(0xFFF8FAFF)
private val PremiumSecondaryText = Color(0xFFA8B8CF)
private val PremiumBorder = Color(0xFF24527D)

@Composable
fun PremiumLoadingState(
    modifier: Modifier = Modifier,
    message: String = "Getting things ready"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
            repeat(3) { index ->
                val dotAlpha by infiniteTransition.animateFloat(
                    initialValue = .24f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(520, delayMillis = index * 130),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "loading-$index"
                )
                Box(
                    Modifier
                        .size(width = 7.dp, height = (12 + index * 4).dp)
                        .clip(CircleShape)
                        .background(PremiumOrange.copy(alpha = dotAlpha))
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = message,
            color = PremiumSecondaryText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PremiumErrorState(
    modifier: Modifier = Modifier,
    title: String = "Content unavailable",
    message: String = "This screen could not be prepared right now.",
    onRetry: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.ErrorOutline, null, tint = Color(0xFFFF6B5F), modifier = Modifier.size(30.dp))
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            color = PremiumText,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            color = PremiumSecondaryText,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            "TRY AGAIN",
            color = PremiumOrange,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.clickable(onClick = onRetry).padding(horizontal = 18.dp, vertical = 10.dp)
        )
    }
}

@Composable
fun PremiumEmptyState(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.SearchOff,
    title: String = "No results found",
    message: String = "Try adjusting your search or filters.",
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = PremiumSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, PremiumBorder)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PremiumSecondaryText,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            color = PremiumText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = PremiumSecondaryText,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = PremiumOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(52.dp).fillMaxWidth(0.7f)
            ) {
                Text(actionText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
