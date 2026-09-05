package com.retropod.player.playback

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.retropod.player.data.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper around a [MediaController] bound to [PlaybackService].
 * Exposes player state as Flows and forwards queue edits to Media3's timeline.
 */
@Singleton
class PlaybackConnection @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private val pending = ArrayDeque<(MediaController) -> Unit>()

    private val _nowPlaying = MutableStateFlow<NowPlaying?>(null)
    val nowPlaying: StateFlow<NowPlaying?> = _nowPlaying.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    private val _bufferedMs = MutableStateFlow(0L)
    val bufferedMs: StateFlow<Long> = _bufferedMs.asStateFlow()

    private val _queue = MutableStateFlow<List<QueueItem>>(emptyList())
    val queue: StateFlow<List<QueueItem>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _shuffle = MutableStateFlow(false)
    val shuffle: StateFlow<Boolean> = _shuffle.asStateFlow()

    private val _repeat = MutableStateFlow(RepeatMode.OFF)
    val repeat: StateFlow<RepeatMode> = _repeat.asStateFlow()

    fun connect() {
        if (controllerFuture != null) return
        val future = MediaController.Builder(context, PlaybackService.token(context)).buildAsync()
        controllerFuture = future
        future.addListener({
            controller = future.get().also { c ->
                c.addListener(PlayerListener())
                syncFromPlayer(c)
                while (pending.isNotEmpty()) pending.removeFirst().invoke(c)
            }
            startPositionLoop()
        }, MoreExecutors.directExecutor())
    }

    fun release() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        controller = null
    }

    private fun withController(block: (MediaController) -> Unit) {
        val c = controller
        if (c != null) block(c) else pending.addLast(block)
    }

    private fun startPositionLoop() {
        scope.launch {
            while (true) {
                controller?.let { c ->
                    _positionMs.value = c.currentPosition.coerceAtLeast(0)
                    _bufferedMs.value = c.bufferedPosition.coerceAtLeast(0)
                    // Safety-net resync: catches timeline/index changes if an
                    // event was missed. StateFlow de-dupes equal values, so this
                    // is a no-op when nothing actually changed.
                    syncFromPlayer(c)
                }
                delay(500)
            }
        }
    }

    // region playback commands
    fun playSongs(songs: List<Song>, startIndex: Int) = withController { c ->
        c.setMediaItems(MediaItems.fromSongs(songs), startIndex.coerceIn(0, (songs.size - 1).coerceAtLeast(0)), 0)
        c.prepare()
        c.play()
    }

    fun playNext(song: Song) = withController { c ->
        if (c.mediaItemCount == 0) {
            c.setMediaItem(MediaItems.fromSong(song))
            c.prepare()
            c.play()
        } else {
            val insertAt = (c.currentMediaItemIndex + 1).coerceAtMost(c.mediaItemCount)
            c.addMediaItem(insertAt, MediaItems.fromSong(song))
        }
    }

    fun addToQueue(song: Song) = playNext(song)

    fun addToQueue(songs: List<Song>) = withController { c ->
        if (c.mediaItemCount == 0 && songs.isNotEmpty()) {
            c.setMediaItems(MediaItems.fromSongs(songs))
            c.prepare()
            c.play()
        } else {
            val insertAt = (c.currentMediaItemIndex + 1).coerceAtMost(c.mediaItemCount)
            c.addMediaItems(insertAt, MediaItems.fromSongs(songs))
        }
    }

    /** Move the item at [index] to play immediately after the current track. */
    fun moveToPlayNext(index: Int) = withController { c ->
        val current = c.currentMediaItemIndex
        if (index !in 0 until c.mediaItemCount || index == current) return@withController
        val target = if (index < current) current else current + 1
        if (index == target) return@withController
        c.moveMediaItem(index, target)
    }

    fun removeFromQueue(index: Int) = withController { c ->
        if (index in 0 until c.mediaItemCount) c.removeMediaItem(index)
    }

    fun moveInQueue(from: Int, to: Int) = withController { c ->
        if (from in 0 until c.mediaItemCount && to in 0 until c.mediaItemCount) c.moveMediaItem(from, to)
    }

    fun playIndex(index: Int) = withController { c ->
        c.seekToDefaultPosition(index); c.play()
    }

    fun togglePlayPause() = withController { c ->
        if (c.isPlaying) c.pause() else { c.prepare(); c.play() }
    }

    fun next() = withController { it.seekToNext() }
    fun previous() = withController { it.seekToPrevious() }
    fun seekTo(positionMs: Long) = withController { it.seekTo(positionMs) }

    fun toggleShuffle() = withController { c -> c.shuffleModeEnabled = !c.shuffleModeEnabled }

    fun cycleRepeat() = withController { c ->
        c.repeatMode = when (c.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }
    // endregion

    private inner class PlayerListener : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            syncFromPlayer(player)
        }
    }

    private fun syncFromPlayer(player: Player) {
        _isPlaying.value = player.isPlaying
        _currentIndex.value = player.currentMediaItemIndex
        _shuffle.value = player.shuffleModeEnabled
        _repeat.value = when (player.repeatMode) {
            Player.REPEAT_MODE_ONE -> RepeatMode.ONE
            Player.REPEAT_MODE_ALL -> RepeatMode.ALL
            else -> RepeatMode.OFF
        }
        val current = player.currentMediaItem
        _nowPlaying.value = current?.let { item ->
            NowPlaying(
                mediaId = item.mediaId,
                title = item.mediaMetadata.title?.toString() ?: "",
                artist = item.mediaMetadata.artist?.toString() ?: "",
                album = item.mediaMetadata.albumTitle?.toString() ?: "",
                albumId = item.mediaMetadata.extras?.getLong(MediaItems.EXTRA_ALBUM_ID) ?: 0L,
                artworkUri = item.mediaMetadata.artworkUri,
                durationMs = if (player.duration > 0) player.duration else item.mediaMetadata.durationMs ?: 0L
            )
        }
        val items = ArrayList<QueueItem>(player.mediaItemCount)
        for (i in 0 until player.mediaItemCount) {
            val mi = player.getMediaItemAt(i)
            items += QueueItem(
                mediaId = mi.mediaId,
                title = mi.mediaMetadata.title?.toString() ?: "",
                artist = mi.mediaMetadata.artist?.toString() ?: "",
                artworkUri = mi.mediaMetadata.artworkUri,
                indexInQueue = i
            )
        }
        _queue.value = items
    }
}
