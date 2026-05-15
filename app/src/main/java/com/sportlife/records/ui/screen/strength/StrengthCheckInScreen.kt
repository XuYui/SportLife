package com.sportlife.records.ui.screen.strength

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportlife.records.domain.model.BodyPart
import com.sportlife.records.domain.model.displayBodyPartName
import com.sportlife.records.ui.component.AppScaffold
import com.sportlife.records.ui.theme.EvolveBackground
import com.sportlife.records.ui.theme.EvolveMuted
import com.sportlife.records.ui.theme.EvolveNeon
import com.sportlife.records.ui.theme.EvolveSurfaceHigh
import com.sportlife.records.ui.theme.EvolveSurfaceLow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StrengthCheckInScreen(
    uiState: StrengthCheckInUiState,
    onDateChange: (String) -> Unit,
    onSplitChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    AppScaffold(title = "健身打卡", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            StrengthHeader(selected = uiState.selectedSplit, splitLabel = uiState.activeSplitLabel)
            Text("选择本次训练", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                uiState.availableSplits.forEach { part ->
                    SplitOptionCard(
                        part = part,
                        selected = uiState.selectedSplit == part,
                        onClick = { onSplitChange(part) },
                        modifier = Modifier.fillMaxWidth(0.48f),
                    )
                }
            }
            CheckInFormCard(
                uiState = uiState,
                onDateChange = onDateChange,
                onNoteChange = onNoteChange,
                onSave = onSave,
            )
        }
    }
}

@Composable
private fun StrengthHeader(selected: String, splitLabel: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = EvolveSurfaceLow),
        border = BorderStroke(1.dp, EvolveNeon),
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
                Text("今日健身 · $splitLabel", color = EvolveMuted, style = MaterialTheme.typography.labelLarge)
                AnimatedContent(
                    targetState = selected,
                    transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                    label = "selected-split",
                ) { part ->
                    Text(
                        text = "${displayBodyPartName(part)}训练",
                        color = EvolveNeon,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                Text("选项会跟随当前健身计划，包括自定义分化。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .background(EvolveNeon, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = EvolveBackground, modifier = Modifier.size(34.dp))
            }
        }
    }
}

@Composable
private fun SplitOptionCard(
    part: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 520f),
        label = "split-card-scale",
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) EvolveNeon else EvolveSurfaceLow,
        animationSpec = tween(220),
        label = "split-card-color",
    )
    val contentColor = if (selected) EvolveBackground else MaterialTheme.colorScheme.onSurface

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .height(142.dp)
            .scale(scale),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (selected) EvolveNeon else MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = contentColor)
                if (selected) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = contentColor)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(displayBodyPartName(part), color = contentColor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Text(
                    splitHint(part),
                    color = if (selected) EvolveBackground.copy(alpha = 0.72f) else EvolveMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun CheckInFormCard(
    uiState: StrengthCheckInUiState,
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
            Text("填写打卡信息", color = EvolveMuted, style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = uiState.date,
                onValueChange = onDateChange,
                label = { Text("日期") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.note,
                onValueChange = onNoteChange,
                label = { Text("备注") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
            AnimatedContent(
                targetState = uiState.message,
                transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(120)) },
                label = "strength-message",
            ) { message ->
                if (message != null) {
                    Text(message, color = EvolveNeon, fontWeight = FontWeight.Bold)
                }
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
                Text("保存 ${displayBodyPartName(uiState.selectedSplit)} 训练", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

private fun splitHint(part: String): String =
    when (BodyPart.entries.firstOrNull { it.name == part || it.label == part }) {
        BodyPart.Back -> "引体、划船、高位下拉"
        BodyPart.Chest -> "卧推、飞鸟、推举"
        BodyPart.Legs -> "深蹲、腿举、硬拉"
        BodyPart.Arms -> "二头、三头、肩臂"
        null -> "跟随当前计划的自定义分化"
    }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BodyPartChips(
    selected: BodyPart,
    onSelected: (BodyPart) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BodyPart.entries.forEach { part ->
            FilterChip(
                selected = selected == part,
                onClick = { onSelected(part) },
                label = { Text(part.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EvolveNeon,
                    selectedLabelColor = EvolveBackground,
                    containerColor = EvolveSurfaceHigh,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
    }
}
