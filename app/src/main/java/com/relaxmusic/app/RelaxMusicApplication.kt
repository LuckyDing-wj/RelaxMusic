package com.relaxmusic.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Room
import com.relaxmusic.app.data.db.AppDatabase
import com.relaxmusic.app.data.local.AppBackupManager
import com.relaxmusic.app.data.local.DirectoryScanner
import com.relaxmusic.app.data.player.MusicPlayerController
import com.relaxmusic.app.data.repository.LibraryRepositoryImpl
import com.relaxmusic.app.data.repository.PlayerRepositoryImpl
import com.relaxmusic.app.data.repository.SettingsRepositoryImpl
import com.relaxmusic.app.domain.repository.LibraryRepository
import com.relaxmusic.app.domain.repository.PlayerRepository
import com.relaxmusic.app.domain.repository.SettingsRepository
import com.relaxmusic.app.domain.model.Song
import com.relaxmusic.app.service.PlaybackNotification

class RelaxMusicApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = DefaultAppContainer(this)
        createPlaybackNotificationChannel()
    }

    private fun createPlaybackNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            PlaybackNotification.CHANNEL_ID,
            PlaybackNotification.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)
    }
}

interface AppContainer {
    val libraryRepository: LibraryRepository
    val playerRepository: PlayerRepository
    val settingsRepository: SettingsRepository
    val musicPlayerController: MusicPlayerController
    val backupManager: AppBackupManager
    fun observeCurrentSong(observer: (Song?) -> Unit)
}

private class DefaultAppContainer(application: Application) : AppContainer {
    private val migration1To2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE songs ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE songs ADD COLUMN last_played_at INTEGER")
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS playback_history (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, song_id TEXT NOT NULL, played_at INTEGER NOT NULL)"
            )
        }
    }

    private val migration2To3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS playlists (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL)"
            )
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS playlist_songs (playlist_id INTEGER NOT NULL, song_id TEXT NOT NULL, PRIMARY KEY(playlist_id, song_id), FOREIGN KEY(playlist_id) REFERENCES playlists(id) ON DELETE CASCADE, FOREIGN KEY(song_id) REFERENCES songs(id) ON DELETE CASCADE)"
            )
        }
    }

    private val migration3To4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE INDEX IF NOT EXISTS index_playlist_songs_song_id ON playlist_songs(song_id)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_playlist_songs_playlist_id ON playlist_songs(playlist_id)")
        }
    }

    private val database: AppDatabase = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "relax_music.db"
    ).addMigrations(migration1To2, migration2To3, migration3To4).build()

    override val musicPlayerController: MusicPlayerController = MusicPlayerController(application)

    override val backupManager: AppBackupManager = AppBackupManager(
        context = application,
        settingsDao = database.settingsDao(),
        playlistDao = database.playlistDao()
    )

    override val libraryRepository: LibraryRepository = LibraryRepositoryImpl(
        context = application,
        songDao = database.songDao(),
        settingsDao = database.settingsDao(),
        playbackHistoryDao = database.playbackHistoryDao(),
        playlistDao = database.playlistDao(),
        directoryScanner = DirectoryScanner()
    )

    override val playerRepository: PlayerRepository = PlayerRepositoryImpl(
        playerController = musicPlayerController
    )

    override val settingsRepository: SettingsRepository = SettingsRepositoryImpl(
        settingsDao = database.settingsDao()
    )

    override fun observeCurrentSong(observer: (Song?) -> Unit) {
        playerRepository.setCurrentSongObserver(observer)
    }
}
