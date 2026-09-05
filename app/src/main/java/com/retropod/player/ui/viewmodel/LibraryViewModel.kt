package com.retropod.player.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retropod.player.data.media.LibrarySync
import com.retropod.player.data.model.Playlist
import com.retropod.player.data.model.Song
import com.retropod.player.data.repo.LibraryRepository
import com.retropod.player.data.repo.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val playlistRepository: PlaylistRepository,
    private val librarySync: LibrarySync
) : ViewModel() {

    val songs = libraryRepository.songs
    val albums = libraryRepository.albums
    val artists = libraryRepository.artists
    val loading = libraryRepository.loading
    val importedPlaylists = playlistRepository.imported

    private val _hasScanned = MutableStateFlow(false)
    val hasScanned: StateFlow<Boolean> = _hasScanned.asStateFlow()

    fun onPermissionGranted() {
        librarySync.start()
        viewModelScope.launch {
            librarySync.sync(scanDisk = true)
            _hasScanned.value = true
        }
    }

    fun songsForAlbum(albumId: Long): List<Song> = libraryRepository.songsForAlbum(albumId)
    fun songsForArtist(name: String): List<Song> = libraryRepository.songsForArtist(name)
    fun albumsForArtist(name: String) = libraryRepository.albumsForArtist(name)
    fun songsByIds(ids: List<Long>): List<Song> = libraryRepository.songsByIds(ids)
    fun importedById(id: Long): Playlist? = playlistRepository.importedById(id)
}
