package com.relaxmusic.app.ui.screens.library

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.relaxmusic.app.domain.model.Playlist
import com.relaxmusic.app.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val libraryRepository: LibraryRepository
) : ViewModel() {
    private val libraryPathLabel = MutableStateFlow("还未选择目录")
    private val libraryDirectories = MutableStateFlow<List<String>>(emptyList())
    private val scanningDirectoryLabel = MutableStateFlow<String?>(null)
    private val scanning = MutableStateFlow(false)
    private val query = MutableStateFlow("")
    private val statusMessage = MutableStateFlow("请选择一个或多个本地音乐目录开始。")
    private val errorMessage = MutableStateFlow<String?>(null)
    private val currentSongId = MutableStateFlow<String?>(null)
    private val selectedPlaylistId = MutableStateFlow<Long?>(null)
    private val selectedAlbumName = MutableStateFlow<String?>(null)
    private val selectedArtistName = MutableStateFlow<String?>(null)

    private data class LibraryTransientState(
        val path: String,
        val directories: List<String>,
        val scanningDirectoryLabel: String?,
        val isScanning: Boolean,
        val queryValue: String,
        val status: String,
        val error: String?,
        val playingId: String?,
        val selectedPlaylistId: Long?,
        val selectedAlbumName: String?,
        val selectedArtistName: String?
    )

    private val transientState = combine(
        libraryPathLabel,
        libraryDirectories,
        scanningDirectoryLabel,
        scanning,
        query,
        statusMessage,
        errorMessage,
        currentSongId,
        selectedPlaylistId,
        selectedAlbumName,
        selectedArtistName
    ) { values ->
        LibraryTransientState(
            path = values[0] as String,
            directories = values[1] as List<String>,
            scanningDirectoryLabel = values[2] as String?,
            isScanning = values[3] as Boolean,
            queryValue = values[4] as String,
            status = values[5] as String,
            error = values[6] as String?,
            playingId = values[7] as String?,
            selectedPlaylistId = values[8] as Long?,
            selectedAlbumName = values[9] as String?,
            selectedArtistName = values[10] as String?
        )
    }

    private val selectedPlaylistSongs = selectedPlaylistId.flatMapLatest { playlistId ->
        if (playlistId == null) flowOf(emptyList()) else libraryRepository.observePlaylistSongs(playlistId)
    }

    val state = combine(
        libraryRepository.observeSongs(),
        libraryRepository.observeFavorites(),
        libraryRepository.observeRecentlyPlayed(),
        libraryRepository.observePlaybackHistory(),
        libraryRepository.observeAlbums(),
        libraryRepository.observeArtists(),
        libraryRepository.observePlaylists(),
        selectedPlaylistSongs,
        transientState
    ) { values ->
        val songs = values[0] as List<com.relaxmusic.app.domain.model.Song>
        val favorites = values[1] as List<com.relaxmusic.app.domain.model.Song>
        val recentSongs = values[2] as List<com.relaxmusic.app.domain.model.Song>
        val historySongs = values[3] as List<com.relaxmusic.app.domain.model.Song>
        val albums = values[4] as List<com.relaxmusic.app.domain.model.Album>
        val artists = values[5] as List<com.relaxmusic.app.domain.model.Artist>
        val playlists = values[6] as List<com.relaxmusic.app.domain.model.Playlist>
        val playlistSongs = values[7] as List<com.relaxmusic.app.domain.model.Song>
        val transient = values[8] as LibraryTransientState

        val queryValue = transient.queryValue
        val filteredSongs = if (queryValue.isBlank()) {
            songs
        } else {
            val keyword = queryValue.trim().lowercase()
            songs.filter {
                it.title.lowercase().contains(keyword) ||
                    it.artist.lowercase().contains(keyword) ||
                    it.album.lowercase().contains(keyword) ||
                    it.fileName.lowercase().contains(keyword)
            }
        }

        LibraryUiState(
            libraryPathLabel = transient.path,
            libraryDirectories = transient.directories,
            libraryDirectoryLabels = transient.directories.associateWith(::formatFolderLabel),
            scanningDirectoryLabel = transient.scanningDirectoryLabel,
            songs = filteredSongs,
            favoriteSongs = favorites,
            recentSongs = recentSongs.filter { it.lastPlayedAt != null },
            historySongs = historySongs.filter { it.lastPlayedAt != null },
            albums = albums,
            artists = artists,
            playlists = playlists,
            totalSongCount = songs.size,
            scanning = transient.isScanning,
            query = queryValue,
            statusMessage = transient.status,
            errorMessage = transient.error,
            currentSongId = transient.playingId,
            selectedPlaylistId = transient.selectedPlaylistId,
            selectedPlaylistSongs = playlistSongs,
            selectedAlbumName = transient.selectedAlbumName,
            selectedArtistName = transient.selectedArtistName
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState()
    )

    init {
        refreshSavedDirectories()
    }

    fun onFolderPicked(uri: String) {
        viewModelScope.launch {
            libraryRepository.saveLibraryUri(uri)
            refreshSavedDirectories()
            statusMessage.value = "已添加音乐目录，开始扫描本地音乐。"
            errorMessage.value = null
            rescan()
        }
    }

    fun removeFolder(uri: String) {
        viewModelScope.launch {
            libraryRepository.removeLibraryUri(uri)
            refreshSavedDirectories()
            statusMessage.value = if (libraryDirectories.value.isEmpty()) "已移除所有目录。" else "目录已移除。"
            if (libraryDirectories.value.isNotEmpty()) {
                rescan()
            }
        }
    }

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun setCurrentSong(songId: String?) {
        currentSongId.value = songId
    }

    fun toggleFavorite(songId: String) {
        viewModelScope.launch { libraryRepository.toggleFavorite(songId) }
    }

    fun markSongPlayed(songId: String) {
        viewModelScope.launch { libraryRepository.markSongPlayed(songId) }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch { libraryRepository.createPlaylist(name) }
    }

    fun renamePlaylist(playlistId: Long, name: String) {
        viewModelScope.launch { libraryRepository.renamePlaylist(playlistId, name) }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            libraryRepository.deletePlaylist(playlistId)
            if (selectedPlaylistId.value == playlistId) selectedPlaylistId.value = null
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch { libraryRepository.addSongToPlaylist(playlistId, songId) }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch { libraryRepository.removeSongFromPlaylist(playlistId, songId) }
    }

    fun selectPlaylist(playlist: Playlist?) {
        selectedPlaylistId.value = playlist?.id
    }

    fun selectAlbum(name: String?) {
        selectedAlbumName.value = name
    }

    fun selectArtist(name: String?) {
        selectedArtistName.value = name
    }

    fun rescan() {
        viewModelScope.launch {
            scanning.value = true
            errorMessage.value = null

            val directories = libraryRepository.getSavedLibraryUris()
            if (directories.isEmpty()) {
                scanning.value = false
                statusMessage.value = "还没有可扫描的目录。"
                return@launch
            }

            directories.forEachIndexed { index, uri ->
                val label = formatFolderLabel(uri)
                scanningDirectoryLabel.value = label
                statusMessage.value = "正在扫描第 ${index + 1}/${directories.size} 个目录: $label"
            }

            val result = libraryRepository.rescanLibrary()
            scanning.value = false
            scanningDirectoryLabel.value = null
            result.onSuccess { count ->
                statusMessage.value = if (count > 0) "扫描完成，已从 ${directories.size} 个目录导入 $count 首歌曲。" else "扫描完成，但没有找到支持的音频文件。"
            }.onFailure { error ->
                errorMessage.value = error.message ?: "扫描失败，请重新选择目录后重试。"
                statusMessage.value = "扫描失败"
            }
        }
    }

    private fun refreshSavedDirectories() {
        viewModelScope.launch {
            val uris = libraryRepository.getSavedLibraryUris()
            val labels = uris.map(::formatFolderLabel)
            libraryDirectories.value = uris
            libraryPathLabel.value = when (labels.size) {
                0 -> "还未选择目录"
                1 -> labels.first()
                else -> "已添加 ${labels.size} 个目录"
            }
        }
    }

    private fun formatFolderLabel(uriValue: String): String {
        val decoded = Uri.decode(uriValue)
        val treePart = decoded.substringAfter("tree/", decoded)
        val folderName = treePart.substringAfterLast(':', treePart).substringAfterLast('/')
        return if (folderName.isBlank()) decoded else folderName
    }
}
