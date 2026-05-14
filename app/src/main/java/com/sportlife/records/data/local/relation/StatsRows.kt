package com.sportlife.records.data.local.relation

data class RunningStatRow(
    val dateEpochDay: Long,
    val distanceKm: Double,
    val paceSecondsPerKm: Int?,
)

data class StrengthBodyPartCountRow(
    val bodyPart: String,
    val count: Int,
)
