package com.relaxmusic.app.ui.screens.nowplaying

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Loop
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayCircle
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
    var isFullscreenLyrics by remember { mutableStateOf(false) }
    val resolvedCenterContentMode = centerContentMode.normalize(hasLyrics = lyricsState.lyrics.isNotEmpty())

    LaunchedEffect(resolvedCenterContentMode) {
        if (centerContentMode != resolvedCenterContentMode) {
            centerContentMode = resolvedCenterContentMode
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            NowPlayingHeader(onBack = onBack, onOpenTimer = onOpenTimer)

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
                    },
                    onEnterFullscreen = { isFullscreenLyrics = true }
                )
                CenterContentMode.NO_LYRICS -> NoLyricsCenterCard(
                    currentSong = artworkState.currentSong,
                    colors = colors,
                    onToggleContent = {
                        centerContentMode = resolvedCenterContentMode.nextOnTap(hasLyrics = lyricsState.lyrics.isNotEmpty())
                    }
                )
            }

            TrackMetaSection(
                currentSong = trackState.currentSong,
                isPlaying = trackState.isPlaying
            )

            ProgressSection(
                progress = progressState.progress,
                progressMs = progressState.progressMs,
                durationMs = progressState.durationMs,
                onChangeProgress = onChangeProgress
            )

            PlaybackControlsSection(
                playMode = controlsState.playMode,
                isPlaying = controlsState.isPlaying,
                onCyclePlayMode = onCyclePlayMode,
                onPrevious = onPrevious,
                onTogglePlay = onTogglePlay,
                onNext = onNext
            )
        }

        // Fullscreen lyrics overlay
        AnimatedVisibility(
            visible = isFullscreenLyrics,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.zIndex(10f)
        ) {
            FullscreenLyricsOverlay(
                lyrics = lyricsState.lyrics,
                currentLyricIndex = lyricsState.currentLyricIndex,
                currentSong = trackState.currentSong,
                progress = progressState.progress,
                progressMs = progressState.progressMs,
                durationMs = progressState.durationMs,
                isPlaying = controlsState.isPlaying,
                colors = colors,
                onExit = { isFullscreenLyrics = false },
                onTogglePlay = onTogglePlay,
                onPrevious = onPrevious,
                onNext = onNext,
                onChangeProgress = onChangeProgress
            )
        }
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
private fun ArtworkCenterCard(
    currentSong: Song?,
    isPlaying: Boolean,
    colors: com.relaxmusic.app.ui.theme.RelaxMusicColorPalette,
    onToggleContent: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleContent),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, colors.panelBorder),
        colors = CardDefaults.cardColors(containerColor = colors.panelSurface)
    ) {
        ArtworkContent(currentSong = currentSong, isPlaying = isPlaying, colors = colors)
    }
}

