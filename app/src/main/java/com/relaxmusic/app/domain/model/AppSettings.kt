package com.relaxmusic.app.domain.model

data class AppSettings(
    val libraryTreeUri: String? = null,
    val defaultVolume: Float = 1f,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val lastPlayMode: PlayMode = PlayMode.SEQUENCE
)
