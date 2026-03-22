package com.relaxmusic.app.data.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.relaxmusic.app.domain.model.PlayMode

class MusicPlayerController(context: Context) : PlayerController {
    private val player = ExoPlayer.Builder(context).build()
    val exoPlayer: ExoPlayer
        get() = player

    override fun setQueue(mediaItems: List<MediaItem>, startIndex: Int) {
        player.setMediaItems(mediaItems, startIndex, 0L)
        player.prepare()
        player.play()
    }

    override fun replaceQueue(
        mediaItems: List<MediaItem>,
        startIndex: Int,
        startPositionMs: Long,
        shouldPlay: Boolean
    ) {
        player.setMediaItems(mediaItems, startIndex, startPositionMs.coerceAtLeast(0L))
        player.prepare()
        player.playWhenReady = shouldPlay
    }

    override fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    override fun seekTo(positionMs: Long) {
        player.seekTo(positionMs.coerceAtLeast(0L))
    }

    override fun playNext() {
        player.seekToNextMediaItem()
    }

    override fun playPrevious() {
        player.seekToPreviousMediaItem()
    }

    override fun stop() {
        player.stop()
        player.clearMediaItems()
    }

    override fun setPlayMode(mode: PlayMode) {
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

    override fun currentIndex(): Int = player.currentMediaItemIndex

    override fun isPlaying(): Boolean = player.isPlaying

    override fun currentPosition(): Long = player.currentPosition

    override fun duration(): Long = player.duration.takeIf { it >= 0 } ?: 0L

    override fun addListener(listener: Player.Listener) {
        player.addListener(listener)
    }

    override fun removeListener(listener: Player.Listener) {
        player.removeListener(listener)
    }

    override fun release() {
        player.release()
    }
}
