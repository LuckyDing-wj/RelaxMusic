package com.relaxmusic.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

enum class TopLevelDestination {
    HOME,
    PLAYER,
    LISTS,
    SETTINGS
}

@Composable
fun BottomNavigationBar(
    current: TopLevelDestination,
    onSelect: (TopLevelDestination) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = current == TopLevelDestination.HOME,
            onClick = { onSelect(TopLevelDestination.HOME) },
            icon = { Icon(Icons.Rounded.Home, contentDescription = "首页") },
            label = { Text("首页") }
        )
        NavigationBarItem(
            selected = current == TopLevelDestination.PLAYER,
            onClick = { onSelect(TopLevelDestination.PLAYER) },
            icon = { Icon(Icons.Rounded.PlayCircle, contentDescription = "播放") },
            label = { Text("播放") }
        )
        NavigationBarItem(
            selected = current == TopLevelDestination.LISTS,
            onClick = { onSelect(TopLevelDestination.LISTS) },
            icon = { Icon(Icons.Rounded.QueueMusic, contentDescription = "列表") },
            label = { Text("列表") }
        )
        NavigationBarItem(
            selected = current == TopLevelDestination.SETTINGS,
            onClick = { onSelect(TopLevelDestination.SETTINGS) },
            icon = { Icon(Icons.Rounded.Settings, contentDescription = "设置") },
            label = { Text("设置") }
        )
    }
}
