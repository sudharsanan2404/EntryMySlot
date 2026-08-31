package com.entrymyslot.app.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val EntryCardAccent: Color = Color(red = 0xFA, green = 0x58, blue = 0x0B)
val EntryCardSubtitle: Color = Color(red = 0x9A, green = 0xA3, blue = 0xC7)

private val ElevatedCardShape = RoundedCornerShape(14.dp)
private val ElevatedCardGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF1D2550), Color(0xFF171E42))
)

@Composable
fun ElevatedContrastCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = ElevatedCardShape,
                ambientColor = Color.Black.copy(alpha = .35f),
                spotColor = Color.Black.copy(alpha = .35f)
            )
            .clip(ElevatedCardShape)
            .background(ElevatedCardGradient)
            .border(1.dp, Color.White.copy(alpha = .06f), ElevatedCardShape)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun ElevatedCardTitle(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 1
) {
    Text(
        text = text,
        modifier = modifier,
        color = Color.White,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun ElevatedCardSubtitle(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 1
) {
    Text(
        text = text,
        modifier = modifier,
        color = EntryCardSubtitle,
        fontSize = 12.sp,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}
