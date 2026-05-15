package com.sportlife.records.domain.model

fun defaultBodyPartValues(): List<String> = BodyPart.entries.map { it.name }

fun displayBodyPartName(value: String?): String {
    val rawValue = value?.trim().orEmpty()
    if (rawValue.isBlank()) return "未设置"
    return BodyPart.entries.firstOrNull { it.name == rawValue || it.label == rawValue }?.label ?: rawValue
}

fun normalizeCustomFocus(value: String): String = value.trim().ifBlank { BodyPart.Back.name }
