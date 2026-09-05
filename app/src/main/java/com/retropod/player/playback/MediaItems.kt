package com.retropod.player.playback

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.retropod.player.data.model.Song

object MediaItems {

    const val EXTRA_ALBUM_ID = "album_id"

    fun fromSong(song: Song): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(song.albumArtUri)
            .setIsPlayable(true)
            .setIsBrowsable(false)
            .setDurationMs(song.durationMs)
            .setExtras(android.os.Bundle().apply { putLong(EXTRA_ALBUM_ID, song.albumId) })
            .build()

        return MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.contentUri)
            .setMediaMetadata(metadata)
            .build()
    }

    fun fromSongs(songs: List<Song>): List<MediaItem> = songs.map { fromSong(it) }
}
