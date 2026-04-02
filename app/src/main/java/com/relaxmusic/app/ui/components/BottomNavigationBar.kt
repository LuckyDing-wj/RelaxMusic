package com.relaxmusic.app.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
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
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        strong = true,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
    ) {
        NavigationBar(
            modifier = Modifier.height(64.dp),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0, 0, 0, 0)
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
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = "首页",
                        modifier = Modifier.size(28.dp)
                    )
                },
                alwaysShowLabel = false,
                colors = itemColors
            )
            NavigationBarItem(
                selected = current == TopLevelDestination.PLAYER,
                onClick = { onSelect(TopLevelDestination.PLAYER) },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.PlayCircle,
                        contentDescription = "播放",
                        modifier = Modifier.size(28.dp)
                    )
                },
                alwaysShowLabel = false,
                colors = itemColors
            )
            NavigationBarItem(
                selected = current == TopLevelDestination.LISTS,
                onClick = { onSelect(TopLevelDestination.LISTS) },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.QueueMusic,
                        contentDescription = "列表",
                        modifier = Modifier.size(28.dp)
                    )
                },
                alwaysShowLabel = false,
                colors = itemColors
            )
        }
    }
}
