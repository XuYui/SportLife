package com.sportlife.records.ui.screen.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sportlife.records.data.local.relation.WorkoutWithSportType
import com.sportlife.records.domain.model.BuiltInSportTypes
import com.sportlife.records.domain.model.displayBodyPartName
import com.sportlife.records.domain.util.formatForDisplay
import com.sportlife.records.domain.util.formatPace
import com.sportlife.records.domain.util.toLocalDate
import com.sportlife.records.ui.component.AppScaffold
import com.sportlife.records.ui.component.AppDateField
import com.sportlife.records.ui.component.DecimalStepperField
import com.sportlife.records.ui.component.PaceQuickField
import com.sportlife.records.ui.component.SaveFeedbackMessage
import com.sportlife.records.ui.theme.EvolveBackground
import com.sportlife.records.ui.theme.EvolveMuted
import com.sportlife.records.ui.theme.EvolveNeon
import com.sportlife.records.ui.theme.EvolveSurfaceHigh
import com.sportlife.records.ui.theme.EvolveSurfaceLow
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onEdit: (WorkoutWithSportType) -> Unit,
    onDelete: (WorkoutWithSportType) -> Unit,
    onEditDateChange: (String) -> Unit,
    onEditNoteChange: (String) -> Unit,
    onEditDistanceChange: (String) -> Unit,
    onEditPaceChange: (String) -> Unit,
    onEditBodyPartChange: (String) -> Unit,
    onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onBack: () -> Unit,
) {
    AppScaffold(title = "历史记录", onBack = onBack) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                CalendarCard(
                    month = uiState.currentMonth,
                    selectedDate = uiState.selectedDate,
                    loads = uiState.calendarTrainingLoadsByDay,
                    maxLoad = uiState.maxCalendarTrainingLoad,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth,
                    onDateSelected = onDateSelected,
                )
            }
            item {
                AnimatedVisibility(
                    visible = uiState.feedback != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    SaveFeedbackMessage(uiState.feedback)
                }
            }
            item {
                DailyDetailSummary(
                    selectedDate = uiState.selectedDate,
                    records = uiState.recordsForSelectedDate,
                    details = uiState.detailsByCheckInId,
                )
            }
            if (uiState.recordsForSelectedDate.isEmpty()) {
                item {
                    Text("这一天还没有打卡", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(uiState.recordsForSelectedDate, key = { it.checkIn.id }) { record ->
                    EditableHistoryRecordCard(
                        record = record,
                        detail = uiState.detailsByCheckInId[record.checkIn.id],
                        onEdit = { onEdit(record) },
                        onDelete = { onDelete(record) },
                    )
                }
            }
        }
    }

    uiState.editing?.let { edit ->
        EditRecordDialog(
            edit = edit,
            onDateChange = onEditDateChange,
            onNoteChange = onEditNoteChange,
            onDistanceChange = onEditDistanceChange,
            onPaceChange = onEditPaceChange,
            onBodyPartChange = onEditBodyPartChange,
            onSave = onSaveEdit,
            onCancel = onCancelEdit,
        )
    }
}

