package com.retropod.player.data.media

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.retropod.player.data.model.Album
import com.retropod.player.data.model.Artist
import com.retropod.player.data.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private val ALBUM_ART_BASE: Uri = Uri.parse("content://media/external/audio/albumart")

@Singleton
class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun scanSongs(): List<Song> = withContext(Dispatchers.IO) {
        val songs = ArrayList<Song>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        context.contentResolver.query(collection, projection, selection, null, sortOrder)?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val trackCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val yearCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val dataCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val nameCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)

            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val data = c.getString(dataCol) ?: ""
                val displayName = c.getString(nameCol) ?: File(data).name
                val taggedTitle = c.getString(titleCol) ?: displayName
                val taggedArtist = c.getString(artistCol) ?: ""
                val taggedAlbum = c.getString(albumCol) ?: ""
                val (title, artist, album) = FilenameMetadata.parse(
                    displayName, taggedTitle, taggedArtist, taggedAlbum
                )
                val albumId = if (album.equals(artist, true)) {
                    0x100000000L + (artist.lowercase().hashCode().toLong() and 0xFFFFFFFFL)
                } else {
                    c.getLong(albumIdCol)
                }
                songs += Song(
                    id = id,
                    title = title,
                    artist = artist,
                    album = album,
                    albumId = albumId,
                    durationMs = c.getLong(durCol),
                    trackNumber = c.getInt(trackCol) % 1000,
                    year = c.getInt(yearCol),
                    data = data,
                    fileName = displayName,
                    contentUri = ContentUris.withAppendedId(collection, id),
                    albumArtUri = ContentUris.withAppendedId(ALBUM_ART_BASE, c.getLong(albumIdCol))
                )
            }
        }
        songs
    }

    fun deriveAlbums(songs: List<Song>): List<Album> =
        songs.groupBy { it.albumId }
            .map { (albumId, tracks) ->
                val first = tracks.first()
                Album(
                    id = albumId,
                    title = first.album,
                    artist = tracks.map { it.artist }.groupingBy { it }.eachCount()
                        .maxByOrNull { it.value }?.key ?: first.artist,
                    artUri = first.albumArtUri,
                    songCount = tracks.size,
                    year = tracks.maxOf { it.year }
                )
            }
            .sortedBy { it.title.lowercase() }

    fun deriveArtists(songs: List<Song>): List<Artist> =
        songs.groupBy { it.artist }
            .map { (name, tracks) ->
                Artist(
                    name = name,
                    albumCount = tracks.map { it.albumId }.distinct().size,
                    songCount = tracks.size
                )
            }
            .sortedBy { it.name.lowercase() }
}
