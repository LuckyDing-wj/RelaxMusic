package com.relaxmusic.app.ui.screens.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.relaxmusic.app.ui.components.EmptyLibraryView
import com.relaxmusic.app.ui.components.ScanProgressCard
import com.relaxmusic.app.ui.theme.RelaxMusicColors

@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onPickFolder: () -> Unit,
    onRemoveFolder: (String) -> Unit,
    onRescan: () -> Unit,
    onOpenFullLibrary: () -> Unit,
    onOpenAlbums: () -> Unit,
    onOpenArtists: () -> Unit,
    onOpenPlaylists: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenRecent: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val colors = RelaxMusicColors
    val favoriteCountText = remember(state.favoriteSongs.size) { "${state.favoriteSongs.size} 首已标记" }
    val recentSubtitle = remember(state.recentSongs) {
        state.recentSongs.take(2).joinToString(" / ") { it.title }
    }
    val playlistSubtitle = remember(state.playlists.size) {
        if (state.playlists.isEmpty()) "还没有歌单" else "${state.playlists.size} 个歌单"
    }
    val albumsSubtitle = remember(state.albums.size) {
        if (state.albums.isEmpty()) "暂无专辑" else "${state.albums.size} 张"
    }
    val artistsSubtitle = remember(state.artists.size) {
        if (state.artists.isEmpty()) "暂无艺术家" else "${state.artists.size} 位"
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("RelaxMusic", style = MaterialTheme.typography.headlineMedium)
                    Text("纯本地音乐播放器 V1", color = colors.textSecondary)
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Rounded.Settings, contentDescription = "settings")
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onPickFolder) { Text("选择目录") }
                OutlinedButton(onClick = onRescan) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null)
                    Text("重新扫描", modifier = Modifier.padding(start = 6.dp))
                }
            }
        }

        item {
            ScanProgressCard(
                currentFolderLabel = state.libraryPathLabel,
                songCount = state.totalSongCount,
                scanning = state.scanning,
                statusMessage = state.statusMessage,
                errorMessage = state.errorMessage
            )
        }

        if (state.libraryDirectories.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.libraryDirectories.forEach { directoryUri ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = colors.panelSurface,
                            border = BorderStroke(1.dp, colors.panelBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = state.libraryDirectoryLabels[directoryUri] ?: directoryUri,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                    color = colors.textSecondary
                                )
                                TextButton(onClick = { onRemoveFolder(directoryUri) }) {
                                    Text("移除")
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = "搜索歌曲、艺术家、专辑",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "search") },
                trailingIcon = { Icon(Icons.Rounded.ArrowForward, contentDescription = "open library") },
                shape = RoundedCornerShape(18.dp),
                label = { Text("搜索入口") },
                singleLine = true,
                readOnly = true
            )
        }

        item {
            QuickInfoCard(
                title = "完整曲库",
                subtitle = if (state.totalSongCount == 0) "还没有歌曲" else "浏览全部 ${state.totalSongCount} 首歌曲",
                icon = { Icon(Icons.Rounded.MusicNote, contentDescription = "full library", tint = colors.accent) },
                onClick = onOpenFullLibrary,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (state.favoriteSongs.isNotEmpty() || state.recentSongs.isNotEmpty()) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (state.favoriteSongs.isNotEmpty()) {
                        QuickInfoCard(
                            title = "收藏",
                            subtitle = favoriteCountText,
                            icon = { Icon(Icons.Rounded.Favorite, contentDescription = "favorites", tint = colors.accent) },
                            onClick = onOpenFavorites,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (state.recentSongs.isNotEmpty()) {
                        QuickInfoCard(
                            title = "最近播放",
                            subtitle = recentSubtitle,
                            icon = { Icon(Icons.Rounded.History, contentDescription = "recent", tint = colors.accent) },
                            onClick = onOpenRecent,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            QuickInfoCard(
                title = "歌单",
                subtitle = playlistSubtitle,
                icon = { Icon(Icons.Rounded.LibraryMusic, contentDescription = "playlists", tint = colors.accent) },
                onClick = onOpenPlaylists,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickInfoCard(
                    title = "专辑",
                    subtitle = albumsSubtitle,
                    icon = { Icon(Icons.Rounded.Album, contentDescription = "albums", tint = colors.accent) },
                    onClick = onOpenAlbums,
                    modifier = Modifier.weight(1f)
                )
                QuickInfoCard(
                    title = "艺术家",
                    subtitle = artistsSubtitle,
                    icon = { Icon(Icons.Rounded.Person, contentDescription = "artists", tint = colors.accent) },
                    onClick = onOpenArtists,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (state.totalSongCount == 0) {
            item {
                EmptyLibraryView(onPickFolder = onPickFolder, modifier = Modifier.fillMaxWidth())
            }
        } else {
            item {
                Text(
                    text = "首页只展示摘要和入口，完整歌曲列表已移到“完整曲库”。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun QuickInfoCard(
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
