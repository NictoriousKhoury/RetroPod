package com.retropod.player.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.retropod.player.ui.theme.Accent
import com.retropod.player.ui.theme.Textures

val LocalNowPlayingArt = compositionLocalOf<Uri?> { null }
val LocalNowPlayingTitle = compositionLocalOf<String?> { null }
val LocalOpenNowPlaying = staticCompositionLocalOf<() -> Unit> { {} }

@Composable
fun IosNavBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) {
    val art = LocalNowPlayingArt.current
    val nowTitle = LocalNowPlayingTitle.current
    val openNowPlaying = LocalOpenNowPlaying.current

    Column(modifier = modifier.fillMaxWidth().background(Textures.navBar)) {
        Box(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().height(64.dp).padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (onBack != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onBack() }
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Back", tint = Color.White)
                    Text("Back", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                }
            }
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                letterSpacing = 0.4.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = if (onBack != null) 72.dp else 12.dp, end = 168.dp)
            )
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (action != null) action()
                NowPlayingChip(art = art, title = nowTitle, onClick = openNowPlaying)
            }
        }
    }
}

/** iPod-style now-playing control: large art + label, tap to open Now Playing. */
@Composable
fun NowPlayingChip(art: Uri?, title: String?, onClick: () -> Unit) {
    val visible = art != null || !title.isNullOrBlank()
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(220)) + scaleIn(spring(dampingRatio = 0.7f), initialScale = 0.7f),
        exit = fadeOut(tween(160)) + scaleOut(tween(160), targetScale = 0.7f)
    ) {
        Row(
            modifier = Modifier
                .padding(end = 2.dp)
                .height(56.dp)
                .shadow(12.dp, RoundedCornerShape(18.dp), clip = false)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xE62A3348))
                .border(1.5.dp, Accent.copy(alpha = 0.85f), RoundedCornerShape(18.dp))
                .clickable(onClick = onClick)
                .padding(start = 5.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF1A2438)),
                contentAlignment = Alignment.Center
            ) {
                if (art != null) {
                    AsyncImage(
                        model = art,
                        contentDescription = "Now Playing",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                } else {
                    Icon(Icons.Filled.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.size(8.dp))
            Column(Modifier.widthIn(min = 72.dp, max = 108.dp)) {
                Text(
                    "Now Playing",
                    color = Accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.2.sp,
                    maxLines = 1
                )
                Text(
                    title ?: "Tap to open",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.Filled.KeyboardArrowDown,
                contentDescription = "Open Now Playing",
                tint = Color.White,
                modifier = Modifier.size(22.dp).padding(start = 2.dp)
            )
        }
    }
}

data class TabItem(val label: String, val icon: ImageVector)

@Composable
fun IosTabBar(
    tabs: List<TabItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(18.dp, RoundedCornerShape(32.dp), clip = false)
                .clip(RoundedCornerShape(32.dp))
                .background(Textures.tabBar)
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(32.dp)),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == selectedIndex
                val tint by animateColorAsState(
                    if (selected) Color.White else Color(0xFF9AA6BC),
                    animationSpec = tween(220),
                    label = "tabTint"
                )
                val scale by animateFloatAsState(
                    if (selected) 1.08f else 1f,
                    animationSpec = spring(dampingRatio = 0.7f),
                    label = "tabScale"
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelect(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selected) Accent.copy(alpha = 0.35f) else Color.Transparent)
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                            .scale(scale),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(tab.icon, contentDescription = tab.label, tint = tint, modifier = Modifier.height(26.dp))
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        tab.label,
                        color = tint,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}
