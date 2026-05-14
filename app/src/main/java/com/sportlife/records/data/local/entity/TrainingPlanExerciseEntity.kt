package com.sportlife.records.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "training_plan_exercises",
    foreignKeys = [
        ForeignKey(
            entity = TrainingDayEntity::class,
            parentColumns = ["id"],
            childColumns = ["trainingDayId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = ExerciseGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseGroupId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["trainingDayId"]),
        Index(value = ["sectionId"]),
        Index(value = ["exerciseId"]),
        Index(value = ["exerciseGroupId"]),
        Index(value = ["bodyPart"]),
    ],
)
data class TrainingPlanExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trainingDayId: Long,
    val sectionId: Long? = null,
    val exerciseId: Long? = null,
    val exerciseGroupId: Long? = null,
    val exerciseName: String,
    val bodyPart: String,
    val sets: Int,
    val defaultWeightKg: Double,
    val defaultReps: Int,
    val note: String = "",
    val sortOrder: Int = 0,
)
