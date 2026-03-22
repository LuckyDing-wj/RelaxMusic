package com.relaxmusic.app.ui.screens.nowplaying

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
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.relaxmusic.app.domain.model.Song
import com.relaxmusic.app.ui.theme.RelaxMusicColors

@Composable
fun QueueScreen(
    queue: List<Song>,
    currentIndex: Int,
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    onRemove: (String) -> Unit
) {
    val colors = RelaxMusicColors
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
            Text("播放队列", style = MaterialTheme.typography.headlineMedium)
            Text("${queue.size} 首", color = colors.textSecondary, modifier = Modifier.padding(top = 12.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(queue, key = { _, song -> song.id }) { index, song ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (index == currentIndex) {
                            Icon(Icons.Rounded.GraphicEq, contentDescription = "current", tint = colors.accent)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (index == currentIndex) colors.accent else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = "${song.artist} · ${song.album}",
                                color = colors.textSecondary,
                                maxLines = 1
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = { onSongClick(song) }) {
                            Text("播放")
                        }
                        TextButton(onClick = { onRemove(song.id) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "remove")
                        }
                    }
                }
            }
        }
    }
}
