package com.sportlife.records.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.sportlife.records.data.local.entity.TrainingDayEntity
import com.sportlife.records.data.local.entity.TrainingDaySectionEntity
import com.sportlife.records.data.local.entity.TrainingPlanEntity
import com.sportlife.records.data.local.entity.TrainingPlanExerciseEntity

data class TrainingDaySectionWithExercises(
    @Embedded val section: TrainingDaySectionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sectionId",
    )
    val exercises: List<TrainingPlanExerciseEntity>,
)

data class TrainingDayWithExercises(
    @Embedded val day: TrainingDayEntity,
    @Relation(
        entity = TrainingDaySectionEntity::class,
        parentColumn = "id",
        entityColumn = "trainingDayId",
    )
    val sections: List<TrainingDaySectionWithExercises>,
    @Relation(
        parentColumn = "id",
        entityColumn = "trainingDayId",
    )
    val exercises: List<TrainingPlanExerciseEntity>,
)

data class TrainingPlanWithDays(
    @Embedded val plan: TrainingPlanEntity,
    @Relation(
        entity = TrainingDayEntity::class,
        parentColumn = "id",
        entityColumn = "planId",
    )
    val days: List<TrainingDayWithExercises>,
)
