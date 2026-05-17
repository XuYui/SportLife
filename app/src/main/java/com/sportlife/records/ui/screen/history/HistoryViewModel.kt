package com.sportlife.records.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportlife.records.data.local.relation.WorkoutWithSportType
import com.sportlife.records.data.repository.RunningCheckInInput
import com.sportlife.records.data.repository.StrengthCheckInInput
import com.sportlife.records.data.repository.WorkoutRepository
import com.sportlife.records.domain.model.BodyPart
import com.sportlife.records.domain.model.BuiltInSportTypes
import com.sportlife.records.domain.model.displayBodyPartName
import com.sportlife.records.domain.util.formatForInput
import com.sportlife.records.domain.util.formatPace
import com.sportlife.records.domain.util.parseInputDate
import com.sportlife.records.domain.util.parsePaceSecondsPerKm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
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
    val bodyPart: String = displayBodyPartName(BodyPart.Back.name),
    val message: String? = null,
)

data class HistoryExerciseSetDetail(
    val weightKg: Double,
    val reps: Int,
)

data class HistoryExerciseDetail(
    val name: String,
    val bodyPart: String,
    val sets: List<HistoryExerciseSetDetail>,
    val note: String,
)

data class HistoryRecordDetail(
    val distanceKm: Double? = null,
    val paceSecondsPerKm: Int? = null,
    val primaryBodyPart: String? = null,
    val exercises: List<HistoryExerciseDetail> = emptyList(),
) {
    val totalSets: Int
        get() = exercises.sumOf { it.sets.size }
}

data class HistoryUiState(
    val history: List<WorkoutWithSportType> = emptyList(),
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val editing: HistoryEditState? = null,
    val detailsByCheckInId: Map<Long, HistoryRecordDetail> = emptyMap(),
    val feedback: String? = null,
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
    private val detailsByCheckInId = MutableStateFlow<Map<Long, HistoryRecordDetail>>(emptyMap())
    private val feedback = MutableStateFlow<String?>(null)

    private val baseUiState =
        combine(
            workoutRepository.observeHistory(),
            currentMonth,
            selectedDate,
            editing,
            detailsByCheckInId,
        ) { history, month, selected, editing, details ->
            HistoryUiState(
                history = history,
                currentMonth = month,
                selectedDate = selected,
                editing = editing,
                detailsByCheckInId = details,
            )
        }

    val uiState: StateFlow<HistoryUiState> =
        combine(baseUiState, feedback) { state, feedback ->
            state.copy(feedback = feedback)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState(),
        )

    init {
        viewModelScope.launch {
            combine(workoutRepository.observeHistory(), selectedDate) { history, selected ->
                history.filter { it.checkIn.dateEpochDay == selected.toEpochDay() }
            }.collectLatest { records ->
                detailsByCheckInId.value = records.mapNotNull { record ->
                    loadRecordDetail(record)?.let { detail -> record.checkIn.id to detail }
                }.toMap()
            }
        }
    }

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
                        bodyPart = displayBodyPartName(strength?.primaryBodyPart ?: BodyPart.Back.name),
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
    fun updateEditBodyPart(value: String) = editing.update { it?.copy(bodyPart = value, message = null) }

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
                    selectedDate.value = date
                    showFeedback("健身记录已保存")
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
            showFeedback("记录已删除")
        }
    }

    private suspend fun loadRecordDetail(record: WorkoutWithSportType): HistoryRecordDetail? =
        when (record.sportType.id) {
            BuiltInSportTypes.Running.id -> {
                val running = workoutRepository.getRunningRecord(record.checkIn.id) ?: return null
                HistoryRecordDetail(
                    distanceKm = running.distanceKm,
                    paceSecondsPerKm = running.paceSecondsPerKm,
                )
            }
            BuiltInSportTypes.StrengthTraining.id -> {
                val strength = workoutRepository.getStrengthRecordWithExercises(record.checkIn.id)
                val fallback = workoutRepository.getStrengthRecord(record.checkIn.id)
                HistoryRecordDetail(
                    primaryBodyPart = strength?.record?.primaryBodyPart ?: fallback?.primaryBodyPart,
                    exercises = strength
                        ?.exercises
                        ?.sortedBy { it.exercise.sortOrder }
                        ?.map { exerciseWithSets ->
                            HistoryExerciseDetail(
                                name = exerciseWithSets.exercise.exerciseName,
                                bodyPart = displayBodyPartName(exerciseWithSets.exercise.bodyPart),
                                sets = exerciseWithSets.sets
                                    .sortedBy { it.setIndex }
                                    .map { set -> HistoryExerciseSetDetail(weightKg = set.weightKg, reps = set.reps) },
                                note = exerciseWithSets.exercise.note,
                            )
                        }
                        .orEmpty(),
                )
            }
            else -> null
        }

    private fun showFeedback(message: String) {
        feedback.value = message
        viewModelScope.launch {
            delay(2_200)
            if (feedback.value == message) {
                feedback.value = null
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
                selectedDate.value = date
                showFeedback("跑步记录已保存")
            }
        }
    }
}
