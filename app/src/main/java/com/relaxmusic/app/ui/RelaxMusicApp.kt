package com.relaxmusic.app.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.relaxmusic.app.RelaxMusicApplication
import com.relaxmusic.app.data.local.UriPermissionManager
import com.relaxmusic.app.service.PlaybackService
import com.relaxmusic.app.ui.components.BottomNavigationBar
import com.relaxmusic.app.ui.components.PlayerBar
import com.relaxmusic.app.ui.components.SleepTimerSheet
import com.relaxmusic.app.ui.components.TopLevelDestination
import com.relaxmusic.app.ui.screens.library.AlbumsScreen
import com.relaxmusic.app.ui.screens.library.ArtistsScreen
import com.relaxmusic.app.ui.screens.library.CollectionScreen
import com.relaxmusic.app.ui.screens.library.FullLibraryScreen
import com.relaxmusic.app.ui.screens.library.GroupDetailScreen
import com.relaxmusic.app.ui.screens.library.LibraryScreen
import com.relaxmusic.app.ui.screens.library.LibraryViewModel
import com.relaxmusic.app.ui.screens.library.LibraryViewModelFactory
import com.relaxmusic.app.ui.screens.library.PlaylistDetailScreen
import com.relaxmusic.app.ui.screens.library.PlaylistsScreen
import com.relaxmusic.app.ui.screens.nowplaying.NowPlayingScreen
import com.relaxmusic.app.ui.screens.nowplaying.PlayerViewModel
import com.relaxmusic.app.ui.screens.nowplaying.PlayerViewModelFactory
import com.relaxmusic.app.ui.screens.nowplaying.QueueScreen
import com.relaxmusic.app.ui.screens.settings.SettingsScreen
import com.relaxmusic.app.ui.screens.settings.SettingsViewModel
import com.relaxmusic.app.ui.theme.AppBackgroundEnd
import com.relaxmusic.app.ui.theme.AppBackgroundStart

