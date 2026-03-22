package com.relaxmusic.app.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("列表", style = MaterialTheme.typography.headlineMedium)
                Text("管理你的歌单、收藏与播放历史。", color = RelaxMusicColors.textSecondary)
            }
        }

        item {
            HubFeatureCard(
                title = "歌单",
                subtitle = if (playlistCount == 0) "还没有歌单" else "$playlistCount 个歌单",
                supportingText = "创建、重命名和整理你自己的播放列表。",
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
