package com.retropod.player.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.retropod.player.playback.RepeatMode
import com.retropod.player.ui.components.Scrubber
import com.retropod.player.ui.components.TransportControls
import com.retropod.player.ui.theme.Accent
import com.retropod.player.ui.theme.Textures
import com.retropod.player.ui.viewmodel.PlayerViewModel

@Composable
fun NowPlayingScreen(
    playerViewModel: PlayerViewModel,
    onBack: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenArtist: (String) -> Unit = {},
    onOpenAlbum: (Long) -> Unit = {},
    onOpenEq: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val nowPlaying by playerViewModel.nowPlaying.collectAsStateWithLifecycle()
    val isPlaying by playerViewModel.isPlaying.collectAsStateWithLifecycle()
    val position by playerViewModel.positionMs.collectAsStateWithLifecycle()
    val repeat by playerViewModel.repeat.collectAsStateWithLifecycle()
    var dragY by remember { mutableFloatStateOf(0f) }
    val haptics = LocalHapticFeedback.current
    LockPortraitWhileVisible()

    Column(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(0, dragY.toInt().coerceAtLeast(0)) }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dy ->
                        change.consume()
                        dragY = (dragY + dy).coerceAtLeast(0f)
                    },
                    onDragEnd = {
                        if (dragY > 120f) onBack()
                        dragY = 0f
                    },
                    onDragCancel = { dragY = 0f }
                )
            }
            .background(Textures.nowPlaying)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
        // top bar
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().height(48.dp).padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.KeyboardArrowDown, "Close", tint = Color.White)
            }
            Spacer(Modifier.weight(1f))
            Text(
                nowPlaying?.album ?: "",
                color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = nowPlaying != null) {
                        val id = nowPlaying?.albumId ?: 0L
                        if (id != 0L) onOpenAlbum(id)
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onOpenQueue) {
                Icon(Icons.AutoMirrored.Filled.QueueMusic, "Queue", tint = Color.White)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Album art + reflection
        Box(
            Modifier.fillMaxWidth().weight(1f).padding(horizontal = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = nowPlaying?.artworkUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .scale(1.35f)
                    .blur(48.dp)
                    .alpha(0.55f)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = nowPlaying?.artworkUri,
                    contentDescription = nowPlaying?.album ?: "Album art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .shadow(28.dp, RoundedCornerShape(28.dp), clip = false)
                        .clip(RoundedCornerShape(28.dp))
                        .border(1.5.dp, Color(0x66E8EEF6), RoundedCornerShape(28.dp))
                        .background(Color(0xFF17181C))
                        .pointerInput(nowPlaying?.albumId) {
                            detectTapGestures(
                                onDoubleTap = {
                                    val id = nowPlaying?.albumId ?: 0L
                                    if (id != 0L) {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onOpenAlbum(id)
                                    }
                                }
                            )
                        }
                )
                // reflection
                Box(Modifier.fillMaxWidth().height(60.dp)) {
                    AsyncImage(
                        model = nowPlaying?.artworkUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .scale(scaleX = 1f, scaleY = -1f)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                listOf(Color(0xCC0C0D10), Color(0xFF0C0D10))
                            )
                        )
                    )
                }
            }
        }

        // Title / artist
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                nowPlaying?.title ?: "Not Playing",
                color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 22.sp,
                letterSpacing = 0.3.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center
            )
            Text(
                nowPlaying?.artist ?: "",
                color = Accent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = !nowPlaying?.artist.isNullOrBlank()) {
                        nowPlaying?.artist?.let(onOpenArtist)
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Spacer(Modifier.height(12.dp))
        } // end swipe-to-dismiss region

        Scrubber(
            positionMs = position,
            durationMs = nowPlaying?.durationMs ?: 1L,
            onSeek = { playerViewModel.seekTo(it) }
        )

        Spacer(Modifier.height(8.dp))

        TransportControls(
            isPlaying = isPlaying,
            onPrevious = { playerViewModel.previous() },
            onPlayPause = { playerViewModel.togglePlayPause() },
            onNext = { playerViewModel.next() }
        )

        // eq / repeat
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 64.dp, vertical = 12.dp).navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToggleIcon(Icons.Filled.Equalizer, "Equalizer", active = false, onClick = onOpenEq)
            ToggleIcon(
                if (repeat == RepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                "Repeat",
                active = repeat != RepeatMode.OFF
            ) { playerViewModel.cycleRepeat() }
        }
    }
}

@Composable
private fun LockPortraitWhileVisible() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context as? android.app.Activity
        val previous = activity?.requestedOrientation
            ?: android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation =
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose { activity?.requestedOrientation = previous }
    }
}

@Composable
private fun ToggleIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).clickable { onClick() },
        contentAlignment = Alignment.Center) {
        Icon(icon, label, tint = if (active) Accent else Color(0xFFB8BCC6),
            modifier = Modifier.size(26.dp))
    }
}
