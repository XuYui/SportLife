package com.sportlife.records.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_check_ins",
    foreignKeys = [
        ForeignKey(
            entity = SportTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["sportTypeId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(value = ["sportTypeId"]),
        Index(value = ["dateEpochDay"]),
    ],
)
data class WorkoutCheckInEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sportTypeId: String,
    val dateEpochDay: Long,
    val summary: String,
    val note: String = "",
    val metadataJson: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(),
)
