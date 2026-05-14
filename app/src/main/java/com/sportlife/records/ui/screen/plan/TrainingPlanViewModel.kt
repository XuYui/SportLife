package com.sportlife.records.ui.screen.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportlife.records.data.local.entity.ExerciseGroupEntity
import com.sportlife.records.data.local.relation.TrainingPlanWithDays
import com.sportlife.records.data.repository.TrainingPlanRepository
import com.sportlife.records.domain.model.TrainingSplitType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TrainingPlanUiState(
    val plan: TrainingPlanWithDays? = null,
    val groups: List<ExerciseGroupEntity> = emptyList(),
)

class TrainingPlanViewModel(
    private val trainingPlanRepository: TrainingPlanRepository,
) : ViewModel() {
    val uiState = combine(
        trainingPlanRepository.observeActivePlan(),
        trainingPlanRepository.observeExerciseGroups(),
    ) { plan, groups ->
        TrainingPlanUiState(plan = plan, groups = groups)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrainingPlanUiState(),
    )

    fun activateSplit(splitType: TrainingSplitType) {
        viewModelScope.launch {
            trainingPlanRepository.activateNewDefaultPlan(splitType)
        }
    }
}
