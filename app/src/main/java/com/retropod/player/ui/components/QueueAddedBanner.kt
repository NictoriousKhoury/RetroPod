package com.retropod.player.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.retropod.player.ui.theme.Accent
import com.retropod.player.ui.viewmodel.QueueToast
import kotlinx.coroutines.delay

@Composable
fun QueueAddedBanner(
    toast: QueueToast?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    LaunchedEffect(toast) {
        if (toast != null) {
            performConfirmHaptic(view)
            delay(2200)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = toast != null,
        enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { it / 2 },
        exit = fadeOut(tween(180)) + slideOutVertically(tween(180)) { it / 3 },
        modifier = modifier
    ) {
        val current = toast ?: return@AnimatedVisibility
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .shadow(16.dp, RoundedCornerShape(18.dp), clip = false)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xF21A2230))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = current.artworkUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF2A3344))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Playing next",
                    color = Accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.6.sp
                )
                Text(
                    current.title,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    current.artist,
                    color = Color(0xFF8B97AB),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Filled.QueueMusic, contentDescription = null, tint = Accent, modifier = Modifier.size(20.dp))
        }
    }
}

fun performConfirmHaptic(view: View) {
    val type = if (Build.VERSION.SDK_INT >= 30) {
        HapticFeedbackConstants.CONFIRM
    } else {
        HapticFeedbackConstants.CONTEXT_CLICK
    }
    view.performHapticFeedback(type)
}
