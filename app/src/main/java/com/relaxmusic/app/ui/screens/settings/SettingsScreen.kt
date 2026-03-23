package com.relaxmusic.app.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.relaxmusic.app.domain.model.ThemeMode
import com.relaxmusic.app.ui.theme.RelaxMusicColors

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onPickFolder: () -> Unit,
    onRemoveFolder: (String) -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    showBackButton: Boolean = true,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val colors = RelaxMusicColors
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
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
            Text(state.libraryFolderLabel, color = colors.textSecondary)
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
                                color = colors.textSecondary,
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
            Column(modifier = Modifier.selectableGroup()) {
                ThemeMode.entries.forEach { themeMode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = state.themeMode == themeMode,
                                onClick = { onThemeModeChange(themeMode) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(themeMode.label)
                        RadioButton(
                            selected = state.themeMode == themeMode,
                            onClick = null
                        )
                    }
                }
            }
        }

        SettingsCard(title = "开发状态") {
            Text(state.projectStageLabel, color = colors.textSecondary)
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
                Text(state.backupStatus, color = colors.textSecondary, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

private val ThemeMode.label: String
    get() = when (this) {
        ThemeMode.SYSTEM -> "跟随系统"
        ThemeMode.LIGHT -> "浅色"
        ThemeMode.DARK -> "深色"
    }

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    val colors = RelaxMusicColors
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.panelSurface),
        border = BorderStroke(1.dp, colors.panelBorder)
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
