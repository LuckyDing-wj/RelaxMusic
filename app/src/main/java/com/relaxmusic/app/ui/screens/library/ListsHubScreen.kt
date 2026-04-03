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
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LibraryMusic
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
fun ListsHubScreen(
    playlistCount: Int,
    favoritesCount: Int,
    historyCount: Int,
    onOpenPlaylists: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenHistory: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("列表", style = MaterialTheme.typography.headlineMedium)
        }

        item {
            HubFeatureCard(
                title = "歌单",
                subtitle = if (playlistCount == 0) "还没有歌单" else "$playlistCount 个歌单",
                icon = { Icon(Icons.Rounded.LibraryMusic, contentDescription = "playlists", tint = RelaxMusicColors.accent) },
                onClick = onOpenPlaylists
            )
        }

        item {
            HubCompactCard(
                title = "收藏",
                subtitle = if (favoritesCount == 0) "还没有收藏歌曲" else "$favoritesCount 首已标记",
                icon = { Icon(Icons.Rounded.Favorite, contentDescription = "favorites", tint = RelaxMusicColors.accent) },
                onClick = onOpenFavorites
            )
        }

        item {
            HubCompactCard(
                title = "历史",
                subtitle = if (historyCount == 0) "还没有播放记录" else "$historyCount 条记录",
                icon = { Icon(Icons.Rounded.History, contentDescription = "history", tint = RelaxMusicColors.accent) },
                onClick = onOpenHistory
            )
        }
    }
}

@Composable
private fun HubFeatureCard(
    title: String,
    subtitle: String,
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
        }
    }
}

@Composable
private fun HubCompactCard(
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
