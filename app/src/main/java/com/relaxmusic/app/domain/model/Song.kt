package com.relaxmusic.app.domain.model

data class Song(
    val id: String,
    val uri: String,
    val fileName: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val size: Long,
    val modifiedAt: Long,
    val isFavorite: Boolean = false,
    val lastPlayedAt: Long? = null
)
