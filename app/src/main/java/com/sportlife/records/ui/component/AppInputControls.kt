package com.sportlife.records.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.sportlife.records.domain.util.formatForInput
import com.sportlife.records.domain.util.parseInputDate
import com.sportlife.records.ui.theme.EvolveBackground
import com.sportlife.records.ui.theme.EvolveMuted
import com.sportlife.records.ui.theme.EvolveNeon
import com.sportlife.records.ui.theme.EvolveSurfaceHigh
import com.sportlife.records.ui.theme.EvolveSurfaceLow
import java.time.LocalDate
import java.util.Locale

@Composable
fun AppDateField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "日期",
) {
    val parsedDate = parseInputDate(value)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(label, color = EvolveMuted, style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                enabled = parsedDate != null,
                onClick = { parsedDate?.minusDays(1)?.let { onValueChange(it.formatForInput()) } },
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "前一天")
            }
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = EvolveSurfaceHigh),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(parsedDate?.formatForInput() ?: value.ifBlank { "未设置" }, fontWeight = FontWeight.Bold)
                    Text(parsedDate?.dayOfWeek?.let { "周${"一二三四五六日"[it.value - 1]}" }.orEmpty(), color = EvolveMuted)
                }
            }
            IconButton(
                enabled = parsedDate != null,
                onClick = { parsedDate?.plusDays(1)?.let { onValueChange(it.formatForInput()) } },
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "后一天")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { onValueChange(LocalDate.now().formatForInput()) }) {
                Text("今天")
            }
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text("手动输入") },
                placeholder = { Text("yyyy-MM-dd") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DecimalStepperField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    unit: String,
    modifier: Modifier = Modifier,
    step: Double = 0.5,
    quickValues: List<Double> = emptyList(),
) {
    val numericValue = value.toDoubleOrNull()
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(label, color = EvolveMuted, style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                enabled = numericValue != null && numericValue > 0.0,
                onClick = {
                    val current = numericValue ?: 0.0
                    onValueChange(formatDecimal((current - step).coerceAtLeast(0.0)))
                },
            ) {
                Icon(Icons.Default.Remove, contentDescription = "减少")
            }
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = EvolveSurfaceHigh),
                border = BorderStroke(1.dp, EvolveNeon),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(value.ifBlank { "--" }, color = EvolveNeon, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    Text(unit, modifier = Modifier.padding(start = 4.dp, bottom = 3.dp), color = EvolveMuted)
                }
            }
            IconButton(
                onClick = {
                    val current = numericValue ?: 0.0
                    onValueChange(formatDecimal(current + step))
                },
            ) {
                Icon(Icons.Default.Add, contentDescription = "增加")
            }
        }
        if (quickValues.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                quickValues.forEach { quickValue ->
                    QuickChip(
                        label = "${formatDecimal(quickValue)}$unit",
                        selected = numericValue == quickValue,
                        onClick = { onValueChange(formatDecimal(quickValue)) },
                    )
                }
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("精确输入") },
            suffix = { Text(unit) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaceQuickField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val quickPaces = listOf("5'00\"/km", "5'30\"/km", "6'00\"/km", "6'30\"/km")
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("配速", color = EvolveMuted, style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            quickPaces.forEach { pace ->
                QuickChip(
                    label = pace,
                    selected = value == pace,
                    onClick = { onValueChange(pace) },
                )
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("手动输入") },
            placeholder = { Text("530 或 5'30\"/km") },
            supportingText = { Text("支持 530、6:00、5'30\"/km") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun SaveFeedbackMessage(
    message: String?,
    modifier: Modifier = Modifier,
) {
    if (message == null) return
    val isSuccess = message.contains("已保存") || message.contains("完成") || message.contains("已删除")
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSuccess) EvolveNeon else EvolveSurfaceHigh,
        ),
        border = BorderStroke(1.dp, if (isSuccess) EvolveNeon else MaterialTheme.colorScheme.error),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isSuccess) EvolveBackground else MaterialTheme.colorScheme.error,
            )
            Text(
                message,
                color = if (isSuccess) EvolveBackground else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun QuickChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = EvolveSurfaceHigh,
            labelColor = EvolveMuted,
            selectedContainerColor = EvolveNeon,
            selectedLabelColor = EvolveBackground,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = EvolveNeon,
        ),
    )
}

private fun formatDecimal(value: Double): String =
    String.format(Locale.US, "%.2f", value)
        .trimEnd('0')
        .trimEnd('.')
