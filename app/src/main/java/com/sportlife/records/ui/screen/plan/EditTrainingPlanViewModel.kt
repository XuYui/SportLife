package com.sportlife.records.ui.screen.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportlife.records.data.local.entity.ExerciseGroupEntity
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
    val bodyPart: BodyPart = BodyPart.Back,
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
    val bodyPart: BodyPart = BodyPart.Back,
    val message: String? = null,
)

data class EditTrainingPlanUiState(
    val plan: TrainingPlanWithDays? = null,
    val groups: List<ExerciseGroupEntity> = emptyList(),
    val form: PlanExerciseFormState = PlanExerciseFormState(),
    val sectionForm: SectionFormState = SectionFormState(),
)

class EditTrainingPlanViewModel(
    private val trainingPlanRepository: TrainingPlanRepository,
) : ViewModel() {
    private val formState = MutableStateFlow(PlanExerciseFormState())
    private val sectionFormState = MutableStateFlow(SectionFormState())

    val uiState: StateFlow<EditTrainingPlanUiState> = combine(
        trainingPlanRepository.observeActivePlan(),
        trainingPlanRepository.observeExerciseGroups(),
        formState,
        sectionFormState,
    ) { plan, groups, form, sectionForm ->
        EditTrainingPlanUiState(plan = plan, groups = groups, form = form, sectionForm = sectionForm)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EditTrainingPlanUiState(),
    )

    fun startAdd(dayId: Long, sectionId: Long?, fallbackBodyPart: BodyPart) {
        formState.value = PlanExerciseFormState(
            trainingDayId = dayId,
            sectionId = sectionId,
            bodyPart = fallbackBodyPart,
        )
    }

    fun startEdit(exercise: TrainingPlanExerciseEntity) {
        formState.value = PlanExerciseFormState(
            trainingDayId = exercise.trainingDayId,
            sectionId = exercise.sectionId,
            exerciseName = exercise.exerciseName,
            bodyPart = BodyPart.fromName(exercise.bodyPart),
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

    fun startAddSection(dayId: Long, fallbackBodyPart: BodyPart) {
        sectionFormState.value = SectionFormState(trainingDayId = dayId, bodyPart = fallbackBodyPart)
    }

    fun clearSectionForm() {
        sectionFormState.value = SectionFormState()
    }

    fun updateName(value: String) = formState.update { it.copy(exerciseName = value, message = null) }
    fun updateBodyPart(value: BodyPart) = formState.update { it.copy(bodyPart = value, groupId = null, message = null) }
    fun updateGroup(value: Long?) = formState.update { it.copy(groupId = value, message = null) }
    fun updateSection(value: Long?) = formState.update { it.copy(sectionId = value, message = null) }
    fun updateSets(value: String) = formState.update { it.copy(sets = value, message = null) }
    fun updateWeight(value: String) = formState.update { it.copy(weightKg = value, message = null) }
    fun updateReps(value: String) = formState.update { it.copy(reps = value, message = null) }
    fun updateNote(value: String) = formState.update { it.copy(note = value, message = null) }

    fun updateSectionName(value: String) = sectionFormState.update { it.copy(name = value, message = null) }
    fun updateSectionBodyPart(value: BodyPart) = sectionFormState.update { it.copy(bodyPart = value, message = null) }

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
        val weight = form.weightKg.toDoubleOrNull() ?: 0.0
        val reps = form.reps.toIntOrNull()
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
                        bodyPart = form.bodyPart.name,
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
