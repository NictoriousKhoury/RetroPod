package com.retropod.player.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Textures {

    val navBar = Brush.verticalGradient(listOf(Color(0xF2263248), Color(0xF2161E2E)))

    val tabBar = Brush.verticalGradient(listOf(Color(0xF2263248), Color(0xF2161E2E)))

    val nowPlaying = Brush.verticalGradient(
        listOf(Color(0xFF2C3A52), Color(0xFF151C28), Color(0xFF0C1018))
    )

    val linen = Brush.verticalGradient(listOf(Color(0xFF1E2838), Color(0xFF141C28)))

    val glassPanel = Brush.verticalGradient(
        listOf(Color(0x33FFFFFF), Color(0x14FFFFFF))
    )

    val glossyButton = Brush.verticalGradient(
        listOf(Color(0xFFFCFCFC), Color(0xFFE2E2E6), Color(0xFFC9C9CF))
    )

    val glossyButtonPressed = Brush.verticalGradient(
        listOf(Color(0xFFB9C4DA), Color(0xFF97A6C4))
    )

    val blueButton = Brush.verticalGradient(listOf(AccentSoft, Accent, IosBlueDark))
}

fun Modifier.linenBackground() = this.background(Textures.linen)

fun Modifier.glassCard(corner: Dp = 18.dp, elevation: Dp = 8.dp) = this
    .shadow(elevation, RoundedCornerShape(corner), clip = false)
    .clip(RoundedCornerShape(corner))
    .background(RowBackground)
    .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(corner))
