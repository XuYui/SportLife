package com.sportlife.records.domain.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

fun LocalDate.toStorageDay(): Long = toEpochDay()

fun Long.toLocalDate(): LocalDate = LocalDate.ofEpochDay(this)

fun LocalDate.formatForInput(): String = dateFormatter.format(this)

fun LocalDate.formatForDisplay(): String = dateFormatter.format(this)

fun parseInputDate(value: String): LocalDate? =
    try {
        LocalDate.parse(value.trim(), dateFormatter)
    } catch (_: DateTimeParseException) {
        null
    }

fun parsePaceSecondsPerKm(value: String): Int? {
    val trimmed = normalizePaceSymbols(value)
    if (trimmed.isEmpty()) return null

    val compact = Regex("""^(\d{1,2})(\d{2})$""").matchEntire(trimmed)
    if (compact != null) {
        val minutes = compact.groupValues[1].toInt()
        val seconds = compact.groupValues[2].toInt()
        if (seconds in 0..59) {
            return minutes * 60 + seconds
        }
        return null
    }

    val byQuote = Regex("""^(\d{1,2})\s*[':]\s*(\d{1,2})\s*"?(?:\s*/\s*km)?$""").matchEntire(trimmed)
    if (byQuote != null) {
        val minutes = byQuote.groupValues[1].toInt()
        val seconds = byQuote.groupValues[2].toInt()
        return minutes * 60 + seconds.coerceIn(0, 59)
    }

    val decimalMinutes = trimmed.toDoubleOrNull() ?: return null
    return (decimalMinutes * 60).toInt()
}

fun normalizePaceInput(value: String): String {
    val seconds = parsePaceSecondsPerKm(value) ?: return value
    val trimmed = normalizePaceSymbols(value)
    val shouldFormat = Regex("""^\d{3,4}$""").matches(trimmed) ||
        Regex("""^\d{1,2}\s*[':]\s*\d{2}\s*"?(?:\s*/\s*km)?$""").matches(trimmed)
    return if (shouldFormat) formatPace(seconds) else value
}

fun formatPace(secondsPerKm: Int?): String {
    if (secondsPerKm == null || secondsPerKm <= 0) return "-"
    val minutes = secondsPerKm / 60
    val seconds = secondsPerKm % 60
    return "%d'%02d\"/km".format(minutes, seconds)
}

private fun normalizePaceSymbols(value: String): String =
    value.trim()
        .replace('：', ':')
        .replace('’', '\'')
        .replace('‘', '\'')
        .replace('＇', '\'')
        .replace('″', '"')
        .replace('”', '"')
        .replace('“', '"')
        .replace('／', '/')
