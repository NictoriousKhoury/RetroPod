package com.retropod.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.retropod.player.data.model.Album
import com.retropod.player.ui.components.IosNavBar
import com.retropod.player.ui.components.ListRow
import com.retropod.player.ui.theme.TableBackground

@Composable
fun ArtistAlbumsScreen(
    artistName: String,
    albums: List<Album>,
    onBack: () -> Unit,
    onOpenAlbum: (Long) -> Unit,
    onOpenAllSongs: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().background(TableBackground)) {
        IosNavBar(title = artistName, onBack = onBack)
        LazyColumn(Modifier.fillMaxSize()) {
            item(key = "__all_songs__") {
                ListRow(
                    title = "All Songs",
                    subtitle = "Every track by $artistName",
                    showArtwork = false,
                    showChevron = true,
                    highlighted = true,
                    onClick = onOpenAllSongs
                )
            }
            items(albums, key = { it.id }) { album ->
                ListRow(
                    title = album.title,
                    subtitle = buildString {
                        if (album.year > 0) append("${album.year} \u2022 ")
                        append("${album.songCount} songs")
                    },
                    artworkUri = album.artUri,
                    showChevron = true,
                    onClick = { onOpenAlbum(album.id) }
                )
            }
        }
    }
}
