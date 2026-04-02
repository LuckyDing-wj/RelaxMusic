package com.relaxmusic.app.domain.repository

import com.relaxmusic.app.domain.model.Album
import com.relaxmusic.app.domain.model.Artist
import com.relaxmusic.app.domain.model.Playlist
import com.relaxmusic.app.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    fun observeSongs(): Flow<List<Song>>
    fun searchSongs(query: String): Flow<List<Song>>
    fun observeFavorites(): Flow<List<Song>>
    fun observeRecentlyPlayed(limit: Int = 10): Flow<List<Song>>
    fun observePlaybackHistory(limit: Int = 30): Flow<List<Song>>
    fun observeAlbums(): Flow<List<Album>>
    fun observeArtists(): Flow<List<Artist>>
    fun observePlaylists(): Flow<List<Playlist>>
    fun observePlaylistSongs(playlistId: Long): Flow<List<Song>>
    suspend fun getSavedLibraryUris(): List<String>
    suspend fun getSavedLibraryUri(): String?
    suspend fun saveLibraryUri(uri: String)
    suspend fun removeLibraryUri(uri: String)
    suspend fun rescanLibrary(): Result<Int>
    suspend fun toggleFavorite(songId: String)
    suspend fun markSongPlayed(songId: String)
    suspend fun createPlaylist(name: String)
    suspend fun addSongToPlaylist(playlistId: Long, songId: String)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String)
    suspend fun renamePlaylist(playlistId: Long, name: String)
    suspend fun deletePlaylist(playlistId: Long)
}
