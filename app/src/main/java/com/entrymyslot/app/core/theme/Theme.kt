package com.entrymyslot.app.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EntryColorScheme = lightColorScheme(
    primary = EntryPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE2D5),
    onPrimaryContainer = Color(0xFF391100),
    secondary = EntryNavy,
    onSecondary = Color.White,
    background = EntrySurface,
    onBackground = EntryOnSurface,
    surface = EntrySurface,
    onSurface = EntryOnSurface,
    outline = Color(0xFFD0D5DD)
)

@Composable
fun EntryMySlotTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = EntryColorScheme, content = content)
}
