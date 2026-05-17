package com.sportlife.records.ui.screen.running

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun RunningCheckInScreen(
    uiState: RunningCheckInUiState,
    onDistanceChange: (String) -> Unit,
    onPaceChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    AppScaffold(title = "跑步打卡", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            RunningHeader(distance = uiState.distanceKm.ifBlank { "0.00" })
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                RunningDataCard(
                    title = "公里数",
                    value = uiState.distanceKm.ifBlank { "--" },
                    unit = "km",
                    icon = Icons.Default.DirectionsRun,
                    highlighted = true,
                    modifier = Modifier.weight(1f),
                )
                RunningDataCard(
                    title = "配速",
                    value = uiState.pace.ifBlank { "--" },
                    unit = "/km",
                    icon = Icons.Default.Speed,
                    highlighted = false,
                    modifier = Modifier.weight(1f),
                )
            }
            RunningDataCard(
                title = "日期",
                value = uiState.date,
                unit = "记录日",
                icon = Icons.Default.CalendarMonth,
                highlighted = false,
                modifier = Modifier.fillMaxWidth(),
            )
            RunningFormCard(
                uiState = uiState,
                onDistanceChange = onDistanceChange,
                onPaceChange = onPaceChange,
                onDateChange = onDateChange,
                onNoteChange = onNoteChange,
                onSave = onSave,
            )
        }
    }
}

@Composable
private fun RunningHeader(distance: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = EvolveSurfaceLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("跑步记录", color = EvolveMuted, style = MaterialTheme.typography.labelLarge)
                Text(
                    text = "$distance km",
                    color = EvolveNeon,
                    fontSize = 40.sp,
                    lineHeight = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text("记录公里数、配速、日期和备注", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(modifier = Modifier.size(84.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(EvolveSurfaceHigh, -90f, 360f, false, style = stroke)
                    drawArc(EvolveNeon, -90f, 250f, false, style = stroke)
                    drawLine(
                        color = EvolveNeon.copy(alpha = 0.42f),
                        start = Offset(size.width * 0.22f, size.height * 0.72f),
                        end = Offset(size.width * 0.78f, size.height * 0.28f),
                        strokeWidth = 5f,
                        cap = StrokeCap.Round,
                    )
                }
                Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = EvolveNeon)
            }
        }
    }
}

@Composable
private fun RunningDataCard(
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    highlighted: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.height(126.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = EvolveSurfaceLow),
        border = BorderStroke(1.dp, if (highlighted) EvolveNeon else MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = if (highlighted) EvolveNeon else EvolveMuted, modifier = Modifier.size(20.dp))
                Text(title, color = EvolveMuted, style = MaterialTheme.typography.labelLarge)
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = if (highlighted) EvolveNeon else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(unit, modifier = Modifier.padding(start = 5.dp, bottom = 3.dp), color = EvolveMuted)
            }
        }
    }
}

@Composable
private fun RunningFormCard(
    uiState: RunningCheckInUiState,
    onDistanceChange: (String) -> Unit,
    onPaceChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = EvolveSurfaceLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("填写跑步信息", color = EvolveMuted, style = MaterialTheme.typography.labelLarge)
            DecimalStepperField(
                value = uiState.distanceKm,
                onValueChange = onDistanceChange,
                label = "公里数",
                unit = "km",
                step = 0.5,
                quickValues = listOf(3.0, 5.0, 8.0, 10.0),
            )
            PaceQuickField(
                value = uiState.pace,
                onValueChange = onPaceChange,
            )
            AppDateField(
                value = uiState.date,
                onValueChange = onDateChange,
                label = "日期",
            )
            OutlinedTextField(
                value = uiState.note,
                onValueChange = onNoteChange,
                label = { Text("备注") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
            AnimatedVisibility(
                visible = uiState.message != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                SaveFeedbackMessage(uiState.message)
            }
            Button(
                onClick = onSave,
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EvolveNeon, contentColor = EvolveBackground),
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Text("保存跑步打卡", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}
