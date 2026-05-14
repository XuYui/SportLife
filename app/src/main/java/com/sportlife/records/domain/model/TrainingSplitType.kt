package com.sportlife.records.domain.model

enum class TrainingSplitType(val id: String, val label: String, val daysPerCycle: Int) {
    ThreeDay("THREE_DAY", "三分化", 3),
    FourDay("FOUR_DAY", "四分化", 4);

    companion object {
        fun fromId(id: String): TrainingSplitType =
            entries.firstOrNull { it.id == id } ?: ThreeDay
    }
}
