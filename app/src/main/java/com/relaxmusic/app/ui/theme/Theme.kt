package com.relaxmusic.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.relaxmusic.app.domain.model.ThemeMode

@Immutable
data class RelaxMusicColorPalette(
    val appBackgroundStart: Color,
    val appBackgroundEnd: Color,
    val panelBackground: Color,
    val panelSurface: Color,
    val panelSurfaceStrong: Color,
    val panelBorder: Color,
    val accent: Color,
    val accentStrong: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val songHighlight: Color
)

private val LightPalette = RelaxMusicColorPalette(
    appBackgroundStart = Color(0xFFEAF6F0),
    appBackgroundEnd = Color(0xFFF8F1E7),
    panelBackground = Color(0xFFF9FBF8),
    panelSurface = Color(0xB8FFFFFF),
    panelSurfaceStrong = Color(0xE0FFFFFF),
    panelBorder = Color(0x1A22392E),
    accent = Color(0xFF3E8A6B),
    accentStrong = Color(0xFF295A47),
    textPrimary = Color(0xFF1D2A24),
    textSecondary = Color(0xFF66756C),
    songHighlight = Color(0xFFE0F0E7)
)

private val DarkPalette = RelaxMusicColorPalette(
    appBackgroundStart = Color(0xFF0E1714),
    appBackgroundEnd = Color(0xFF151A24),
    panelBackground = Color(0xFF111A18),
    panelSurface = Color(0xCC182522),
    panelSurfaceStrong = Color(0xE61D2C28),
    panelBorder = Color(0x334F7768),
    accent = Color(0xFF7BC7A5),
    accentStrong = Color(0xFFB2E7CE),
    textPrimary = Color(0xFFF2F7F4),
    textSecondary = Color(0xFFA7BBB1),
    songHighlight = Color(0xFF1E332B)
)

internal fun resolveIsDarkTheme(themeMode: ThemeMode, systemInDarkTheme: Boolean): Boolean {
    return when (themeMode) {
        ThemeMode.SYSTEM -> systemInDarkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
}

internal fun resolveRelaxMusicColorPalette(
    themeMode: ThemeMode,
    systemInDarkTheme: Boolean
): RelaxMusicColorPalette {
    return if (resolveIsDarkTheme(themeMode, systemInDarkTheme)) DarkPalette else LightPalette
}

private val LocalRelaxMusicColors = staticCompositionLocalOf { LightPalette }

val RelaxMusicColors: RelaxMusicColorPalette
    @Composable get() = LocalRelaxMusicColors.current

@Composable
fun RelaxMusicTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val palette = resolveRelaxMusicColorPalette(
        themeMode = themeMode,
        systemInDarkTheme = isSystemInDarkTheme()
    )

    val colorScheme = if (palette == DarkPalette) {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = Color(0xFF0A130F),
            secondary = palette.accentStrong,
            background = palette.panelBackground,
            surface = palette.panelBackground,
            onSurface = palette.textPrimary,
            onBackground = palette.textPrimary
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = Color.White,
            secondary = palette.accentStrong,
            background = palette.panelBackground,
            surface = palette.panelBackground,
            onSurface = palette.textPrimary,
            onBackground = palette.textPrimary
        )
    }

    CompositionLocalProvider(LocalRelaxMusicColors provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = RelaxMusicTypography,
            content = content
        )
    }
}
