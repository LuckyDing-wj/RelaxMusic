package com.relaxmusic.app.data.repository

import com.relaxmusic.app.data.db.dao.SettingsDao
import com.relaxmusic.app.data.db.entity.SettingsEntity
import com.relaxmusic.app.domain.model.ThemeMode
import com.relaxmusic.app.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl(
    private val settingsDao: SettingsDao
) : SettingsRepository {
    override fun observeLibraryDirectories(): Flow<List<String>> {
        return combine(
            settingsDao.observe(KEY_LIBRARY_URIS),
            settingsDao.observe(KEY_LIBRARY_URI)
        ) { multi, legacy ->
            val multiValue = multi?.value
                ?.split(SEPARATOR)
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                .orEmpty()

            if (multiValue.isNotEmpty()) {
                multiValue
            } else {
                legacy?.value?.takeIf { it.isNotBlank() }?.let(::listOf).orEmpty()
            }
        }
    }

    override fun observeThemeMode(): Flow<ThemeMode> {
        return combine(
            settingsDao.observe(KEY_THEME_MODE),
            settingsDao.observe(KEY_FOLLOW_SYSTEM_THEME)
        ) { themeModeEntity, legacyEntity ->
            parseThemeMode(
                rawThemeMode = themeModeEntity?.value,
                legacyFollowSystemTheme = legacyEntity?.value
            )
        }
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        withContext(Dispatchers.IO) {
            settingsDao.put(
                SettingsEntity(
                    key = KEY_THEME_MODE,
                    value = themeMode.name
                )
            )
        }
    }

    internal fun parseThemeMode(
        rawThemeMode: String?,
        legacyFollowSystemTheme: String?
    ): ThemeMode {
        return ThemeMode.entries.firstOrNull { it.name == rawThemeMode }
            ?: when (legacyFollowSystemTheme?.toBooleanStrictOrNull()) {
                true -> ThemeMode.SYSTEM
                false -> ThemeMode.LIGHT
                null -> ThemeMode.SYSTEM
            }
    }

    private companion object {
        const val KEY_LIBRARY_URI = "library_tree_uri"
        const val KEY_LIBRARY_URIS = "library_tree_uris"
        const val KEY_FOLLOW_SYSTEM_THEME = "follow_system_theme"
        const val KEY_THEME_MODE = "theme_mode"
        const val SEPARATOR = "||"
    }
}
