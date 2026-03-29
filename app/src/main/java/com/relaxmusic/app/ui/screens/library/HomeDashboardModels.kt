package com.relaxmusic.app.ui.screens.library

import com.relaxmusic.app.domain.model.Song

data class HomeDashboardModel(
    val continueTitle: String,
    val continueSubtitle: String,
    val continueProgress: Float,
    val showRecentSection: Boolean,
    val recentSongs: List<Song>,
    val recentSubtitle: String
)

internal fun buildHomeDashboardModel(
    libraryState: LibraryUiState,
    currentSong: Song?,
    isPlaying: Boolean,
    playbackProgress: Float
): HomeDashboardModel {
    val recentSongs = libraryState.recentSongs.ifEmpty { libraryState.historySongs }.take(5)
    return HomeDashboardModel(
        continueTitle = currentSong?.title ?: "还没有开始播放",
        continueSubtitle = when {
            currentSong == null -> "从曲库中选择一首歌开始"
            isPlaying -> currentSong.artist
            else -> "${currentSong.artist} · 已暂停"
        },
        continueProgress = if (currentSong != null) playbackProgress.coerceIn(0f, 1f) else 0f,
        showRecentSection = recentSongs.isNotEmpty(),
        recentSongs = recentSongs,
        recentSubtitle = when {
            recentSongs.isEmpty() -> "播放后会出现在这里"
            recentSongs.size == 1 -> recentSongs.first().title
            else -> recentSongs.take(2).joinToString(" / ") { it.title }
        }
    )
}
