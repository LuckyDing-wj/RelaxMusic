package com.relaxmusic.app.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.relaxmusic.app.ui.components.EmptyLibraryView
import com.relaxmusic.app.ui.components.PremiumSurface
import com.relaxmusic.app.ui.components.PremiumSectionHeader
import com.relaxmusic.app.ui.components.ScanProgressCard
import com.relaxmusic.app.ui.screens.nowplaying.HomePlaybackSummary
import com.relaxmusic.app.ui.theme.RelaxMusicColors

@Composable
fun LibraryScreen(
    state: LibraryUiState,
    homePlaybackSummary: HomePlaybackSummary,
    onPickFolder: () -> Unit,
    onRemoveFolder: (String) -> Unit,
    onRescan: () -> Unit,
    onOpenFullLibrary: () -> Unit,
    onOpenRecent: () -> Unit,
    onOpenNowPlaying: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenAlbums: () -> Unit,
    onOpenArtists: () -> Unit,
    onOpenPlaylists: () -> Unit
) {
    val colors = RelaxMusicColors
    val dashboardModel = buildHomeDashboardModel(
        libraryState = state,
        homePlaybackSummary = homePlaybackSummary
    )

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PremiumSectionHeader(
                title = "RelaxMusic",
                subtitle = "你的本地音乐，按最近状态直接续播",
                onOpenSettings = onOpenSettings
            )
        }

        item {
            HomeHeroCard(
                model = dashboardModel,
                isPlaying = homePlaybackSummary.isPlaying,
                onOpenNowPlaying = onOpenNowPlaying
            )
        }

        item {
            RecentPlaybackStrip(
                items = dashboardModel.recentSongs,
                onOpenRecent = onOpenRecent
            )
        }

        item {
            HomeQuickActionsRow(
                actions = dashboardModel.quickActions,
                onActionClick = { label ->
                    when (label) {
                        "全部歌曲" -> onOpenFullLibrary()
                        "歌单" -> onOpenPlaylists()
                        "收藏" -> onOpenFavorites()
                        "历史" -> onOpenRecent()
                    }
                }
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "曲库与工具",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary
                )
                ScanProgressCard(
                    currentFolderLabel = state.libraryPathLabel,
                    songCount = state.totalSongCount,
                    scanning = state.scanning,
                    statusMessage = state.librarySummaryText.ifBlank { dashboardModel.utilityStatusText },
                    errorMessage = state.errorMessage
                )
                if (state.libraryDirectories.isNotEmpty()) {
                    LibraryDirectoryList(
                        labels = state.libraryDirectoryLabels,
                        onRemoveFolder = onRemoveFolder
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onPickFolder,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("添加目录", fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = onRescan,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("同步曲库", modifier = Modifier.padding(start = 4.dp), fontSize = 13.sp)
                    }
                }
            }
        }

        if (state.totalSongCount == 0) {
            item {
                EmptyLibraryView(
                    onPickFolder = onPickFolder,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun LibraryDirectoryList(
    labels: Map<String, String>,
    onRemoveFolder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RelaxMusicColors
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        labels.forEach { (uri, label) ->
            PremiumSurface {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary
                        )
                        Text(
                            text = uri,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            maxLines = 1
                        )
                    }
                    IconButton(onClick = { onRemoveFolder(uri) }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "移除目录",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
