package com.sportlife.records.ui.screen.strength

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportlife.records.data.repository.StrengthCheckInInput
import com.sportlife.records.data.repository.TrainingPlanRepository
import com.sportlife.records.data.repository.WorkoutRepository
import com.sportlife.records.domain.model.BodyPart
import com.sportlife.records.domain.model.TrainingSplitType
import com.sportlife.records.domain.model.defaultBodyPartValues
import com.sportlife.records.domain.util.formatForInput
import com.sportlife.records.domain.util.parseInputDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class StrengthCheckInUiState(
    val date: String = LocalDate.now().formatForInput(),
    val selectedSplit: String = BodyPart.Back.name,
    val availableSplits: List<String> = defaultBodyPartValues(),
    val activeSplitLabel: String = "默认分化",
    val note: String = "",
    val isSaving: Boolean = false,
    val message: String? = null,
)

class StrengthCheckInViewModel(
    private val workoutRepository: WorkoutRepository,
    trainingPlanRepository: TrainingPlanRepository,
) : ViewModel() {
    private val formState = MutableStateFlow(StrengthCheckInUiState())
    val uiState: StateFlow<StrengthCheckInUiState> = combine(
        formState,
        trainingPlanRepository.observeActivePlan(),
    ) { form, activePlan ->
        val availableSplits = activePlan
            ?.days
            ?.sortedBy { it.day.dayIndex }
            ?.map { it.day.focusBodyPart?.takeIf(String::isNotBlank) ?: it.day.name }
            ?.distinct()
            ?.takeIf { it.isNotEmpty() }
            ?: defaultBodyPartValues()
        val selected = if (form.selectedSplit in availableSplits) {
            form.selectedSplit
        } else {
            availableSplits.first()
        }
        form.copy(
            selectedSplit = selected,
            availableSplits = availableSplits,
            activeSplitLabel = activePlan?.plan?.splitId?.let { TrainingSplitType.fromId(it).label } ?: "默认分化",
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StrengthCheckInUiState(),
    )

    fun updateDate(value: String) = formState.update { it.copy(date = value, message = null) }
    fun updateSelectedSplit(value: String) = formState.update { it.copy(selectedSplit = value, message = null) }
    fun updateNote(value: String) = formState.update { it.copy(note = value, message = null) }

    fun save(onSaved: () -> Unit) {
        val state = uiState.value
        val date = parseInputDate(state.date)
        if (date == null) {
            formState.update { it.copy(message = "日期格式请使用 yyyy-MM-dd") }
            return
        }

        viewModelScope.launch {
            formState.update { it.copy(isSaving = true, message = null) }
            workoutRepository.saveStrengthCheckIn(
                StrengthCheckInInput(
                    date = date,
                    primaryBodyPart = state.selectedSplit,
                    note = state.note,
                ),
            )
            formState.update { it.copy(isSaving = false, selectedSplit = state.selectedSplit, message = "健身打卡已保存") }
            delay(700)
            onSaved()
            formState.value = StrengthCheckInUiState(selectedSplit = state.selectedSplit)
        }
    }
}
