package com.sportlife.records.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "strength_exercise_sets",
    foreignKeys = [
        ForeignKey(
            entity = StrengthExerciseRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseRecordId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["exerciseRecordId"]),
        Index(value = ["exerciseRecordId", "setIndex"], unique = true),
    ],
)
data class StrengthExerciseSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseRecordId: Long,
    val setIndex: Int,
    val weightKg: Double,
    val reps: Int,
)
