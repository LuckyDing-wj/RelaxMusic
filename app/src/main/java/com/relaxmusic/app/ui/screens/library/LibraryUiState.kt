package com.relaxmusic.app.ui.screens.library

import com.relaxmusic.app.domain.model.Album
import com.relaxmusic.app.domain.model.Artist
import com.relaxmusic.app.domain.model.Playlist
import com.relaxmusic.app.domain.model.Song

data class LibraryUiState(
    val libraryPathLabel: String = "还未选择目录",
    val libraryDirectories: List<String> = emptyList(),
    val libraryDirectoryLabels: Map<String, String> = emptyMap(),
    val scanningDirectoryLabel: String? = null,
    val allSongs: List<Song> = emptyList(),
    val songs: List<Song> = emptyList(),
    val favoriteSongs: List<Song> = emptyList(),
    val recentSongs: List<Song> = emptyList(),
    val historySongs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val totalSongCount: Int = 0,
    val scanning: Boolean = false,
    val query: String = "",
    val statusMessage: String = "请选择一个本地音乐目录开始。",
    val librarySummaryText: String = "",
    val errorMessage: String? = null,
    val currentSongId: String? = null
)
