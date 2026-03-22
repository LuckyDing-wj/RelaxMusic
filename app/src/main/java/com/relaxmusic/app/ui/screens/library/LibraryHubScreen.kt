package com.relaxmusic.app.ui.screens.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.relaxmusic.app.ui.theme.RelaxMusicColors

@Composable
fun LibraryHubScreen(
    songCount: Int,
    albumCount: Int,
    artistCount: Int,
    onOpenSearch: () -> Unit,
    onOpenFullLibrary: () -> Unit,
    onOpenAlbums: () -> Unit,
    onOpenArtists: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("曲库", style = MaterialTheme.typography.headlineMedium)
                Text("浏览全部歌曲、专辑与艺术家。", color = RelaxMusicColors.textSecondary)
            }
        }

        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenSearch),
                shape = RoundedCornerShape(18.dp),
                color = RelaxMusicColors.panelSurface,
                tonalElevation = 0.dp,
                border = BorderStroke(1.dp, RelaxMusicColors.panelBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Rounded.Search, contentDescription = "search", tint = RelaxMusicColors.accent)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("搜索入口", style = MaterialTheme.typography.titleMedium, color = RelaxMusicColors.accent)
                        Text(
                            text = "搜索歌曲、艺术家、专辑",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RelaxMusicColors.textSecondary
                        )
                    }
                    Icon(Icons.Rounded.ArrowForward, contentDescription = "open full library", tint = RelaxMusicColors.textSecondary)
                }
            }
        }

        item {
            HubFeatureCard(
                title = "全部歌曲",
                subtitle = if (songCount == 0) "还没有导入歌曲" else "$songCount 首歌曲",
                supportingText = "进入完整曲库，支持搜索、收藏和加入歌单。",
                icon = { Icon(Icons.Rounded.LibraryMusic, contentDescription = "full library", tint = RelaxMusicColors.accent) },
                onClick = onOpenFullLibrary
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HubCompactCard(
                    title = "专辑",
                    subtitle = if (albumCount == 0) "暂无专辑" else "$albumCount 张",
                    icon = { Icon(Icons.Rounded.Album, contentDescription = "albums", tint = RelaxMusicColors.accent) },
                    onClick = onOpenAlbums,
                    modifier = Modifier.weight(1f)
                )
                HubCompactCard(
                    title = "艺术家",
                    subtitle = if (artistCount == 0) "暂无艺术家" else "$artistCount 位",
                    icon = { Icon(Icons.Rounded.Person, contentDescription = "artists", tint = RelaxMusicColors.accent) },
                    onClick = onOpenArtists,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
internal fun HubFeatureCard(
    title: String,
    subtitle: String,
    supportingText: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RelaxMusicColors
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = colors.panelSurfaceStrong,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, colors.panelBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            icon()
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(text = subtitle, style = MaterialTheme.typography.titleMedium, color = colors.accent)
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
internal fun HubCompactCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RelaxMusicColors
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = colors.panelSurface,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, colors.panelBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            icon()
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = colors.accent)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
