package com.relaxmusic.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

enum class TopLevelDestination {
    HOME,
    PLAYLISTS,
    HISTORY,
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
            selected = current == TopLevelDestination.PLAYLISTS,
            onClick = { onSelect(TopLevelDestination.PLAYLISTS) },
            icon = { Icon(Icons.Rounded.LibraryMusic, contentDescription = "歌单") },
            label = { Text("歌单") }
        )
        NavigationBarItem(
            selected = current == TopLevelDestination.HISTORY,
            onClick = { onSelect(TopLevelDestination.HISTORY) },
            icon = { Icon(Icons.Rounded.History, contentDescription = "历史") },
            label = { Text("历史") }
        )
        NavigationBarItem(
            selected = current == TopLevelDestination.SETTINGS,
            onClick = { onSelect(TopLevelDestination.SETTINGS) },
            icon = { Icon(Icons.Rounded.Settings, contentDescription = "设置") },
            label = { Text("设置") }
        )
    }
}
