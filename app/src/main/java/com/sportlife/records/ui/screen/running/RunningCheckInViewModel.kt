package com.sportlife.records.ui.screen.running

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportlife.records.data.repository.RunningCheckInInput
import com.sportlife.records.data.repository.WorkoutRepository
import com.sportlife.records.domain.util.formatForInput
import com.sportlife.records.domain.util.normalizePaceInput
import com.sportlife.records.domain.util.parseInputDate
import com.sportlife.records.domain.util.parsePaceSecondsPerKm
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class RunningCheckInUiState(
    val distanceKm: String = "",
    val pace: String = "",
    val date: String = LocalDate.now().formatForInput(),
    val note: String = "",
    val isSaving: Boolean = false,
    val message: String? = null,
)

class RunningCheckInViewModel(
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RunningCheckInUiState())
    val uiState: StateFlow<RunningCheckInUiState> = _uiState

    fun updateDistance(value: String) = _uiState.update { it.copy(distanceKm = value, message = null) }
    fun updatePace(value: String) = _uiState.update { it.copy(pace = normalizePaceInput(value), message = null) }
    fun updateDate(value: String) = _uiState.update { it.copy(date = value, message = null) }
    fun updateNote(value: String) = _uiState.update { it.copy(note = value, message = null) }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        val distance = state.distanceKm.toDoubleOrNull()
        val date = parseInputDate(state.date)
        val pace = parsePaceSecondsPerKm(state.pace)

        when {
            distance == null || distance <= 0.0 -> {
                _uiState.update { it.copy(message = "请输入有效的跑步公里数") }
                return
            }
            date == null -> {
                _uiState.update { it.copy(message = "日期格式请使用 yyyy-MM-dd") }
                return
            }
            state.pace.isNotBlank() && pace == null -> {
                _uiState.update { it.copy(message = "配速格式示例：5'30\"/km") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null) }
            workoutRepository.saveRunningCheckIn(
                RunningCheckInInput(
                    date = date,
                    distanceKm = distance,
                    paceSecondsPerKm = pace,
                    note = state.note,
                ),
            )
            _uiState.update { it.copy(isSaving = false, message = "跑步打卡已保存") }
            delay(700)
            onSaved()
            _uiState.value = RunningCheckInUiState()
        }
    }
}
