package com.relaxmusic.app.ui.player

sealed interface PlayerOverlayState {
    data object Hidden : PlayerOverlayState
    data object NowPlaying : PlayerOverlayState
    data object Queue : PlayerOverlayState
}

fun PlayerOverlayState.openNowPlaying(): PlayerOverlayState = PlayerOverlayState.NowPlaying

fun PlayerOverlayState.openQueue(): PlayerOverlayState = PlayerOverlayState.Queue

fun PlayerOverlayState.back(): PlayerOverlayState = when (this) {
    PlayerOverlayState.Hidden -> PlayerOverlayState.Hidden
    PlayerOverlayState.NowPlaying -> PlayerOverlayState.Hidden
    PlayerOverlayState.Queue -> PlayerOverlayState.NowPlaying
}

fun PlayerOverlayState.dismiss(): PlayerOverlayState = PlayerOverlayState.Hidden
