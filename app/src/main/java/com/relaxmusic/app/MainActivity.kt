package com.relaxmusic.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.relaxmusic.app.domain.model.ThemeMode
import com.relaxmusic.app.ui.RelaxMusicApp
import com.relaxmusic.app.ui.theme.RelaxMusicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (application as RelaxMusicApplication).appContainer
        setContent {
            val themeMode by appContainer.settingsRepository
                .observeThemeMode()
                .collectAsState(initial = ThemeMode.SYSTEM)

            RelaxMusicTheme(themeMode = themeMode) {
                RelaxMusicApp()
            }
        }
    }
}
