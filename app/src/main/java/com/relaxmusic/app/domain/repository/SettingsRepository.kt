package com.relaxmusic.app.domain.repository

import com.relaxmusic.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeLibraryDirectories(): Flow<List<String>>
    fun observeThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(themeMode: ThemeMode)
}
