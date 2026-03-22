package com.relaxmusic.app.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.relaxmusic.app.domain.model.Playlist
import com.relaxmusic.app.domain.model.Song
import com.relaxmusic.app.ui.theme.TextSecondary

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist?,
    playlistSongs: List<Song>,
    allSongs: List<Song>,
    currentSongId: String?,
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onAddSong: (String) -> Unit,
    onRemoveSong: (String) -> Unit,
    onRenamePlaylist: (Long, String) -> Unit,
    onDeletePlaylist: (Long) -> Unit
) {
    var adding by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "back")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(onClick = { renaming = true }) {
                    Icon(Icons.Rounded.Edit, contentDescription = "rename")
                }
                IconButton(onClick = { deleting = true }) {
                    Icon(Icons.Rounded.Delete, contentDescription = "delete")
                }
                Button(onClick = { adding = true }) {
                    Icon(Icons.Rounded.PlaylistAdd, contentDescription = "add")
                    Text("添加歌曲", modifier = Modifier.padding(start = 6.dp))
                }
            }
        }

        Text(playlist?.name ?: "歌单", style = MaterialTheme.typography.headlineMedium)
        Text("${playlistSongs.size} 首歌曲", color = TextSecondary)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(playlistSongs, key = { it.id }) { song ->
                val rowModel = SongRowUiModel(
                    id = song.id,
                    title = song.title,
                    subtitle = if (currentSongId == song.id) {
                        "正在播放 · ${song.artist} · ${song.album}"
                    } else {
                        "${song.artist} · ${song.album}"
                    },
                    durationText = com.relaxmusic.app.utils.TimeFormatter.formatSongDuration(song.duration),
                    isFavorite = song.isFavorite,
                    isCurrent = currentSongId == song.id
                )
                LibrarySongRow(
                    row = rowModel,
                    onClick = { onSongClick(song) },
                    onToggleFavorite = { onToggleFavorite(song.id) }
                )
                TextButton(onClick = { onRemoveSong(song.id) }) {
                    Text("从歌单移除")
                }
            }
        }
    }

    if (adding && playlist != null) {
        AlertDialog(
            onDismissRequest = { adding = false },
            title = { Text("添加到 ${playlist.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allSongs.filterNot { existing -> playlistSongs.any { it.id == existing.id } }.take(12).forEach { song ->
                        TextButton(onClick = {
                            onAddSong(song.id)
                            adding = false
                        }) {
                            Text(song.title)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { adding = false }) {
                    Text("关闭")
                }
            }
        )
    }

    if (renaming && playlist != null) {
        var renameText by remember(playlist.id) { mutableStateOf(playlist.name) }
        AlertDialog(
            onDismissRequest = { renaming = false },
            title = { Text("重命名歌单") },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("歌单名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onRenamePlaylist(playlist.id, renameText)
                    renaming = false
                }) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { renaming = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (deleting && playlist != null) {
        AlertDialog(
            onDismissRequest = { deleting = false },
            title = { Text("删除歌单") },
            text = { Text("确定删除 ${playlist.name} 吗？") },
            confirmButton = {
                TextButton(onClick = {
                    onDeletePlaylist(playlist.id)
                    deleting = false
                    onBack()
                }) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleting = false }) {
                    Text("取消")
                }
            }
        )
    }
}
