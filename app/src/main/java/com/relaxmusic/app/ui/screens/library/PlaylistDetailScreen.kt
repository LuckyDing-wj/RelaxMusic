package com.relaxmusic.app.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.relaxmusic.app.domain.model.Playlist
import com.relaxmusic.app.domain.model.Song
import com.relaxmusic.app.ui.theme.RelaxMusicColors

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
    val colors = RelaxMusicColors
    var adding by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "back")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { renaming = true }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "rename")
                    }
                    IconButton(onClick = { deleting = true }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "delete")
                    }
                    Button(
                        onClick = { adding = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Rounded.PlaylistAdd, contentDescription = "add", modifier = Modifier.size(18.dp))
                        Text("加歌", modifier = Modifier.padding(start = 4.dp), fontSize = 13.sp)
                    }
                }
            }
        }

        item {
            Text(
                text = playlist?.name ?: "歌单",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp)
            )
            Text(
                text = "${playlistSongs.size} 首歌曲",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
        items(playlistSongs, key = { it.id }) { song ->
            val rowModel = remember(song, currentSongId) {
                SongRowUiModel(
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
            }
            Column {
                LibrarySongRow(
                    row = rowModel,
                    onClick = { onSongClick(song) },
                    onToggleFavorite = { onToggleFavorite(song.id) }
                )
                TextButton(
                    onClick = { onRemoveSong(song.id) },
                    modifier = Modifier.padding(start = 12.dp).height(32.dp)
                ) {
                    Text("从歌单移除", fontSize = 11.sp)
                }
            }
        }
    }

    if (adding && playlist != null) {
        PlaylistSongSelectionSheet(
            playlist = playlist,
            allSongs = allSongs,
            existingSongIds = playlistSongs.map { it.id }.toSet(),
            onDismiss = { adding = false },
            onAddSong = onAddSong
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
