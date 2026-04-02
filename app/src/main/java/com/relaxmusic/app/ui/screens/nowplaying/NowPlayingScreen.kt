package com.relaxmusic.app.ui.screens.nowplaying

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Reorder
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue
import com.relaxmusic.app.domain.model.LyricLine
import com.relaxmusic.app.domain.model.PlayMode
import com.relaxmusic.app.domain.model.Song
import com.relaxmusic.app.ui.components.PremiumSurface
import com.relaxmusic.app.ui.theme.RelaxMusicColors
import com.relaxmusic.app.utils.TimeFormatter

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

private const val DEFAULT_LYRIC_FONT_SCALE = 1f
private val LYRIC_FONT_SCALE_RANGE = 0.85f..1.3f
private val LYRIC_ACCENT_SWATCHES = listOf(
    Color.Unspecified,
    Color(0xFFFF8A65),
    Color(0xFF4FC3F7),
    Color(0xFFA5D66F),
    Color(0xFFF48FB1)
)

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
    onBack: (() -> Unit)?,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onChangeProgress: (Float) -> Unit,
    onCyclePlayMode: () -> Unit,
    onOpenTimer: () -> Unit
) {
    val colors = RelaxMusicColors
    val hasCurrentSong = trackState.currentSong != null
    var centerContentMode by remember(artworkState.currentSong?.id) { mutableStateOf(CenterContentMode.ARTWORK) }
    var lyricStylePanelVisible by rememberSaveable { mutableStateOf(false) }
    var lyricFontScale by rememberSaveable { mutableStateOf(DEFAULT_LYRIC_FONT_SCALE) }
    var lyricAccentIndex by rememberSaveable { mutableStateOf(0) }
    val resolvedCenterContentMode = centerContentMode.normalize(hasLyrics = lyricsState.lyrics.isNotEmpty())
    val lyricHighlightColor = LYRIC_ACCENT_SWATCHES
        .getOrNull(lyricAccentIndex)
        ?.takeIf { it != Color.Unspecified }
        ?: colors.accent

    LaunchedEffect(resolvedCenterContentMode) {
        if (centerContentMode != resolvedCenterContentMode) {
            centerContentMode = resolvedCenterContentMode
        }
        if (resolvedCenterContentMode != CenterContentMode.LYRICS && lyricStylePanelVisible) {
            lyricStylePanelVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundBrush)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                NowPlayingHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    title = if (hasCurrentSong) "正在播放" else "播放",
                    onBack = onBack
                )
            },
            bottomBar = {
                if (hasCurrentSong) {
                    BottomPlaybackDock(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 8.dp, bottom = 4.dp),
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
                        onNext = onNext,
                        onOpenTimer = onOpenTimer
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 2.dp)
            ) {
                if (hasCurrentSong) {
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
                            lyricFontScale = lyricFontScale,
                            lyricHighlightColor = lyricHighlightColor,
                            showStyleControls = lyricStylePanelVisible,
                            colors = colors,
                            onToggleStyleControls = { lyricStylePanelVisible = !lyricStylePanelVisible },
                            onLyricFontScaleChange = { lyricFontScale = it },
                            onLyricAccentChange = { lyricAccentIndex = it },
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
                } else {
                    PlayerEmptyState(colors = colors)
                }
            }
        }
    }
}

