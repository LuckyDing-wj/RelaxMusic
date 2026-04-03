package com.relaxmusic.app.ui.screens.settings

import com.relaxmusic.app.domain.model.ThemeMode

data class SettingsUiState(
    val libraryFolderLabel: String = "尚未授权",
    val libraryFolders: List<String> = emptyList(),
    val libraryFolderLabels: Map<String, String> = emptyMap(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val backupStatus: String? = null
)
