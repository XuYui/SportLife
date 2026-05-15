package com.sportlife.records.data.local

import androidx.room.withTransaction
import com.sportlife.records.data.local.entity.ExerciseEntity
import com.sportlife.records.data.local.entity.ExerciseGroupEntity
import com.sportlife.records.data.local.entity.TrainingDayEntity
import com.sportlife.records.data.local.entity.TrainingDaySectionEntity
import com.sportlife.records.data.local.entity.TrainingPlanEntity
import com.sportlife.records.data.local.entity.TrainingPlanExerciseEntity
import com.sportlife.records.data.local.entity.toEntity
import com.sportlife.records.data.repository.StarterExercise
import com.sportlife.records.data.repository.defaultSections
import com.sportlife.records.domain.model.BodyPart
import com.sportlife.records.domain.model.BuiltInSportTypes
import com.sportlife.records.domain.model.TrainingSplitType

class DefaultDataSeeder(
    private val database: SportLifeDatabase,
) {
    suspend fun seedIfNeeded() {
        database.withTransaction {
            database.sportTypeDao().upsertAll(BuiltInSportTypes.all.map { it.toEntity() })
            database.trainingPlanDao().upsertSplits(TrainingSplitType.entries.map { it.toEntity() })

            if (database.exerciseDao().groupCount() == 0) {
                val backGroupId = database.exerciseDao().insertGroup(
                    ExerciseGroupEntity(name = "背部训练", bodyPart = BodyPart.Back.name, sortOrder = 10),
                )
                val chestGroupId = database.exerciseDao().insertGroup(
                    ExerciseGroupEntity(name = "胸部训练", bodyPart = BodyPart.Chest.name, sortOrder = 20),
                )
                val legsGroupId = database.exerciseDao().insertGroup(
                    ExerciseGroupEntity(name = "腿部训练", bodyPart = BodyPart.Legs.name, sortOrder = 30),
                )
                val armsGroupId = database.exerciseDao().insertGroup(
                    ExerciseGroupEntity(name = "手臂训练", bodyPart = BodyPart.Arms.name, sortOrder = 40),
                )

                val exercises = listOf(
                    ExerciseEntity(name = "引体向上", bodyPart = BodyPart.Back.name, groupId = backGroupId, defaultSets = 4, defaultReps = 8, isBuiltIn = true),
                    ExerciseEntity(name = "坐姿划船", bodyPart = BodyPart.Back.name, groupId = backGroupId, defaultSets = 4, defaultWeightKg = 35.0, defaultReps = 10, isBuiltIn = true),
                    ExerciseEntity(name = "高位下拉", bodyPart = BodyPart.Back.name, groupId = backGroupId, defaultSets = 4, defaultWeightKg = 40.0, defaultReps = 10, isBuiltIn = true),
                    ExerciseEntity(name = "卧推", bodyPart = BodyPart.Chest.name, groupId = chestGroupId, defaultSets = 4, defaultWeightKg = 40.0, defaultReps = 8, isBuiltIn = true),
                    ExerciseEntity(name = "哑铃飞鸟", bodyPart = BodyPart.Chest.name, groupId = chestGroupId, defaultSets = 3, defaultWeightKg = 8.0, defaultReps = 12, isBuiltIn = true),
                    ExerciseEntity(name = "深蹲", bodyPart = BodyPart.Legs.name, groupId = legsGroupId, defaultSets = 4, defaultWeightKg = 50.0, defaultReps = 8, isBuiltIn = true),
                    ExerciseEntity(name = "腿举", bodyPart = BodyPart.Legs.name, groupId = legsGroupId, defaultSets = 4, defaultWeightKg = 80.0, defaultReps = 10, isBuiltIn = true),
                    ExerciseEntity(name = "二头弯举", bodyPart = BodyPart.Arms.name, groupId = armsGroupId, defaultSets = 3, defaultWeightKg = 10.0, defaultReps = 12, isBuiltIn = true),
                    ExerciseEntity(name = "绳索下压", bodyPart = BodyPart.Arms.name, groupId = armsGroupId, defaultSets = 3, defaultWeightKg = 20.0, defaultReps = 12, isBuiltIn = true),
                )
                exercises.forEach { database.exerciseDao().insertExercise(it) }
            }

            if (database.trainingPlanDao().planCount() == 0) {
                createDefaultPlan(TrainingSplitType.ThreeDay)
            }

            if (database.trainingPlanDao().sectionCount() == 0) {
                ensureSectionsForExistingDays()
            }
        }
    }

    private suspend fun createDefaultPlan(split: TrainingSplitType) {
        val planId = database.trainingPlanDao().insertPlan(
            TrainingPlanEntity(
                name = "${split.label}默认计划",
                splitId = split.id,
                isActive = true,
            ),
        )

        val dayNames = when (split) {
            TrainingSplitType.ThreeDay -> listOf("第 1 天 背部", "第 2 天 胸和手臂", "第 3 天 腿部")
            TrainingSplitType.FourDay -> listOf("第 1 天 背部", "第 2 天 胸部", "第 3 天 腿部", "第 4 天 手臂")
            TrainingSplitType.Custom -> listOf("第 1 天 自定义")
        }
        val focusParts = when (split) {
            TrainingSplitType.ThreeDay -> listOf(BodyPart.Back, BodyPart.Chest, BodyPart.Legs)
            TrainingSplitType.FourDay -> listOf(BodyPart.Back, BodyPart.Chest, BodyPart.Legs, BodyPart.Arms)
            TrainingSplitType.Custom -> emptyList()
        }

        dayNames.forEachIndexed { index, name ->
            val focusBodyPart = focusParts.getOrNull(index)
            val dayId = database.trainingPlanDao().insertDay(
                TrainingDayEntity(
                    planId = planId,
                    dayIndex = index,
                    name = name,
                    focusBodyPart = focusBodyPart?.name ?: "自定义",
                ),
            )
            focusBodyPart?.let { insertStarterSectionsAndExercises(dayId, it) }
        }
    }

    private suspend fun ensureSectionsForExistingDays() {
        database.trainingPlanDao().getAllDays().forEach { day ->
            val focusBodyPart = day.focusBodyPart?.let { BodyPart.fromName(it) } ?: BodyPart.Back
            val sectionIds = defaultSections(focusBodyPart).mapIndexed { index, section ->
                val sectionId = database.trainingPlanDao().insertSection(
                    TrainingDaySectionEntity(
                        trainingDayId = day.id,
                        name = section.name,
                        bodyPart = section.bodyPart.name,
                        sortOrder = index,
                    ),
                )
                section.bodyPart to sectionId
            }
            val existingExercises = database.trainingPlanDao().getExercisesForDay(day.id)
            if (existingExercises.isEmpty()) {
                defaultSections(focusBodyPart).forEachIndexed { sectionIndex, section ->
                    val sectionId = sectionIds.getOrNull(sectionIndex)?.second ?: return@forEachIndexed
                    section.exercises.forEachIndexed { exerciseIndex, starter ->
                        insertStarterExercise(day.id, sectionId, exerciseIndex, starter)
                    }
                }
            } else {
                existingExercises.forEach { exercise ->
                    val sectionId = sectionIds.firstOrNull { it.first.name == exercise.bodyPart }?.second
                        ?: sectionIds.firstOrNull()?.second
                    database.trainingPlanDao().updateExerciseSection(exercise.id, sectionId)
                }
            }
        }
    }

    private suspend fun insertStarterSectionsAndExercises(dayId: Long, bodyPart: BodyPart) {
        defaultSections(bodyPart).forEachIndexed { sectionIndex, section ->
            val sectionId = database.trainingPlanDao().insertSection(
                TrainingDaySectionEntity(
                    trainingDayId = dayId,
                    name = section.name,
                    bodyPart = section.bodyPart.name,
                    sortOrder = sectionIndex,
                ),
            )
            section.exercises.forEachIndexed { exerciseIndex, starter ->
                insertStarterExercise(dayId, sectionId, exerciseIndex, starter)
            }
        }
    }

    private suspend fun insertStarterExercise(
        dayId: Long,
        sectionId: Long,
        sortOrder: Int,
        starter: StarterExercise,
    ) {
        val exercise = database.exerciseDao().findExercise(starter.name, starter.bodyPart.name)
        database.trainingPlanDao().insertPlanExercise(
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