@Composable
private fun LyricsCenterCard(
    lyrics: List<LyricLine>,
    currentLyricIndex: Int,
    colors: com.relaxmusic.app.ui.theme.RelaxMusicColorPalette,
    onToggleContent: () -> Unit,
    onEnterFullscreen: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleContent),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, colors.panelBorder),
        colors = CardDefaults.cardColors(containerColor = colors.panelSurface)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
            LyricsContent(lyrics = lyrics, currentLyricIndex = currentLyricIndex, colors = colors)
            // Fullscreen button
            IconButton(
                onClick = onEnterFullscreen,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Icon(
                    Icons.Rounded.Fullscreen,
                    contentDescription = "全屏歌词",
                    tint = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun NoLyricsCenterCard(
    currentSong: Song?,
    colors: com.relaxmusic.app.ui.theme.RelaxMusicColorPalette,
    onToggleContent: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleContent),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, colors.panelBorder),
        colors = CardDefaults.cardColors(containerColor = colors.panelSurface)
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
            .fillMaxWidth()
            .height(320.dp)
            .padding(18.dp)
            .scale(albumArtScale)
            .clip(RoundedCornerShape(24.dp))
            .background(colors.accent.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        if (albumArtBitmap != null) {
            Image(
                bitmap = albumArtBitmap!!.asImageBitmap(),
                contentDescription = currentSong?.title ?: "当前歌曲封面",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(
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
            .fillMaxWidth()
            .height(320.dp)
            .padding(horizontal = 20.dp, vertical = 18.dp),
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
            .fillMaxWidth()
            .height(320.dp)
            .padding(18.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(colors.accent.copy(alpha = 0.08f)),
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
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
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

        UtilityActionCard(
            title = playModeLabel(playMode.name),
            icon = { Icon(Icons.Rounded.Loop, contentDescription = "play mode") },
            onClick = onCyclePlayMode,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun UtilityActionCard(
    title: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RelaxMusicColors
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, colors.panelBorder),
        colors = CardDefaults.cardColors(containerColor = colors.panelSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FullscreenLyricsOverlay(
    lyrics: List<LyricLine>,
    currentLyricIndex: Int,
    currentSong: Song?,
    progress: Float,
    progressMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    colors: com.relaxmusic.app.ui.theme.RelaxMusicColorPalette,
    onExit: () -> Unit,
    onTogglePlay: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onChangeProgress: (Float) -> Unit
) {
    var showControls by remember { mutableStateOf(true) }
    val lyricListState = rememberLazyListState()

    // Auto-scroll lyrics
    LaunchedEffect(currentLyricIndex, lyrics.size) {
        if (currentLyricIndex >= 0 && lyrics.isNotEmpty()) {
            val targetIndex = (currentLyricIndex - 6).coerceAtLeast(0)
            lyricListState.animateScrollToItem(targetIndex)
        }
    }

    // Auto-hide controls after 3 seconds
    LaunchedEffect(showControls) {
        if (showControls) {
            kotlinx.coroutines.delay(3000)
            showControls = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colors.appBackgroundStart,
                        colors.appBackgroundEnd,
                        colors.appBackgroundStart
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { showControls = !showControls }
                )
            }
    ) {
        // Lyrics content (full screen)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(top = 80.dp, bottom = 240.dp),
            state = lyricListState,
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(lyrics) { index, line ->
                val isActive = index == currentLyricIndex
                Text(
                    text = line.text,
                    color = if (isActive) colors.accent else colors.textSecondary.copy(alpha = 0.6f),
                    fontSize = if (isActive) 24.sp else 18.sp,
                    style = if (isActive) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Top bar: exit button
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200)),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            IconButton(
                onClick = onExit,
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.panelSurface)
                        .padding(4.dp)
                ) {
                    Icon(
                        Icons.Rounded.ExpandLess,
                        contentDescription = "退出全屏",
                        tint = colors.textPrimary
                    )
                }
            }
        }

        // Bottom controls overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                colors.appBackgroundEnd.copy(alpha = 0.95f)
                            )
                        )
                    )
                    .padding(bottom = 32.dp, top = 24.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Song info
                Text(
                    text = currentSong?.title ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentSong?.artist ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Progress slider
                var pendingProgress by remember { mutableStateOf<Float?>(null) }
                val sliderValue = pendingProgress ?: progress

                Slider(
                    value = sliderValue,
                    onValueChange = { pendingProgress = it },
                    onValueChangeFinished = {
                        pendingProgress?.let(onChangeProgress)
                        pendingProgress = null
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = TimeFormatter.formatSongDuration(progressMs),
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = TimeFormatter.formatSongDuration(durationMs),
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Playback controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPrevious) {
                        Icon(
                            Icons.Rounded.FastRewind,
                            contentDescription = "previous",
                            tint = colors.textPrimary,
                            modifier = Modifier.scale(1.3f)
                        )
                    }
                    IconButton(onClick = onTogglePlay) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.PauseCircle else Icons.Rounded.PlayCircle,
                            contentDescription = "toggle play",
                            tint = colors.textPrimary,
                            modifier = Modifier.scale(2f)
                        )
                    }
                    IconButton(onClick = onNext) {
                        Icon(
                            Icons.Rounded.FastForward,
                            contentDescription = "next",
                            tint = colors.textPrimary,
                            modifier = Modifier.scale(1.3f)
                        )
                    }
                }
            }
        }
    }
}
