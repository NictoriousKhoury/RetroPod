package com.retropod.player.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retropod.player.data.db.PlaylistEntity
import com.retropod.player.data.repo.LibraryRepository
import com.retropod.player.data.repo.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    val importedPlaylists = playlistRepository.imported

    val userPlaylists: StateFlow<List<PlaylistEntity>> =
        playlistRepository.observeUserPlaylists()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createPlaylist(name: String, onCreated: (Long) -> Unit = {}) = viewModelScope.launch {
        onCreated(playlistRepository.createPlaylist(name))
    }

    fun renamePlaylist(id: Long, name: String) = viewModelScope.launch {
        playlistRepository.renamePlaylist(id, name)
    }

    fun deletePlaylist(id: Long) = viewModelScope.launch { playlistRepository.deletePlaylist(id) }

    fun addToPlaylist(id: Long, songIds: List<Long>) = viewModelScope.launch {
        playlistRepository.addToPlaylist(id, songIds)
    }

    fun removeFromPlaylist(id: Long, songId: Long) = viewModelScope.launch {
        playlistRepository.removeFromPlaylist(id, songId)
    }

    fun setPlaylistSongs(id: Long, songIds: List<Long>) = viewModelScope.launch {
        playlistRepository.setPlaylistSongs(id, songIds)
    }

    suspend fun songsForImported(id: Long) =
        libraryRepository.songsByIds(playlistRepository.importedById(id)?.songIds ?: emptyList())

    suspend fun songsForUser(id: Long) =
        libraryRepository.songsByIds(playlistRepository.userPlaylistSongIds(id))

    fun userPlaylistSongIdsFlow(id: Long): Flow<List<Long>> =
        playlistRepository.observeUserPlaylistSongIds(id)
}
