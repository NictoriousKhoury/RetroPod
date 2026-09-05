package com.retropod.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.retropod.player.ui.theme.Textures
import com.retropod.player.data.model.Song
import com.retropod.player.ui.components.IosNavBar
import com.retropod.player.ui.components.ListRow
import com.retropod.player.ui.components.SwipeSongRow
import com.retropod.player.ui.theme.TableBackground
import com.retropod.player.ui.viewmodel.PlayerViewModel

@Composable
fun TrackListScreen(
    title: String,
    songs: List<Song>,
    onBack: () -> Unit,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
    onOpenNowPlaying: () -> Unit = {},
    navAction: (@Composable () -> Unit)? = null,
    onRemoveSong: ((Song) -> Unit)? = null
) {
    Column(modifier.fillMaxSize().background(TableBackground)) {
        IosNavBar(title = title, onBack = onBack, action = navAction)
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PillButton("Play", Icons.Filled.PlayArrow, Modifier.weight(1f)) {
                        if (songs.isNotEmpty()) { playerViewModel.playFrom(songs, 0); onOpenNowPlaying() }
                    }
                    PillButton("Shuffle", Icons.Filled.Shuffle, Modifier.weight(1f)) {
                        if (songs.isNotEmpty()) {
                            playerViewModel.playFrom(songs.shuffled(), 0); onOpenNowPlaying()
                        }
                    }
                    PillButton("Queue", Icons.Filled.QueueMusic, Modifier.weight(1f), accent = false) {
                        if (songs.isNotEmpty()) playerViewModel.addToQueue(songs)
                    }
                }
            }
            itemsIndexed(songs, key = { _, s -> s.id }) { index, song ->
                SwipeSongRow(
                    onAddToQueue = { playerViewModel.addToQueue(song) },
                    onRemove = onRemoveSong?.let { remove -> { remove(song) } }
                ) {
                    ListRow(
                        title = song.title,
                        subtitle = song.artist,
                        artworkUri = song.albumArtUri,
                        onClick = { playerViewModel.playFrom(songs, index); onOpenNowPlaying() }
                    )
                }
            }
        }
    }
}

@Composable
private fun PillButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier,
    accent: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(if (accent) Textures.blueButton else Textures.metalPanel)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.height(18.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}
