package com.sportlife.records.ui.screen.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportlife.records.data.backup.DataBackupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class DataMigrationUiState(
    val isBusy: Boolean = false,
    val message: String? = null,
)

class DataMigrationViewModel(
    private val dataBackupRepository: DataBackupRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DataMigrationUiState())
    val uiState: StateFlow<DataMigrationUiState> = _uiState

    fun exportBackup(onReady: (fileName: String, json: String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = DataMigrationUiState(isBusy = true)
            runCatching { dataBackupRepository.exportJson() }
                .onSuccess { json ->
                    val stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                    _uiState.value = DataMigrationUiState(message = "备份已生成，请选择保存位置")
                    onReady("sportlife-backup-$stamp.json", json)
                }
                .onFailure { throwable ->
                    _uiState.value = DataMigrationUiState(message = "导出失败：${throwable.message ?: "未知错误"}")
                }
        }
    }

    fun importBackup(json: String) {
        viewModelScope.launch {
            _uiState.value = DataMigrationUiState(isBusy = true)
            runCatching { dataBackupRepository.importJson(json) }
                .onSuccess { result ->
                    _uiState.value = DataMigrationUiState(
                        message = "恢复完成：${result.importedTables} 张表，${result.importedRows} 条数据",
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = DataMigrationUiState(message = "恢复失败：${throwable.message ?: "备份文件格式不正确"}")
                }
        }
    }

    fun notifyExportSaved() {
        _uiState.update { it.copy(isBusy = false, message = "备份文件已保存") }
    }

    fun notifyExportCancelled() {
        _uiState.update { it.copy(isBusy = false, message = "已取消导出") }
    }
}
