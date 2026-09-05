package com.retropod.player.di

import android.content.Context
import androidx.room.Room
import com.retropod.player.data.db.AppDatabase
import com.retropod.player.data.db.PlaybackStateDao
import com.retropod.player.data.db.PlaylistDao
import com.retropod.player.data.db.StatsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "retropod.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun providePlaylistDao(db: AppDatabase): PlaylistDao = db.playlistDao()

    @Provides
    fun provideStatsDao(db: AppDatabase): StatsDao = db.statsDao()

    @Provides
    fun providePlaybackStateDao(db: AppDatabase): PlaybackStateDao = db.playbackStateDao()
}
