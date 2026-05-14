package com.sportlife.records.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sportlife.records.domain.model.TrainingSplitType

@Entity(tableName = "training_splits")
data class TrainingSplitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val daysPerCycle: Int,
)

fun TrainingSplitType.toEntity(): TrainingSplitEntity =
    TrainingSplitEntity(
        id = id,
        name = label,
        daysPerCycle = daysPerCycle,
    )
