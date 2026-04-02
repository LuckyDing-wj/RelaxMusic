package com.relaxmusic.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.relaxmusic.app.ui.theme.RelaxMusicColors

@Composable
fun PremiumSurface(
    modifier: Modifier = Modifier,
    strong: Boolean = false,
    fillWidth: Boolean = true,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val colors = RelaxMusicColors
    Surface(
        modifier = if (fillWidth) modifier.fillMaxWidth() else modifier,
        shape = shape,
        color = if (strong) colors.glassSurfaceStrong else colors.glassSurface,
        border = BorderStroke(1.dp, colors.glassBorder),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(content = content)
    }
}

@Composable
fun HeroSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(30.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val colors = RelaxMusicColors
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, colors.glassBorder)
    ) {
        Box(content = content)
    }
}
