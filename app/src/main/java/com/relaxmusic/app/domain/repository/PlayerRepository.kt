package com.relaxmusic.app.domain.repository

import com.relaxmusic.app.domain.model.PlayMode
import com.relaxmusic.app.domain.model.PlaybackState
import com.relaxmusic.app.domain.model.Song
import android.content.Context
import kotlinx.coroutines.flow.StateFlow

interface PlayerRepository {
    val playbackState: StateFlow<PlaybackState>
    fun bindContext(context: Context)
    fun playSong(song: Song, queue: List<Song>)
    fun togglePlayPause()
    fun playNext()
    fun playPrevious()
    fun seekTo(progress: Float)
    fun cyclePlayMode()
    fun stop()
    fun removeFromQueue(songId: String)
    fun playQueueSong(songId: String)
    fun release()
    fun setCurrentSongObserver(observer: (Song?) -> Unit)
}
