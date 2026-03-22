package com.relaxmusic.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.Text
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.relaxmusic.app.domain.model.Song
import com.relaxmusic.app.ui.components.TopLevelDestination
import com.relaxmusic.app.ui.screens.library.AlbumsScreen
import com.relaxmusic.app.ui.screens.library.ArtistsScreen
import com.relaxmusic.app.ui.screens.library.CollectionScreen
import com.relaxmusic.app.ui.screens.library.FullLibraryScreen
import com.relaxmusic.app.ui.screens.library.GroupDetailScreen
import com.relaxmusic.app.ui.screens.library.LibraryScreen
import com.relaxmusic.app.ui.screens.library.LibraryUiState
import com.relaxmusic.app.ui.screens.library.LibraryViewModel
import com.relaxmusic.app.ui.screens.library.PlaylistDetailScreen
import com.relaxmusic.app.ui.screens.library.PlaylistsScreen
import com.relaxmusic.app.ui.screens.nowplaying.PlayerViewModel
import com.relaxmusic.app.ui.screens.nowplaying.NowPlayingScreen
import com.relaxmusic.app.ui.screens.nowplaying.QueueScreen
import com.relaxmusic.app.ui.screens.settings.SettingsScreen
import com.relaxmusic.app.ui.screens.settings.SettingsUiState
import com.relaxmusic.app.ui.screens.settings.SettingsViewModel

