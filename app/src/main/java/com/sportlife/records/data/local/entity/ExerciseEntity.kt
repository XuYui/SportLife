package com.sportlife.records.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["bodyPart"]),
        Index(value = ["groupId"]),
        Index(value = ["name", "bodyPart"], unique = true),
    ],
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val bodyPart: String,
    val groupId: Long? = null,
    val defaultSets: Int = 4,
    val defaultWeightKg: Double = 0.0,
    val defaultReps: Int = 10,
    val note: String = "",
    val isBuiltIn: Boolean = false,
)
