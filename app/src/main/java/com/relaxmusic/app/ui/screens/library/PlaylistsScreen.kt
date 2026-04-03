package com.relaxmusic.app.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.relaxmusic.app.ui.theme.RelaxMusicColors

@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    onBack: () -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onRenamePlaylist: (Long, String) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    showBackButton: Boolean = true,
    onOpenPlaylist: (Playlist) -> Unit
) {
    val colors = RelaxMusicColors
    var creating by remember { mutableStateOf(false) }
    var playlistName by remember { mutableStateOf("") }
    var editingPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var deletingPlaylist by remember { mutableStateOf<Playlist?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (showBackButton) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "back")
                }
            } else {
                Box(modifier = Modifier.padding(24.dp))
            }
            Button(onClick = { creating = true }) {
                Icon(Icons.Rounded.PlaylistAdd, contentDescription = "create")
                Text("新建歌单", modifier = Modifier.padding(start = 6.dp))
            }
        }

        Text("歌单", style = MaterialTheme.typography.headlineMedium)

        if (playlists.isEmpty()) {
            Text("暂无歌单", color = colors.textSecondary)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(playlists, key = { it.id }) { playlist ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.panelSurface),
                        onClick = { onOpenPlaylist(playlist) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Rounded.LibraryMusic, contentDescription = "playlist", tint = colors.accent)
                                Column {
                                    Text(playlist.name, style = MaterialTheme.typography.titleMedium)
                                    Text("${playlist.songCount} 首歌曲", color = colors.textSecondary)
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { editingPlaylist = playlist }) {
                                    Icon(Icons.Rounded.Edit, contentDescription = "rename")
                                }
                                IconButton(onClick = { deletingPlaylist = playlist }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (creating) {
        AlertDialog(
            onDismissRequest = { creating = false },
            title = { Text("创建歌单") },
            text = {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text("歌单名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onCreatePlaylist(playlistName)
                    playlistName = ""
                    creating = false
                }) {
                    Text("创建")
                }
            },
            dismissButton = {
                TextButton(onClick = { creating = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (editingPlaylist != null) {
        var renameText by remember(editingPlaylist?.id) { mutableStateOf(editingPlaylist?.name.orEmpty()) }
        AlertDialog(
            onDismissRequest = { editingPlaylist = null },
            title = { Text("重命名歌单") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("歌单名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    editingPlaylist?.let { onRenamePlaylist(it.id, renameText) }
                    editingPlaylist = null
                }) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPlaylist = null }) {
                    Text("取消")
                }
            }
        )
    }

    if (deletingPlaylist != null) {
        AlertDialog(
            onDismissRequest = { deletingPlaylist = null },
            title = { Text("删除歌单") },
            text = { Text("确定删除 ${deletingPlaylist?.name} 吗？歌单中的关联会一起移除。") },
            confirmButton = {
                TextButton(onClick = {
                    deletingPlaylist?.let { onDeletePlaylist(it.id) }
                    deletingPlaylist = null
                }) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingPlaylist = null }) {
                    Text("取消")
                }
            }
        )
    }
}