@Composable
private fun CalendarCard(
    month: YearMonth,
    selectedDate: LocalDate,
    loads: Map<Long, CalendarTrainingLoad>,
    maxLoad: Double,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = EvolveSurfaceLow,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "上个月")
                }
                Text("${month.year} 年 ${month.monthValue} 月", fontWeight = FontWeight.Bold)
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "下个月")
                }
            }
            CalendarHeatLegend()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("日", "一", "二", "三", "四", "五", "六").forEach { label ->
                    Text(
                        label,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            val firstDay = month.atDay(1)
            val leadingEmpty = firstDay.dayOfWeek.value % 7
            val days = (1..month.lengthOfMonth()).map { month.atDay(it) }
            val cells = List<LocalDate?>(leadingEmpty) { null } + days
            cells.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    week.forEach { date ->
                        if (date == null) {
                            Spacer(modifier = Modifier.weight(1f).height(54.dp))
                        } else {
                            CalendarDayCell(
                                date = date,
                                load = loads[date.toEpochDay()] ?: CalendarTrainingLoad(),
                                maxLoad = maxLoad,
                                selected = date == selectedDate,
                                onClick = { onDateSelected(date) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    repeat(7 - week.size) {
                        Spacer(modifier = Modifier.weight(1f).height(54.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarHeatLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("训练热力", color = EvolveMuted, style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("少", color = EvolveMuted, style = MaterialTheme.typography.labelSmall)
            listOf(0.22f, 0.44f, 0.68f, 0.9f).forEach { alpha ->
                Box(
                    modifier = Modifier
                        .size(18.dp, 8.dp)
                        .background(EvolveNeon.copy(alpha = alpha), RoundedCornerShape(999.dp)),
                )
            }
            Text("多", color = EvolveMuted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    load: CalendarTrainingLoad,
    maxLoad: Double,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val intensity = if (load.score > 0.0 && maxLoad > 0.0) {
        (load.score / maxLoad).coerceIn(0.24, 1.0).toFloat()
    } else {
        0f
    }
    val containerColor = when {
        selected -> EvolveNeon
        intensity > 0f -> EvolveNeon.copy(alpha = 0.18f + intensity * 0.62f)
        else -> EvolveSurfaceHigh
    }
    val contentColor = if (selected || intensity > 0.72f) {
        EvolveBackground
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(8.dp),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface) else null,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (intensity > 0f || selected) 2.dp else 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                date.dayOfMonth.toString(),
                color = contentColor,
                fontWeight = if (intensity > 0f || selected) FontWeight.Bold else FontWeight.Normal,
            )
            if (load.score > 0.0) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(contentColor.copy(alpha = 0.82f), CircleShape),
                    )
                    Text(
                        text = loadLabel(load),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private fun loadLabel(load: CalendarTrainingLoad): String =
    when {
        load.runningDistanceKm > 0.0 && load.strengthSets > 0 -> "${load.recordCount}项"
        load.runningDistanceKm > 0.0 -> "${formatDistance(load.runningDistanceKm)}km"
        load.strengthSets > 0 -> "${load.strengthSets}组"
        load.recordCount > 0 -> "${load.recordCount}条"
        else -> ""
    }

@Composable
private fun DailyDetailSummary(
    selectedDate: LocalDate,
    records: List<WorkoutWithSportType>,
    details: Map<Long, HistoryRecordDetail>,
) {
    val runningDistance = records
        .filter { it.sportType.id == BuiltInSportTypes.Running.id }
        .sumOf { details[it.checkIn.id]?.distanceKm ?: 0.0 }
    val strengthParts = records
        .filter { it.sportType.id == BuiltInSportTypes.StrengthTraining.id }
        .mapNotNull { details[it.checkIn.id]?.primaryBodyPart }
        .map(::displayBodyPartName)
        .distinct()
    val totalStrengthSets = records.sumOf { details[it.checkIn.id]?.totalSets ?: 0 }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = EvolveSurfaceLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "${selectedDate.formatForDisplay()} 的详情",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (records.isEmpty()) {
                Text("这一天还没有打卡", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    DetailMetric("记录", "${records.size}", "条", Modifier.weight(1f))
                    DetailMetric("跑步", formatDistance(runningDistance), "km", Modifier.weight(1f))
                    DetailMetric("健身", if (totalStrengthSets > 0) "$totalStrengthSets" else "${strengthParts.size}", if (totalStrengthSets > 0) "组" else "项", Modifier.weight(1f))
                }
                if (strengthParts.isNotEmpty()) {
                    Text("训练部位：${strengthParts.joinToString(" / ")}", color = EvolveMuted)
                }
            }
        }
    }
}

@Composable
private fun DetailMetric(
    title: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = EvolveSurfaceHigh),
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = EvolveMuted, style = MaterialTheme.typography.labelSmall)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontWeight = FontWeight.ExtraBold, color = EvolveNeon)
                Text(unit, modifier = Modifier.padding(start = 3.dp, bottom = 1.dp), color = EvolveMuted, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun EditableHistoryRecordCard(
    record: WorkoutWithSportType,
    detail: HistoryRecordDetail?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = EvolveSurfaceLow,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(record.sportType.displayName, fontWeight = FontWeight.SemiBold)
                Text(record.checkIn.dateEpochDay.toLocalDate().formatForDisplay(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (record.sportType.id == BuiltInSportTypes.Running.id) Icons.Default.DirectionsRun else Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = EvolveNeon,
                    modifier = Modifier.size(20.dp),
                )
                Text(record.checkIn.summary, fontWeight = FontWeight.Medium)
            }
            when (record.sportType.id) {
                BuiltInSportTypes.Running.id -> RunningRecordDetail(detail)
                BuiltInSportTypes.StrengthTraining.id -> StrengthRecordDetail(detail)
            }
            if (record.checkIn.note.isNotBlank()) {
                Text(record.checkIn.note, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Text("修改")
                }
                TextButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Text("删除")
                }
            }
        }
    }
}

@Composable
private fun RunningRecordDetail(detail: HistoryRecordDetail?) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        DetailMetric(
            title = "里程",
            value = detail?.distanceKm?.let(::formatDistance) ?: "--",
            unit = "km",
            modifier = Modifier.weight(1f),
        )
        DetailMetric(
            title = "配速",
            value = detail?.paceSecondsPerKm?.let(::formatPace)?.takeIf { it != "-" } ?: "未记录",
            unit = "",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StrengthRecordDetail(detail: HistoryRecordDetail?) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "训练部位：${displayBodyPartName(detail?.primaryBodyPart)}",
            color = EvolveMuted,
            style = MaterialTheme.typography.bodyMedium,
        )
        if (detail == null || detail.exercises.isEmpty()) {
            Text("未记录具体动作", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        } else {
            detail.exercises.forEach { exercise ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EvolveSurfaceHigh, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(exercise.name, fontWeight = FontWeight.SemiBold)
                        Text(exercise.bodyPart, color = EvolveMuted)
                    }
                    Text(
                        exercise.sets.takeIf { it.isNotEmpty() }?.joinToString(" / ") { set ->
                            val weight = if (set.weightKg > 0.0) "${formatDistance(set.weightKg)}kg" else "自重"
                            "$weight x ${set.reps}"
                        } ?: "未记录组数",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (exercise.note.isNotBlank()) {
                        Text(exercise.note, color = EvolveMuted, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditRecordDialog(
    edit: HistoryEditState,
    onDateChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onDistanceChange: (String) -> Unit,
    onPaceChange: (String) -> Unit,
    onBodyPartChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(edit.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AppDateField(
                    value = edit.date,
                    onValueChange = onDateChange,
                    label = "日期",
                )
                if (edit.sportTypeId == BuiltInSportTypes.Running.id) {
                    DecimalStepperField(
                        value = edit.distanceKm,
                        onValueChange = onDistanceChange,
                        label = "公里数",
                        unit = "km",
                        step = 0.5,
                        quickValues = listOf(3.0, 5.0, 8.0, 10.0),
                    )
                    PaceQuickField(
                        value = edit.pace,
                        onValueChange = onPaceChange,
                    )
                } else {
                    Text("训练部位", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = edit.bodyPart,
                        onValueChange = onBodyPartChange,
                        label = { Text("训练部位") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                OutlinedTextField(
                    value = edit.note,
                    onValueChange = onNoteChange,
                    label = { Text("备注") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (edit.message != null) {
                    Text(edit.message, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("取消")
            }
        },
    )
}

private fun formatDistance(value: Double): String =
    String.format(Locale.US, "%.2f", value)
        .trimEnd('0')
        .trimEnd('.')
