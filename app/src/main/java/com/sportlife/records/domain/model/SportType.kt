package com.sportlife.records.domain.model

data class SportType(
    val id: String,
    val displayName: String,
    val description: String,
    val capabilities: Set<SportCapability>,
)

enum class SportCapability {
    Distance,
    Pace,
    ExerciseSets,
    TrainingPlan,
}

object BuiltInSportTypes {
    val Running = SportType(
        id = "RUNNING",
        displayName = "跑步",
        description = "公里数、配速、备注",
        capabilities = setOf(SportCapability.Distance, SportCapability.Pace),
    )

    val StrengthTraining = SportType(
        id = "STRENGTH_TRAINING",
        displayName = "健身",
        description = "部位、动作、组数、重量、次数",
        capabilities = setOf(SportCapability.ExerciseSets, SportCapability.TrainingPlan),
    )

    val all = listOf(Running, StrengthTraining)
}
