package com.relaxmusic.app.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.relaxmusic.app.domain.model.ThemeMode
import com.relaxmusic.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    var uiState by mutableStateOf(SettingsUiState())
        private set

    private fun formatFolderLabel(raw: String): String {
        val decoded = android.net.Uri.decode(raw)
        val treePart = decoded.substringAfter("tree/", decoded)
        val folderName = treePart.substringAfterLast(':', treePart).substringAfterLast('/')
        return if (folderName.isBlank()) decoded else folderName
    }

    init {
        viewModelScope.launch {
            settingsRepository.observeLibraryDirectories().collectLatest { directories ->
                uiState = uiState.copy(
                    libraryFolderLabel = when (directories.size) {
                        0 -> "尚未授权"
                        1 -> formatFolderLabel(directories.first())
                        else -> "已添加 ${directories.size} 个目录"
                    },
                    libraryFolders = directories,
                    libraryFolderLabels = directories.associateWith(::formatFolderLabel)
                )
            }
        }

        viewModelScope.launch {
            settingsRepository.observeThemeMode().collectLatest { themeMode ->
                uiState = uiState.copy(themeMode = themeMode)
            }
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(themeMode)
        }
    }

    fun setBackupStatus(status: String) {
        uiState = uiState.copy(backupStatus = status)
    }
}
