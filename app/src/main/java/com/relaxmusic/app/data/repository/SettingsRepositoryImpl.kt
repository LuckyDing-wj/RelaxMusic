package com.relaxmusic.app.data.repository

import com.relaxmusic.app.domain.repository.SettingsRepository

class SettingsRepositoryImpl : SettingsRepository {
    override suspend fun rememberLibraryUri(uri: String) = Unit
}
