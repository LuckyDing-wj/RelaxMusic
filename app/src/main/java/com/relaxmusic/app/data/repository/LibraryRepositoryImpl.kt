package com.relaxmusic.app.data.repository

import android.content.Context
import android.net.Uri
import com.relaxmusic.app.data.db.dao.PlaybackHistoryDao
import com.relaxmusic.app.data.db.dao.PlaylistDao
import com.relaxmusic.app.data.db.dao.SettingsDao
import com.relaxmusic.app.data.db.dao.SongDao
import com.relaxmusic.app.data.db.entity.PlaybackHistoryEntity
import com.relaxmusic.app.data.db.entity.PlaylistEntity
import com.relaxmusic.app.data.db.entity.PlaylistSongEntity
import com.relaxmusic.app.data.db.entity.SettingsEntity
import com.relaxmusic.app.data.db.entity.SongEntity
import com.relaxmusic.app.data.local.DirectoryScanner
import com.relaxmusic.app.domain.model.Album
import com.relaxmusic.app.domain.model.Artist
import com.relaxmusic.app.domain.model.Playlist
import com.relaxmusic.app.domain.model.Song
import com.relaxmusic.app.domain.repository.LibraryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LibraryRepositoryImpl(
    private val context: Context,
    private val songDao: SongDao,
    private val settingsDao: SettingsDao,
    private val playbackHistoryDao: PlaybackHistoryDao,
    private val playlistDao: PlaylistDao,
    private val directoryScanner: DirectoryScanner
) : LibraryRepository {

    override fun observeSongs(): Flow<List<Song>> {
        return songDao.observeAll().map { list -> list.map { it.toDomain() } }
    }

    override fun observeFavorites(): Flow<List<Song>> {
        return songDao.observeFavorites().map { list -> list.map { it.toDomain() } }
    }

    override fun observeRecentlyPlayed(limit: Int): Flow<List<Song>> {
        return songDao.observeRecentlyPlayed(limit).map { list -> list.map { it.toDomain() } }
    }

    override fun observePlaybackHistory(limit: Int): Flow<List<Song>> {
        return playbackHistoryDao.observeRecentSongs(limit).map { list -> list.map { it.toDomain() } }
    }

    override fun observeAlbums(): Flow<List<Album>> {
        return observeSongs().map { songs ->
            songs.groupBy { it.album to it.artist }
                .map { (key, groupedSongs) -> Album(name = key.first, artist = key.second, songs = groupedSongs) }
                .sortedBy { it.name.lowercase() }
        }
    }

    override fun observeArtists(): Flow<List<Artist>> {
        return observeSongs().map { songs ->
            songs.groupBy { it.artist }
                .map { (artistName, groupedSongs) -> Artist(name = artistName, songs = groupedSongs) }
                .sortedBy { it.name.lowercase() }
        }
    }

    override fun observePlaylists(): Flow<List<Playlist>> {
        return playlistDao.observePlaylists().map { list ->
            list.map { Playlist(id = it.id, name = it.name, songCount = it.songCount) }
        }
    }

    override fun observePlaylistSongs(playlistId: Long): Flow<List<Song>> {
        return playlistDao.observePlaylistSongs(playlistId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getSavedLibraryUri(): String? {
        return getSavedLibraryUris().firstOrNull()
    }

    override suspend fun getSavedLibraryUris(): List<String> {
        return withContext(Dispatchers.IO) {
            val multi = settingsDao.get(KEY_LIBRARY_URIS)
                ?.value
                ?.split(SEPARATOR)
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                .orEmpty()
            if (multi.isNotEmpty()) return@withContext multi

            val legacy = settingsDao.get(KEY_LIBRARY_URI)?.value?.takeIf { it.isNotBlank() }
            if (legacy != null) listOf(legacy) else emptyList()
        }
    }

    override suspend fun saveLibraryUri(uri: String) {
        withContext(Dispatchers.IO) {
            val updated = (getSavedLibraryUris() + uri).distinct()
            settingsDao.put(SettingsEntity(key = KEY_LIBRARY_URIS, value = updated.joinToString(SEPARATOR)))
            settingsDao.put(SettingsEntity(key = KEY_LIBRARY_URI, value = updated.firstOrNull().orEmpty()))
        }
    }

    override suspend fun removeLibraryUri(uri: String) {
        withContext(Dispatchers.IO) {
            val updated = getSavedLibraryUris().filterNot { it == uri }
            settingsDao.put(SettingsEntity(key = KEY_LIBRARY_URIS, value = updated.joinToString(SEPARATOR)))
            settingsDao.put(SettingsEntity(key = KEY_LIBRARY_URI, value = updated.firstOrNull().orEmpty()))
        }
    }

    override suspend fun rescanLibrary(): Result<Int> {
        val saved = getSavedLibraryUris()
        if (saved.isEmpty()) return Result.failure(IllegalStateException("No library selected"))
        return runCatching {
            withContext(Dispatchers.IO) {
                val existingById = songDao.getAll().associateBy { it.id }
                val songs = saved
                    .flatMap { uri -> directoryScanner.scan(context, Uri.parse(uri)) }
                    .distinctBy { it.id }
                    .map { song ->
                        val existing = existingById[song.id]
                        song.copy(
                            isFavorite = existing?.isFavorite ?: song.isFavorite,
                            lastPlayedAt = existing?.lastPlayedAt ?: song.lastPlayedAt
                        )
                    }

                if (songs.isEmpty()) {
                    songDao.clear()
                } else {
                    songDao.upsertAll(songs.map { it.toEntity() })
                    songDao.deleteMissing(songs.map { it.id })
                }
                songs.size
            }
        }
    }

    override suspend fun toggleFavorite(songId: String) {
        withContext(Dispatchers.IO) {
            songDao.toggleFavorite(songId)
        }
    }

    override suspend fun markSongPlayed(songId: String) {
        withContext(Dispatchers.IO) {
            val playedAt = System.currentTimeMillis()
            songDao.markPlayed(songId, playedAt)
            playbackHistoryDao.insert(PlaybackHistoryEntity(songId = songId, playedAt = playedAt))
        }
    }

    override suspend fun createPlaylist(name: String) {
        withContext(Dispatchers.IO) {
            val trimmed = name.trim()
            if (trimmed.isNotEmpty()) {
                playlistDao.insertPlaylist(PlaylistEntity(name = trimmed))
            }
        }
    }

    override suspend fun renamePlaylist(playlistId: Long, name: String) {
        withContext(Dispatchers.IO) {
            val trimmed = name.trim()
            if (trimmed.isNotEmpty()) {
                playlistDao.renamePlaylist(playlistId, trimmed)
            }
        }
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        withContext(Dispatchers.IO) {
            playlistDao.deletePlaylist(playlistId)
        }
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: String) {
        withContext(Dispatchers.IO) {
            playlistDao.addSongToPlaylist(PlaylistSongEntity(playlistId = playlistId, songId = songId))
        }
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        withContext(Dispatchers.IO) {
            playlistDao.removeSongFromPlaylist(playlistId, songId)
        }
    }

    private fun SongEntity.toDomain(): Song = Song(
        id = id,
        uri = uri,
        fileName = fileName,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        size = size,
        modifiedAt = modifiedAt,
        isFavorite = isFavorite,
        lastPlayedAt = lastPlayedAt
    )

    private fun Song.toEntity(): SongEntity = SongEntity(
        id = id,
        uri = uri,
        fileName = fileName,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        size = size,
        modifiedAt = modifiedAt,
        isFavorite = isFavorite,
        lastPlayedAt = lastPlayedAt
    )

    private companion object {
        const val KEY_LIBRARY_URI = "library_tree_uri"
        const val KEY_LIBRARY_URIS = "library_tree_uris"
        const val SEPARATOR = "||"
    }
}
