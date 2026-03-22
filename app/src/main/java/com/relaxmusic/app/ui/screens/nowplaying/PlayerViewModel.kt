package com.relaxmusic.app.ui.screens.nowplaying

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.relaxmusic.app.domain.model.PlaybackState
import com.relaxmusic.app.domain.model.Song
import com.relaxmusic.app.domain.repository.PlayerRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val playerRepository: PlayerRepository
) : ViewModel() {
    var uiState by mutableStateOf(PlayerUiState())
        private set

    var miniPlayerSong by mutableStateOf<Song?>(null)
        private set

    var miniPlayerIsPlaying by mutableStateOf(false)
        private set

    var miniPlayerProgress by mutableStateOf(0f)
        private set

    private var sleepTimerJob: Job? = null

    init {
        viewModelScope.launch {
            playerRepository.playbackState.collectLatest { state ->
                val sleepRemaining = uiState.sleepTimerRemaining
                uiState = state.toUiState(uiState.sleepTimerRemaining)
                miniPlayerSong = state.currentSong
                miniPlayerIsPlaying = state.isPlaying
                miniPlayerProgress = if (state.durationMs > 0) {
                    state.progressMs.coerceAtLeast(0L).toFloat() / state.durationMs.toFloat()
                } else {
                    0f
                }
                if (sleepRemaining != uiState.sleepTimerRemaining) {
                    uiState = uiState.copy(sleepTimerRemaining = sleepRemaining)
                }
            }
        }
    }

    fun bindContext(context: Context) {
        playerRepository.bindContext(context)
    }

    fun playSong(song: Song, queue: List<Song>) {
        playerRepository.playSong(song, queue)
    }

    fun togglePlay() {
        playerRepository.togglePlayPause()
    }

    fun next() {
        playerRepository.playNext()
    }

    fun previous() {
        playerRepository.playPrevious()
    }

    fun seekTo(progress: Float) {
        playerRepository.seekTo(progress)
    }

    fun cyclePlayMode() {
        playerRepository.cyclePlayMode()
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        val totalSeconds = minutes * 60L
        uiState = uiState.copy(sleepTimerRemaining = totalSeconds)
        sleepTimerJob = viewModelScope.launch {
            var remain = totalSeconds
            while (remain > 0) {
                delay(1_000)
                remain -= 1
                uiState = uiState.copy(sleepTimerRemaining = remain)
            }
            playerRepository.stop()
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        uiState = uiState.copy(sleepTimerRemaining = 0)
    }

    fun removeFromQueue(songId: String) {
        playerRepository.removeFromQueue(songId)
    }

    fun playQueueSong(songId: String) {
        playerRepository.playQueueSong(songId)
    }

    override fun onCleared() {
        sleepTimerJob?.cancel()
        super.onCleared()
    }

    private fun PlaybackState.toUiState(sleepTimerRemaining: Long): PlayerUiState {
        val safeDuration = durationMs.coerceAtLeast(0L)
        val safeProgress = progressMs.coerceAtLeast(0L)
        return PlayerUiState(
            currentSong = currentSong,
            isPlaying = isPlaying,
            progress = if (safeDuration > 0) safeProgress.toFloat() / safeDuration.toFloat() else 0f,
            progressMs = safeProgress,
            durationMs = safeDuration,
            playMode = playMode,
            sleepTimerRemaining = sleepTimerRemaining,
            queue = queue,
            currentIndex = currentIndex,
            lyrics = lyrics,
            currentLyricIndex = currentLyricIndex
        )
    }
}
