package com.retropod.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.retropod.player.data.media.LibrarySync
import com.retropod.player.ui.RetroPodApp
import com.retropod.player.ui.theme.RetroPodTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var librarySync: LibrarySync

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        librarySync.start()
        enableEdgeToEdge()
        setContent {
            RetroPodTheme {
                RetroPodApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        librarySync.requestSync(scanDisk = true)
    }
}
