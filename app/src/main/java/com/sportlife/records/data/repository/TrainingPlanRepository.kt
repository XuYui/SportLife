package com.sportlife.records.data.repository

import androidx.room.withTransaction
import com.sportlife.records.data.backup.TrainingPlanSnapshotRepository
import com.sportlife.records.data.local.SportLifeDatabase
import com.sportlife.records.data.local.entity.ExerciseEntity
import com.sportlife.records.data.local.entity.ExerciseGroupEntity
import com.sportlife.records.data.local.entity.TrainingDayEntity
import com.sportlife.records.data.local.entity.TrainingDaySectionEntity
import com.sportlife.records.data.local.entity.TrainingPlanEntity
import com.sportlife.records.data.local.entity.TrainingPlanExerciseEntity
import com.sportlife.records.data.local.entity.toEntity
import com.sportlife.records.data.local.relation.TrainingPlanWithDays
import com.sportlife.records.domain.model.BodyPart
import com.sportlife.records.domain.model.TrainingSplitType
import com.sportlife.records.domain.model.normalizeCustomFocus
import kotlinx.coroutines.flow.Flow

data class PlanExerciseInput(
    val trainingDayId: Long,
    val sectionId: Long?,
    val exerciseName: String,
    val bodyPart: String,
    val exerciseGroupId: Long?,
    val sets: Int,
    val defaultWeightKg: Double,
    val defaultReps: Int,
    val note: String,
)

interface TrainingPlanRepository {
    fun observeActivePlan(): Flow<TrainingPlanWithDays?>
    fun observeExerciseGroups(): Flow<List<ExerciseGroupEntity>>
    fun observeExercises(): Flow<List<ExerciseEntity>>
    suspend fun activateNewDefaultPlan(splitType: TrainingSplitType)
    suspend fun addTrainingDay(planId: Long, name: String, focusBodyPart: String?)
    suspend fun updateTrainingDay(day: TrainingDayEntity)
    suspend fun deleteTrainingDay(day: TrainingDayEntity)
    suspend fun addSection(dayId: Long, name: String, bodyPart: String?)
    suspend fun deleteSection(section: TrainingDaySectionEntity)
    suspend fun addPlanExercise(input: PlanExerciseInput)
    suspend fun updatePlanExercise(exercise: TrainingPlanExerciseEntity)
    suspend fun deletePlanExercise(exercise: TrainingPlanExerciseEntity)
}

