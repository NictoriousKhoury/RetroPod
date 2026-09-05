package com.retropod.player.playback

import android.net.Uri

/** Snapshot of what is currently playing, surfaced to the UI as Flow. */
data class NowPlaying(
    val mediaId: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long = 0L,
    val artworkUri: Uri?,
    val durationMs: Long
)

data class QueueItem(
    val mediaId: String,
    val title: String,
    val artist: String,
    val artworkUri: Uri?,
    val indexInQueue: Int
)

enum class RepeatMode { OFF, ALL, ONE }
