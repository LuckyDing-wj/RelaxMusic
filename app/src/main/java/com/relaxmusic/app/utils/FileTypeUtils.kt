package com.relaxmusic.app.utils

object FileTypeUtils {
    private val supported = setOf("mp3", "flac", "wav", "m4a", "ogg")

    fun isSupportedAudioFile(name: String?): Boolean {
        if (name.isNullOrBlank() || !name.contains('.')) return false
        val extension = name.substringAfterLast('.').lowercase()
        return extension in supported
    }
}
