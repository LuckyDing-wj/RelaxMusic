package com.relaxmusic.app.data.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.relaxmusic.app.domain.model.PlayMode

class MusicPlayerController(context: Context) {
    private val player = ExoPlayer.Builder(context).build()
    val exoPlayer: ExoPlayer
        get() = player

    fun setQueue(mediaItems: List<MediaItem>, startIndex: Int) {
        player.setMediaItems(mediaItems, startIndex, 0L)
        player.prepare()
        player.play()
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs.coerceAtLeast(0L))
    }

    fun playNext() {
        player.seekToNextMediaItem()
    }

    fun playPrevious() {
        player.seekToPreviousMediaItem()
    }

    fun stop() {
        player.pause()
        player.seekTo(0L)
    }

    fun setPlayMode(mode: PlayMode) {
        when (mode) {
            PlayMode.SEQUENCE -> {
                player.repeatMode = Player.REPEAT_MODE_OFF
                player.shuffleModeEnabled = false
            }

            PlayMode.REPEAT_ONE -> {
                player.repeatMode = Player.REPEAT_MODE_ONE
                player.shuffleModeEnabled = false
            }

            PlayMode.REPEAT_ALL -> {
                player.repeatMode = Player.REPEAT_MODE_ALL
                player.shuffleModeEnabled = false
            }

            PlayMode.SHUFFLE -> {
                player.repeatMode = Player.REPEAT_MODE_ALL
                player.shuffleModeEnabled = true
            }
        }
    }

    fun currentIndex(): Int = player.currentMediaItemIndex

    fun isPlaying(): Boolean = player.isPlaying

    fun currentPosition(): Long = player.currentPosition

    fun duration(): Long = player.duration.takeIf { it >= 0 } ?: 0L

    fun addListener(listener: Player.Listener) {
        player.addListener(listener)
    }

    fun removeListener(listener: Player.Listener) {
        player.removeListener(listener)
    }

    fun release() {
        player.release()
    }
}
