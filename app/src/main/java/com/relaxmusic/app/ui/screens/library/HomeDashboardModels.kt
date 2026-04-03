package com.relaxmusic.app.ui.screens.library

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.ui.graphics.vector.ImageVector
import com.relaxmusic.app.ui.screens.nowplaying.HomePlaybackSummary

data class HomeRecentPreview(
    val id: String,
    val title: String,
    val artist: String
)

data class HomeQuickAction(
    val destinationLabel: String,
    val icon: ImageVector
)

data class HomeDashboardModel(
    val heroTitle: String,
    val heroSubtitle: String,
    val heroProgress: Float,
    val heroMeta: String,
    val recentSongs: List<HomeRecentPreview>,
    val quickActions: List<HomeQuickAction>,
    val utilityStatusText: String
)

internal fun buildHomeDashboardModel(
    libraryState: LibraryUiState,
    homePlaybackSummary: HomePlaybackSummary
): HomeDashboardModel {
    val currentSong = homePlaybackSummary.currentSong
    val recentSongs = libraryState.recentSongs
        .ifEmpty { libraryState.historySongs }
        .take(6)
        .map { song ->
            HomeRecentPreview(
                id = song.id,
                title = song.title,
                artist = song.artist
            )
        }

    return HomeDashboardModel(
        heroTitle = currentSong?.title ?: "继续播放",
        heroSubtitle = when {
            currentSong == null -> ""
            homePlaybackSummary.isPlaying -> "${currentSong.artist} · 正在播放"
            else -> "${currentSong.artist} · 已暂停"
        },
        heroProgress = when {
            currentSong == null -> 0f
            else -> homePlaybackSummary.progress.coerceIn(0f, 1f)
        },
        heroMeta = currentSong?.album.orEmpty(),
        recentSongs = recentSongs,
        quickActions = listOf(
            HomeQuickAction("全部歌曲", Icons.Rounded.MusicNote),
            HomeQuickAction("歌单", Icons.Rounded.LibraryMusic),
            HomeQuickAction("收藏", Icons.Rounded.Favorite),
            HomeQuickAction("历史", Icons.Rounded.History)
        ),
        utilityStatusText = if (libraryState.totalSongCount == 0) {
            "还没有导入本地音乐"
        } else {
            "已导入 ${libraryState.totalSongCount} 首，${libraryState.librarySummaryText.ifBlank { libraryState.statusMessage }}"
        }
    )
}
