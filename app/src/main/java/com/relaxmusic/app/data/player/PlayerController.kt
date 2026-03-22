package com.relaxmusic.app.data.player

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.relaxmusic.app.domain.model.PlayMode

interface PlayerController {
    fun setQueue(mediaItems: List<MediaItem>, startIndex: Int)
    fun replaceQueue(mediaItems: List<MediaItem>, startIndex: Int, startPositionMs: Long, shouldPlay: Boolean)
    fun togglePlayPause()
    fun seekTo(positionMs: Long)
    fun playNext()
    fun playPrevious()
    fun stop()
    fun setPlayMode(mode: PlayMode)
    fun currentIndex(): Int
    fun isPlaying(): Boolean
    fun currentPosition(): Long
    fun duration(): Long
    fun addListener(listener: Player.Listener)
    fun removeListener(listener: Player.Listener)
    fun release()
}
