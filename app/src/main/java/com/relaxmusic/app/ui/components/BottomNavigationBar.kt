package com.relaxmusic.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.relaxmusic.app.ui.theme.RelaxMusicColors

enum class TopLevelDestination {
    HOME,
    PLAYER,
    LISTS
}

@Composable
fun BottomNavigationBar(
    current: TopLevelDestination,
    onSelect: (TopLevelDestination) -> Unit
) {
    val colors = RelaxMusicColors
    PremiumSurface(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        strong = true,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            val itemColors = NavigationBarItemDefaults.colors(
                selectedIconColor = colors.accentStrong,
                selectedTextColor = colors.textPrimary,
                unselectedIconColor = colors.textSecondary,
                unselectedTextColor = colors.textSecondary,
                indicatorColor = colors.songHighlight.copy(alpha = 0.75f)
            )
            NavigationBarItem(
                selected = current == TopLevelDestination.HOME,
                onClick = { onSelect(TopLevelDestination.HOME) },
                icon = { Icon(Icons.Rounded.Home, contentDescription = "首页") },
                label = { Text("首页") },
                colors = itemColors
            )
            NavigationBarItem(
                selected = current == TopLevelDestination.PLAYER,
                onClick = { onSelect(TopLevelDestination.PLAYER) },
                icon = { Icon(Icons.Rounded.PlayCircle, contentDescription = "播放") },
                label = { Text("播放") },
                colors = itemColors
            )
            NavigationBarItem(
                selected = current == TopLevelDestination.LISTS,
                onClick = { onSelect(TopLevelDestination.LISTS) },
                icon = { Icon(Icons.Rounded.QueueMusic, contentDescription = "列表") },
                label = { Text("列表") },
                colors = itemColors
            )
        }
    }
}
