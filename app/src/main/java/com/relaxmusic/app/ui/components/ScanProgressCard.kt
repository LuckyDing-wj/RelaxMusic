package com.relaxmusic.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.relaxmusic.app.ui.theme.PanelBorder
import com.relaxmusic.app.ui.theme.TextSecondary

@Composable
fun ScanProgressCard(
    currentFolderLabel: String,
    songCount: Int,
    scanning: Boolean,
    statusMessage: String,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.72f)),
        border = BorderStroke(1.dp, PanelBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (scanning) "正在扫描音乐目录" else "本地曲库概览",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = currentFolderLabel,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Text("歌曲数: $songCount", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = if (scanning) "状态: 扫描中" else "状态: 就绪",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
