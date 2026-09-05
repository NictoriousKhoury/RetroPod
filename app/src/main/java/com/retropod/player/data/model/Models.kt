package com.retropod.player.data.model

import android.net.Uri

/** A single audio track discovered on the device via MediaStore. */
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val trackNumber: Int,
    val year: Int,
    val data: String,          // absolute file path
    val fileName: String,      // basename, used to match .m3u8 entries
    val contentUri: Uri,
    val albumArtUri: Uri
)

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val artUri: Uri,
    val songCount: Int,
    val year: Int
)

data class Artist(
    val name: String,
    val albumCount: Int,
    val songCount: Int
)

/** Domain playlist (either an imported .m3u8 or a user-created one in Room). */
data class Playlist(
    val id: Long,
    val name: String,
    val isUserPlaylist: Boolean,
    val songIds: List<Long>,
    val artUri: Uri? = null
)
