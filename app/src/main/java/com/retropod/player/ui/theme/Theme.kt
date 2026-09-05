package com.retropod.player.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RetroColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    secondary = Champagne,
    background = TableBackground,
    onBackground = PrimaryText,
    surface = RowBackground,
    onSurface = PrimaryText,
    error = IosRed
)

@Composable
fun RetroPodTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RetroColorScheme,
        typography = RetroTypography,
        content = content
    )
}
