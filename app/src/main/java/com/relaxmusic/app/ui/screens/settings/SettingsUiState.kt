package com.relaxmusic.app.ui.screens.settings

data class SettingsUiState(
    val libraryFolderLabel: String = "尚未授权",
    val libraryFolders: List<String> = emptyList(),
    val libraryFolderLabels: Map<String, String> = emptyMap(),
    val followSystemTheme: Boolean = true,
    val backupStatus: String? = null,
    val projectStageLabel: String = "当前已接入扫描、真实播放、后台服务与通知栏控制，可继续做体验增强。"
)
