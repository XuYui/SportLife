package com.sportlife.records.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "strength_exercise_records",
    foreignKeys = [
        ForeignKey(
            entity = StrengthRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["strengthRecordId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["strengthRecordId"]),
        Index(value = ["exerciseId"]),
        Index(value = ["bodyPart"]),
    ],
)
data class StrengthExerciseRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val strengthRecordId: Long,
    val exerciseId: Long? = null,
    val exerciseName: String,
    val bodyPart: String,
    val note: String = "",
    val sortOrder: Int = 0,
)
