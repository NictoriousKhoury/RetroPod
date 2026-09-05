package com.retropod.player.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "position"],
    indices = [Index("playlistId")]
)
data class PlaylistSongEntity(
    val playlistId: Long,
    val songId: Long,
    val position: Int
)

/** Per-song stats: play count, favourite flag, last played. */
@Entity(tableName = "song_stats")
data class SongStatsEntity(
    @PrimaryKey val songId: Long,
    val playCount: Int = 0,
    val favorite: Boolean = false,
    val lastPlayed: Long = 0
)

/** Single-row snapshot so playback resumes where the user left off. */
@Entity(tableName = "playback_state")
data class PlaybackStateEntity(
    @PrimaryKey val id: Int = 0,
    val songId: Long,
    val positionMs: Long,
    val queueCsv: String
)
