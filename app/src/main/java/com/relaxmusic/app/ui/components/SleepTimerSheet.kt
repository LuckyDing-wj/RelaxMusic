package com.relaxmusic.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SleepTimerSheet(
    remainSeconds: Long,
    onPresetClick: (Int) -> Unit,
    onCancelTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("睡眠定时", style = MaterialTheme.typography.titleLarge)
        Text(
            text = if (remainSeconds > 0) "剩余 ${remainSeconds / 60} 分钟" else "选择一个预设时间，到点后停止播放",
            style = MaterialTheme.typography.bodyMedium
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(15, 30, 60).forEach { minutes ->
                FilterChip(
                    selected = false,
                    onClick = { onPresetClick(minutes) },
                    label = { Text("${minutes} 分钟") }
                )
            }
        }
        OutlinedButton(onClick = onCancelTimer) {
            Text("取消定时")
        }
    }
}
