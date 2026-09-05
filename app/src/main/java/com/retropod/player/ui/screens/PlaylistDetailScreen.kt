package com.retropod.player.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.retropod.player.data.model.Song
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
    onOpenNowPlaying: () -> Unit = {}
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

    TrackListScreen(
        title = title,
        songs = songs,
        onBack = onBack,
        playerViewModel = playerViewModel,
        modifier = modifier,
        onOpenNowPlaying = onOpenNowPlaying
    )
}
