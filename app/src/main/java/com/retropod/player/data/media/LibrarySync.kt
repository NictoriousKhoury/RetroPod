package com.retropod.player.data.media

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.retropod.player.data.repo.LibraryRepository
import com.retropod.player.data.repo.PlaylistRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Keeps the in-app library in sync with files on the device:
 * indexes new audio Android hasn't scanned yet, re-queries MediaStore,
 * then re-imports .m3u8 playlists so new tracks show in Songs and playlists.
 */
@Singleton
class LibrarySync @Inject constructor(
    @ApplicationContext private val context: Context,
    private val libraryRepository: LibraryRepository,
    private val playlistRepository: PlaylistRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutex = Mutex()
    private var debounceJob: Job? = null
    private var observer: ContentObserver? = null
    @Volatile private var applying = false
    @Volatile private var lastDiskScanAt = 0L

    fun start() {
        if (observer != null) return
        val obs = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) = onChange(selfChange, null)
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                if (!applying) requestSync(scanDisk = false)
            }
        }
        context.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            obs
        )
        observer = obs
    }

    fun requestSync(scanDisk: Boolean = true) {
        if (!hasAudioPermission()) return
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(if (scanDisk) 250 else 700)
            sync(scanDisk)
        }
    }

    suspend fun sync(scanDisk: Boolean = true) {
        if (!hasAudioPermission()) return
        mutex.withLock {
            applying = true
            try {
                val now = System.currentTimeMillis()
                if (scanDisk && now - lastDiskScanAt > 8_000L) {
                    indexUntrackedFiles()
                    lastDiskScanAt = System.currentTimeMillis()
                }
                libraryRepository.refresh()
                playlistRepository.refreshImported()
            } finally {
                delay(400)
                applying = false
            }
        }
    }

    private fun hasAudioPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    private suspend fun indexUntrackedFiles() = withContext(Dispatchers.IO) {
        val roots = listOfNotNull(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            File(Environment.getExternalStorageDirectory(), "Music")
        ).distinctBy { it.absolutePath.lowercase() }.filter { it.isDirectory }

        val onDisk = ArrayList<File>()
        for (root in roots) {
            root.walkTopDown().maxDepth(8).forEach { file ->
                if (file.isFile && file.extension.lowercase() in AUDIO_EXT) onDisk += file
            }
        }
        if (onDisk.isEmpty()) return@withContext

        val indexed = indexedPaths()
        val missing = onDisk.map { it.absolutePath }.filter { it.lowercase() !in indexed }
        if (missing.isEmpty()) return@withContext
        missing.chunked(40).forEach { chunk -> scanPaths(chunk) }
    }

    private fun indexedPaths(): Set<String> {
        val out = HashSet<String>()
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media.DATA),
            null,
            null,
            null
        )?.use { c ->
            val col = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            while (c.moveToNext()) {
                c.getString(col)?.let { out += it.lowercase() }
            }
        }
        return out
    }

    private suspend fun scanPaths(paths: List<String>) {
        withTimeoutOrNull(25_000) {
            suspendCancellableCoroutine { cont ->
                val left = AtomicInteger(paths.size)
                fun finish() {
                    if (left.decrementAndGet() <= 0 && cont.isActive) cont.resume(Unit)
                }
                MediaScannerConnection.scanFile(
                    context,
                    paths.toTypedArray(),
                    Array(paths.size) { "audio/*" },
                ) { _, _ -> finish() }
            }
        }
    }

    companion object {
        private val AUDIO_EXT = setOf("mp3", "m4a", "aac", "flac", "ogg", "wav", "opus")
    }
}
