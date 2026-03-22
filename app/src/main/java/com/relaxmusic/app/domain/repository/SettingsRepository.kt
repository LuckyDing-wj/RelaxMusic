package com.relaxmusic.app.domain.repository

interface SettingsRepository {
    suspend fun rememberLibraryUri(uri: String)
}
