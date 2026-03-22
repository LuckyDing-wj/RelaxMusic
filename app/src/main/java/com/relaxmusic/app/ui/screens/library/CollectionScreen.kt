package com.relaxmusic.app.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.relaxmusic.app.domain.model.Playlist
import com.relaxmusic.app.domain.model.Song
import com.relaxmusic.app.ui.components.EmptyLibraryView
import com.relaxmusic.app.ui.theme.RelaxMusicColors

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
    emptyMessageAction: (() -> Unit)? = null,
    supportingText: String? = null
) {
    val colors = RelaxMusicColors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (showBackButton) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "back")
            }
        }
        Text(title, style = MaterialTheme.typography.headlineMedium)
        if (supportingText != null) {
            Text(supportingText, color = colors.textSecondary)
        }

        if (songs.isEmpty()) {
            EmptyLibraryView(
                onPickFolder = { emptyMessageAction?.invoke() ?: Unit },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(songs, key = { index, song -> "${song.id}-$index" }) { _, song ->
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
