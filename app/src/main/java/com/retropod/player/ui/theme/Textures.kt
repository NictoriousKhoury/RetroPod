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

    val navBar = Brush.verticalGradient(
        listOf(Color(0xF24A5568), Color(0xF2262C38), Color(0xF214181F))
    )

    val tabBar = Brush.verticalGradient(
        listOf(Color(0xF24A5568), Color(0xF2262C38), Color(0xF214181F))
    )

    val nowPlaying = Brush.verticalGradient(
        listOf(Color(0xFF3A4456), Color(0xFF1A202A), Color(0xFF0A0C10))
    )

    val linen = Brush.verticalGradient(listOf(Color(0xFF1A202A), Color(0xFF0C0F14)))

    val glassPanel = Brush.verticalGradient(
        listOf(Color(0x40FFFFFF), Color(0x10FFFFFF))
    )

    val metalPanel = Brush.verticalGradient(
        listOf(Color(0xFF2C3340), Color(0xFF161B24))
    )

    val glossyButton = Brush.verticalGradient(
        listOf(Color(0xFFF5F7FA), Color(0xFFC5CDD8), Color(0xFF8E98A8))
    )

    val glossyButtonPressed = Brush.verticalGradient(
        listOf(Color(0xFF9BB8C8), Color(0xFF5E7A8C))
    )

    val blueButton = Brush.verticalGradient(
        listOf(Color(0xFFC6F0FF), Color(0xFF7ED4FF), Color(0xFF2E8FB8))
    )

    val chromeRing = Brush.verticalGradient(
        listOf(Color(0xCCFFFFFF), Color(0x33FFFFFF), Color(0x66C5CDD8))
    )
}

fun Modifier.linenBackground() = this.background(Textures.linen)

fun Modifier.glassCard(corner: Dp = 18.dp, elevation: Dp = 10.dp) = this
    .shadow(elevation, RoundedCornerShape(corner), clip = false)
    .clip(RoundedCornerShape(corner))
    .background(Textures.metalPanel)
    .border(1.dp, GlassStroke, RoundedCornerShape(corner))
