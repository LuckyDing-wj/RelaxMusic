package com.relaxmusic.app.ui.screens.nowplaying

import com.relaxmusic.app.domain.model.PlayMode
import com.relaxmusic.app.domain.model.LyricLine
import com.relaxmusic.app.domain.model.Song

data class PlayerUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val progressMs: Long = 0L,
    val durationMs: Long = 0L,
    val playMode: PlayMode = PlayMode.SEQUENCE,
    val sleepTimerRemaining: Long = 0,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val lyrics: List<LyricLine> = emptyList(),
    val currentLyricIndex: Int = -1
)
