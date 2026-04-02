package com.relaxmusic.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.relaxmusic.app.data.db.dao.PlaybackHistoryDao
import com.relaxmusic.app.data.db.dao.PlaylistDao
import com.relaxmusic.app.data.db.dao.SettingsDao
import com.relaxmusic.app.data.db.dao.SongDao
import com.relaxmusic.app.data.db.entity.PlaybackHistoryEntity
import com.relaxmusic.app.data.db.entity.PlaylistEntity
import com.relaxmusic.app.data.db.entity.PlaylistSongEntity
import com.relaxmusic.app.data.db.entity.SettingsEntity
import com.relaxmusic.app.data.db.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        SettingsEntity::class,
        PlaybackHistoryEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun settingsDao(): SettingsDao
    abstract fun playbackHistoryDao(): PlaybackHistoryDao
    abstract fun playlistDao(): PlaylistDao
}
