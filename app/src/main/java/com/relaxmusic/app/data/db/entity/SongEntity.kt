package com.relaxmusic.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [
        Index("title"),
        Index("artist"),
        Index("album"),
        Index("is_favorite"),
        Index("last_played_at")
    ]
)
data class SongEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "uri") val uri: String,
    @ColumnInfo(name = "file_name") val fileName: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val size: Long,
    @ColumnInfo(name = "modified_at") val modifiedAt: Long,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean,
    @ColumnInfo(name = "last_played_at") val lastPlayedAt: Long?
)
