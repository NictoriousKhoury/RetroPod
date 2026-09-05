package com.retropod.player.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.retropod.player.ui.components.ListRow
import com.retropod.player.ui.components.SearchField
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
                SearchField(value = query, onValueChange = { query = it }, placeholder = "Search songs")
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
