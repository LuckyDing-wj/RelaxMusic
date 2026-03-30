package com.relaxmusic.app.service

import android.app.PendingIntent
import android.app.NotificationManager
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.getSystemService
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.relaxmusic.app.RelaxMusicApplication
import com.relaxmusic.app.MainActivity
import com.relaxmusic.app.domain.model.PlaybackState
import com.relaxmusic.app.domain.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var playbackState: PlaybackState = PlaybackState()
    private var notificationState: PlaybackNotificationState? = null
    private var isForeground = false

    override fun onCreate() {
        super.onCreate()
        val app = application as RelaxMusicApplication
        mediaSession = MediaSession.Builder(this, app.appContainer.musicPlayerController.exoPlayer).build()
        serviceScope.launch {
            app.appContainer.playerRepository.playbackState.collectLatest { state ->
                val nextNotificationState = state.toNotificationState()
                val shouldRefreshNotification = nextNotificationState != notificationState
                playbackState = state
                if (shouldRefreshNotification) {
                    notificationState = nextNotificationState
                    refreshNotification()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = application as RelaxMusicApplication
        when (intent?.action) {
            PlaybackAction.PLAY_PAUSE -> app.appContainer.playerRepository.togglePlayPause()
            PlaybackAction.NEXT -> app.appContainer.playerRepository.playNext()
            PlaybackAction.PREVIOUS -> app.appContainer.playerRepository.playPrevious()
            PlaybackAction.STOP -> app.appContainer.playerRepository.stop()
        }
        return START_NOT_STICKY
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        serviceScope.cancel()
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }

    private fun refreshNotification() {
        val shouldShowNotification = playbackState.currentSong != null

        if (!shouldShowNotification) {
            if (isForeground) {
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                isForeground = false
            } else {
                notificationManager()?.cancel(PlaybackNotification.NOTIFICATION_ID)
            }
            notificationState = null
            stopSelf()
            return
        }

        val notification = buildNotification(playbackState.currentSong, playbackState.isPlaying)
        startForeground(PlaybackNotification.NOTIFICATION_ID, notification)
        isForeground = true
    }

    private fun buildNotification(currentSong: Song?, isPlaying: Boolean) = NotificationCompat.Builder(this, PlaybackNotification.CHANNEL_ID)
        .setContentTitle(currentSong?.title ?: "RelaxMusic")
        .setContentText(currentSong?.let { "${it.artist} · ${it.album}" } ?: "本地音乐播放服务已就绪")
        .setSmallIcon(android.R.drawable.ic_media_play)
        .setContentIntent(contentIntent())
        .addAction(
            android.R.drawable.ic_media_previous,
            "上一首",
            actionIntent(PlaybackAction.PREVIOUS, 2)
        )
        .addAction(
            if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
            if (isPlaying) "暂停" else "播放",
            actionIntent(PlaybackAction.PLAY_PAUSE, 3)
        )
        .addAction(
            android.R.drawable.ic_media_next,
            "下一首",
            actionIntent(PlaybackAction.NEXT, 4)
        )
        .setContentIntent(contentIntent())
        .setStyle(androidx.media.app.NotificationCompat.MediaStyle())
        .setOngoing(isPlaying)
        .setOnlyAlertOnce(true)
        .build()

    private fun notificationManager(): NotificationManager? = getSystemService()

    private fun contentIntent(): PendingIntent {
        return PendingIntent.getActivity(
            this,
            1,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun actionIntent(action: String, requestCode: Int): PendingIntent {
        return PendingIntent.getService(
            this,
            requestCode,
            Intent(this, PlaybackService::class.java).apply { this.action = action },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