class OfflineTrainingPlanRepository(
    private val database: SportLifeDatabase,
    private val trainingPlanSnapshotRepository: TrainingPlanSnapshotRepository? = null,
) : TrainingPlanRepository {
    private val trainingPlanDao = database.trainingPlanDao()
    private val exerciseDao = database.exerciseDao()

    override fun observeActivePlan(): Flow<TrainingPlanWithDays?> =
        trainingPlanDao.observeActivePlan()

    override fun observeExerciseGroups(): Flow<List<ExerciseGroupEntity>> =
        exerciseDao.observeGroups()

    override fun observeExercises(): Flow<List<ExerciseEntity>> =
        exerciseDao.observeExercises()

    override suspend fun activateNewDefaultPlan(splitType: TrainingSplitType) {
        database.withTransaction {
            trainingPlanDao.upsertSplits(TrainingSplitType.entries.map { it.toEntity() })
            trainingPlanDao.deactivatePlans()
            val planId = trainingPlanDao.insertPlan(
                TrainingPlanEntity(
                    name = "${splitType.label}计划",
                    splitId = splitType.id,
                    isActive = true,
                ),
            )
            val focusParts = when (splitType) {
                TrainingSplitType.ThreeDay -> listOf(BodyPart.Back, BodyPart.Chest, BodyPart.Legs)
                TrainingSplitType.FourDay -> listOf(BodyPart.Back, BodyPart.Chest, BodyPart.Legs, BodyPart.Arms)
                TrainingSplitType.Custom -> emptyList()
            }
            focusParts.forEachIndexed { index, bodyPart ->
                val dayId = trainingPlanDao.insertDay(
                    TrainingDayEntity(
                        planId = planId,
                        dayIndex = index,
                        name = "第 ${index + 1} 天 ${bodyPart.label}",
                        focusBodyPart = bodyPart.name,
                    ),
                )
                insertDefaultSectionsAndExercises(dayId, bodyPart)
            }
            if (splitType == TrainingSplitType.Custom) {
                trainingPlanDao.insertDay(
                    TrainingDayEntity(
                        planId = planId,
                        dayIndex = 0,
                        name = "第 1 天 自定义",
                        focusBodyPart = "自定义",
                    ),
                )
            }
        }
        snapshotActivePlan()
    }

    override suspend fun addTrainingDay(planId: Long, name: String, focusBodyPart: String?) {
        val nextIndex = trainingPlanDao.getMaxDayIndex(planId) + 1
        trainingPlanDao.insertDay(
            TrainingDayEntity(
                planId = planId,
                dayIndex = nextIndex,
                name = name.ifBlank { "第 ${nextIndex + 1} 天" },
                focusBodyPart = focusBodyPart?.takeIf { it.isNotBlank() }?.let(::normalizeCustomFocus),
            ),
        )
        snapshotActivePlan()
    }

    override suspend fun updateTrainingDay(day: TrainingDayEntity) {
        trainingPlanDao.updateDay(
            day.copy(
                name = day.name.ifBlank { "训练日" },
                focusBodyPart = day.focusBodyPart?.let(::normalizeCustomFocus),
            ),
        )
        snapshotActivePlan()
    }

    override suspend fun deleteTrainingDay(day: TrainingDayEntity) {
        trainingPlanDao.deleteDay(day)
        snapshotActivePlan()
    }

    override suspend fun addSection(dayId: Long, name: String, bodyPart: String?) {
        trainingPlanDao.insertSection(
            TrainingDaySectionEntity(
                trainingDayId = dayId,
                name = name,
                bodyPart = bodyPart?.takeIf { it.isNotBlank() }?.let(::normalizeCustomFocus),
                sortOrder = (System.currentTimeMillis() % 10_000).toInt(),
            ),
        )
        snapshotActivePlan()
    }

    override suspend fun deleteSection(section: TrainingDaySectionEntity) {
        database.withTransaction {
            trainingPlanDao.clearSectionFromExercises(section.id)
            trainingPlanDao.deleteSection(section)
        }
        snapshotActivePlan()
    }

    override suspend fun addPlanExercise(input: PlanExerciseInput) {
        trainingPlanDao.insertPlanExercise(
            TrainingPlanExerciseEntity(
                trainingDayId = input.trainingDayId,
                sectionId = input.sectionId,
                exerciseGroupId = input.exerciseGroupId,
                exerciseName = input.exerciseName,
                bodyPart = normalizeCustomFocus(input.bodyPart),
                sets = input.sets,
                defaultWeightKg = input.defaultWeightKg,
                defaultReps = input.defaultReps,
                note = input.note,
            ),
        )
        snapshotActivePlan()
    }

    override suspend fun updatePlanExercise(exercise: TrainingPlanExerciseEntity) {
        trainingPlanDao.updatePlanExercise(exercise.copy(bodyPart = normalizeCustomFocus(exercise.bodyPart)))
        snapshotActivePlan()
    }

    override suspend fun deletePlanExercise(exercise: TrainingPlanExerciseEntity) {
        trainingPlanDao.deletePlanExercise(exercise)
        snapshotActivePlan()
    }

    private suspend fun snapshotActivePlan() {
        trainingPlanSnapshotRepository?.saveActivePlanSnapshot()
    }

    private suspend fun insertDefaultSectionsAndExercises(dayId: Long, bodyPart: BodyPart) {
        defaultSections(bodyPart).forEachIndexed { sectionIndex, section ->
            val sectionId = trainingPlanDao.insertSection(
                TrainingDaySectionEntity(
                    trainingDayId = dayId,
                    name = section.name,
                    bodyPart = section.bodyPart.name,
                    sortOrder = sectionIndex,
                ),
            )
            section.exercises.forEachIndexed { exerciseIndex, exercise ->
                insertPlanExercise(dayId, sectionId, exerciseIndex, exercise)
            }
        }
    }

    private suspend fun insertPlanExercise(
        dayId: Long,
        sectionId: Long,
        sortOrder: Int,
        starter: StarterExercise,
    ) {
        val exercise = exerciseDao.findExercise(starter.name, starter.bodyPart.name)
        trainingPlanDao.insertPlanExercise(
            TrainingPlanExerciseEntity(
                trainingDayId = dayId,
                sectionId = sectionId,
                exerciseId = exercise?.id,
                exerciseGroupId = exercise?.groupId,
                exerciseName = starter.name,
                bodyPart = starter.bodyPart.name,
                sets = starter.sets,
                defaultWeightKg = starter.weightKg,
                defaultReps = starter.reps,
                sortOrder = sortOrder,
            ),
        )
    }
}

