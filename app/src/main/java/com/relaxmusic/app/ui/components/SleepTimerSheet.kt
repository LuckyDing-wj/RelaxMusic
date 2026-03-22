package com.relaxmusic.app.ui.components

import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.relaxmusic.app.ui.screens.nowplaying.formatSleepTimerRemainingDescription

private enum class SleepTimerSheetMode {
    PRESET,
    CUSTOM
}

@Composable
fun SleepTimerSheet(
    remainSeconds: Long,
    onPresetClick: (Int) -> Unit,
    onCustomConfirm: (Int) -> Unit,
    onCancelTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(SleepTimerSheetMode.PRESET) }
    var selectedHours by remember { mutableIntStateOf(0) }
    var selectedMinutes by remember { mutableIntStateOf(0) }
    val selection = coerceCustomSleepTimerSelection(selectedHours, selectedMinutes)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("睡眠定时", style = MaterialTheme.typography.titleLarge)
        Text(
            text = formatSleepTimerRemainingDescription(remainSeconds),
            style = MaterialTheme.typography.bodyMedium
        )

        when (mode) {
            SleepTimerSheetMode.PRESET -> {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(15, 30, 60).forEach { minutes ->
                        FilterChip(
                            selected = false,
                            onClick = { onPresetClick(minutes) },
                            label = { Text("${minutes} 分钟") }
                        )
                    }
                }
                TextButton(onClick = { mode = SleepTimerSheetMode.CUSTOM }) {
                    Text("自定义时间")
                }
            }

            SleepTimerSheetMode.CUSTOM -> {
                Text("选择关闭倒计时", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SleepTimerNumberPicker(
                        label = "小时",
                        value = selection.hours,
                        range = 0..12,
                        onValueChange = { selectedHours = it },
                        modifier = Modifier.weight(1f)
                    )
                    SleepTimerNumberPicker(
                        label = "分钟",
                        value = selection.minutes,
                        range = if (selection.hours == 12) 0..0 else 0..59,
                        onValueChange = { selectedMinutes = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { mode = SleepTimerSheetMode.PRESET },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("返回预设")
                    }
                    Button(
                        onClick = {
                            onCustomConfirm(
                                customSleepTimerTotalMinutes(
                                    hours = selection.hours,
                                    minutes = selection.minutes
                                )
                            )
                        },
                        enabled = isValidCustomSleepTimer(
                            hours = selection.hours,
                            minutes = selection.minutes
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("开始定时")
                    }
                }
            }
        }

        OutlinedButton(onClick = onCancelTimer) {
            Text("取消定时")
        }
    }
}

@Composable
private fun SleepTimerNumberPicker(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AndroidView(
            factory = {
                NumberPicker(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                    wrapSelectorWheel = false
                    setFormatter { pickerValue -> "%02d".format(pickerValue) }
                }
            },
            update = { picker ->
                if (picker.minValue != range.first) {
                    picker.minValue = range.first
                }
                if (picker.maxValue != range.last) {
                    picker.maxValue = range.last
                }
                if (picker.value != value) {
                    picker.value = value
                }
                picker.setOnValueChangedListener { _, _, newValue ->
                    onValueChange(newValue)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        )
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
