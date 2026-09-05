package com.retropod.player.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.retropod.player.ui.components.ListRow
import com.retropod.player.ui.components.SectionHeader
import com.retropod.player.ui.viewmodel.PlaylistViewModel

private val VIBES = setOf("Sad Songs", "Upbeat", "Chill", "Party", "Love Songs")
private val DECADE = Regex("^\\d{4}s$")

@Composable
fun PlaylistsScreen(
    playlistViewModel: PlaylistViewModel,
    onOpenImported: (Long) -> Unit,
    onOpenUser: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val imported by playlistViewModel.importedPlaylists.collectAsStateWithLifecycle()
    val user by playlistViewModel.userPlaylists.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    val genres = imported.filter { !DECADE.matches(it.name) && it.name !in VIBES }.sortedBy { it.name }
    val decades = imported.filter { DECADE.matches(it.name) }.sortedBy { it.name }
    val vibes = imported.filter { it.name in VIBES }.sortedBy { it.name }

    LazyColumn(modifier.fillMaxSize()) {
        item {
            ListRow(
                title = "New Playlist\u2026",
                showArtwork = false,
                highlighted = true,
                trailing = { Icon(Icons.Filled.Add, contentDescription = null) },
                onClick = { showDialog = true }
            )
        }
        if (user.isNotEmpty()) {
            item { SectionHeader("My Playlists") }
            items(user, key = { "u${it.id}" }) { p ->
                ListRow(title = p.name, showArtwork = false, showChevron = true,
                    onClick = { onOpenUser(p.id) })
            }
        }
        if (genres.isNotEmpty()) {
            item { SectionHeader("Genres") }
            items(genres, key = { "g${it.id}" }) { p ->
                ListRow(title = p.name, subtitle = "${p.songIds.size} songs",
                    showArtwork = false, showChevron = true, onClick = { onOpenImported(p.id) })
            }
        }
        if (decades.isNotEmpty()) {
            item { SectionHeader("Decades") }
            items(decades, key = { "d${it.id}" }) { p ->
                ListRow(title = p.name, subtitle = "${p.songIds.size} songs",
                    showArtwork = false, showChevron = true, onClick = { onOpenImported(p.id) })
            }
        }
        if (vibes.isNotEmpty()) {
            item { SectionHeader("Vibes") }
            items(vibes, key = { "v${it.id}" }) { p ->
                ListRow(title = p.name, subtitle = "${p.songIds.size} songs",
                    showArtwork = false, showChevron = true, onClick = { onOpenImported(p.id) })
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false; newName = "" },
            title = { Text("New Playlist") },
            text = {
                OutlinedTextField(value = newName, onValueChange = { newName = it },
                    singleLine = true, label = { Text("Name") })
            },
            confirmButton = {
                TextButton(onClick = {
                    val name = newName.trim()
                    if (name.isNotEmpty()) playlistViewModel.createPlaylist(name)
                    showDialog = false; newName = ""
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false; newName = "" }) { Text("Cancel") } }
        )
    }
}
