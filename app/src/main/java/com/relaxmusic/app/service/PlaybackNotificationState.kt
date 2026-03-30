package com.relaxmusic.app.service

import com.relaxmusic.app.domain.model.PlaybackState

internal data class PlaybackNotificationState(
    val songId: String?,
    val title: String,
    val subtitle: String,
    val isPlaying: Boolean
)

internal fun PlaybackState.toNotificationState(): PlaybackNotificationState {
    val song = currentSong
    return PlaybackNotificationState(
        songId = song?.id,
        title = song?.title ?: "RelaxMusic",
        subtitle = song?.let { "${it.artist} · ${it.album}" } ?: "本地音乐播放服务已就绪",
        isPlaying = isPlaying
    )
}
