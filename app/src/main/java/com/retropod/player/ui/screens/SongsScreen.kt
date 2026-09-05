package com.retropod.player.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.retropod.player.ui.components.ListRow
import com.retropod.player.ui.components.SwipeAddToQueue
import com.retropod.player.ui.viewmodel.LibraryViewModel
import com.retropod.player.ui.viewmodel.PlayerViewModel

@Composable
fun SongsScreen(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
    onOpenNowPlaying: () -> Unit = {}
) {
    val songs by libraryViewModel.songs.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    val q = query.trim().lowercase()

    val visible = if (q.isBlank()) songs
    else songs.filter { it.title.lowercase().contains(q) || it.artist.lowercase().contains(q) }

    // Start scrolled just past the search bar (item 0) so it's hidden like iOS;
    // scrolling to the very top reveals it.
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 1)

    Column(modifier.fillMaxSize()) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            item(key = "__search__") {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    placeholder = { Text("Search songs") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF171E28),
                        unfocusedContainerColor = Color(0xFF171E28),
                        focusedTextColor = Color(0xFFF3F5FA),
                        unfocusedTextColor = Color(0xFFF3F5FA),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFF7C9CFF)
                    )
                )
            }
            items(visible, key = { it.id }) { song ->
                SwipeAddToQueue(onAddToQueue = { playerViewModel.addToQueue(song) }) {
                    ListRow(
                        title = song.title,
                        subtitle = song.artist,
                        artworkUri = song.albumArtUri,
                        onClick = {
                            playerViewModel.playFrom(visible, visible.indexOfFirst { it.id == song.id })
                            onOpenNowPlaying()
                        }
                    )
                }
            }
        }
    }
}
