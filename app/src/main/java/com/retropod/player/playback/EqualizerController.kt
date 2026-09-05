package com.retropod.player.playback

import android.media.audiofx.Equalizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class EqBand(
    val index: Short,
    val freqHz: Int,
    val minMilliBel: Short,
    val maxMilliBel: Short,
    val levelMilliBel: Short
)

/**
 * Binds [android.media.audiofx.Equalizer] to the live ExoPlayer audio session
 * (not session 0, which is a no-op on most modern devices).
 */
@Singleton
class EqualizerController @Inject constructor() {

    private val _enabled = MutableStateFlow(false)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _bands = MutableStateFlow<List<EqBand>>(emptyList())
    val bands: StateFlow<List<EqBand>> = _bands.asStateFlow()

    private val _available = MutableStateFlow(false)
    val available: StateFlow<Boolean> = _available.asStateFlow()

    private var equalizer: Equalizer? = null
    private var sessionId: Int = 0

    @Synchronized
    fun attach(newSessionId: Int) {
        if (newSessionId == 0) {
            releaseLocked()
            return
        }
        if (newSessionId == sessionId && equalizer != null) return
        val wasEnabled = _enabled.value
        val previousLevels = _bands.value.associate { it.index to it.levelMilliBel }
        releaseLocked()
        sessionId = newSessionId
        try {
            val eq = Equalizer(0, newSessionId)
            equalizer = eq
            _bands.value = (0 until eq.numberOfBands).map { i ->
                val band = i.toShort()
                EqBand(
                    index = band,
                    freqHz = eq.getCenterFreq(band) / 1000,
                    minMilliBel = eq.bandLevelRange[0],
                    maxMilliBel = eq.bandLevelRange[1],
                    levelMilliBel = previousLevels[band] ?: eq.getBandLevel(band)
                )
            }
            previousLevels.forEach { (index, level) ->
                try { eq.setBandLevel(index, level) } catch (_: Exception) {}
            }
            eq.enabled = wasEnabled
            _enabled.value = wasEnabled
            _available.value = true
        } catch (_: Exception) {
            equalizer = null
            _available.value = false
            _bands.value = emptyList()
        }
    }

    @Synchronized
    fun setEnabled(on: Boolean) {
        _enabled.value = on
        try { equalizer?.enabled = on } catch (_: Exception) {}
    }

    @Synchronized
    fun setBandLevel(index: Short, level: Short) {
        try { equalizer?.setBandLevel(index, level) } catch (_: Exception) {}
        _bands.value = _bands.value.map {
            if (it.index == index) it.copy(levelMilliBel = level) else it
        }
    }

    @Synchronized
    fun release() = releaseLocked()

    private fun releaseLocked() {
        try { equalizer?.release() } catch (_: Exception) {}
        equalizer = null
        sessionId = 0
        _available.value = false
    }
}
