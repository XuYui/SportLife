package com.sportlife.records.ui.screen.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportlife.records.data.local.relation.RunningStatRow
import com.sportlife.records.data.local.relation.StrengthBodyPartCountRow
import com.sportlife.records.data.repository.WorkoutRepository
import com.sportlife.records.domain.model.BodyPart
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale

data class RunningChartPoint(
    val date: LocalDate,
    val distanceKm: Double,
    val paceSecondsPerKm: Int?,
)

data class BarChartValue(
    val label: String,
    val value: Double,
)

data class RadarChartValue(
    val label: String,
    val value: Double,
)

data class StatsUiState(
    val runningPoints: List<RunningChartPoint> = emptyList(),
    val weeklyRunning: List<BarChartValue> = emptyList(),
    val monthlyRunning: List<BarChartValue> = emptyList(),
    val strengthFrequency: List<RadarChartValue> = BodyPart.entries.map { RadarChartValue(it.label, 0.0) },
)

class StatsViewModel(
    workoutRepository: WorkoutRepository,
) : ViewModel() {
    val uiState = combine(
        workoutRepository.observeRunningStats(),
        workoutRepository.observeStrengthBodyPartCounts(),
    ) { runningRows, strengthRows ->
        StatsUiState(
            runningPoints = runningRows.mapToRunningPoints(),
            weeklyRunning = runningRows.toWeeklyVolumes(),
            monthlyRunning = runningRows.toMonthlyVolumes(),
            strengthFrequency = strengthRows.toRadarValues(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(),
    )
}

private fun List<RunningStatRow>.mapToRunningPoints(): List<RunningChartPoint> =
    map {
        RunningChartPoint(
            date = LocalDate.ofEpochDay(it.dateEpochDay),
            distanceKm = it.distanceKm,
            paceSecondsPerKm = it.paceSecondsPerKm,
        )
    }.sortedBy { it.date }

private fun List<RunningStatRow>.toWeeklyVolumes(): List<BarChartValue> {
    val weekFields = WeekFields.of(Locale.getDefault())
    return groupBy { row ->
        val date = LocalDate.ofEpochDay(row.dateEpochDay)
        val year = date.get(weekFields.weekBasedYear())
        val week = date.get(weekFields.weekOfWeekBasedYear())
        year to week
    }.entries
        .sortedWith(compareBy<Map.Entry<Pair<Int, Int>, List<RunningStatRow>>> { it.key.first }.thenBy { it.key.second })
        .takeLast(8)
        .map { entry ->
            BarChartValue(
                label = "${entry.key.second}周",
                value = entry.value.sumOf { it.distanceKm },
            )
        }
}

private fun List<RunningStatRow>.toMonthlyVolumes(): List<BarChartValue> =
    groupBy { YearMonth.from(LocalDate.ofEpochDay(it.dateEpochDay)) }
        .entries
        .sortedBy { it.key }
        .takeLast(6)
        .map { entry ->
            BarChartValue(
                label = "${entry.key.monthValue}月",
                value = entry.value.sumOf { it.distanceKm },
            )
        }

private fun List<StrengthBodyPartCountRow>.toRadarValues(): List<RadarChartValue> {
    val counts = associate { BodyPart.fromName(it.bodyPart) to it.count }
    return BodyPart.entries.map { part ->
        RadarChartValue(
            label = part.label,
            value = (counts[part] ?: 0).toDouble(),
        )
    }
}