data class DefaultSection(
    val name: String,
    val bodyPart: BodyPart,
    val exercises: List<StarterExercise>,
)

data class StarterExercise(
    val name: String,
    val bodyPart: BodyPart,
    val sets: Int,
    val weightKg: Double,
    val reps: Int,
)

fun defaultSections(bodyPart: BodyPart): List<DefaultSection> =
    when (bodyPart) {
        BodyPart.Back -> listOf(
            DefaultSection(
                name = "背",
                bodyPart = BodyPart.Back,
                exercises = listOf(
                    StarterExercise("引体向上", BodyPart.Back, 4, 0.0, 8),
                    StarterExercise("坐姿划船", BodyPart.Back, 4, 35.0, 10),
                    StarterExercise("高位下拉", BodyPart.Back, 4, 40.0, 10),
                ),
            ),
            DefaultSection(
                name = "肩前束",
                bodyPart = BodyPart.Chest,
                exercises = listOf(StarterExercise("哑铃前平举", BodyPart.Chest, 3, 6.0, 12)),
            ),
            DefaultSection(
                name = "肱二头",
                bodyPart = BodyPart.Arms,
                exercises = listOf(StarterExercise("二头弯举", BodyPart.Arms, 3, 10.0, 12)),
            ),
        )
        BodyPart.Chest -> listOf(
            DefaultSection(
                name = "胸",
                bodyPart = BodyPart.Chest,
                exercises = listOf(
                    StarterExercise("卧推", BodyPart.Chest, 4, 40.0, 8),
                    StarterExercise("哑铃飞鸟", BodyPart.Chest, 3, 8.0, 12),
                ),
            ),
            DefaultSection(
                name = "肩前束",
                bodyPart = BodyPart.Chest,
                exercises = listOf(StarterExercise("哑铃推举", BodyPart.Chest, 4, 12.0, 10)),
            ),
            DefaultSection(
                name = "肱三头",
                bodyPart = BodyPart.Arms,
                exercises = listOf(StarterExercise("绳索下压", BodyPart.Arms, 3, 20.0, 12)),
            ),
        )
        BodyPart.Legs -> listOf(
            DefaultSection(
                name = "腿",
                bodyPart = BodyPart.Legs,
                exercises = listOf(
                    StarterExercise("深蹲", BodyPart.Legs, 4, 50.0, 8),
                    StarterExercise("腿举", BodyPart.Legs, 4, 80.0, 10),
                ),
            ),
            DefaultSection(
                name = "腘绳肌",
                bodyPart = BodyPart.Legs,
                exercises = listOf(StarterExercise("罗马尼亚硬拉", BodyPart.Legs, 4, 40.0, 10)),
            ),
            DefaultSection(
                name = "小腿",
                bodyPart = BodyPart.Legs,
                exercises = listOf(StarterExercise("提踵", BodyPart.Legs, 4, 30.0, 15)),
            ),
        )
        BodyPart.Arms -> listOf(
            DefaultSection(
                name = "肱二头",
                bodyPart = BodyPart.Arms,
                exercises = listOf(StarterExercise("二头弯举", BodyPart.Arms, 4, 10.0, 12)),
            ),
            DefaultSection(
                name = "肱三头",
                bodyPart = BodyPart.Arms,
                exercises = listOf(StarterExercise("绳索下压", BodyPart.Arms, 4, 20.0, 12)),
            ),
            DefaultSection(
                name = "前臂",
                bodyPart = BodyPart.Arms,
                exercises = listOf(StarterExercise("腕弯举", BodyPart.Arms, 3, 8.0, 15)),
            ),
        )
    }
