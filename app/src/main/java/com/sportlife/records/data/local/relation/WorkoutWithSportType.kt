package com.sportlife.records.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.sportlife.records.data.local.entity.SportTypeEntity
import com.sportlife.records.data.local.entity.WorkoutCheckInEntity

data class WorkoutWithSportType(
    @Embedded val checkIn: WorkoutCheckInEntity,
    @Relation(
        parentColumn = "sportTypeId",
        entityColumn = "id",
    )
    val sportType: SportTypeEntity,
)
