package com.retropod.player.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.retropod.player.ui.components.ListRow
import com.retropod.player.ui.components.SearchField
import com.retropod.player.ui.viewmodel.LibraryViewModel

@Composable
fun ArtistsScreen(
    libraryViewModel: LibraryViewModel,
    onOpenArtist: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val artists by libraryViewModel.artists.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    val q = query.trim().lowercase()
    val visible = if (q.isBlank()) artists
    else artists.filter { it.name.lowercase().contains(q) }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 1)

    LazyColumn(modifier.fillMaxSize(), state = listState) {
        item(key = "__search__") {
            SearchField(value = query, onValueChange = { query = it }, placeholder = "Search artists")
        }
        items(visible, key = { it.name }) { artist ->
            ListRow(
                title = artist.name,
                subtitle = "${artist.songCount} songs \u2022 ${artist.albumCount} albums",
                showArtwork = false,
                showChevron = true,
                onClick = { onOpenArtist(artist.name) }
            )
        }
    }
}
