package com.relaxmusic.app.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.relaxmusic.app.domain.model.Playlist
import com.relaxmusic.app.domain.model.Song
import com.relaxmusic.app.ui.components.EmptyLibraryView

@Composable
fun CollectionScreen(
    title: String,
    songs: List<Song>,
    playlists: List<Playlist>,
    currentSongId: String?,
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onAddSongToPlaylist: (Long, String) -> Unit,
    showBackButton: Boolean = true,
    emptyMessageAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBackButton) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "back")
                }
            }
            Text(
                title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            )
        }

        if (songs.isEmpty()) {
            EmptyLibraryView(
                onPickFolder = { emptyMessageAction?.invoke() ?: Unit },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(songs, key = { index, song -> "${song.id}-$index" }) { _, song ->
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
                    LibrarySongRow(
                        row = rowModel,
                        playlists = playlists,
                        onClick = { onSongClick(song) },
                        onToggleFavorite = { onToggleFavorite(song.id) },
                        onAddSongToPlaylist = onAddSongToPlaylist
                    )
                }
            }
        }
    }
}
