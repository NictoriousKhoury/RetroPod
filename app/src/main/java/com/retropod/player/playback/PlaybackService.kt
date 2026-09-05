package com.retropod.player.playback

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.common.MediaMetadata
import androidx.media3.session.SessionToken
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.retropod.player.MainActivity
import com.retropod.player.data.db.PlaybackStateDao
import com.retropod.player.data.db.PlaybackStateEntity
import com.retropod.player.data.media.LibrarySync
import com.retropod.player.data.repo.LibraryRepository
import com.retropod.player.data.repo.PlaylistRepository
import com.retropod.player.playback.MediaItems
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val ROOT_ID = "root"
private const val CAT_SONGS = "cat_songs"
private const val CAT_ALBUMS = "cat_albums"
private const val CAT_ARTISTS = "cat_artists"
private const val CAT_PLAYLISTS = "cat_playlists"

@UnstableApi
@AndroidEntryPoint
class PlaybackService : MediaLibraryService() {

    @Inject lateinit var libraryRepository: LibraryRepository
    @Inject lateinit var playlistRepository: PlaylistRepository
    @Inject lateinit var playbackStateDao: PlaybackStateDao
    @Inject lateinit var librarySync: LibrarySync
    @Inject lateinit var equalizerController: EqualizerController

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var player: ExoPlayer
    private lateinit var session: MediaLibrarySession

    override fun onCreate() {
        super.onCreate()

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        val sessionActivity = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        session = MediaLibrarySession.Builder(this, player, LibraryCallback())
            .setSessionActivity(sessionActivity)
            .build()

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) = saveState()
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) = saveState()
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                equalizerController.attach(audioSessionId)
            }
        })
        equalizerController.attach(player.audioSessionId)

        serviceScope.launch {
            librarySync.sync(scanDisk = true)
            restoreState()
            libraryRepository.songs
                .map { it.isNotEmpty() }
                .distinctUntilChanged()
                .filter { it }
                .collect { restoreState() }
        }
    }

    private fun saveState() {
        val ids = (0 until player.mediaItemCount).map { player.getMediaItemAt(it).mediaId }
        val currentId = player.currentMediaItem?.mediaId?.toLongOrNull() ?: return
        val position = player.currentPosition
        serviceScope.launch {
            playbackStateDao.save(
                PlaybackStateEntity(songId = currentId, positionMs = position, queueCsv = ids.joinToString(","))
            )
        }
    }

    private suspend fun restoreState() {
        if (player.mediaItemCount > 0) return
        val saved = playbackStateDao.get() ?: return
        val ids = saved.queueCsv.split(",").mapNotNull { it.toLongOrNull() }
        val songs = libraryRepository.songsByIds(ids)
        if (songs.isEmpty()) return
        val startIndex = songs.indexOfFirst { it.id == saved.songId }.coerceAtLeast(0)
        player.setMediaItems(MediaItems.fromSongs(songs), startIndex, saved.positionMs)
        player.prepare() // prepared but paused - resumes where the user left off
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession = session

    override fun onTaskRemoved(rootIntent: Intent?) {
        saveState()
        if (::player.isInitialized) {
            player.playWhenReady = false
            player.pause()
            player.stop()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        saveState()
        equalizerController.release()
        if (::session.isInitialized) session.release()
        if (::player.isInitialized) player.release()
        serviceScope.cancel()
        super.onDestroy()
    }

    private inner class LibraryCallback : MediaLibrarySession.Callback {

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val root = browsable(ROOT_ID, "RetroPod")
            return Futures.immediateFuture(LibraryResult.ofItem(root, params))
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            val children: List<MediaItem> = when (parentId) {
                ROOT_ID -> listOf(
                    browsable(CAT_PLAYLISTS, "Playlists"),
                    browsable(CAT_ARTISTS, "Artists"),
                    browsable(CAT_ALBUMS, "Albums"),
                    browsable(CAT_SONGS, "Songs")
                )
                CAT_SONGS -> MediaItems.fromSongs(libraryRepository.songs.value.take(500))
                CAT_ALBUMS -> libraryRepository.albums.value.map {
                    browsable("album_${it.id}", it.title, it.artist, it.artUri)
                }
                CAT_ARTISTS -> libraryRepository.artists.value.map {
                    browsable("artist_${it.name}", it.name)
                }
                CAT_PLAYLISTS -> playlistRepository.imported.value.map {
                    browsable("playlist_${it.id}", it.name)
                }
                else -> resolveDynamicChildren(parentId)
            }
            return Futures.immediateFuture(LibraryResult.ofItemList(ImmutableList.copyOf(children), params))
        }

        private fun resolveDynamicChildren(parentId: String): List<MediaItem> = when {
            parentId.startsWith("album_") -> {
                val albumId = parentId.removePrefix("album_").toLongOrNull() ?: return emptyList()
                MediaItems.fromSongs(libraryRepository.songsForAlbum(albumId))
            }
            parentId.startsWith("artist_") ->
                MediaItems.fromSongs(libraryRepository.songsForArtist(parentId.removePrefix("artist_")))
            parentId.startsWith("playlist_") -> {
                val id = parentId.removePrefix("playlist_").toLongOrNull() ?: return emptyList()
                val ids = playlistRepository.importedById(id)?.songIds ?: emptyList()
                MediaItems.fromSongs(libraryRepository.songsByIds(ids))
            }
            else -> emptyList()
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val song = mediaId.toLongOrNull()?.let { libraryRepository.songById(it) }
                ?: return Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
            return Futures.immediateFuture(LibraryResult.ofItem(MediaItems.fromSong(song), null))
        }

        // Turn browse mediaIds picked from the tree into playable items.
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            val resolved = mediaItems.map { item ->
                item.mediaId.toLongOrNull()?.let { id ->
                    libraryRepository.songById(id)?.let { MediaItems.fromSong(it) }
                } ?: item
            }.toMutableList()
            return Futures.immediateFuture(resolved)
        }

        private fun browsable(
            id: String,
            title: String,
            subtitle: String? = null,
            artUri: android.net.Uri? = null
        ): MediaItem {
            val metadata = MediaMetadata.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setArtworkUri(artUri)
                .setIsBrowsable(true)
                .setIsPlayable(false)
                .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                .build()
            return MediaItem.Builder().setMediaId(id).setMediaMetadata(metadata).build()
        }
    }

    companion object {
        fun token(context: android.content.Context) =
            SessionToken(context, android.content.ComponentName(context, PlaybackService::class.java))
    }
}
