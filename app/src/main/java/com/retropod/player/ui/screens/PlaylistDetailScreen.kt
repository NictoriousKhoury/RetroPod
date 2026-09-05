package com.retropod.player.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.retropod.player.data.model.Song
import com.retropod.player.ui.theme.IosRed
import com.retropod.player.ui.viewmodel.PlayerViewModel
import com.retropod.player.ui.viewmodel.PlaylistViewModel
import kotlinx.coroutines.flow.flowOf

@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    imported: Boolean,
    playlistViewModel: PlaylistViewModel,
    playerViewModel: PlayerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenNowPlaying: () -> Unit = {},
    onAddSongs: (() -> Unit)? = null
) {
    val importedPlaylists by playlistViewModel.importedPlaylists.collectAsStateWithLifecycle()
    val userPlaylists by playlistViewModel.userPlaylists.collectAsStateWithLifecycle()
    val userSongIds by remember(playlistId, imported) {
        if (imported) flowOf(emptyList())
        else playlistViewModel.userPlaylistSongIdsFlow(playlistId)
    }.collectAsStateWithLifecycle(emptyList())

    val title = if (imported) {
        importedPlaylists.firstOrNull { it.id == playlistId }?.name ?: "Playlist"
    } else {
        userPlaylists.firstOrNull { it.id == playlistId }?.name ?: "Playlist"
    }

    var songs by remember(playlistId, imported) { mutableStateOf<List<Song>>(emptyList()) }
    LaunchedEffect(playlistId, imported, importedPlaylists, userSongIds) {
        songs = if (imported) playlistViewModel.songsForImported(playlistId)
        else playlistViewModel.songsForUser(playlistId)
    }

    var confirmDelete by remember { mutableStateOf(false) }
    val editable = !imported

    TrackListScreen(
        title = title,
        songs = songs,
        onBack = onBack,
        playerViewModel = playerViewModel,
        modifier = modifier,
        onOpenNowPlaying = onOpenNowPlaying,
        navAction = if (editable) {
            {
                IconButton(onClick = { onAddSongs?.invoke() }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add songs", tint = Color.White)
                }
                IconButton(onClick = { confirmDelete = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete playlist", tint = Color.White)
                }
            }
        } else null,
        onRemoveSong = if (editable) {
            { song -> playlistViewModel.removeFromPlaylist(playlistId, song.id) }
        } else null
    )

    if (confirmDelete && editable) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete playlist?") },
            text = { Text("“$title” and its song list will be removed. The audio files stay on the device.") },
            confirmButton = {
                TextButton(onClick = {
                    playlistViewModel.deletePlaylist(playlistId)
                    confirmDelete = false
                    onBack()
                }) { Text("Delete", color = IosRed) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }
}
