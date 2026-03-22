package com.relaxmusic.app.ui.screens.nowplaying

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Loop
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.relaxmusic.app.domain.model.LyricLine
import com.relaxmusic.app.domain.model.PlayMode
import com.relaxmusic.app.domain.model.Song
import com.relaxmusic.app.ui.theme.Accent
import com.relaxmusic.app.ui.theme.PanelBorder
import com.relaxmusic.app.ui.theme.TextSecondary
import com.relaxmusic.app.utils.TimeFormatter

private enum class CenterContentState {
    ARTWORK,
    LYRICS,
    NO_LYRICS
}

private fun playModeLabel(raw: String): String {
    return when (raw) {
        "SEQUENCE" -> "顺序播放"
        "REPEAT_ONE" -> "单曲循环"
        "REPEAT_ALL" -> "列表循环"
        "SHUFFLE" -> "随机播放"
        else -> raw
    }
}

@Composable
fun NowPlayingScreen(
    state: PlayerUiState,
    onBack: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onChangeProgress: (Float) -> Unit,
    onCyclePlayMode: () -> Unit,
    onOpenTimer: () -> Unit,
    onOpenQueue: () -> Unit
) {
    var centerContentState by remember(state.currentSong?.id) { mutableStateOf(CenterContentState.ARTWORK) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        NowPlayingHeader(onBack = onBack, onOpenTimer = onOpenTimer)

        CenterContentArea(
            currentSong = state.currentSong,
            lyrics = state.lyrics,
            currentLyricIndex = state.currentLyricIndex,
            isPlaying = state.isPlaying,
            contentState = centerContentState,
            onToggleContent = {
                centerContentState = when (centerContentState) {
                    CenterContentState.ARTWORK -> {
                        if (state.lyrics.isNotEmpty()) CenterContentState.LYRICS else CenterContentState.NO_LYRICS
                    }
                    CenterContentState.LYRICS,
                    CenterContentState.NO_LYRICS -> CenterContentState.ARTWORK
                }
            }
        )

        TrackMetaSection(
            currentSong = state.currentSong,
            isPlaying = state.isPlaying
        )

        ProgressSection(
            progress = state.progress,
            progressMs = state.progressMs,
            durationMs = state.durationMs,
            onChangeProgress = onChangeProgress
        )

        PlaybackControlsSection(
            playMode = state.playMode,
            isPlaying = state.isPlaying,
            sleepTimerRemaining = state.sleepTimerRemaining,
            onCyclePlayMode = onCyclePlayMode,
            onPrevious = onPrevious,
            onTogglePlay = onTogglePlay,
            onNext = onNext,
            onOpenQueue = onOpenQueue,
            onOpenTimer = onOpenTimer
        )
    }
}

@Composable
private fun NowPlayingHeader(
    onBack: () -> Unit,
    onOpenTimer: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Rounded.ArrowBack, contentDescription = "back")
        }
        Text("正在播放", style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = onOpenTimer) {
            Icon(Icons.Rounded.Bedtime, contentDescription = "timer")
        }
    }
}

@Composable
private fun CenterContentArea(
    currentSong: Song?,
    lyrics: List<LyricLine>,
    currentLyricIndex: Int,
    isPlaying: Boolean,
    contentState: CenterContentState,
    onToggleContent: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleContent),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, PanelBorder),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f))
    ) {
        when (contentState) {
            CenterContentState.ARTWORK -> ArtworkContent(isPlaying = isPlaying)
            CenterContentState.LYRICS -> LyricsContent(lyrics = lyrics, currentLyricIndex = currentLyricIndex)
            CenterContentState.NO_LYRICS -> NoLyricsContent(currentSong = currentSong)
        }
    }
}

@Composable
private fun ArtworkContent(isPlaying: Boolean) {
    val albumArtScale = if (isPlaying) {
        rememberInfiniteTransition(label = "album-art").animateFloat(
            initialValue = 1f,
            targetValue = 1.018f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2400),
                repeatMode = RepeatMode.Reverse
            ),
            label = "album-art-scale"
        ).value
    } else {
        1f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .padding(18.dp)
            .scale(albumArtScale)
            .clip(RoundedCornerShape(24.dp))
            .background(Accent.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Text("Album Art", color = TextSecondary)
    }
}

@Composable
private fun LyricsContent(
    lyrics: List<LyricLine>,
    currentLyricIndex: Int
) {
    val lyricListState = rememberLazyListState()

    LaunchedEffect(currentLyricIndex, lyrics.size) {
        if (currentLyricIndex >= 0 && lyrics.isNotEmpty()) {
            val visibleItems = lyricListState.layoutInfo.visibleItemsInfo
            val firstVisible = visibleItems.firstOrNull()?.index ?: 0
            val lastVisible = visibleItems.lastOrNull()?.index ?: 0
            val targetIndex = (currentLyricIndex - 4).coerceAtLeast(0)

            if (currentLyricIndex <= firstVisible + 1 || currentLyricIndex >= lastVisible - 1) {
                lyricListState.animateScrollToItem(targetIndex)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        state = lyricListState,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(lyrics) { index, line ->
            Text(
                text = line.text,
                color = if (index == currentLyricIndex) Accent else TextSecondary,
                style = if (index == currentLyricIndex) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NoLyricsContent(currentSong: Song?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .padding(18.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Accent.copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("无歌词", style = MaterialTheme.typography.headlineMedium, color = Accent)
            Text(
                text = currentSong?.let { "当前歌曲未找到本地歌词" } ?: "当前没有歌曲",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TrackMetaSection(
    currentSong: Song?,
    isPlaying: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = currentSong?.title ?: "还没有开始播放",
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().then(
                if (currentSong != null && isPlaying) Modifier.basicMarquee() else Modifier
            )
        )
        Text(
            text = currentSong?.let { "${it.artist} · ${it.album}" } ?: "从曲库中选择一首歌开始",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ProgressSection(
    progress: Float,
    progressMs: Long,
    durationMs: Long,
    onChangeProgress: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Slider(value = progress, onValueChange = onChangeProgress)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(TimeFormatter.formatSongDuration(progressMs))
            Text(TimeFormatter.formatSongDuration(durationMs))
        }
    }
}

@Composable
private fun PlaybackControlsSection(
    playMode: PlayMode,
    isPlaying: Boolean,
    sleepTimerRemaining: Long,
    onCyclePlayMode: () -> Unit,
    onPrevious: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenTimer: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCyclePlayMode) {
                Icon(Icons.Rounded.Loop, contentDescription = "play mode")
            }
            IconButton(onClick = onPrevious) {
                Icon(Icons.Rounded.FastRewind, contentDescription = "previous")
            }
            IconButton(onClick = onTogglePlay) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.PauseCircle else Icons.Rounded.PlayCircle,
                    contentDescription = "toggle play"
                )
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Rounded.FastForward, contentDescription = "next")
            }
            IconButton(onClick = onOpenQueue) {
                Icon(Icons.Rounded.QueueMusic, contentDescription = "queue")
            }
            Button(onClick = onOpenTimer) {
                Text(if (sleepTimerRemaining > 0) "剩余 ${sleepTimerRemaining / 60}m" else "定时")
            }
        }

        Text("播放模式: ${playModeLabel(playMode.name)}", color = TextSecondary)
    }
}
