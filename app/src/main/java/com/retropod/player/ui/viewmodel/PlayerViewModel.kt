package com.retropod.player.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retropod.player.data.db.StatsDao
import com.retropod.player.data.model.Song
import com.retropod.player.playback.PlaybackConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    val connection: PlaybackConnection,
    private val statsDao: StatsDao
) : ViewModel() {

    val nowPlaying = connection.nowPlaying
    val isPlaying = connection.isPlaying
    val positionMs = connection.positionMs
    val bufferedMs = connection.bufferedMs
    val queue = connection.queue
    val currentIndex = connection.currentIndex
    val shuffle = connection.shuffle
    val repeat = connection.repeat

    private val _favorite = MutableStateFlow(false)
    val favorite: StateFlow<Boolean> = _favorite.asStateFlow()

    init {
        connection.connect()
        viewModelScope.launch {
            nowPlaying.collect { np ->
                val id = np?.mediaId?.toLongOrNull() ?: return@collect
                _favorite.value = statsDao.get(id)?.favorite == true
            }
        }
    }

    fun playFrom(songs: List<Song>, index: Int) {
        connection.playSongs(songs, index)
        songs.getOrNull(index)?.let { bumpPlayCount(it.id) }
    }

    private val _queueToast = MutableStateFlow<QueueToast?>(null)
    val queueToast: StateFlow<QueueToast?> = _queueToast.asStateFlow()

    fun playNext(song: Song) = connection.playNext(song)
    fun addToQueue(song: Song) {
        connection.playNext(song)
        _queueToast.value = QueueToast(song.title, song.artist, song.albumArtUri)
    }
    fun addToQueue(songs: List<Song>) {
        connection.addToQueue(songs)
        songs.lastOrNull()?.let {
            _queueToast.value = QueueToast(it.title, it.artist, it.albumArtUri)
        }
    }
    fun consumeQueueToast() { _queueToast.value = null }
    fun removeFromQueue(index: Int) = connection.removeFromQueue(index)
    fun moveInQueue(from: Int, to: Int) = connection.moveInQueue(from, to)
    fun moveToPlayNext(index: Int) = connection.moveToPlayNext(index)
    fun playIndex(index: Int) = connection.playIndex(index)

    fun togglePlayPause() = connection.togglePlayPause()
    fun next() = connection.next()
    fun previous() = connection.previous()
    fun seekTo(ms: Long) = connection.seekTo(ms)
    fun toggleShuffle() = connection.toggleShuffle()
    fun cycleRepeat() = connection.cycleRepeat()

    fun toggleFavorite() {
        val id = nowPlaying.value?.mediaId?.toLongOrNull() ?: return
        viewModelScope.launch { _favorite.value = statsDao.toggleFavorite(id) }
    }

    private fun bumpPlayCount(id: Long) = viewModelScope.launch { statsDao.incrementPlayCount(id) }

    override fun onCleared() {
        connection.release()
        super.onCleared()
    }
}

data class QueueToast(
    val title: String,
    val artist: String,
    val artworkUri: Uri?
)
