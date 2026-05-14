package com.sportlife.records.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_groups",
    indices = [
        Index(value = ["bodyPart"]),
        Index(value = ["name", "bodyPart"], unique = true),
    ],
)
data class ExerciseGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val bodyPart: String,
    val sortOrder: Int = 0,
)