@Composable
private fun NowPlayingHeader(
    modifier: Modifier = Modifier,
    title: String,
    onBack: (() -> Unit)?
) {
    val colors = RelaxMusicColors
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "back", tint = colors.textPrimary)
            }
        } else {
            Box(modifier = Modifier.size(48.dp))
        }
        Text(title, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
        Box(modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun PlayerEmptyState(
    colors: com.relaxmusic.app.ui.theme.RelaxMusicColorPalette
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = "还没有开始播放",
                tint = colors.accent,
                modifier = Modifier.size(72.dp)
            )
            Text(
                text = "还没有开始播放",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "从首页或列表选择一首歌开始",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
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
    onNext: () -> Unit,
    onOpenTimer: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PremiumSurface(strong = true) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    onNext = onNext,
                    onOpenTimer = onOpenTimer
                )
            }
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
    lyricFontScale: Float,
    lyricHighlightColor: Color,
    showStyleControls: Boolean,
    colors: com.relaxmusic.app.ui.theme.RelaxMusicColorPalette,
    onToggleStyleControls: () -> Unit,
    onLyricFontScaleChange: (Float) -> Unit,
    onLyricAccentChange: (Int) -> Unit,
    onToggleContent: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onToggleContent)
    ) {
        LyricsContent(
            lyrics = lyrics,
            currentLyricIndex = currentLyricIndex,
            lyricFontScale = lyricFontScale,
            lyricHighlightColor = lyricHighlightColor,
            showStyleControls = showStyleControls,
            colors = colors,
            onToggleStyleControls = onToggleStyleControls,
            onLyricFontScaleChange = onLyricFontScaleChange,
            onLyricAccentChange = onLyricAccentChange
        )
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
    val density = LocalDensity.current
    val artworkCache = remember { ArtworkBitmapCache() }
    val albumArtBitmap by produceState<Bitmap?>(initialValue = null, currentSong?.uri) {
        value = artworkCache.load(context, currentSong?.uri, with(density) { 320.dp.roundToPx() })
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
            PremiumSurface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .scale(albumArtScale),
                strong = true,
                fillWidth = false,
                shape = RoundedCornerShape(34.dp)
            ) {
                Image(
                    bitmap = albumArtBitmap!!.asImageBitmap(),
                    contentDescription = currentSong?.title ?: "当前歌曲封面",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            PremiumSurface(
                modifier = Modifier.fillMaxWidth(0.92f),
                strong = true,
                fillWidth = false,
                shape = RoundedCornerShape(34.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = "暂无封面",
                        tint = colors.accent,
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = currentSong?.album?.takeIf { it.isNotBlank() } ?: "暂无内嵌封面",
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LyricsContent(
    lyrics: List<LyricLine>,
    currentLyricIndex: Int,
    lyricFontScale: Float,
    lyricHighlightColor: Color,
    showStyleControls: Boolean,
    onToggleStyleControls: () -> Unit,
    onLyricFontScaleChange: (Float) -> Unit,
    onLyricAccentChange: (Int) -> Unit,
    colors: com.relaxmusic.app.ui.theme.RelaxMusicColorPalette
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val lyricListState = rememberLazyListState()
        val edgePadding = (maxHeight * 0.30f).coerceAtLeast(72.dp)
        val consumeTapModifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {}
        )
        val activeLyricStyle = MaterialTheme.typography.titleLarge.copy(
            fontSize = MaterialTheme.typography.titleLarge.fontSize * lyricFontScale
        )
        val regularLyricStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = MaterialTheme.typography.bodyLarge.fontSize * lyricFontScale
        )

        LaunchedEffect(currentLyricIndex, lyrics.size) {
            if (currentLyricIndex >= 0 && lyrics.isNotEmpty()) {
                val targetIndex = (currentLyricIndex - 1).coerceAtLeast(0)
                val farJump = (lyricListState.firstVisibleItemIndex - targetIndex).absoluteValue > 4
                if (farJump) {
                    lyricListState.scrollToItem(index = targetIndex)
                } else {
                    lyricListState.animateScrollToItem(index = targetIndex)
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                state = lyricListState,
                contentPadding = PaddingValues(vertical = edgePadding),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(lyrics) { index, line ->
                    val distance = (index - currentLyricIndex).absoluteValue
                    val targetAlpha = when {
                        currentLyricIndex < 0 -> 0.58f
                        distance == 0 -> 1f
                        distance == 1 -> 0.72f
                        distance == 2 -> 0.46f
                        else -> 0.24f
                    }
                    val targetScale = when {
                        currentLyricIndex < 0 -> 1f
                        distance == 0 -> 1.08f
                        distance == 1 -> 1f
                        else -> 0.94f
                    }
                    val animatedAlpha by animateFloatAsState(
                        targetValue = targetAlpha,
                        animationSpec = tween(durationMillis = 220),
                        label = "lyric-alpha"
                    )
                    val shouldAnimateEmphasis = distance <= 1
                    val renderedScale = if (shouldAnimateEmphasis) {
                        animateFloatAsState(
                            targetValue = targetScale,
                            animationSpec = spring(dampingRatio = 0.9f, stiffness = 500f),
                            label = "lyric-scale"
                        ).value
                    } else {
                        targetScale
                    }
                    val renderedColor = if (shouldAnimateEmphasis) {
                        animateColorAsState(
                            targetValue = if (distance == 0) {
                                lyricHighlightColor
                            } else {
                                colors.textSecondary.copy(alpha = 0.9f)
                            },
                            animationSpec = tween(durationMillis = 220),
                            label = "lyric-color"
                        ).value
                    } else {
                        colors.textSecondary.copy(alpha = 0.9f)
                    }

                    Text(
                        text = line.text,
                        color = renderedColor,
                        style = if (distance == 0) activeLyricStyle else regularLyricStyle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(animatedAlpha)
                            .scale(renderedScale),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 4.dp)
                    .then(consumeTapModifier),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PremiumSurface(
                    modifier = Modifier
                        .size(46.dp)
                        .clickable(onClick = onToggleStyleControls),
                    strong = showStyleControls,
                    fillWidth = false,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = "歌词样式",
                            tint = colors.textPrimary
                        )
                    }
                }

                if (showStyleControls) {
                    PremiumSurface(
                        modifier = Modifier.width(224.dp),
                        strong = true,
                        fillWidth = false,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .width(224.dp)
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "歌词样式",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "字号 ${(lyricFontScale * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                            Slider(
                                value = lyricFontScale,
                                onValueChange = onLyricFontScaleChange,
                                valueRange = LYRIC_FONT_SCALE_RANGE
                            )
                            Text(
                                text = "高亮色",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                LYRIC_ACCENT_SWATCHES.forEachIndexed { index, swatch ->
                                    val resolvedSwatchColor = if (swatch == Color.Unspecified) {
                                        colors.accent
                                    } else {
                                        swatch
                                    }
                                    Surface(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clickable { onLyricAccentChange(index) },
                                        shape = RoundedCornerShape(14.dp),
                                        color = resolvedSwatchColor,
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (lyricHighlightColor == resolvedSwatchColor) 2.dp else 1.dp,
                                            color = if (lyricHighlightColor == resolvedSwatchColor) {
                                                colors.textPrimary
                                            } else {
                                                colors.glassBorder
                                            }
                                        )
                                    ) {}
                                }
                            }
                        }
                    }
                }
            }
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
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val timeWidth = 42.dp
        val gap = 8.dp
        val sliderWidth = (maxWidth - timeWidth - timeWidth - gap - gap).coerceAtLeast(0.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = TimeFormatter.formatSongDuration(progressMs),
                color = RelaxMusicColors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.End,
                modifier = Modifier.width(timeWidth)
            )
            Slider(
                value = sliderValue,
                onValueChange = { pendingProgress = it },
                onValueChangeFinished = {
                    pendingProgress?.let(onChangeProgress)
                    pendingProgress = null
                },
                modifier = Modifier
                    .width(sliderWidth)
                    .graphicsLayer(scaleY = 0.62f)
            )
            Text(
                text = TimeFormatter.formatSongDuration(durationMs),
                color = RelaxMusicColors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Start,
                modifier = Modifier.width(timeWidth)
            )
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
    onNext: () -> Unit,
    onOpenTimer: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
            IconButton(onClick = onOpenTimer) {
                Icon(Icons.Rounded.AccessTime, contentDescription = "timer")
            }
        }
    }
}
