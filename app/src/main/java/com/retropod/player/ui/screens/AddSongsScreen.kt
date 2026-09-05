package com.retropod.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.retropod.player.ui.components.IosNavBar
import com.retropod.player.ui.components.ListRow
import com.retropod.player.ui.components.SearchField
import com.retropod.player.ui.theme.Accent
import com.retropod.player.ui.theme.TableBackground
import com.retropod.player.ui.viewmodel.LibraryViewModel

@Composable
fun AddSongsScreen(
    alreadyInPlaylist: Set<Long>,
    libraryViewModel: LibraryViewModel,
    onBack: () -> Unit,
    onConfirm: (List<Long>) -> Unit,
    modifier: Modifier = Modifier
) {
    val songs by libraryViewModel.songs.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(setOf<Long>()) }
    val q = query.trim().lowercase()
    val visible = songs.filter { song ->
        song.id !in alreadyInPlaylist && (
            q.isBlank() ||
                song.title.lowercase().contains(q) ||
                song.artist.lowercase().contains(q) ||
                song.album.lowercase().contains(q)
            )
    }

    Column(modifier.fillMaxSize().background(TableBackground)) {
        IosNavBar(
            title = if (selected.isEmpty()) "Add Songs" else "${selected.size} selected",
            onBack = onBack,
            action = {
                TextButton(
                    onClick = { onConfirm(selected.toList()) },
                    enabled = selected.isNotEmpty()
                ) {
                    Text("Add", color = if (selected.isEmpty()) Color(0x66FFFFFF) else Accent)
                }
            }
        )
        SearchField(value = query, onValueChange = { query = it }, placeholder = "Search songs")
        LazyColumn(Modifier.fillMaxSize()) {
            items(visible, key = { it.id }) { song ->
                val checked = song.id in selected
                ListRow(
                    title = song.title,
                    subtitle = song.artist,
                    artworkUri = song.albumArtUri,
                    highlighted = checked,
                    trailing = {
                        if (checked) {
                            Icon(Icons.Filled.Check, contentDescription = "Selected", tint = Accent)
                        }
                    },
                    onClick = {
                        selected = if (checked) selected - song.id else selected + song.id
                    }
                )
            }
        }
    }
}
