package com.relaxmusic.app.domain.model

data class Album(
    val name: String,
    val artist: String,
    val songs: List<Song>
)