private enum class Destination {
    LIBRARY,
    FULL_LIBRARY,
    ALBUMS,
    ALBUM_DETAIL,
    ARTISTS,
    ARTIST_DETAIL,
    PLAYLISTS,
    PLAYLIST_DETAIL,
    FAVORITES,
    RECENT,
    QUEUE,
    NOW_PLAYING,
    SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelaxMusicApp() {
    val context = LocalContext.current
    val application = context.applicationContext as RelaxMusicApplication
    val uriPermissionManager = remember { UriPermissionManager() }
    val backupManager = application.appContainer.backupManager

    val libraryViewModel: LibraryViewModel = viewModel(
        factory = LibraryViewModelFactory(application.appContainer.libraryRepository)
    )
    val playerViewModel: PlayerViewModel = viewModel(
        factory = PlayerViewModelFactory(application.appContainer.playerRepository)
    )
    val settingsViewModel: SettingsViewModel = viewModel()
    val libraryUiState by libraryViewModel.state.collectAsState()

    var destination by remember { mutableStateOf(Destination.LIBRARY) }
    var topLevelDestination by remember { mutableStateOf<TopLevelDestination>(TopLevelDestination.HOME) }
    var timerSheetVisible by remember { mutableStateOf(false) }

    val switchTopLevelDestination: (TopLevelDestination) -> Unit = { topLevel ->
        topLevelDestination = topLevel
        destination = when (topLevel) {
            TopLevelDestination.HOME -> Destination.LIBRARY
            TopLevelDestination.PLAYLISTS -> Destination.PLAYLISTS
            TopLevelDestination.HISTORY -> Destination.RECENT
            TopLevelDestination.SETTINGS -> Destination.SETTINGS
        }
    }

    LaunchedEffect(Unit) {
        ContextCompat.startForegroundService(
            context,
            Intent(context, PlaybackService::class.java)
        )
        playerViewModel.bindContext(context)
    }

    LaunchedEffect(playerViewModel.uiState.currentSong?.id) {
        libraryViewModel.setCurrentSong(playerViewModel.uiState.currentSong?.id)
    }

    val pickFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            uriPermissionManager.takePersistablePermission(context, uri)
            val label = uri.toString()
            libraryViewModel.onFolderPicked(label)
            settingsViewModel.onFolderPicked(label)
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(AppBackgroundStart, AppBackgroundEnd)))
        ) {
            Scaffold(
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                bottomBar = {
                    Column {
                        PlayerBar(
                            song = playerViewModel.miniPlayerSong,
                            isPlaying = playerViewModel.miniPlayerIsPlaying,
                            progress = playerViewModel.miniPlayerProgress,
                            onTogglePlay = playerViewModel::togglePlay,
                            onOpenNowPlaying = { destination = Destination.NOW_PLAYING },
                            onOpenTimer = { timerSheetVisible = true },
                            onOpenQueue = { destination = Destination.QUEUE }
                        )
                        BottomNavigationBar(
                            current = topLevelDestination,
                            onSelect = switchTopLevelDestination
                        )
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    when (destination) {
                        Destination.LIBRARY -> LibraryScreen(
                            state = libraryUiState,
                            onPickFolder = { pickFolderLauncher.launch(null) },
                            onRemoveFolder = {
                                libraryViewModel.removeFolder(it)
                                settingsViewModel.onFolderRemoved(it)
                            },
                            onRescan = libraryViewModel::rescan,
                            onOpenFullLibrary = { destination = Destination.FULL_LIBRARY },
                            onOpenAlbums = { destination = Destination.ALBUMS },
                            onOpenArtists = { destination = Destination.ARTISTS },
                            onOpenPlaylists = { destination = Destination.PLAYLISTS },
                            onOpenFavorites = { destination = Destination.FAVORITES },
                            onOpenRecent = { destination = Destination.RECENT },
                            onOpenSettings = { destination = Destination.SETTINGS }
                        )

                        Destination.FULL_LIBRARY -> FullLibraryScreen(
                            songs = libraryUiState.songs,
                            playlists = libraryUiState.playlists,
                            currentSongId = libraryUiState.currentSongId,
                            query = libraryUiState.query,
                            onBack = { switchTopLevelDestination(TopLevelDestination.HOME) },
                            onQueryChange = libraryViewModel::onQueryChange,
                            onSongClick = { song ->
                                playerViewModel.playSong(song, libraryUiState.songs)
                                libraryViewModel.setCurrentSong(song.id)
                                libraryViewModel.markSongPlayed(song.id)
                                destination = Destination.NOW_PLAYING
                            },
                            onToggleFavorite = libraryViewModel::toggleFavorite,
                            onAddSongToPlaylist = libraryViewModel::addSongToPlaylist
                        )

                        Destination.ALBUMS -> AlbumsScreen(
                            albums = libraryUiState.albums,
                            onBack = { switchTopLevelDestination(TopLevelDestination.HOME) },
                            onOpenAlbum = { album ->
                                libraryViewModel.selectAlbum(album.name)
                                destination = Destination.ALBUM_DETAIL
                            }
                        )

                        Destination.ALBUM_DETAIL -> GroupDetailScreen(
                            title = libraryUiState.selectedAlbumName ?: "专辑",
                            songs = libraryUiState.albums.firstOrNull { it.name == libraryUiState.selectedAlbumName }?.songs.orEmpty(),
                            currentSongId = libraryUiState.currentSongId,
                            playlists = libraryUiState.playlists,
                            onBack = { destination = Destination.ALBUMS },
                            onSongClick = { song ->
                                val queue = libraryUiState.albums.firstOrNull { it.name == libraryUiState.selectedAlbumName }?.songs.orEmpty()
                                playerViewModel.playSong(song, queue)
                                libraryViewModel.setCurrentSong(song.id)
                                libraryViewModel.markSongPlayed(song.id)
                                destination = Destination.NOW_PLAYING
                            },
                            onToggleFavorite = libraryViewModel::toggleFavorite,
                            onAddSongToPlaylist = libraryViewModel::addSongToPlaylist
                        )

                        Destination.ARTISTS -> ArtistsScreen(
                            artists = libraryUiState.artists,
                            onBack = { switchTopLevelDestination(TopLevelDestination.HOME) },
                            onOpenArtist = { artist ->
                                libraryViewModel.selectArtist(artist.name)
                                destination = Destination.ARTIST_DETAIL
                            }
                        )

                        Destination.ARTIST_DETAIL -> GroupDetailScreen(
                            title = libraryUiState.selectedArtistName ?: "艺术家",
                            songs = libraryUiState.artists.firstOrNull { it.name == libraryUiState.selectedArtistName }?.songs.orEmpty(),
                            currentSongId = libraryUiState.currentSongId,
                            playlists = libraryUiState.playlists,
                            onBack = { destination = Destination.ARTISTS },
                            onSongClick = { song ->
                                val queue = libraryUiState.artists.firstOrNull { it.name == libraryUiState.selectedArtistName }?.songs.orEmpty()
                                playerViewModel.playSong(song, queue)
                                libraryViewModel.setCurrentSong(song.id)
                                libraryViewModel.markSongPlayed(song.id)
                                destination = Destination.NOW_PLAYING
                            },
                            onToggleFavorite = libraryViewModel::toggleFavorite,
                            onAddSongToPlaylist = libraryViewModel::addSongToPlaylist
                        )

                        Destination.PLAYLISTS -> PlaylistsScreen(
                            playlists = libraryUiState.playlists,
                            onBack = { switchTopLevelDestination(TopLevelDestination.HOME) },
                            onCreatePlaylist = libraryViewModel::createPlaylist,
                            onRenamePlaylist = libraryViewModel::renamePlaylist,
                            onDeletePlaylist = libraryViewModel::deletePlaylist,
                            showBackButton = false,
                            onOpenPlaylist = { playlist ->
                                libraryViewModel.selectPlaylist(playlist)
                                destination = Destination.PLAYLIST_DETAIL
                            }
                        )

                        Destination.PLAYLIST_DETAIL -> PlaylistDetailScreen(
                            playlist = libraryUiState.playlists.firstOrNull { it.id == libraryUiState.selectedPlaylistId },
                            playlistSongs = libraryUiState.selectedPlaylistSongs,
                            allSongs = libraryUiState.songs,
                            currentSongId = libraryUiState.currentSongId,
                            onBack = { destination = Destination.PLAYLISTS },
                            onSongClick = { song ->
                                playerViewModel.playSong(song, libraryUiState.selectedPlaylistSongs)
                                libraryViewModel.setCurrentSong(song.id)
                                libraryViewModel.markSongPlayed(song.id)
                                destination = Destination.NOW_PLAYING
                            },
                            onToggleFavorite = libraryViewModel::toggleFavorite,
                            onAddSong = { songId ->
                                libraryUiState.selectedPlaylistId?.let { playlistId ->
                                    libraryViewModel.addSongToPlaylist(playlistId, songId)
                                }
                            },
                            onRemoveSong = { songId ->
                                libraryUiState.selectedPlaylistId?.let { playlistId ->
                                    libraryViewModel.removeSongFromPlaylist(playlistId, songId)
                                }
                            },
                            onRenamePlaylist = libraryViewModel::renamePlaylist,
                            onDeletePlaylist = libraryViewModel::deletePlaylist
                        )

                        Destination.FAVORITES -> CollectionScreen(
                            title = "我的收藏",
                            songs = libraryUiState.favoriteSongs,
                            playlists = libraryUiState.playlists,
                            currentSongId = libraryUiState.currentSongId,
                            onBack = { switchTopLevelDestination(TopLevelDestination.HOME) },
                            onSongClick = { song ->
                                playerViewModel.playSong(song, libraryUiState.favoriteSongs)
                                libraryViewModel.setCurrentSong(song.id)
                                libraryViewModel.markSongPlayed(song.id)
                                destination = Destination.NOW_PLAYING
                            },
                            onToggleFavorite = libraryViewModel::toggleFavorite,
                            onAddSongToPlaylist = libraryViewModel::addSongToPlaylist,
                            showBackButton = false,
                            emptyMessageAction = { destination = Destination.LIBRARY }
                        )

                        Destination.RECENT -> CollectionScreen(
                            title = "播放历史",
                            songs = libraryUiState.historySongs,
                            playlists = libraryUiState.playlists,
                            currentSongId = libraryUiState.currentSongId,
                            onBack = { switchTopLevelDestination(TopLevelDestination.HOME) },
                            onSongClick = { song ->
                                playerViewModel.playSong(song, libraryUiState.historySongs)
                                libraryViewModel.setCurrentSong(song.id)
                                libraryViewModel.markSongPlayed(song.id)
                                destination = Destination.NOW_PLAYING
                            },
                            onToggleFavorite = libraryViewModel::toggleFavorite,
                            onAddSongToPlaylist = libraryViewModel::addSongToPlaylist,
                            showBackButton = false,
                            emptyMessageAction = { destination = Destination.LIBRARY }
                        )

                        Destination.NOW_PLAYING -> NowPlayingScreen(
                            state = playerViewModel.uiState,
                            onBack = { destination = Destination.LIBRARY },
                            onTogglePlay = playerViewModel::togglePlay,
                            onNext = playerViewModel::next,
                            onPrevious = playerViewModel::previous,
                            onChangeProgress = playerViewModel::seekTo,
                            onCyclePlayMode = playerViewModel::cyclePlayMode,
                            onOpenTimer = { timerSheetVisible = true },
                            onOpenQueue = { destination = Destination.QUEUE }
                        )

                        Destination.QUEUE -> QueueScreen(
                            queue = playerViewModel.uiState.queue,
                            currentIndex = playerViewModel.uiState.currentIndex,
                            onBack = { destination = Destination.NOW_PLAYING },
                            onSongClick = { song ->
                                playerViewModel.playQueueSong(song.id)
                                libraryViewModel.setCurrentSong(song.id)
                                destination = Destination.NOW_PLAYING
                            },
                            onRemove = playerViewModel::removeFromQueue
                        )

                        Destination.SETTINGS -> SettingsScreen(
                            state = settingsViewModel.uiState,
                            onBack = { switchTopLevelDestination(TopLevelDestination.HOME) },
                            onPickFolder = { pickFolderLauncher.launch(null) },
                            onRemoveFolder = {
                                libraryViewModel.removeFolder(it)
                                settingsViewModel.onFolderRemoved(it)
                            },
                            onExportBackup = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    val result = backupManager.exportBackup()
                                    settingsViewModel.setBackupStatus(
                                        result.fold(
                                            onSuccess = { "已导出到: $it" },
                                            onFailure = { "导出失败: ${it.message}" }
                                        )
                                    )
                                }
                            },
                            onImportBackup = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    val result = backupManager.importBackup()
                                    settingsViewModel.setBackupStatus(
                                        result.fold(
                                            onSuccess = { "已导入备份: $it" },
                                            onFailure = { "导入失败: ${it.message}" }
                                        )
                                    )
                                    libraryViewModel.rescan()
                                }
                            },
                            showBackButton = false,
                            onUseEmbeddedTheme = settingsViewModel::toggleThemeFollowSystem
                        )
                    }
                }
            }

            if (timerSheetVisible) {
                ModalBottomSheet(
                    onDismissRequest = { timerSheetVisible = false },
                    containerColor = com.relaxmusic.app.ui.theme.PanelBackground
                ) {
                    SleepTimerSheet(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        remainSeconds = playerViewModel.uiState.sleepTimerRemaining,
                        onPresetClick = { minutes ->
                            playerViewModel.startSleepTimer(minutes)
                            timerSheetVisible = false
                        },
                        onCancelTimer = {
                            playerViewModel.cancelSleepTimer()
                            timerSheetVisible = false
                        }
                    )
                }
            }
        }
    }
}
