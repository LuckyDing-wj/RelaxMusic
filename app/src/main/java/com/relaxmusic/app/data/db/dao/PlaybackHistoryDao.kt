package com.relaxmusic.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.relaxmusic.app.data.db.entity.PlaybackHistoryEntity
import com.relaxmusic.app.data.db.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaybackHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PlaybackHistoryEntity)

    @Query("SELECT * FROM playback_history ORDER BY played_at DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<PlaybackHistoryEntity>>

    @Query(
        """
        SELECT s.* FROM playback_history ph
        INNER JOIN songs s ON s.id = ph.song_id
        ORDER BY ph.played_at DESC
        LIMIT :limit
        """
    )
    fun observeRecentSongs(limit: Int): Flow<List<SongEntity>>
}
