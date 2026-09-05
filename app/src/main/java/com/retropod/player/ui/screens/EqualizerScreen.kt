package com.retropod.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.retropod.player.playback.EqualizerController
import com.retropod.player.ui.components.IosNavBar
import com.retropod.player.ui.theme.Accent
import com.retropod.player.ui.theme.PrimaryText
import com.retropod.player.ui.theme.SecondaryText
import com.retropod.player.ui.theme.TableBackground
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    val equalizer: EqualizerController
) : ViewModel()

@Composable
fun EqualizerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EqualizerViewModel = hiltViewModel()
) {
    val enabled by viewModel.equalizer.enabled.collectAsStateWithLifecycle()
    val bands by viewModel.equalizer.bands.collectAsStateWithLifecycle()
    val available by viewModel.equalizer.available.collectAsStateWithLifecycle()

    Column(modifier.fillMaxSize().background(TableBackground)) {
        IosNavBar(title = "Equalizer", onBack = onBack)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("On", color = PrimaryText, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                Switch(
                    checked = enabled && available,
                    onCheckedChange = { viewModel.equalizer.setEnabled(it) },
                    enabled = available,
                    colors = SwitchDefaults.colors(checkedTrackColor = Accent)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                if (!available) "Start playback so the equalizer can attach to the audio session."
                else "Bands apply to the current playing output.",
                color = SecondaryText,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(20.dp))
            bands.forEach { band ->
                val label = when {
                    band.freqHz >= 1000 -> "${band.freqHz / 1000} kHz"
                    else -> "${band.freqHz} Hz"
                }
                Text(label, color = PrimaryText, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Slider(
                    value = band.levelMilliBel.toFloat(),
                    onValueChange = {
                        viewModel.equalizer.setBandLevel(band.index, it.toInt().toShort())
                    },
                    valueRange = band.minMilliBel.toFloat()..band.maxMilliBel.toFloat(),
                    enabled = available && enabled,
                    colors = SliderDefaults.colors(
                        thumbColor = Accent,
                        activeTrackColor = Accent
                    )
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
