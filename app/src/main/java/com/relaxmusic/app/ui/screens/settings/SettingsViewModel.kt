package com.relaxmusic.app.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {
    var uiState by mutableStateOf(SettingsUiState())
        private set

    private fun formatFolderLabel(raw: String): String {
        val decoded = android.net.Uri.decode(raw)
        val treePart = decoded.substringAfter("tree/", decoded)
        val folderName = treePart.substringAfterLast(':', treePart).substringAfterLast('/')
        return if (folderName.isBlank()) decoded else folderName
    }

    fun onFolderPicked(label: String) {
        val updated = (uiState.libraryFolders + label).distinct()
        uiState = uiState.copy(
            libraryFolderLabel = when (updated.size) {
                0 -> "尚未授权"
                1 -> formatFolderLabel(updated.first())
                else -> "已添加 ${updated.size} 个目录"
            },
            libraryFolders = updated,
            libraryFolderLabels = updated.associateWith(::formatFolderLabel)
        )
    }

    fun onFolderRemoved(label: String) {
        val updated = uiState.libraryFolders.filterNot { it == label }
        uiState = uiState.copy(
            libraryFolderLabel = when (updated.size) {
                0 -> "尚未授权"
                1 -> formatFolderLabel(updated.first())
                else -> "已添加 ${updated.size} 个目录"
            },
            libraryFolders = updated,
            libraryFolderLabels = updated.associateWith(::formatFolderLabel)
        )
    }

    fun toggleThemeFollowSystem() {
        uiState = uiState.copy(followSystemTheme = !uiState.followSystemTheme)
    }

    fun setBackupStatus(status: String) {
        uiState = uiState.copy(backupStatus = status)
    }
}
