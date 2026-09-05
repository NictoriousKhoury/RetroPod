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
        val files = candidateDirs()
            .filter { it.isDirectory }
            .flatMap { dir ->
                dir.listFiles { f ->
                    f.isFile && (f.extension.equals("m3u8", true) || f.extension.equals("m3u", true))
                }?.toList().orEmpty()
            }
            .distinctBy { it.nameWithoutExtension.lowercase() }
            .sortedBy { it.name.lowercase() }
        if (files.isEmpty()) return@withContext emptyList()

        files.map { file ->
            val seen = LinkedHashSet<Long>()
            val lines = try {
                file.readLines(Charsets.UTF_8)
            } catch (_: Exception) {
                emptyList()
            }
            lines.forEach { raw ->
                val line = raw.trim().trimStart('\uFEFF')
                if (line.isEmpty() || line.startsWith("#")) return@forEach
                val decoded = try {
                    android.net.Uri.decode(line)
                } catch (_: Exception) {
                    line
                }
                val base = File(decoded.replace('\\', '/')).name
                val song = byName[base.lowercase()] ?: byName[matchKey(base)]
                if (song != null) seen += song.id
            }
            Playlist(
                id = importedId(file.nameWithoutExtension),
                name = file.nameWithoutExtension,
                isUserPlaylist = false,
                songIds = seen.toList()
            )
        }.filter { it.songIds.isNotEmpty() }
    }

    /** Stable negative id so adding/removing a .m3u8 file does not shift others. */
    private fun importedId(name: String): Long {
        val h = name.lowercase().hashCode().toLong() and 0xFFFFFFFFL
        return -(h + 1)
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
