package com.sportlife.records.ui.screen.history

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.unit.dp
import com.sportlife.records.data.local.relation.WorkoutWithSportType
import com.sportlife.records.domain.model.BuiltInSportTypes
import com.sportlife.records.domain.util.formatForDisplay
import com.sportlife.records.domain.util.toLocalDate
import com.sportlife.records.ui.component.AppScaffold
import com.sportlife.records.ui.screen.strength.BodyPartChips
import com.sportlife.records.ui.theme.EvolveBackground
import com.sportlife.records.ui.theme.EvolveNeon
import com.sportlife.records.ui.theme.EvolveSurfaceHigh
import com.sportlife.records.ui.theme.EvolveSurfaceLow
import java.time.LocalDate
import java.time.YearMonth

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
    onEditBodyPartChange: (com.sportlife.records.domain.model.BodyPart) -> Unit,
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
                    counts = uiState.checkInCountsByDay,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth,
                    onDateSelected = onDateSelected,
                )
            }
            item {
                Text(
                    "${uiState.selectedDate.formatForDisplay()} 的记录",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
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
    counts: Map<Long, Int>,
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
                                count = counts[date.toEpochDay()] ?: 0,
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
private fun CalendarDayCell(
    date: LocalDate,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = when {
        selected -> EvolveNeon
        count > 0 -> EvolveNeon
        else -> EvolveSurfaceHigh
    }
    val contentColor = if (selected || count > 0) {
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
        elevation = CardDefaults.cardElevation(defaultElevation = if (count > 0 || selected) 2.dp else 0.dp),
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
                fontWeight = if (count > 0 || selected) FontWeight.Bold else FontWeight.Normal,
            )
            if (count > 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(contentColor.copy(alpha = 0.82f), CircleShape),
                    )
                    if (count > 1) {
                        Text("x$count", style = MaterialTheme.typography.labelSmall, color = contentColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditableHistoryRecordCard(
    record: WorkoutWithSportType,
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
            Text(record.checkIn.summary)
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
private fun EditRecordDialog(
    edit: HistoryEditState,
    onDateChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onDistanceChange: (String) -> Unit,
    onPaceChange: (String) -> Unit,
    onBodyPartChange: (com.sportlife.records.domain.model.BodyPart) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(edit.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = edit.date,
                    onValueChange = onDateChange,
                    label = { Text("日期") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (edit.sportTypeId == BuiltInSportTypes.Running.id) {
                    OutlinedTextField(
                        value = edit.distanceKm,
                        onValueChange = onDistanceChange,
                        label = { Text("公里数") },
                        suffix = { Text("km") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = edit.pace,
                        onValueChange = onPaceChange,
                        label = { Text("配速") },
                        placeholder = { Text("5'30\"/km") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Text("四分化类型", fontWeight = FontWeight.SemiBold)
                    BodyPartChips(selected = edit.bodyPart, onSelected = onBodyPartChange)
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
