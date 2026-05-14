package com.sportlife.records.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportlife.records.data.local.relation.WorkoutWithSportType
import com.sportlife.records.data.repository.UserPreferencesRepository
import com.sportlife.records.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val todayCount: Int = 0,
    val recentCheckIns: List<WorkoutWithSportType> = emptyList(),
    val slogan: String = UserPreferencesRepository.DEFAULT_HOME_SLOGAN,
    val isEditingSlogan: Boolean = false,
    val sloganDraft: String = "",
) {
    val checkedInToday: Boolean = todayCount > 0
}

private data class SloganEditorState(
    val isEditing: Boolean = false,
    val draft: String = "",
)

class HomeViewModel(
    workoutRepository: WorkoutRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val sloganEditorState = MutableStateFlow(SloganEditorState())

    val uiState = combine(
        workoutRepository.observeTodayCheckInCount(),
        workoutRepository.observeRecentCheckIns(limit = 5),
        userPreferencesRepository.homeSlogan,
        sloganEditorState,
    ) { todayCount, recent, slogan, editor ->
        HomeUiState(
            todayCount = todayCount,
            recentCheckIns = recent,
            slogan = slogan,
            isEditingSlogan = editor.isEditing,
            sloganDraft = editor.draft,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun startEditSlogan() {
        sloganEditorState.value = SloganEditorState(isEditing = true, draft = uiState.value.slogan)
    }

    fun updateSloganDraft(value: String) {
        sloganEditorState.update { it.copy(draft = value) }
    }

    fun cancelEditSlogan() {
        sloganEditorState.value = SloganEditorState()
    }

    fun saveSlogan() {
        val draft = sloganEditorState.value.draft
        viewModelScope.launch {
            userPreferencesRepository.saveHomeSlogan(draft)
            sloganEditorState.value = SloganEditorState()
        }
    }
}
