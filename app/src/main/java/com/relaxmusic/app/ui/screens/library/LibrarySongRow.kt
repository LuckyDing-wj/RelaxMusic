package com.relaxmusic.app.ui.screens.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.relaxmusic.app.domain.model.Playlist
import com.relaxmusic.app.ui.theme.RelaxMusicColors

@Immutable
data class SongRowUiModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val durationText: String,
    val isFavorite: Boolean,
    val isCurrent: Boolean
)

@Composable
fun LibrarySongRow(
    row: SongRowUiModel,
    playlists: List<Playlist> = emptyList(),
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit = {},
    onAddSongToPlaylist: (Long, String) -> Unit = { _, _ -> }
) {
    val colors = RelaxMusicColors
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, colors.panelBorder),
        colors = CardDefaults.cardColors(
            containerColor = if (row.isCurrent) colors.panelSurfaceStrong else colors.panelSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (row.isCurrent) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = "current song",
                        tint = colors.accent,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                    Text(
                        text = row.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (row.isCurrent) colors.accent else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth().then(
                            if (row.isCurrent) Modifier.basicMarquee() else Modifier
                        )
                    )
                    Text(
                        text = row.subtitle,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (playlists.isNotEmpty()) {
                    TextButton(onClick = onAddToPlaylist) {
                        Icon(
                            imageVector = Icons.Rounded.LibraryMusic,
                            contentDescription = "add to playlist",
                            tint = colors.textSecondary
                        )
                    }
                }
                TextButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (row.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "favorite",
                        tint = if (row.isFavorite) colors.accent else colors.textSecondary
                    )
                }
                Text(row.durationText)
            }
        }
    }
}
