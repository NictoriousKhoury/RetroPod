package com.retropod.player.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** iOS-style time scrubber: elapsed / remaining labels flanking a thin track. */
@Composable
fun Scrubber(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = Color(0xFF4C9BFF),
    labelColor: Color = Color(0xFFB8BCC6)
) {
    var dragValue by remember { mutableStateOf<Float?>(null) }
    val duration = durationMs.coerceAtLeast(1)
    val fraction = (dragValue ?: (positionMs.toFloat() / duration)).coerceIn(0f, 1f)

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(formatTime((fraction * duration).toLong()), color = labelColor, fontSize = 12.sp)
        Box(Modifier.weight(1f).padding(horizontal = 8.dp)) {
            Slider(
                value = fraction,
                onValueChange = { dragValue = it },
                onValueChangeFinished = {
                    dragValue?.let { onSeek((it * duration).toLong()) }
                    dragValue = null
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = accent,
                    inactiveTrackColor = Color(0x33FFFFFF)
                )
            )
        }
        Text("-" + formatTime(duration - (fraction * duration).toLong()), color = labelColor, fontSize = 12.sp)
    }
}

fun formatTime(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
