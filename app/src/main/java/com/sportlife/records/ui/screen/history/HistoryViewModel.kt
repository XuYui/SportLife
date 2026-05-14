package com.sportlife.records.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportlife.records.data.local.relation.WorkoutWithSportType
import com.sportlife.records.data.repository.RunningCheckInInput
import com.sportlife.records.data.repository.StrengthCheckInInput
import com.sportlife.records.data.repository.WorkoutRepository
import com.sportlife.records.domain.model.BodyPart
import com.sportlife.records.domain.model.BuiltInSportTypes
import com.sportlife.records.domain.util.formatForInput
import com.sportlife.records.domain.util.formatPace
import com.sportlife.records.domain.util.parseInputDate
import com.sportlife.records.domain.util.parsePaceSecondsPerKm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class HistoryEditState(
    val checkInId: Long,
    val sportTypeId: String,
    val title: String,
    val date: String,
    val note: String,
    val distanceKm: String = "",
    val pace: String = "",
    val bodyPart: BodyPart = BodyPart.Back,
    val message: String? = null,
)

data class HistoryUiState(
    val history: List<WorkoutWithSportType> = emptyList(),
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val editing: HistoryEditState? = null,
) {
    val recordsForSelectedDate: List<WorkoutWithSportType>
        get() = history.filter { it.checkIn.dateEpochDay == selectedDate.toEpochDay() }

    val checkInCountsByDay: Map<Long, Int>
        get() = history.groupingBy { it.checkIn.dateEpochDay }.eachCount()
}

class HistoryViewModel(
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {
    private val currentMonth = MutableStateFlow(YearMonth.now())
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val editing = MutableStateFlow<HistoryEditState?>(null)

    val uiState: StateFlow<HistoryUiState> =
        combine(
            workoutRepository.observeHistory(),
            currentMonth,
            selectedDate,
            editing,
        ) { history, month, selected, editing ->
            HistoryUiState(
                history = history,
                currentMonth = month,
                selectedDate = selected,
                editing = editing,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState(),
        )

    fun previousMonth() {
        currentMonth.update { it.minusMonths(1) }
    }

    fun nextMonth() {
        currentMonth.update { it.plusMonths(1) }
    }

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
        currentMonth.value = YearMonth.from(date)
    }

    fun startEdit(record: WorkoutWithSportType) {
        viewModelScope.launch {
            val checkIn = record.checkIn
            when (record.sportType.id) {
                BuiltInSportTypes.Running.id -> {
                    val running = workoutRepository.getRunningRecord(checkIn.id) ?: return@launch
                    editing.value = HistoryEditState(
                        checkInId = checkIn.id,
                        sportTypeId = record.sportType.id,
                        title = "编辑跑步记录",
                        date = LocalDate.ofEpochDay(checkIn.dateEpochDay).formatForInput(),
                        note = checkIn.note,
                        distanceKm = running.distanceKm.toString(),
                        pace = formatPace(running.paceSecondsPerKm).takeIf { it != "-" }.orEmpty(),
                    )
                }
                BuiltInSportTypes.StrengthTraining.id -> {
                    val strength = workoutRepository.getStrengthRecord(checkIn.id)
                    editing.value = HistoryEditState(
                        checkInId = checkIn.id,
                        sportTypeId = record.sportType.id,
                        title = "编辑健身记录",
                        date = LocalDate.ofEpochDay(checkIn.dateEpochDay).formatForInput(),
                        note = checkIn.note,
                        bodyPart = BodyPart.fromName(strength?.primaryBodyPart ?: BodyPart.Back.name),
                    )
                }
            }
        }
    }

    fun clearEdit() {
        editing.value = null
    }

    fun updateEditDate(value: String) = editing.update { it?.copy(date = value, message = null) }
    fun updateEditNote(value: String) = editing.update { it?.copy(note = value, message = null) }
    fun updateEditDistance(value: String) = editing.update { it?.copy(distanceKm = value, message = null) }
    fun updateEditPace(value: String) = editing.update { it?.copy(pace = value, message = null) }
    fun updateEditBodyPart(value: BodyPart) = editing.update { it?.copy(bodyPart = value, message = null) }

    fun saveEdit() {
        val state = editing.value ?: return
        val date = parseInputDate(state.date)
        if (date == null) {
            editing.update { it?.copy(message = "日期格式请使用 yyyy-MM-dd") }
            return
        }

        viewModelScope.launch {
            when (state.sportTypeId) {
                BuiltInSportTypes.Running.id -> saveRunningEdit(state, date)
                BuiltInSportTypes.StrengthTraining.id -> {
                    workoutRepository.updateStrengthCheckIn(
                        state.checkInId,
                        StrengthCheckInInput(
                            date = date,
                            primaryBodyPart = state.bodyPart,
                            note = state.note,
                        ),
                    )
                    editing.value = null
                }
            }
        }
    }

    fun delete(record: WorkoutWithSportType) {
        viewModelScope.launch {
            workoutRepository.deleteCheckIn(record.checkIn.id)
            if (editing.value?.checkInId == record.checkIn.id) {
                editing.value = null
            }
        }
    }

    private suspend fun saveRunningEdit(state: HistoryEditState, date: LocalDate) {
        val distance = state.distanceKm.toDoubleOrNull()
        val pace = parsePaceSecondsPerKm(state.pace)
        when {
            distance == null || distance <= 0.0 -> {
                editing.update { it?.copy(message = "请输入有效公里数") }
            }
            state.pace.isNotBlank() && pace == null -> {
                editing.update { it?.copy(message = "配速格式示例：5'30\"/km") }
            }
            else -> {
                workoutRepository.updateRunningCheckIn(
                    state.checkInId,
                    RunningCheckInInput(
                        date = date,
                        distanceKm = distance,
                        paceSecondsPerKm = pace,
                        note = state.note,
                    ),
                )
                editing.value = null
            }
        }
    }
}
