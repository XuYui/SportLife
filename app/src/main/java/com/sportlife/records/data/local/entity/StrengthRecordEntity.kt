package com.sportlife.records.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "strength_records",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutCheckInEntity::class,
            parentColumns = ["id"],
            childColumns = ["checkInId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["checkInId"]),
        Index(value = ["primaryBodyPart"]),
    ],
)
data class StrengthRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val checkInId: Long,
    val primaryBodyPart: String,
    val note: String = "",
)
