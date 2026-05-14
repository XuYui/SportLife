package com.sportlife.records.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "training_day_sections",
    foreignKeys = [
        ForeignKey(
            entity = TrainingDayEntity::class,
            parentColumns = ["id"],
            childColumns = ["trainingDayId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["trainingDayId"]),
        Index(value = ["trainingDayId", "sortOrder"]),
    ],
)
data class TrainingDaySectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trainingDayId: Long,
    val name: String,
    val bodyPart: String? = null,
    val sortOrder: Int = 0,
)
