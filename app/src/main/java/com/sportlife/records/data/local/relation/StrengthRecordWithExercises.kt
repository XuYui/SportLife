package com.sportlife.records.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.sportlife.records.data.local.entity.StrengthExerciseRecordEntity
import com.sportlife.records.data.local.entity.StrengthExerciseSetEntity
import com.sportlife.records.data.local.entity.StrengthRecordEntity

data class StrengthExerciseWithSets(
    @Embedded val exercise: StrengthExerciseRecordEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "exerciseRecordId",
    )
    val sets: List<StrengthExerciseSetEntity>,
)

data class StrengthRecordWithExercises(
    @Embedded val record: StrengthRecordEntity,
    @Relation(
        entity = StrengthExerciseRecordEntity::class,
        parentColumn = "id",
        entityColumn = "strengthRecordId",
    )
    val exercises: List<StrengthExerciseWithSets>,
)
