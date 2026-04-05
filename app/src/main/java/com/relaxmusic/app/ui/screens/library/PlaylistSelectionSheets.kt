package com.relaxmusic.app.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.relaxmusic.app.domain.model.Playlist
import com.relaxmusic.app.domain.model.Song
import com.relaxmusic.app.ui.theme.RelaxMusicColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongPlaylistSelectionSheet(
    song: Song,
    playlists: List<Playlist>,
    selectedPlaylistIds: Set<Long>,
    onDismiss: () -> Unit,
    onAddSongToPlaylist: (Long, String) -> Unit
) {
    val colors = RelaxMusicColors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.panelBackground
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SheetHeader(
                title = "添加到歌单",
                subtitle = "${song.title} · ${song.artist}"
            )

            if (playlists.isEmpty()) {
                SheetEmptyState(message = "还没有歌单，先去新建一个歌单。")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        val alreadyAdded = playlist.id in selectedPlaylistIds
                        SheetRow(
                            title = playlist.name,
                            subtitle = "${playlist.songCount} 首歌曲",
                            trailing = if (alreadyAdded) "已添加" else "添加",
                            enabled = !alreadyAdded,
                            onClick = {
                                if (!alreadyAdded) {
                                    onAddSongToPlaylist(playlist.id, song.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistSongSelectionSheet(
    playlist: Playlist,
    allSongs: List<Song>,
    existingSongIds: Set<String>,
    onDismiss: () -> Unit,
    onAddSong: (String) -> Unit
) {
    val colors = RelaxMusicColors
    var query by rememberSaveable(playlist.id) { mutableStateOf("") }
    val candidates = remember(allSongs, existingSongIds, query) {
        val normalizedQuery = query.trim().lowercase()
        allSongs
            .asSequence()
            .filterNot { it.id in existingSongIds }
            .filter { song ->
                normalizedQuery.isBlank() || song.matchesQuery(normalizedQuery)
            }
            .toList()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.panelBackground
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SheetHeader(
                title = "添加到 ${playlist.name}",
                subtitle = "从完整曲库中选择要加入歌单的歌曲"
            )

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = "search songs")
                },
                label = { Text("搜索歌曲、艺术家、专辑") }
            )

            if (candidates.isEmpty()) {
                SheetEmptyState(
                    message = if (query.isBlank()) {
                        "没有更多可添加的歌曲了。"
                    } else {
                        "没有找到匹配的歌曲。"
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(candidates, key = { it.id }) { song ->
                        SheetRow(
                            title = song.title,
                            subtitle = "${song.artist} · ${song.album}",
                            trailing = "添加",
                            enabled = true,
                            onClick = { onAddSong(song.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetHeader(
    title: String,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = RelaxMusicColors.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SheetRow(
    title: String,
    subtitle: String,
    trailing: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val colors = RelaxMusicColors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.LibraryMusic,
                contentDescription = null,
                tint = if (enabled) colors.accent else colors.textSecondary.copy(alpha = 0.5f)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) colors.accent else colors.textSecondary
            )
        }
        HorizontalDivider(color = colors.panelBorder.copy(alpha = 0.25f))
    }
}

@Composable
private fun SheetEmptyState(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = RelaxMusicColors.textSecondary,
        modifier = Modifier.padding(vertical = 24.dp)
    )
}

private fun Song.matchesQuery(query: String): Boolean {
    return title.contains(query, ignoreCase = true) ||
        artist.contains(query, ignoreCase = true) ||
        album.contains(query, ignoreCase = true) ||
        fileName.contains(query, ignoreCase = true)
}
