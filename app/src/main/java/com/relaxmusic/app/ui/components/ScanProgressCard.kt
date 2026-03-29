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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.relaxmusic.app.ui.theme.RelaxMusicColors

@Composable
fun ScanProgressCard(
    currentFolderLabel: String,
    songCount: Int,
    scanning: Boolean,
    statusMessage: String,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    val colors = RelaxMusicColors
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.panelSurface),
        border = BorderStroke(1.dp, colors.panelBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (scanning) "正在扫描..." else "本地曲库",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.accent
                )
                Text(
                    text = "共 $songCount 首",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary
                )
            }
            Text(
                text = currentFolderLabel,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                color = colors.textSecondary
            )
            if (statusMessage.isNotBlank()) {
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = colors.textSecondary
                )
            }
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
