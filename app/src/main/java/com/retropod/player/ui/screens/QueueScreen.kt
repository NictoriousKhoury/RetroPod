package com.retropod.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.retropod.player.playback.QueueItem
import com.retropod.player.ui.components.IosNavBar
import com.retropod.player.ui.theme.Accent
import com.retropod.player.ui.theme.PrimaryText
import com.retropod.player.ui.theme.RowBackground
import com.retropod.player.ui.theme.SecondaryText
import com.retropod.player.ui.theme.TableBackground
import com.retropod.player.ui.viewmodel.PlayerViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun QueueScreen(
    playerViewModel: PlayerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val queue by playerViewModel.queue.collectAsStateWithLifecycle()
    val currentIndex by playerViewModel.currentIndex.collectAsStateWithLifecycle()

    val local: SnapshotStateList<QueueItem> = remember { mutableStateListOf() }
    var dragging by remember { mutableStateOf(false) }
    LaunchedEffect(queue, dragging) {
        if (!dragging) {
            local.clear()
            local.addAll(queue)
        }
    }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        local.add(to.index, local.removeAt(from.index))
        playerViewModel.moveInQueue(from.index, to.index)
    }

    LaunchedEffect(currentIndex, local.size, dragging) {
        if (!dragging && currentIndex in local.indices) {
            lazyListState.animateScrollToItem(currentIndex)
        }
    }

    Column(modifier.fillMaxSize().background(TableBackground)) {
        IosNavBar(title = "Up Next", onBack = onBack)
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            items(local, key = { System.identityHashCode(it) }) { item ->
                ReorderableItem(reorderState, key = System.identityHashCode(item)) {
                    val itemScope = this
                    val index = local.indexOf(item).takeIf { it >= 0 } ?: return@ReorderableItem
                    val isCurrent = index == currentIndex
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            val at = local.indexOf(item)
                            when {
                                at < 0 -> false
                                value == SwipeToDismissBoxValue.EndToStart && at != currentIndex -> {
                                    playerViewModel.removeFromQueue(at)
                                    true
                                }
                                value == SwipeToDismissBoxValue.StartToEnd && at != currentIndex -> {
                                    playerViewModel.moveToPlayNext(at)
                                    false
                                }
                                else -> false
                            }
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val dir = dismissState.dismissDirection
                            val color = when (dir) {
                                SwipeToDismissBoxValue.EndToStart -> Color(0xFF5C1E24)
                                SwipeToDismissBoxValue.StartToEnd -> Accent.copy(alpha = 0.35f)
                                else -> Color.Transparent
                            }
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(color)
                            )
                        }
                    ) {
                        QueueRow(
                            item = item,
                            isCurrent = isCurrent,
                            alreadyPlayed = index < currentIndex,
                            dragHandle = {
                                Icon(
                                    Icons.Filled.DragHandle, "Reorder",
                                    tint = Color(0xFF5A6578),
                                    modifier = Modifier.size(22.dp).then(
                                        with(itemScope) {
                                            Modifier.draggableHandle(
                                                onDragStarted = { dragging = true },
                                                onDragStopped = { dragging = false }
                                            )
                                        }
                                    )
                                )
                            },
                            onClick = {
                                val at = local.indexOf(item)
                                if (at >= 0) playerViewModel.playIndex(at)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueRow(
    item: QueueItem,
    isCurrent: Boolean,
    alreadyPlayed: Boolean,
    dragHandle: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val titleColor = when {
        isCurrent -> Accent
        alreadyPlayed -> SecondaryText.copy(alpha = 0.5f)
        else -> PrimaryText
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isCurrent) Accent.copy(alpha = 0.12f) else RowBackground)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.artworkUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF2A3344))
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                item.title,
                color = titleColor,
                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                item.artist,
                color = if (alreadyPlayed) SecondaryText.copy(alpha = 0.4f) else SecondaryText,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        dragHandle()
    }
}
