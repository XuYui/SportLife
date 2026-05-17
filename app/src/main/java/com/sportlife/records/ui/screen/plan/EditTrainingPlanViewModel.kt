package com.sportlife.records.ui.screen.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportlife.records.data.local.entity.ExerciseGroupEntity
import com.sportlife.records.data.local.entity.TrainingDayEntity
import com.sportlife.records.data.local.entity.TrainingDaySectionEntity
import com.sportlife.records.data.local.entity.TrainingPlanExerciseEntity
import com.sportlife.records.data.local.relation.TrainingPlanWithDays
import com.sportlife.records.data.repository.PlanExerciseInput
import com.sportlife.records.data.repository.TrainingPlanRepository
import com.sportlife.records.domain.model.BodyPart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlanExerciseFormState(
    val trainingDayId: Long? = null,
    val sectionId: Long? = null,
    val exerciseName: String = "",
    val bodyPart: String = BodyPart.Back.name,
    val groupId: Long? = null,
    val sets: String = "4",
    val weightKg: String = "",
    val reps: String = "10",
    val note: String = "",
    val editingExercise: TrainingPlanExerciseEntity? = null,
    val message: String? = null,
)

data class SectionFormState(
    val trainingDayId: Long? = null,
    val name: String = "",
    val bodyPart: String = BodyPart.Back.name,
    val message: String? = null,
)

data class TrainingDayFormState(
    val planId: Long? = null,
    val editingDay: TrainingDayEntity? = null,
    val name: String = "",
    val focusBodyPart: String = "",
    val message: String? = null,
)

data class EditTrainingPlanUiState(
    val plan: TrainingPlanWithDays? = null,
    val groups: List<ExerciseGroupEntity> = emptyList(),
    val form: PlanExerciseFormState = PlanExerciseFormState(),
    val sectionForm: SectionFormState = SectionFormState(),
    val dayForm: TrainingDayFormState = TrainingDayFormState(),
)

