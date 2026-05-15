package com.sportlife.records.ui.screen.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportlife.records.ui.component.AppScaffold
import com.sportlife.records.ui.theme.EvolveBackground
import com.sportlife.records.ui.theme.EvolveMuted
import com.sportlife.records.ui.theme.EvolveNeon
import com.sportlife.records.ui.theme.EvolveSurfaceHigh
import com.sportlife.records.ui.theme.EvolveSurfaceLow

@Composable
fun DataMigrationScreen(
    uiState: DataMigrationUiState,
    onExportRequested: ((String, String) -> Unit) -> Unit,
    onImportText: (String) -> Unit,
    onExportSaved: () -> Unit,
    onExportCancelled: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var pendingExportJson by remember { mutableStateOf<String?>(null) }
    val createDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri: Uri? ->
        val json = pendingExportJson
        if (uri != null && json != null) {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(json.toByteArray(Charsets.UTF_8))
            }
            pendingExportJson = null
            onExportSaved()
        } else {
            pendingExportJson = null
            onExportCancelled()
        }
    }
    val openDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri != null) {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
            if (text != null) {
                onImportText(text)
            }
        }
    }

    AppScaffold(title = "数据迁移", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            MigrationHero()
            ActionCard(
                title = "导出完整备份",
                description = "把打卡记录、跑步详情、健身计划、计划快照、动作库和小板块保存成 JSON 文件。",
                icon = { Icon(Icons.Default.FileDownload, contentDescription = null) },
            ) {
                Button(
                    onClick = {
                        onExportRequested { fileName, json ->
                            pendingExportJson = json
                            createDocument.launch(fileName)
                        }
                    },
                    enabled = !uiState.isBusy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("导出备份文件")
                }
            }
            ActionCard(
                title = "从备份恢复",
                description = "选择之前导出的 JSON 文件，恢复后会覆盖当前本地数据。",
                icon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
            ) {
                OutlinedButton(
                    onClick = { openDocument.launch(arrayOf("application/json", "text/*", "*/*")) },
                    enabled = !uiState.isBusy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("选择备份文件恢复")
                }
            }
            if (uiState.message != null) {
                Text(uiState.message, color = MaterialTheme.colorScheme.primary)
            }
            Text(
                "提示：正常覆盖安装会保留本地数据；训练计划也会自动保存快照。卸载重装、换手机、调试签名变化前，请先导出备份。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun MigrationHero() {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = EvolveSurfaceHigh),
        border = BorderStroke(1.dp, EvolveNeon),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .background(EvolveNeon, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, tint = EvolveBackground)
                }
                Text("数据迁移", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Text("更新靠数据库迁移，跨设备和重装靠备份恢复。", color = EvolveMuted)
            }
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    description: String,
    icon: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = EvolveSurfaceLow,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                contentAlignment = Alignment.Center,
            ) {
                icon()
            }
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}
