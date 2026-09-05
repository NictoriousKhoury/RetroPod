package com.retropod.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.border
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.retropod.player.ui.theme.Textures

@Composable
fun TransportControls(
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious, modifier = Modifier.size(64.dp)) {
            Icon(Icons.Filled.SkipPrevious, "Previous", tint = tint, modifier = Modifier.size(40.dp))
        }
        IconButton(onClick = onPlayPause, modifier = Modifier.size(80.dp)) {
            Box(
                Modifier
                    .size(72.dp)
                    .shadow(16.dp, CircleShape, clip = false)
                    .clip(CircleShape)
                    .background(Textures.blueButton)
                    .border(1.5.dp, Color(0x99FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        IconButton(onClick = onNext, modifier = Modifier.size(64.dp)) {
            Icon(Icons.Filled.SkipNext, "Next", tint = tint, modifier = Modifier.size(40.dp))
        }
    }
}
