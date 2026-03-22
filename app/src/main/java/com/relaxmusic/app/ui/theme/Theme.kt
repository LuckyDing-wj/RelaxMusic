package com.relaxmusic.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Accent,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = AccentStrong,
    background = PanelBackground,
    surface = PanelBackground,
    onSurface = TextPrimary,
    onBackground = TextPrimary
)

private val DarkColors = darkColorScheme(
    primary = Accent,
    secondary = AccentStrong
)

@Composable
fun RelaxMusicTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = RelaxMusicTypography,
        content = content
    )
}
