package com.retropod.player.data.playlist

import android.os.Environment
import com.retropod.player.data.model.Playlist
import com.retropod.player.data.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Imports the curated genre/decade/vibe playlists exported by _export_m3u.ps1.
 * Entries are matched to scanned [Song]s by file name (basename), so the exact
 * on-device folder does not matter.
 */
@Singleton
class M3uPlaylistImporter @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private fun candidateDirs(): List<File> {
        val music = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        return listOf(
            File(music, "RetroPod/Playlists-M3U"),
            File(music, "RetroPod"),
            File(Environment.getExternalStorageDirectory(), "Music/RetroPod/Playlists-M3U"),
            File(Environment.getExternalStorageDirectory(), "RetroPod/Playlists-M3U")
        )
    }

    suspend fun importAll(songs: List<Song>): List<Playlist> = withContext(Dispatchers.IO) {
        val byName = LinkedHashMap<String, Song>()
        songs.forEach { song ->
            byName.putIfAbsent(song.fileName.lowercase(), song)
            byName.putIfAbsent(matchKey(song.fileName), song)
        }
        val dir = candidateDirs().firstOrNull { it.isDirectory } ?: return@withContext emptyList()
        val files = dir.listFiles { f -> f.extension.equals("m3u8", true) || f.extension.equals("m3u", true) }
            ?.sortedBy { it.name.lowercase() }
            ?: return@withContext emptyList()

        files.mapIndexed { index, file ->
            val ids = ArrayList<Long>()
            file.readLines(Charsets.UTF_8).forEach { raw ->
                val line = raw.trim()
                if (line.isEmpty() || line.startsWith("#")) return@forEach
                val base = File(line.replace('\\', '/')).name
                val song = byName[base.lowercase()] ?: byName[matchKey(base)]
                song?.let { ids += it.id }
            }
            Playlist(
                id = -(index.toLong() + 1),          // negative ids = imported (read-only)
                name = file.nameWithoutExtension,
                isUserPlaylist = false,
                songIds = ids
            )
        }.filter { it.songIds.isNotEmpty() }
    }

    /** Matches playlist lines to files even if SPOTISAVER / copy suffixes differ. */
    private fun matchKey(fileName: String): String =
        fileName.lowercase()
            .substringAfterLast('/')
            .substringAfterLast('\\')
            .replace(Regex("\\.(mp3|m4a|aac|flac|ogg|wav|opus)$"), "")
            .replace("(spotisaver)", "")
            .replace("spotisaver", "")
            .replace("spotidownloader.com", "")
            .replace(Regex("""\s*\(\d+\)\s*$"""), "")
            .replace(Regex("\\s+"), " ")
            .trim(' ', '-', '.', '_')
}
