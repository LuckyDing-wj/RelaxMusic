package com.relaxmusic.app.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.relaxmusic.app.ui.theme.PanelBorder
import com.relaxmusic.app.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onPickFolder: () -> Unit,
    onRemoveFolder: (String) -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    showBackButton: Boolean = true,
    onUseEmbeddedTheme: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showBackButton) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "back")
                }
            }
            Text("设置", style = MaterialTheme.typography.headlineMedium)
        }

        SettingsCard(title = "音乐目录") {
            Text(state.libraryFolderLabel, color = TextSecondary)
            Button(onClick = onPickFolder, modifier = Modifier.padding(top = 12.dp)) {
                Text("添加目录")
            }
            if (state.libraryFolders.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.libraryFolders.forEach { folder ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                state.libraryFolderLabels[folder] ?: folder,
                                color = TextSecondary,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            OutlinedButton(onClick = { onRemoveFolder(folder) }) {
                                Text("移除")
                            }
                        }
                    }
                }
            }
        }

        SettingsCard(title = "界面") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("跟随系统主题")
                Switch(checked = state.followSystemTheme, onCheckedChange = { onUseEmbeddedTheme() })
            }
        }

        SettingsCard(title = "开发状态") {
            Text(state.projectStageLabel, color = TextSecondary)
            OutlinedButton(onClick = {}, modifier = Modifier.padding(top = 12.dp)) {
                Text("下一步继续完善后台播放与体验")
            }
        }

        SettingsCard(title = "备份与恢复") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onExportBackup) {
                    Text("导出")
                }
                OutlinedButton(onClick = onImportBackup) {
                    Text("导入")
                }
            }
            if (state.backupStatus != null) {
                Text(state.backupStatus, color = TextSecondary, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.72f)),
        border = BorderStroke(1.dp, PanelBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}
