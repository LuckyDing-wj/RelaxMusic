package com.relaxmusic.app.service

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.relaxmusic.app.RelaxMusicApplication
import com.relaxmusic.app.MainActivity
import com.relaxmusic.app.domain.model.Song

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private var currentSong: Song? = null

    override fun onCreate() {
        super.onCreate()
        val app = application as RelaxMusicApplication
        mediaSession = MediaSession.Builder(this, app.appContainer.musicPlayerController.exoPlayer).build()
        app.appContainer.observeCurrentSong { song ->
            currentSong = song
            refreshNotification()
        }
        startForeground(PlaybackNotification.NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = application as RelaxMusicApplication
        when (intent?.action) {
            PlaybackAction.PLAY_PAUSE -> app.appContainer.playerRepository.togglePlayPause()
            PlaybackAction.NEXT -> app.appContainer.playerRepository.playNext()
            PlaybackAction.PREVIOUS -> app.appContainer.playerRepository.playPrevious()
            PlaybackAction.STOP -> app.appContainer.playerRepository.stop()
        }
        refreshNotification()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }

    private fun refreshNotification() {
        startForeground(PlaybackNotification.NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification() = NotificationCompat.Builder(this, PlaybackNotification.CHANNEL_ID)
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
            android.R.drawable.ic_media_pause,
            "播放/暂停",
            actionIntent(PlaybackAction.PLAY_PAUSE, 3)
        )
        .addAction(
            android.R.drawable.ic_media_next,
            "下一首",
            actionIntent(PlaybackAction.NEXT, 4)
        )
        .setContentIntent(contentIntent())
        .setStyle(androidx.media.app.NotificationCompat.MediaStyle())
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .build()

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
