package com.retropod.player.data.repo

import com.retropod.player.data.db.PlaylistDao
import com.retropod.player.data.db.PlaylistEntity
import com.retropod.player.data.model.Playlist
import com.retropod.player.data.playlist.M3uPlaylistImporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val importer: M3uPlaylistImporter,
    private val playlistDao: PlaylistDao,
    private val libraryRepository: LibraryRepository
) {
    private val _imported = MutableStateFlow<List<Playlist>>(emptyList())
    val imported: StateFlow<List<Playlist>> = _imported.asStateFlow()

    suspend fun refreshImported() {
        _imported.value = importer.importAll(libraryRepository.songs.value)
    }

    fun observeUserPlaylists() = playlistDao.observePlaylists()
    fun observeUserPlaylistSongIds(id: Long) = playlistDao.observeSongIds(id)

    suspend fun createPlaylist(name: String): Long =
        playlistDao.insertPlaylist(PlaylistEntity(name = name))

    suspend fun renamePlaylist(id: Long, name: String) = playlistDao.renamePlaylist(id, name)
    suspend fun deletePlaylist(id: Long) = playlistDao.deletePlaylist(id)

    /** Drop user-playlist rows whose MediaStore ids no longer exist. */
    suspend fun pruneMissingSongs(validIds: Collection<Long>) {
        if (validIds.isEmpty()) return
        playlistDao.deleteSongsNotIn(validIds.toList())
    }
    suspend fun setPlaylistSongs(id: Long, songIds: List<Long>) = playlistDao.setSongs(id, songIds)
    suspend fun addToPlaylist(id: Long, songIds: List<Long>) = playlistDao.appendSongs(id, songIds)
    suspend fun userPlaylistSongIds(id: Long): List<Long> = playlistDao.getSongIds(id)

    fun importedById(id: Long): Playlist? = _imported.value.firstOrNull { it.id == id }
}
