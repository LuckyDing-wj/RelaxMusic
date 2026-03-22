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
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.relaxmusic.app.domain.model.Song
import com.relaxmusic.app.ui.components.EmptyLibraryView
import com.relaxmusic.app.ui.components.ScanProgressCard
import com.relaxmusic.app.ui.theme.RelaxMusicColors

@Composable
fun LibraryScreen(
    state: LibraryUiState,
    currentSong: Song?,
    isPlaying: Boolean,
    playbackProgress: Float,
    onPickFolder: () -> Unit,
    onRemoveFolder: (String) -> Unit,
    onRescan: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenRecent: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenTimer: () -> Unit,
    onOpenNowPlaying: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val colors = RelaxMusicColors
    val dashboardModel = buildHomeDashboardModel(
        libraryState = state,
        currentSong = currentSong,
        isPlaying = isPlaying,
        playbackProgress = playbackProgress
    )

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
            ContinuePlaybackCard(
                title = dashboardModel.continueTitle,
                subtitle = dashboardModel.continueSubtitle,
                progress = dashboardModel.continueProgress,
                isPlaying = isPlaying,
                enabled = currentSong != null,
                onClick = onOpenNowPlaying,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            QuickInfoCard(
                title = dashboardModel.libraryEntryTitle,
                subtitle = dashboardModel.libraryEntrySubtitle,
                icon = { Icon(Icons.Rounded.MusicNote, contentDescription = "library", tint = colors.accent) },
                onClick = onOpenLibrary,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickInfoCard(
                    title = "播放队列",
                    subtitle = "查看当前待播列表",
                    icon = { Icon(Icons.Rounded.QueueMusic, contentDescription = "queue", tint = colors.accent) },
                    onClick = onOpenQueue,
                    modifier = Modifier.weight(1f)
                )
                QuickInfoCard(
                    title = "睡眠定时",
                    subtitle = "设置自动停止播放",
                    icon = { Icon(Icons.Rounded.Bedtime, contentDescription = "sleep timer", tint = colors.accent) },
                    onClick = onOpenTimer,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (dashboardModel.showRecentSection) {
            item {
                QuickInfoCard(
                    title = "最近播放",
                    subtitle = dashboardModel.recentSubtitle,
                    icon = { Icon(Icons.Rounded.History, contentDescription = "recent", tint = colors.accent) },
                    onClick = onOpenRecent,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (state.totalSongCount == 0) {
            item {
                EmptyLibraryView(onPickFolder = onPickFolder, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ContinuePlaybackCard(
    title: String,
    subtitle: String,
    progress: Float,
    isPlaying: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RelaxMusicColors
    Surface(
        modifier = modifier.then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(20.dp),
        color = colors.panelSurfaceStrong,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, colors.panelBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (enabled && isPlaying) "正在播放" else "继续播放",
                style = MaterialTheme.typography.labelLarge,
                color = colors.accent
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )
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
