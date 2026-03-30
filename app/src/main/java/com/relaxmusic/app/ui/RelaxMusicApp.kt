package com.relaxmusic.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import com.relaxmusic.app.RelaxMusicApplication
import com.relaxmusic.app.data.local.UriPermissionManager
import com.relaxmusic.app.ui.components.BottomNavigationBar
import com.relaxmusic.app.ui.components.SleepTimerSheet
import com.relaxmusic.app.ui.components.TopLevelDestination
import com.relaxmusic.app.ui.navigation.RelaxMusicDestination
import com.relaxmusic.app.ui.navigation.RelaxMusicNavGraph
import com.relaxmusic.app.ui.screens.library.LibraryViewModel
import com.relaxmusic.app.ui.screens.library.LibraryViewModelFactory
import com.relaxmusic.app.ui.screens.nowplaying.PlayerViewModel
import com.relaxmusic.app.ui.screens.nowplaying.PlayerViewModelFactory
import com.relaxmusic.app.ui.screens.settings.SettingsViewModel
import com.relaxmusic.app.ui.screens.settings.SettingsViewModelFactory
import com.relaxmusic.app.ui.theme.RelaxMusicColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelaxMusicApp() {
    val context = LocalContext.current
    val application = context.applicationContext as RelaxMusicApplication
    val uriPermissionManager = remember { UriPermissionManager() }
    val backupManager = application.appContainer.backupManager
    val navController = rememberNavController()

    val libraryViewModel: LibraryViewModel = viewModel(
        factory = LibraryViewModelFactory(application.appContainer.libraryRepository)
    )
    val playerViewModel: PlayerViewModel = viewModel(
        factory = PlayerViewModelFactory(application.appContainer.playerRepository)
    )
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(application.appContainer.settingsRepository)
    )

    val libraryUiState by libraryViewModel.state.collectAsStateWithLifecycle()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    var topLevelDestination by remember { mutableStateOf(TopLevelDestination.HOME) }
    var timerSheetVisible by remember { mutableStateOf(false) }
    val colors = RelaxMusicColors
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        playerViewModel.bindContext(context)
    }

    LaunchedEffect(playerViewModel.nowPlayingTrackUiState.currentSong?.id) {
        libraryViewModel.setCurrentSong(playerViewModel.nowPlayingTrackUiState.currentSong?.id)
    }

    LaunchedEffect(currentBackStackEntry?.destination?.route) {
        RelaxMusicDestination.topLevelDestinationForRoute(currentBackStackEntry?.destination?.route)?.let { topLevel ->
            topLevelDestination = topLevel
        }
    }

    val pickFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            uriPermissionManager.takePersistablePermission(context, uri)
            libraryViewModel.onFolderPicked(uri.toString())
        }
    }

    val navigateToTopLevel: (TopLevelDestination) -> Unit = { topLevel ->
        val route = when (topLevel) {
            TopLevelDestination.HOME -> RelaxMusicDestination.Home.route
            TopLevelDestination.PLAYER -> RelaxMusicDestination.NowPlaying.route
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

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(colors.appBackgroundStart, colors.appBackgroundEnd)))
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    BottomNavigationBar(
                        current = topLevelDestination,
                        onSelect = navigateToTopLevel
                    )
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    RelaxMusicNavGraph(
                        navController = navController,
                        libraryUiState = libraryUiState,
                        settingsUiState = settingsViewModel.uiState,
                        libraryViewModel = libraryViewModel,
                        playerViewModel = playerViewModel,
                        settingsViewModel = settingsViewModel,
                        onPickFolder = { pickFolderLauncher.launch(null) },
                        onOpenTimer = { timerSheetVisible = true },
                        onExportBackup = {
                            coroutineScope.launch {
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
                            coroutineScope.launch {
                                val result = backupManager.importBackup()
                                settingsViewModel.setBackupStatus(
                                    result.fold(
                                        onSuccess = { "已导入备份: $it" },
                                        onFailure = { "导入失败: ${it.message}" }
                                    )
                                )
                                libraryViewModel.rescan()
                            }
                        }
                    )
                }
            }

            if (timerSheetVisible) {
                ModalBottomSheet(
                    onDismissRequest = { timerSheetVisible = false },
                    containerColor = colors.panelBackground
                ) {
                    SleepTimerSheet(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        remainSeconds = playerViewModel.nowPlayingControlsUiState.sleepTimerRemaining,
                        onPresetClick = { minutes ->
                            playerViewModel.startSleepTimer(minutes)
                            timerSheetVisible = false
                        },
                        onCustomConfirm = { totalMinutes ->
                            playerViewModel.startSleepTimerForMinutes(totalMinutes)
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
