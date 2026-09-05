package com.retropod.player.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.retropod.player.ui.theme.Accent
import com.retropod.player.ui.theme.PrimaryText
import com.retropod.player.ui.theme.RowBackground
import com.retropod.player.ui.theme.SecondaryText
import com.retropod.player.ui.theme.TableBackground

@Composable
fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    artworkUri: android.net.Uri? = null,
    showArtwork: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
    showChevron: Boolean = false,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.985f else 1f, tween(120), label = "rowScale")

    Box(modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .clip(RoundedCornerShape(16.dp))
                .background(RowBackground)
                .then(
                    if (onClick != null) Modifier.clickable(interactionSource = interaction, indication = null) { onClick() }
                    else Modifier
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showArtwork) {
                AsyncImage(
                    model = artworkUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(if (subtitle != null) 48.dp else 40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2A3344))
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (highlighted) Accent else PrimaryText,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = SecondaryText,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (trailing != null) {
                Spacer(Modifier.width(8.dp))
                trailing()
            }
            if (showChevron) {
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFFB0BAC8))
            }
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Box(
        Modifier.fillMaxWidth().background(TableBackground).padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text,
            color = SecondaryText,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            letterSpacing = 1.2.sp
        )
    }
}

/** Spotify-style: swipe left-to-right to add a song to the queue, then snap back. */
@Composable
fun SwipeAddToQueue(
    onAddToQueue: () -> Unit,
    content: @Composable () -> Unit
) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) onAddToQueue()
            false
        }
    )
    SwipeToDismissBox(
        state = state,
        enableDismissFromEndToStart = false,
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Accent),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    Modifier.padding(start = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.QueueMusic, contentDescription = "Play next", tint = Color.White)
                }
            }
        }
    ) { content() }
}
