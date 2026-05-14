package com.sportlife.records.domain.model

enum class BodyPart(val label: String) {
    Back("背"),
    Chest("胸"),
    Legs("腿"),
    Arms("手臂");

    companion object {
        fun fromName(name: String): BodyPart =
            entries.firstOrNull { it.name == name } ?: Back
    }
}
