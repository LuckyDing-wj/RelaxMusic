package com.relaxmusic.app.data.repository

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.relaxmusic.app.data.local.LyricLoader
import com.relaxmusic.app.data.player.MusicPlayerController
import com.relaxmusic.app.domain.model.PlayMode
import com.relaxmusic.app.domain.model.PlaybackState
import com.relaxmusic.app.domain.model.Song
import com.relaxmusic.app.domain.repository.PlayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class PlayerRepositoryImpl(
    private val playerController: MusicPlayerController
) : PlayerRepository {
    private var appContext: Context? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _playbackState = MutableStateFlow(PlaybackState())
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var queue: List<Song> = emptyList()
    private var progressJob: Job? = null
    private var currentSongObserver: ((Song?) -> Unit)? = null
    private val lyricLoader = LyricLoader()
    private var cachedLyricSongId: String? = null
    private var cachedLyrics = emptyList<com.relaxmusic.app.domain.model.LyricLine>()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateState()
            if (isPlaying) startProgressUpdates() else stopProgressUpdates()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            updateState()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateState(resetProgress = true)
        }
    }

    init {
        playerController.addListener(playerListener)
    }

    override fun bindContext(context: Context) {
        appContext = context.applicationContext
    }

    override fun playSong(song: Song, queue: List<Song>) {
        val startIndex = queue.indexOfFirst { it.id == song.id }.takeIf { it >= 0 } ?: 0
        this.queue = queue.ifEmpty { listOf(song) }
        val mediaItems = this.queue.map { queuedSong -> MediaItem.fromUri(queuedSong.uri) }
        playerController.setQueue(mediaItems, startIndex)
        updateState(resetProgress = true)
        startProgressUpdates()
    }

    override fun togglePlayPause() {
        playerController.togglePlayPause()
        updateState()
    }

    override fun playNext() {
        if (queue.size > 1 || _playbackState.value.playMode != PlayMode.SEQUENCE) {
            playerController.playNext()
            updateState(resetProgress = true)
        }
    }

    override fun playPrevious() {
        if (queue.size > 1 || _playbackState.value.playMode != PlayMode.SEQUENCE) {
            playerController.playPrevious()
            updateState(resetProgress = true)
        }
    }

    override fun seekTo(progress: Float) {
        val duration = _playbackState.value.durationMs
        playerController.seekTo((duration * progress.coerceIn(0f, 1f)).toLong())
        updateState()
    }

    override fun cyclePlayMode() {
        val nextMode = when (_playbackState.value.playMode) {
            PlayMode.SEQUENCE -> PlayMode.REPEAT_ONE
            PlayMode.REPEAT_ONE -> PlayMode.REPEAT_ALL
            PlayMode.REPEAT_ALL -> PlayMode.SHUFFLE
            PlayMode.SHUFFLE -> PlayMode.SEQUENCE
        }
        playerController.setPlayMode(nextMode)
        _playbackState.value = _playbackState.value.copy(playMode = nextMode)
        updateState()
    }

    override fun stop() {
        playerController.stop()
        stopProgressUpdates()
        updateState(resetProgress = true)
    }

    override fun removeFromQueue(songId: String) {
        val updatedQueue = queue.filterNot { it.id == songId }
        if (updatedQueue.isEmpty()) {
            queue = emptyList()
            stop()
            _playbackState.value = _playbackState.value.copy(queue = emptyList(), currentIndex = -1, currentSong = null)
            return
        }

        val currentSongId = currentSong()?.id
        queue = updatedQueue
        val nextSong = updatedQueue.firstOrNull { it.id == currentSongId } ?: updatedQueue.first()
        val mediaItems = queue.map { MediaItem.fromUri(it.uri) }
        val startIndex = queue.indexOfFirst { it.id == nextSong.id }.coerceAtLeast(0)
        playerController.setQueue(mediaItems, startIndex)
        updateState(resetProgress = true)
    }

    override fun playQueueSong(songId: String) {
        val song = queue.firstOrNull { it.id == songId } ?: return
        playSong(song, queue)
    }

    override fun release() {
        stopProgressUpdates()
        playerController.removeListener(playerListener)
        playerController.release()
    }

    override fun setCurrentSongObserver(observer: (Song?) -> Unit) {
        currentSongObserver = observer
        observer(currentSong())
    }

    private fun currentSong(): Song? {
        val index = playerController.currentIndex()
        return queue.getOrNull(index)
    }

    private fun updateState(resetProgress: Boolean = false) {
        val progressMs = if (resetProgress) playerController.currentPosition() else playerController.currentPosition()
        val current = currentSong()
        val lyrics = when {
            current == null -> {
                cachedLyricSongId = null
                cachedLyrics = emptyList()
                emptyList()
            }

            cachedLyricSongId == current.id -> cachedLyrics

            else -> {
                cachedLyricSongId = current.id
                cachedLyrics = appContext?.let { context ->
                    runBlocking { lyricLoader.load(context, current.uri, current.fileName) }
                }.orEmpty()
                cachedLyrics
            }
        }
        val currentLyricIndex = lyrics.indexOfLast { it.timeMs <= progressMs }
        _playbackState.value = _playbackState.value.copy(
            currentSong = current,
            isPlaying = playerController.isPlaying(),
            progressMs = progressMs,
            durationMs = playerController.duration().coerceAtLeast(current?.duration ?: 0L),
            queue = queue,
            currentIndex = playerController.currentIndex(),
            lyrics = lyrics,
            currentLyricIndex = currentLyricIndex
        )
        currentSongObserver?.invoke(current)
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                updateState()
                delay(500)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }
}
