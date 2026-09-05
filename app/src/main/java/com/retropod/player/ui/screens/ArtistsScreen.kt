package com.retropod.player.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.retropod.player.ui.components.ListRow
import com.retropod.player.ui.viewmodel.LibraryViewModel

@Composable
fun ArtistsScreen(
    libraryViewModel: LibraryViewModel,
    onOpenArtist: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val artists by libraryViewModel.artists.collectAsStateWithLifecycle()
    LazyColumn(modifier.fillMaxSize()) {
        items(artists, key = { it.name }) { artist ->
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
