package com.relaxmusic.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.relaxmusic.app.data.db.entity.PlaylistEntity
import com.relaxmusic.app.data.db.entity.PlaylistSongEntity
import com.relaxmusic.app.data.db.entity.SongEntity
import kotlinx.coroutines.flow.Flow

data class PlaylistWithCount(
    val id: Long,
    val name: String,
    val songCount: Int
)

@Dao
interface PlaylistDao {
    @Query(
        """
        SELECT p.id AS id, p.name AS name, COUNT(ps.song_id) AS songCount
        FROM playlists p
        LEFT JOIN playlist_songs ps ON p.id = ps.playlist_id
        GROUP BY p.id, p.name
        ORDER BY p.name ASC
        """
    )
    fun observePlaylists(): Flow<List<PlaylistWithCount>>

    @Query("SELECT * FROM playlists ORDER BY name ASC")
    suspend fun observePlaylistsSnapshot(): List<PlaylistEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(entity: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :name WHERE id = :playlistId")
    suspend fun renamePlaylist(playlistId: Long, name: String)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongToPlaylist(entity: PlaylistSongEntity)

    @Query("SELECT playlist_id FROM playlist_songs WHERE song_id = :songId")
    fun observePlaylistIdsForSong(songId: String): Flow<List<Long>>

    @Query(
        """
        SELECT s.* FROM songs s
        INNER JOIN playlist_songs ps ON s.id = ps.song_id
        WHERE ps.playlist_id = :playlistId
        ORDER BY s.title ASC
        """
    )
    fun observePlaylistSongs(playlistId: Long): Flow<List<SongEntity>>

    @Query("SELECT song_id FROM playlist_songs WHERE playlist_id = :playlistId")
    suspend fun getPlaylistSongIds(playlistId: Long): List<String>

    @Query("DELETE FROM playlist_songs WHERE playlist_id = :playlistId AND song_id = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String)

    @Query("DELETE FROM playlists")
    suspend fun clearAllPlaylists()
}
