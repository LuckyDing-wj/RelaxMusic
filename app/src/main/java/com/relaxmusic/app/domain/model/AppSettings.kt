package com.relaxmusic.app.domain.model

data class AppSettings(
    val libraryTreeUri: String? = null,
    val defaultVolume: Float = 1f,
    val followSystemTheme: Boolean = true,
    val lastPlayMode: PlayMode = PlayMode.SEQUENCE
)
