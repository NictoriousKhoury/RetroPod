package com.retropod.player.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistSongEntity::class,
        SongStatsEntity::class,
        PlaybackStateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun statsDao(): StatsDao
    abstract fun playbackStateDao(): PlaybackStateDao
}
