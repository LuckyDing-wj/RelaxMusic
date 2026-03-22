package com.relaxmusic.app.domain.model

data class SleepTimerState(
    val enabled: Boolean = false,
    val remainSeconds: Long = 0,
    val targetTimeMillis: Long? = null,
    val action: TimerAction = TimerAction.STOP
)

enum class TimerAction {
    STOP
}