@Composable
fun RelaxMusicNavGraph(
    navController: NavHostController,
    libraryUiState: LibraryUiState,
    settingsUiState: SettingsUiState,
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    settingsViewModel: SettingsViewModel,
    onPickFolder: () -> Unit,
    onOpenTimer: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit
) {
    fun navigateToNowPlaying() {
        navController.navigate(RelaxMusicDestination.NowPlaying.route) {
            launchSingleTop = true
        }
    }

    fun playSong(song: Song, queue: List<Song>) {
        playerViewModel.playSong(song, queue)
        libraryViewModel.setCurrentSong(song.id)
        libraryViewModel.markSongPlayed(song.id)
        navigateToNowPlaying()
    }

    fun navigateToTopLevel(destination: TopLevelDestination) {
        val route = when (destination) {
            TopLevelDestination.HOME -> RelaxMusicDestination.Home.route
            TopLevelDestination.LIBRARY -> RelaxMusicDestination.LibraryHub.route
            TopLevelDestination.LISTS -> RelaxMusicDestination.ListsHub.route
            TopLevelDestination.SETTINGS -> RelaxMusicDestination.Settings.route
        }
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = RelaxMusicDestination.Home.route
    ) {
        composable(RelaxMusicDestination.Home.route) {
            LibraryScreen(
                state = libraryUiState,
                currentSong = playerViewModel.miniPlayerSong,
                isPlaying = playerViewModel.miniPlayerIsPlaying,
                playbackProgress = playerViewModel.miniPlayerProgress,
                onPickFolder = onPickFolder,
                onRemoveFolder = libraryViewModel::removeFolder,
                onRescan = libraryViewModel::rescan,
                onOpenLibrary = { navigateToTopLevel(TopLevelDestination.LIBRARY) },
                onOpenAlbums = { navController.navigate(RelaxMusicDestination.Albums.route) },
                onOpenArtists = { navController.navigate(RelaxMusicDestination.Artists.route) },
                onOpenPlaylists = { navController.navigate(RelaxMusicDestination.Playlists.route) },
                onOpenFavorites = { navController.navigate(RelaxMusicDestination.Favorites.route) },
                onOpenRecent = { navController.navigate(RelaxMusicDestination.History.route) },
                onOpenNowPlaying = ::navigateToNowPlaying,
                onOpenSettings = { navigateToTopLevel(TopLevelDestination.SETTINGS) }
            )
        }

        composable(RelaxMusicDestination.LibraryHub.route) {
            Text("曲库")
        }

        composable(RelaxMusicDestination.ListsHub.route) {
            Text("列表")
        }

        composable(RelaxMusicDestination.FullLibrary.route) {
            FullLibraryScreen(
                songs = libraryUiState.songs,
                playlists = libraryUiState.playlists,
                currentSongId = libraryUiState.currentSongId,
                query = libraryUiState.query,
                onBack = { navController.navigateUp() },
                onQueryChange = libraryViewModel::onQueryChange,
                onSongClick = { song -> playSong(song, libraryUiState.songs) },
                onToggleFavorite = libraryViewModel::toggleFavorite,
                onAddSongToPlaylist = libraryViewModel::addSongToPlaylist
            )
        }

        composable(RelaxMusicDestination.Albums.route) {
            AlbumsScreen(
                albums = libraryUiState.albums,
                onBack = { navController.navigateUp() },
                onOpenAlbum = { album ->
                    navController.navigate(
                        RelaxMusicDestination.AlbumDetail.createRoute(
                            albumName = album.name,
                            albumArtist = album.artist
                        )
                    )
                }
            )
        }

        composable(
            route = RelaxMusicDestination.AlbumDetail.route,
            arguments = listOf(
                navArgument(RelaxMusicDestination.AlbumDetail.ALBUM_NAME_ARG) { type = NavType.StringType },
                navArgument(RelaxMusicDestination.AlbumDetail.ALBUM_ARTIST_ARG) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val albumName = backStackEntry.arguments?.getString(RelaxMusicDestination.AlbumDetail.ALBUM_NAME_ARG).orEmpty()
            val albumArtist = backStackEntry.arguments?.getString(RelaxMusicDestination.AlbumDetail.ALBUM_ARTIST_ARG).orEmpty()
            val album = libraryUiState.albums.firstOrNull { it.name == albumName && it.artist == albumArtist }

            GroupDetailScreen(
                title = album?.name ?: "专辑",
                songs = album?.songs.orEmpty(),
                currentSongId = libraryUiState.currentSongId,
                playlists = libraryUiState.playlists,
                onBack = { navController.navigateUp() },
                onSongClick = { song -> playSong(song, album?.songs.orEmpty()) },
                onToggleFavorite = libraryViewModel::toggleFavorite,
                onAddSongToPlaylist = libraryViewModel::addSongToPlaylist
            )
        }

        composable(RelaxMusicDestination.Artists.route) {
            ArtistsScreen(
                artists = libraryUiState.artists,
                onBack = { navController.navigateUp() },
                onOpenArtist = { artist ->
                    navController.navigate(RelaxMusicDestination.ArtistDetail.createRoute(artist.name))
                }
            )
        }

        composable(
            route = RelaxMusicDestination.ArtistDetail.route,
            arguments = listOf(
                navArgument(RelaxMusicDestination.ArtistDetail.ARTIST_NAME_ARG) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val artistName = backStackEntry.arguments?.getString(RelaxMusicDestination.ArtistDetail.ARTIST_NAME_ARG).orEmpty()
            val artist = libraryUiState.artists.firstOrNull { it.name == artistName }

            GroupDetailScreen(
                title = artist?.name ?: "艺术家",
                songs = artist?.songs.orEmpty(),
                currentSongId = libraryUiState.currentSongId,
                playlists = libraryUiState.playlists,
                onBack = { navController.navigateUp() },
                onSongClick = { song -> playSong(song, artist?.songs.orEmpty()) },
                onToggleFavorite = libraryViewModel::toggleFavorite,
                onAddSongToPlaylist = libraryViewModel::addSongToPlaylist
            )
        }

        composable(RelaxMusicDestination.Playlists.route) {
            PlaylistsScreen(
                playlists = libraryUiState.playlists,
                onBack = { navController.navigateUp() },
                onCreatePlaylist = libraryViewModel::createPlaylist,
                onRenamePlaylist = libraryViewModel::renamePlaylist,
                onDeletePlaylist = libraryViewModel::deletePlaylist,
                showBackButton = false,
                onOpenPlaylist = { playlist ->
                    navController.navigate(RelaxMusicDestination.PlaylistDetail.createRoute(playlist.id))
                }
            )
        }

        composable(
            route = RelaxMusicDestination.PlaylistDetail.route,
            arguments = listOf(
                navArgument(RelaxMusicDestination.PlaylistDetail.PLAYLIST_ID_ARG) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong(RelaxMusicDestination.PlaylistDetail.PLAYLIST_ID_ARG) ?: -1L
            val playlistSongsFlow = remember(playlistId) { libraryViewModel.observePlaylistSongs(playlistId) }
            val playlistSongs by playlistSongsFlow.collectAsState(initial = emptyList())
            val playlist = libraryUiState.playlists.firstOrNull { it.id == playlistId }

            PlaylistDetailScreen(
                playlist = playlist,
                playlistSongs = playlistSongs,
                allSongs = libraryUiState.songs,
                currentSongId = libraryUiState.currentSongId,
                onBack = { navController.navigateUp() },
                onSongClick = { song -> playSong(song, playlistSongs) },
                onToggleFavorite = libraryViewModel::toggleFavorite,
                onAddSong = { songId -> libraryViewModel.addSongToPlaylist(playlistId, songId) },
                onRemoveSong = { songId -> libraryViewModel.removeSongFromPlaylist(playlistId, songId) },
                onRenamePlaylist = libraryViewModel::renamePlaylist,
                onDeletePlaylist = libraryViewModel::deletePlaylist
            )
        }

        composable(RelaxMusicDestination.Favorites.route) {
            CollectionScreen(
                title = "我的收藏",
                songs = libraryUiState.favoriteSongs,
                playlists = libraryUiState.playlists,
                currentSongId = libraryUiState.currentSongId,
                onBack = { navController.navigateUp() },
                onSongClick = { song -> playSong(song, libraryUiState.favoriteSongs) },
                onToggleFavorite = libraryViewModel::toggleFavorite,
                onAddSongToPlaylist = libraryViewModel::addSongToPlaylist,
                emptyMessageAction = { navigateToTopLevel(TopLevelDestination.HOME) }
            )
        }

        composable(RelaxMusicDestination.History.route) {
            CollectionScreen(
                title = "播放历史",
                songs = libraryUiState.historySongs,
                playlists = libraryUiState.playlists,
                currentSongId = libraryUiState.currentSongId,
                onBack = { navController.navigateUp() },
                onSongClick = { song -> playSong(song, libraryUiState.historySongs) },
                onToggleFavorite = libraryViewModel::toggleFavorite,
                onAddSongToPlaylist = libraryViewModel::addSongToPlaylist,
                showBackButton = false,
                emptyMessageAction = { navigateToTopLevel(TopLevelDestination.HOME) },
                supportingText = "按播放时间倒序展示，每次播放都会保留一条记录。"
            )
        }

        composable(RelaxMusicDestination.NowPlaying.route) {
            NowPlayingScreen(
                artworkState = playerViewModel.nowPlayingArtworkUiState,
                lyricsState = playerViewModel.nowPlayingLyricsUiState,
                trackState = playerViewModel.nowPlayingTrackUiState,
                progressState = playerViewModel.nowPlayingProgressUiState,
                controlsState = playerViewModel.nowPlayingControlsUiState,
                onBack = { navController.navigateUp() },
                onTogglePlay = playerViewModel::togglePlay,
                onNext = playerViewModel::next,
                onPrevious = playerViewModel::previous,
                onChangeProgress = playerViewModel::seekTo,
                onCyclePlayMode = playerViewModel::cyclePlayMode,
                onOpenTimer = onOpenTimer,
                onOpenQueue = {
                    navController.navigate(RelaxMusicDestination.Queue.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(RelaxMusicDestination.Queue.route) {
            QueueScreen(
                queue = playerViewModel.uiState.queue,
                currentIndex = playerViewModel.uiState.currentIndex,
                onBack = { navController.navigateUp() },
                onSongClick = { song ->
                    playerViewModel.playQueueSong(song.id)
                    libraryViewModel.setCurrentSong(song.id)
                    navController.navigateUp()
                },
                onRemove = playerViewModel::removeFromQueue
            )
        }

        composable(RelaxMusicDestination.Settings.route) {
            SettingsScreen(
                state = settingsUiState,
                onBack = { navController.navigateUp() },
                onPickFolder = onPickFolder,
                onRemoveFolder = libraryViewModel::removeFolder,
                onExportBackup = onExportBackup,
                onImportBackup = onImportBackup,
                showBackButton = false,
                onThemeModeChange = settingsViewModel::setThemeMode
            )
        }
    }
}
