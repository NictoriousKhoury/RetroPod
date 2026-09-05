package com.retropod.player.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.retropod.player.ui.components.IosNavBar
import com.retropod.player.ui.components.IosTabBar
import com.retropod.player.ui.components.LocalNowPlayingArt
import com.retropod.player.ui.components.LocalNowPlayingTitle
import com.retropod.player.ui.components.LocalOpenNowPlaying
import com.retropod.player.ui.components.QueueAddedBanner
import com.retropod.player.ui.components.TabItem
import com.retropod.player.ui.nav.Routes
import com.retropod.player.ui.screens.AlbumsScreen
import com.retropod.player.ui.screens.ArtistsScreen
import com.retropod.player.ui.screens.CoverFlowScreen
import com.retropod.player.ui.screens.NowPlayingScreen
import com.retropod.player.ui.screens.PlaylistDetailScreen
import com.retropod.player.ui.screens.PlaylistsScreen
import com.retropod.player.ui.screens.QueueScreen
import com.retropod.player.ui.screens.SongsScreen
import com.retropod.player.ui.screens.TrackListScreen
import com.retropod.player.ui.theme.TableBackground
import com.retropod.player.ui.viewmodel.LibraryViewModel
import com.retropod.player.ui.viewmodel.PlayerViewModel
import com.retropod.player.ui.viewmodel.PlaylistViewModel
import java.net.URLDecoder

