package com.retropod.player.data.repo

import com.retropod.player.data.media.MediaStoreScanner
import com.retropod.player.data.model.Album
import com.retropod.player.data.model.Artist
import com.retropod.player.data.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepository @Inject constructor(
    private val scanner: MediaStoreScanner
) {
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val refreshMutex = Mutex()
    private var byId: Map<Long, Song> = emptyMap()

    suspend fun refresh() = refreshMutex.withLock {
        _loading.value = true
        try {
            val songs = scanner.scanSongs()
            byId = songs.associateBy { it.id }
            _songs.value = songs
            _albums.value = scanner.deriveAlbums(songs)
            _artists.value = scanner.deriveArtists(songs)
        } catch (_: SecurityException) {
            // Permission revoked mid-scan — keep the last good library.
        } finally {
            _loading.value = false
        }
    }

    fun songById(id: Long): Song? = byId[id]
    fun songsByIds(ids: List<Long>): List<Song> = ids.mapNotNull { byId[it] }
    fun songsForArtist(name: String): List<Song> = _songs.value.filter { it.artist == name }
    fun songsForAlbum(albumId: Long): List<Song> =
        _songs.value.filter { it.albumId == albumId }.sortedBy { it.trackNumber }
}
