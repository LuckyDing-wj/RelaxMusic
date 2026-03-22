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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.relaxmusic.app.domain.model.Album
import com.relaxmusic.app.ui.theme.RelaxMusicColors

@Composable
fun AlbumsScreen(
    albums: List<Album>,
    onBack: () -> Unit,
    onOpenAlbum: (Album) -> Unit
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
            Text("${albums.size} 张", color = colors.textSecondary, modifier = Modifier.padding(top = 12.dp))
        }
        Text("专辑", style = MaterialTheme.typography.headlineMedium)
        Text("按专辑浏览曲库内容。", color = colors.textSecondary)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(albums, key = { "${it.name}-${it.artist}" }) { album ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.panelSurface),
                    onClick = { onOpenAlbum(album) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Rounded.Album, contentDescription = "album", tint = colors.accent)
                        Column {
                            Text(album.name, style = MaterialTheme.typography.titleMedium)
                            Text("${album.artist} · ${album.songs.size} 首", color = colors.textSecondary)
                        }
                    }
                }
            }
        }
    }
}
