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
fun AlbumsScreen(
    libraryViewModel: LibraryViewModel,
    onOpenAlbum: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val albums by libraryViewModel.albums.collectAsStateWithLifecycle()
    LazyColumn(modifier.fillMaxSize()) {
        items(albums, key = { it.id }) { album ->
            ListRow(
                title = album.title,
                subtitle = "${album.artist} \u2022 ${album.songCount} songs",
                artworkUri = album.artUri,
                showChevron = true,
                onClick = { onOpenAlbum(album.id) }
            )
        }
    }
}