@Composable
fun RetroPodApp() {
    val context = LocalContext.current
    val libraryViewModel: LibraryViewModel = hiltViewModel()
    val playlistViewModel: PlaylistViewModel = hiltViewModel()
    val playerViewModel: PlayerViewModel = hiltViewModel()

    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
    }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    LaunchedEffect(granted) {
        if (granted) {
            libraryViewModel.onPermissionGranted()
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    if (!granted) {
        PermissionGate { launcher.launch(Manifest.permission.READ_MEDIA_AUDIO) }
        return
    }

    MainShell(libraryViewModel, playlistViewModel, playerViewModel)
}

@Composable
private fun PermissionGate(onGrant: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(TableBackground).systemBarsPadding().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("RetroPod", fontWeight = FontWeight.SemiBold, color = Color(0xFF5B7CFF))
        Text("Allow access to your music to build your library.",
            color = Color(0xFF54586A))
        Box(
            Modifier.padding(top = 20.dp).clickable { onGrant() }
                .background(Color(0xFF5B7CFF), RoundedCornerShape(20.dp))
                .padding(horizontal = 28.dp, vertical = 14.dp)
        ) { Text("Grant access", color = Color.White, fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
private fun MainShell(
    libraryViewModel: LibraryViewModel,
    playlistViewModel: PlaylistViewModel,
    playerViewModel: PlayerViewModel
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isTopLevel = currentRoute in Routes.topLevel
    val selectedIndex = Routes.topLevel.indexOf(currentRoute).coerceAtLeast(0)

    val nowPlaying by playerViewModel.nowPlaying.collectAsStateWithLifecycle()
    val queueToast by playerViewModel.queueToast.collectAsStateWithLifecycle()

    val tabs = listOf(
        TabItem("Playlists", Icons.AutoMirrored.Filled.QueueMusic),
        TabItem("Artists", Icons.Filled.Person),
        TabItem("Songs", Icons.Filled.MusicNote),
        TabItem("Albums", Icons.Filled.Album)
    )

    fun openTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.PLAYLISTS) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val openNowPlaying: () -> Unit = {
        navController.navigate(Routes.NOW_PLAYING) { launchSingleTop = true }
    }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val hideNowPlayingChip = currentRoute == Routes.NOW_PLAYING || isLandscape

    CompositionLocalProvider(
        LocalNowPlayingArt provides nowPlaying?.artworkUri.takeIf { !hideNowPlayingChip },
        LocalNowPlayingTitle provides nowPlaying?.title.takeIf { !hideNowPlayingChip },
        LocalOpenNowPlaying provides openNowPlaying
    ) {
    Box(Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = TableBackground,
        bottomBar = {
            if (isTopLevel) {
                IosTabBar(tabs = tabs, selectedIndex = selectedIndex,
                    onSelect = { openTopLevel(Routes.topLevel[it]) })
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.PLAYLISTS,
            modifier = Modifier.padding(padding),
            enterTransition = {
                fadeIn(tween(280)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start, tween(280)
                )
            },
            exitTransition = {
                fadeOut(tween(220)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start, tween(220)
                )
            },
            popEnterTransition = {
                fadeIn(tween(280)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, tween(280)
                )
            },
            popExitTransition = {
                fadeOut(tween(220)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, tween(220)
                )
            }
        ) {
            composable(Routes.PLAYLISTS) {
                TopLevel("Playlists") {
                    PlaylistsScreen(
                        playlistViewModel = playlistViewModel,
                        onOpenImported = { navController.navigate(Routes.playlist(it, true)) },
                        onOpenUser = { navController.navigate(Routes.playlist(it, false)) }
                    )
                }
            }
            composable(Routes.ARTISTS) {
                TopLevel("Artists") {
                    ArtistsScreen(libraryViewModel,
                        onOpenArtist = { navController.navigate(Routes.artist(it)) })
                }
            }
            composable(Routes.SONGS) {
                TopLevel("Songs") {
                    SongsScreen(libraryViewModel, playerViewModel, onOpenNowPlaying = openNowPlaying)
                }
            }
            composable(Routes.ALBUMS) {
                TopLevel("Albums") {
                    AlbumsScreen(libraryViewModel,
                        onOpenAlbum = { navController.navigate(Routes.album(it)) })
                }
            }

            composable(
                Routes.ALBUM_DETAIL,
                arguments = listOf(navArgument("albumId") { type = NavType.LongType })
            ) { entry ->
                val albumId = entry.arguments?.getLong("albumId") ?: 0L
                val album = libraryViewModel.albums.value.firstOrNull { it.id == albumId }
                TrackListScreen(
                    title = album?.title ?: "Album",
                    songs = libraryViewModel.songsForAlbum(albumId),
                    onBack = { navController.popBackStack() },
                    playerViewModel = playerViewModel,
                    onOpenNowPlaying = openNowPlaying
                )
            }
            composable(
                Routes.ARTIST_DETAIL,
                arguments = listOf(navArgument("artistName") { type = NavType.StringType })
            ) { entry ->
                val name = URLDecoder.decode(entry.arguments?.getString("artistName") ?: "", "UTF-8")
                TrackListScreen(
                    title = name,
                    songs = libraryViewModel.songsForArtist(name),
                    onBack = { navController.popBackStack() },
                    playerViewModel = playerViewModel,
                    onOpenNowPlaying = openNowPlaying
                )
            }
            composable(
                Routes.PLAYLIST_DETAIL,
                arguments = listOf(
                    navArgument("playlistId") { type = NavType.LongType },
                    navArgument("imported") { type = NavType.BoolType }
                )
            ) { entry ->
                PlaylistDetailScreen(
                    playlistId = entry.arguments?.getLong("playlistId") ?: 0L,
                    imported = entry.arguments?.getBoolean("imported") ?: true,
                    playlistViewModel = playlistViewModel,
                    playerViewModel = playerViewModel,
                    onBack = { navController.popBackStack() },
                    onOpenNowPlaying = openNowPlaying
                )
            }

            composable(
                Routes.NOW_PLAYING,
                enterTransition = {
                    fadeIn(tween(280)) + slideInVertically(tween(320)) { it }
                },
                exitTransition = {
                    fadeOut(tween(200)) + slideOutVertically(tween(280)) { it }
                },
                popEnterTransition = { fadeIn(tween(200)) },
                popExitTransition = {
                    fadeOut(tween(200)) + slideOutVertically(tween(280)) { it }
                }
            ) {
                NowPlayingScreen(
                    playerViewModel = playerViewModel,
                    onBack = { navController.popBackStack() },
                    onOpenQueue = {
                        navController.navigate(Routes.QUEUE) { launchSingleTop = true }
                    },
                    onOpenArtist = { name ->
                        if (name.isNotBlank()) navController.navigate(Routes.artist(name))
                    },
                    onOpenAlbum = { albumId ->
                        val id = albumId.takeIf { it != 0L }
                            ?: playerViewModel.nowPlaying.value?.mediaId?.toLongOrNull()?.let { songId ->
                                libraryViewModel.songs.value.firstOrNull { it.id == songId }?.albumId
                            }
                        if (id != null && id != 0L) navController.navigate(Routes.album(id))
                    }
                )
            }
            composable(Routes.QUEUE) {
                QueueScreen(playerViewModel, onBack = { navController.popBackStack() })
            }
        }
    }
    QueueAddedBanner(
        toast = queueToast,
        onDismiss = { playerViewModel.consumeQueueToast() },
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = if (isTopLevel) 84.dp else 8.dp)
    )
    if (isLandscape && currentRoute in Routes.topLevel) {
        CoverFlowScreen(
            libraryViewModel = libraryViewModel,
            playerViewModel = playerViewModel,
            onOpenAlbum = { navController.navigate(Routes.album(it)) },
            modifier = Modifier.fillMaxSize()
        )
    }
    }
    }
}

@Composable
private fun TopLevel(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        IosNavBar(title = title)
        Box(Modifier.fillMaxWidth().weight(1f)) { content() }
    }
}
