package com.relaxmusic.app.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SleepTimerManager {
    private val _remainSeconds = MutableStateFlow(0L)
    val remainSeconds: StateFlow<Long> = _remainSeconds.asStateFlow()

    fun start(minutes: Int) {
        _remainSeconds.value = minutes * 60L
    }

    fun cancel() {
        _remainSeconds.value = 0L
    }
}
