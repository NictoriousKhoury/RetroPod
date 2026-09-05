package com.retropod.player.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY name COLLATE NOCASE ASC")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylist(id: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :name WHERE id = :id")
    suspend fun renamePlaylist(id: Long, name: String)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylistRow(id: Long)

    @Transaction
    suspend fun deletePlaylist(id: Long) {
        clearSongs(id)
        deletePlaylistRow(id)
    }

    @Query("DELETE FROM playlist_songs WHERE songId NOT IN (:validIds)")
    suspend fun deleteSongsNotIn(validIds: List<Long>)

    @Query("SELECT songId FROM playlist_songs WHERE playlistId = :playlistId ORDER BY position ASC")
    fun observeSongIds(playlistId: Long): Flow<List<Long>>

    @Query("SELECT songId FROM playlist_songs WHERE playlistId = :playlistId ORDER BY position ASC")
    suspend fun getSongIds(playlistId: Long): List<Long>

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun clearSongs(playlistId: Long)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun deleteSongCrossRef(playlistId: Long, songId: Long)

    @Transaction
    suspend fun removeSong(playlistId: Long, songId: Long) {
        deleteSongCrossRef(playlistId, songId)
        val remaining = getSongIds(playlistId)
        clearSongs(playlistId)
        insertSongs(remaining.mapIndexed { i, sid -> PlaylistSongEntity(playlistId, sid, i) })
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(entries: List<PlaylistSongEntity>)

    @Query("SELECT COALESCE(MAX(position), -1) FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun maxPosition(playlistId: Long): Int

    @Transaction
    suspend fun setSongs(playlistId: Long, songIds: List<Long>) {
        clearSongs(playlistId)
        insertSongs(songIds.mapIndexed { i, sid -> PlaylistSongEntity(playlistId, sid, i) })
    }

    @Transaction
    suspend fun appendSongs(playlistId: Long, songIds: List<Long>) {
        val existing = getSongIds(playlistId).toSet()
        val toAdd = songIds.filter { it !in existing }
        if (toAdd.isEmpty()) return
        var pos = maxPosition(playlistId) + 1
        insertSongs(toAdd.map { PlaylistSongEntity(playlistId, it, pos++) })
    }
}

@Dao
interface StatsDao {

    @Query("SELECT * FROM song_stats WHERE songId = :songId")
    suspend fun get(songId: Long): SongStatsEntity?

    @Query("SELECT * FROM song_stats WHERE favorite = 1")
    fun observeFavorites(): Flow<List<SongStatsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stats: SongStatsEntity)

    @Transaction
    suspend fun incrementPlayCount(songId: Long) {
        val cur = get(songId) ?: SongStatsEntity(songId)
        upsert(cur.copy(playCount = cur.playCount + 1, lastPlayed = System.currentTimeMillis()))
    }

    @Transaction
    suspend fun toggleFavorite(songId: Long): Boolean {
        val cur = get(songId) ?: SongStatsEntity(songId)
        val next = !cur.favorite
        upsert(cur.copy(favorite = next))
        return next
    }
}

@Dao
interface PlaybackStateDao {

    @Query("SELECT * FROM playback_state WHERE id = 0")
    suspend fun get(): PlaybackStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(state: PlaybackStateEntity)
}
