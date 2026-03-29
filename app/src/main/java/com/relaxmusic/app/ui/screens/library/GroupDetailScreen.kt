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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.relaxmusic.app.domain.model.Playlist
import com.relaxmusic.app.domain.model.Song
import com.relaxmusic.app.utils.TimeFormatter

@Composable
fun GroupDetailScreen(
    title: String,
    songs: List<Song>,
    currentSongId: String?,
    playlists: List<Playlist>,
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onAddSongToPlaylist: (Long, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Rounded.ArrowBack, contentDescription = "back")
        }
        Text(title, style = MaterialTheme.typography.headlineMedium)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(songs, key = { it.id }) { song ->
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
