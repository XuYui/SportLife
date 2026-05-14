package com.sportlife.records.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "running_records",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutCheckInEntity::class,
            parentColumns = ["id"],
            childColumns = ["checkInId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["checkInId"])],
)
data class RunningRecordEntity(
    @PrimaryKey val checkInId: Long,
    val distanceKm: Double,
    val paceSecondsPerKm: Int?,
    val durationSeconds: Long? = null,
    val averageHeartRate: Int? = null,
    val routeSnapshotJson: String? = null,
    val extrasJson: String? = null,
)
