package com.relaxmusic.app.ui.components

data class CustomSleepTimerSelection(
    val hours: Int,
    val minutes: Int
)

fun coerceCustomSleepTimerSelection(hours: Int, minutes: Int): CustomSleepTimerSelection {
    val safeHours = hours.coerceIn(0, 12)
    val safeMinutes = if (safeHours == 12) 0 else minutes.coerceIn(0, 59)
    return CustomSleepTimerSelection(
        hours = safeHours,
        minutes = safeMinutes
    )
}

fun isValidCustomSleepTimer(hours: Int, minutes: Int): Boolean {
    val selection = coerceCustomSleepTimerSelection(hours, minutes)
    return selection.hours > 0 || selection.minutes > 0
}

fun customSleepTimerTotalMinutes(hours: Int, minutes: Int): Int {
    val selection = coerceCustomSleepTimerSelection(hours, minutes)
    return selection.hours * 60 + selection.minutes
}
