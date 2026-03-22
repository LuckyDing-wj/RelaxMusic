package com.relaxmusic.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

enum class TopLevelDestination {
    HOME,
    LIBRARY,
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
            selected = current == TopLevelDestination.LIBRARY,
            onClick = { onSelect(TopLevelDestination.LIBRARY) },
            icon = { Icon(Icons.Rounded.LibraryMusic, contentDescription = "曲库") },
            label = { Text("曲库") }
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
