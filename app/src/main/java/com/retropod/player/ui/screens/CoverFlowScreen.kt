package com.retropod.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.retropod.player.ui.theme.Textures
import com.retropod.player.ui.viewmodel.LibraryViewModel
import com.retropod.player.ui.viewmodel.PlayerViewModel
import kotlin.math.absoluteValue
import kotlin.math.min

@Composable
fun CoverFlowScreen(
    libraryViewModel: LibraryViewModel,
    onOpenAlbum: (Long) -> Unit,
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel? = null
) {
    val albums by libraryViewModel.albums.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { albums.size })

    Box(
        modifier.fillMaxSize().background(Textures.nowPlaying)
    ) {
        if (albums.isEmpty()) {
            Text("No albums", color = Color.White, modifier = Modifier.align(Alignment.Center))
            return@Box
        }
        BoxWithConstraints(Modifier.fillMaxSize()) {
            // Landscape phones are wide and short — cap art so covers aren't huge.
            val art = min(min(maxWidth.value * 0.22f, maxHeight.value * 0.42f), 168f).dp
            val sidePad = ((maxWidth - art) / 2).coerceAtLeast(24.dp)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.align(Alignment.Center).fillMaxWidth().height(art + 72.dp),
                contentPadding = PaddingValues(horizontal = sidePad),
                pageSize = PageSize.Fixed(art),
                pageSpacing = (-art.value * 0.12f).dp,
                beyondViewportPageCount = 2
            ) { page ->
                val album = albums.getOrNull(page) ?: return@HorizontalPager
                val offset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                val absOffset = offset.absoluteValue.coerceIn(0f, 2f)
                Column(
                    modifier = Modifier
                        .graphicsLayer {
                            rotationY = offset * 42f
                            val scale = 1f - 0.12f * absOffset.coerceAtMost(1f)
                            scaleX = scale
                            scaleY = scale
                            transformOrigin = TransformOrigin(if (offset > 0) 1f else 0f, 0.5f)
                            cameraDistance = 22f * density
                            alpha = 1f - 0.35f * (absOffset - 1f).coerceAtLeast(0f)
                        }
                        .clickable {
                            if (page == pagerState.currentPage) {
                                val tracks = libraryViewModel.songsForAlbum(album.id)
                                if (tracks.isNotEmpty() && playerViewModel != null) {
                                    playerViewModel.playFrom(tracks, 0)
                                } else {
                                    onOpenAlbum(album.id)
                                }
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = album.artUri,
                        contentDescription = album.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(art)
                            .clip(RoundedCornerShape(10.dp)).background(Color(0xFF17181C))
                    )
                    Box(Modifier.size(width = art, height = art * 0.28f)) {
                        AsyncImage(
                            model = album.artUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(art)
                                .scale(scaleX = 1f, scaleY = -1f)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Box(Modifier.fillMaxSize().background(
                            Brush.verticalGradient(listOf(Color(0x990A0D12), Color(0xFF0A0D12)))
                        ))
                    }
                }
            }

            val current = albums.getOrNull(pagerState.currentPage) ?: return@BoxWithConstraints
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 28.dp)
                    .clickable { onOpenAlbum(current.id) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    current.title, color = Color.White, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    current.artist, color = Color(0xFFB0B0B0), maxLines = 1,
                    overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center
                )
            }
        }
    }
}
