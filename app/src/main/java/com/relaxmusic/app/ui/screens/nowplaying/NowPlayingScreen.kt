package com.relaxmusic.app.ui.screens.nowplaying

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Reorder
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.relaxmusic.app.data.local.EmbeddedArtworkReader
import com.relaxmusic.app.domain.model.LyricLine
import com.relaxmusic.app.domain.model.PlayMode
import com.relaxmusic.app.domain.model.Song
import com.relaxmusic.app.ui.theme.RelaxMusicColors
import com.relaxmusic.app.utils.TimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class CenterContentMode {
    ARTWORK,
    LYRICS,
    NO_LYRICS
}

fun CenterContentMode.normalize(hasLyrics: Boolean): CenterContentMode {
    return when {
        this == CenterContentMode.NO_LYRICS && hasLyrics -> CenterContentMode.LYRICS
        this == CenterContentMode.LYRICS && !hasLyrics -> CenterContentMode.NO_LYRICS
        else -> this
    }
}

fun CenterContentMode.nextOnTap(hasLyrics: Boolean): CenterContentMode {
    return when (this) {
        CenterContentMode.ARTWORK -> if (hasLyrics) CenterContentMode.LYRICS else CenterContentMode.NO_LYRICS
        CenterContentMode.LYRICS,
        CenterContentMode.NO_LYRICS -> CenterContentMode.ARTWORK
    }
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

private fun playModeIcon(playMode: PlayMode): ImageVector {
    return when (playMode) {
        PlayMode.SEQUENCE -> Icons.Rounded.Reorder
        PlayMode.REPEAT_ONE -> Icons.Rounded.RepeatOne
        PlayMode.REPEAT_ALL -> Icons.Rounded.Repeat
        PlayMode.SHUFFLE -> Icons.Rounded.Shuffle
    }
}

@Composable
fun NowPlayingScreen(
    artworkState: NowPlayingArtworkUiState,
    lyricsState: NowPlayingLyricsUiState,
    trackState: NowPlayingTrackUiState,
    progressState: NowPlayingProgressUiState,
    controlsState: NowPlayingControlsUiState,
    onBack: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onChangeProgress: (Float) -> Unit,
    onCyclePlayMode: () -> Unit,
    onOpenTimer: () -> Unit
) {
    val colors = RelaxMusicColors
    var centerContentMode by remember(artworkState.currentSong?.id) { mutableStateOf(CenterContentMode.ARTWORK) }
    val resolvedCenterContentMode = centerContentMode.normalize(hasLyrics = lyricsState.lyrics.isNotEmpty())

    LaunchedEffect(resolvedCenterContentMode) {
        if (centerContentMode != resolvedCenterContentMode) {
            centerContentMode = resolvedCenterContentMode
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                NowPlayingHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    onBack = onBack,
                    onOpenTimer = onOpenTimer
                )
            },
            bottomBar = {
                BottomPlaybackDock(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    currentSong = trackState.currentSong,
                    isPlaying = trackState.isPlaying,
                    progress = progressState.progress,
                    progressMs = progressState.progressMs,
                    durationMs = progressState.durationMs,
                    playMode = controlsState.playMode,
                    onChangeProgress = onChangeProgress,
                    onCyclePlayMode = onCyclePlayMode,
                    onPrevious = onPrevious,
                    onTogglePlay = onTogglePlay,
                    onNext = onNext
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 8.dp)
            ) {
                when (resolvedCenterContentMode) {
                    CenterContentMode.ARTWORK -> ArtworkCenterCard(
                        currentSong = artworkState.currentSong,
                        isPlaying = artworkState.isPlaying,
                        colors = colors,
                        onToggleContent = {
                            centerContentMode = resolvedCenterContentMode.nextOnTap(hasLyrics = lyricsState.lyrics.isNotEmpty())
                        }
                    )
                    CenterContentMode.LYRICS -> LyricsCenterCard(
                        lyrics = lyricsState.lyrics,
                        currentLyricIndex = lyricsState.currentLyricIndex,
                        colors = colors,
                        onToggleContent = {
                            centerContentMode = resolvedCenterContentMode.nextOnTap(hasLyrics = lyricsState.lyrics.isNotEmpty())
                        }
                    )
                    CenterContentMode.NO_LYRICS -> NoLyricsCenterCard(
                        currentSong = artworkState.currentSong,
                        colors = colors,
                        onToggleContent = {
                            centerContentMode = resolvedCenterContentMode.nextOnTap(hasLyrics = lyricsState.lyrics.isNotEmpty())
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NowPlayingHeader(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onOpenTimer: () -> Unit
) {
    Row(
        modifier = modifier,
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
private fun BottomPlaybackDock(
    modifier: Modifier = Modifier,
    currentSong: Song?,
    isPlaying: Boolean,
    progress: Float,
    progressMs: Long,
    durationMs: Long,
    playMode: PlayMode,
    onChangeProgress: (Float) -> Unit,
    onCyclePlayMode: () -> Unit,
    onPrevious: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TrackMetaSection(
            currentSong = currentSong,
            isPlaying = isPlaying
        )

        ProgressSection(
            progress = progress,
            progressMs = progressMs,
            durationMs = durationMs,
            onChangeProgress = onChangeProgress
        )

        PlaybackControlsSection(
            playMode = playMode,
            isPlaying = isPlaying,
            onCyclePlayMode = onCyclePlayMode,
            onPrevious = onPrevious,
            onTogglePlay = onTogglePlay,
            onNext = onNext
        )
    }
}

@Composable
private fun ArtworkCenterCard(
    currentSong: Song?,
    isPlaying: Boolean,
    colors: com.relaxmusic.app.ui.theme.RelaxMusicColorPalette,
    onToggleContent: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onToggleContent)
    ) {
        ArtworkContent(currentSong = currentSong, isPlaying = isPlaying, colors = colors)
    }
}

@Composable
private fun LyricsCenterCard(
    lyrics: List<LyricLine>,
    currentLyricIndex: Int,
    colors: com.relaxmusic.app.ui.theme.RelaxMusicColorPalette,
    onToggleContent: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onToggleContent)
    ) {
        LyricsContent(lyrics = lyrics, currentLyricIndex = currentLyricIndex, colors = colors)
    }
}

@Composable
private fun NoLyricsCenterCard(
    currentSong: Song?,
    colors: com.relaxmusic.app.ui.theme.RelaxMusicColorPalette,
    onToggleContent: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onToggleContent)
    ) {
        NoLyricsContent(currentSong = currentSong, colors = colors)
    }
}

@Composable
private fun ArtworkContent(
    currentSong: Song?,
    isPlaying: Boolean,
    colors: com.relaxmusic.app.ui.theme.RelaxMusicColorPalette
) {
    val context = LocalContext.current
    val artworkReader = remember { EmbeddedArtworkReader() }
    val albumArtBitmap by produceState<Bitmap?>(initialValue = null, currentSong?.uri) {
        value = withContext(Dispatchers.IO) {
            artworkReader.read(context, currentSong?.uri)?.let { bytes ->
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        }
    }
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
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (albumArtBitmap != null) {
            Image(
                bitmap = albumArtBitmap!!.asImageBitmap(),
                contentDescription = currentSong?.title ?: "当前歌曲封面",
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(1f)
                    .scale(albumArtScale),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = "暂无封面",
                    tint = colors.accent
                )
                Text(
                    text = currentSong?.album?.takeIf { it.isNotBlank() } ?: "暂无内嵌封面",
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun LyricsContent(
    lyrics: List<LyricLine>,
    currentLyricIndex: Int,
    colors: com.relaxmusic.app.ui.theme.RelaxMusicColorPalette
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
            .fillMaxSize()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        state = lyricListState,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(lyrics) { index, line ->
            Text(
                text = line.text,
                color = if (index == currentLyricIndex) colors.accent else colors.textSecondary,
                style = if (index == currentLyricIndex) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NoLyricsContent(
    currentSong: Song?,
    colors: com.relaxmusic.app.ui.theme.RelaxMusicColorPalette
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("暂无歌词", style = MaterialTheme.typography.titleMedium, color = colors.accent)
            Text(
                text = currentSong?.let { "当前歌曲未找到本地歌词" } ?: "当前没有歌曲",
                color = colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium,
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
    val colors = RelaxMusicColors
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = currentSong?.title ?: "还没有开始播放",
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().then(
                if (currentSong != null && isPlaying) Modifier.basicMarquee() else Modifier
            )
        )
        Text(
            text = currentSong?.let { "${it.artist} · ${it.album}" } ?: "从曲库中选择一首歌开始",
            color = colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
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
    var pendingProgress by remember { mutableStateOf<Float?>(null) }
    val sliderValue = pendingProgress ?: progress

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Slider(
            value = sliderValue,
            onValueChange = { pendingProgress = it },
            onValueChangeFinished = {
                pendingProgress?.let(onChangeProgress)
                pendingProgress = null
            }
        )
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
    onCyclePlayMode: () -> Unit,
    onPrevious: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCyclePlayMode) {
                Icon(
                    imageVector = playModeIcon(playMode),
                    contentDescription = playModeLabel(playMode.name),
                    tint = RelaxMusicColors.accent
                )
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
        }
    }
}