class EditTrainingPlanViewModel(
    private val trainingPlanRepository: TrainingPlanRepository,
) : ViewModel() {
    private val formState = MutableStateFlow(PlanExerciseFormState())
    private val sectionFormState = MutableStateFlow(SectionFormState())
    private val dayFormState = MutableStateFlow(TrainingDayFormState())

    val uiState: StateFlow<EditTrainingPlanUiState> = combine(
        trainingPlanRepository.observeActivePlan(),
        trainingPlanRepository.observeExerciseGroups(),
        formState,
        sectionFormState,
        dayFormState,
    ) { plan, groups, form, sectionForm, dayForm ->
        EditTrainingPlanUiState(
            plan = plan,
            groups = groups,
            form = form,
            sectionForm = sectionForm,
            dayForm = dayForm,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EditTrainingPlanUiState(),
    )

    fun startAddDay(planId: Long, nextIndex: Int) {
        dayFormState.value = TrainingDayFormState(
            planId = planId,
            name = "第 ${nextIndex + 1} 天",
            focusBodyPart = "自定义",
        )
    }

    fun startEditDay(day: TrainingDayEntity) {
        dayFormState.value = TrainingDayFormState(
            planId = day.planId,
            editingDay = day,
            name = day.name,
            focusBodyPart = day.focusBodyPart.orEmpty(),
        )
    }

    fun clearDayForm() {
        dayFormState.value = TrainingDayFormState()
    }

    fun updateDayName(value: String) = dayFormState.update { it.copy(name = value, message = null) }
    fun updateDayFocus(value: String) = dayFormState.update { it.copy(focusBodyPart = value, message = null) }

    fun saveDayForm() {
        val form = dayFormState.value
        val planId = form.planId
        val name = form.name.trim()
        val focusBodyPart = form.focusBodyPart.trim()
        if (planId == null || name.isEmpty()) {
            dayFormState.update { it.copy(message = "请输入训练日名称") }
            return
        }
        viewModelScope.launch {
            val editing = form.editingDay
            if (editing == null) {
                trainingPlanRepository.addTrainingDay(planId, name, focusBodyPart)
            } else {
                trainingPlanRepository.updateTrainingDay(
                    editing.copy(
                        name = name,
                        focusBodyPart = focusBodyPart.ifBlank { null },
                    ),
                )
            }
            clearDayForm()
        }
    }

    fun deleteDay(day: TrainingDayEntity) {
        viewModelScope.launch {
            trainingPlanRepository.deleteTrainingDay(day)
        }
    }

    fun startAdd(dayId: Long, sectionId: Long?, fallbackBodyPart: String) {
        formState.value = PlanExerciseFormState(
            trainingDayId = dayId,
            sectionId = sectionId,
            bodyPart = fallbackBodyPart.ifBlank { BodyPart.Back.name },
        )
    }

    fun startEdit(exercise: TrainingPlanExerciseEntity) {
        formState.value = PlanExerciseFormState(
            trainingDayId = exercise.trainingDayId,
            sectionId = exercise.sectionId,
            exerciseName = exercise.exerciseName,
            bodyPart = exercise.bodyPart,
            groupId = exercise.exerciseGroupId,
            sets = exercise.sets.toString(),
            weightKg = if (exercise.defaultWeightKg == 0.0) "" else exercise.defaultWeightKg.toString(),
            reps = exercise.defaultReps.toString(),
            note = exercise.note,
            editingExercise = exercise,
        )
    }

    fun clearForm() {
        formState.value = PlanExerciseFormState()
    }

    fun startAddSection(dayId: Long, fallbackBodyPart: String) {
        sectionFormState.value = SectionFormState(
            trainingDayId = dayId,
            bodyPart = fallbackBodyPart.ifBlank { BodyPart.Back.name },
        )
    }

    fun clearSectionForm() {
        sectionFormState.value = SectionFormState()
    }

    fun updateName(value: String) = formState.update { it.copy(exerciseName = value, message = null) }
    fun updateBodyPart(value: String) = formState.update { it.copy(bodyPart = value, groupId = null, message = null) }
    fun updateGroup(value: Long?) = formState.update { it.copy(groupId = value, message = null) }
    fun updateSection(value: Long?) = formState.update { it.copy(sectionId = value, message = null) }
    fun updateSets(value: String) = formState.update { it.copy(sets = value, message = null) }
    fun updateWeight(value: String) = formState.update { it.copy(weightKg = value, message = null) }
    fun updateReps(value: String) = formState.update { it.copy(reps = value, message = null) }
    fun updateNote(value: String) = formState.update { it.copy(note = value, message = null) }

    fun updateSectionName(value: String) = sectionFormState.update { it.copy(name = value, message = null) }
    fun updateSectionBodyPart(value: String) = sectionFormState.update { it.copy(bodyPart = value, message = null) }

    fun saveSectionForm() {
        val form = sectionFormState.value
        val dayId = form.trainingDayId
        val name = form.name.trim()
        if (dayId == null || name.isEmpty()) {
            sectionFormState.update { it.copy(message = "请输入小板块名称") }
            return
        }
        viewModelScope.launch {
            trainingPlanRepository.addSection(dayId, name, form.bodyPart)
            clearSectionForm()
        }
    }

    fun saveForm() {
        val form = formState.value
        val dayId = form.trainingDayId
        val name = form.exerciseName.trim()
        val sets = form.sets.toIntOrNull()
        val weightInput = form.weightKg.trim()
        val weight = when {
            weightInput.isBlank() || weightInput == "自重" -> 0.0
            else -> weightInput.toDoubleOrNull()
        }
        val reps = form.reps.toIntOrNull()
        if (weight == null || weight < 0.0) {
            formState.update { it.copy(message = "重量请输入数字，或选择自重") }
            return
        }
        if (dayId == null || name.isEmpty() || sets == null || sets <= 0 || reps == null || reps <= 0) {
            formState.update { it.copy(message = "请完整填写动作、组数和次数") }
            return
        }

        viewModelScope.launch {
            val editing = form.editingExercise
            if (editing == null) {
                trainingPlanRepository.addPlanExercise(
                    PlanExerciseInput(
                        trainingDayId = dayId,
                        sectionId = form.sectionId,
                        exerciseName = name,
                        bodyPart = form.bodyPart,
                        exerciseGroupId = form.groupId,
                        sets = sets,
                        defaultWeightKg = weight,
                        defaultReps = reps,
                        note = form.note,
                    ),
                )
            } else {
                trainingPlanRepository.updatePlanExercise(
                    editing.copy(
                        exerciseName = name,
                        sectionId = form.sectionId,
                        bodyPart = form.bodyPart,
                        exerciseGroupId = form.groupId,
                        sets = sets,
                        defaultWeightKg = weight,
                        defaultReps = reps,
                        note = form.note,
                    ),
                )
            }
            clearForm()
        }
    }

    fun deleteExercise(exercise: TrainingPlanExerciseEntity) {
        viewModelScope.launch {
            trainingPlanRepository.deletePlanExercise(exercise)
        }
    }

    fun deleteSection(section: TrainingDaySectionEntity) {
        viewModelScope.launch {
            trainingPlanRepository.deleteSection(section)
        }
    }
}
