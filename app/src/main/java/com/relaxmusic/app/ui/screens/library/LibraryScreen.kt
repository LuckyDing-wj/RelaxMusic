package com.relaxmusic.app.ui.screens.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayCircle
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        currentSong = currentSong,
        isPlaying = isPlaying,
        playbackProgress = playbackProgress
    )

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RelaxMusic",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                )
                IconButton(onClick = onOpenSettings, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Rounded.Settings, contentDescription = "settings", modifier = Modifier.size(24.dp))
                }
            }
        }

        item {
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

        item {
            ScanProgressCard(
                currentFolderLabel = state.libraryPathLabel,
                songCount = state.totalSongCount,
                scanning = state.scanning,
                statusMessage = state.statusMessage,
                errorMessage = state.errorMessage
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GridHubItem(
                        title = "全部歌曲",
                        icon = Icons.Rounded.MusicNote,
                        onClick = onOpenFullLibrary,
                        modifier = Modifier.weight(1f)
                    )
                    GridHubItem(
                        title = "我的收藏",
                        icon = Icons.Rounded.Favorite,
                        onClick = onOpenFavorites,
                        modifier = Modifier.weight(1f)
                    )
                    GridHubItem(
                        title = "我的歌单",
                        icon = Icons.Rounded.LibraryMusic,
                        onClick = onOpenPlaylists,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GridHubItem(
                        title = "播放历史",
                        icon = Icons.Rounded.History,
                        onClick = onOpenRecent,
                        modifier = Modifier.weight(1f)
                    )
                    GridHubItem(
                        title = "专辑分类",
                        icon = Icons.Rounded.Album,
                        onClick = onOpenAlbums,
                        modifier = Modifier.weight(1f)
                    )
                    GridHubItem(
                        title = "艺术家",
                        icon = Icons.Rounded.Person,
                        onClick = onOpenArtists,
                        modifier = Modifier.weight(1f)
                    )
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

        if (dashboardModel.showRecentSection) {
            item {
                Text(
                    text = "最近播放",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = dashboardModel.recentSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
private fun GridHubItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RelaxMusicColors
    Surface(
        modifier = modifier.height(84.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = colors.panelSurface,
        border = BorderStroke(1.dp, colors.panelBorder)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = colors.accent,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                textAlign = TextAlign.Center
            )
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
        shape = RoundedCornerShape(16.dp),
        color = colors.panelSurfaceStrong,
        border = BorderStroke(1.dp, colors.panelBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (enabled && isPlaying) "正在播放" else "继续播放",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.accent
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    trackColor = colors.panelBorder,
                    color = colors.accent
                )
            }
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.PauseCircle else Icons.Rounded.PlayCircle,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
