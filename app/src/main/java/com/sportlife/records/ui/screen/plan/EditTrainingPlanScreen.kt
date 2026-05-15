package com.sportlife.records.ui.screen.plan

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.sportlife.records.data.local.entity.TrainingDayEntity
import com.sportlife.records.data.local.entity.TrainingDaySectionEntity
import com.sportlife.records.data.local.entity.TrainingPlanExerciseEntity
import com.sportlife.records.data.local.relation.TrainingDaySectionWithExercises
import com.sportlife.records.data.local.relation.TrainingDayWithExercises
import com.sportlife.records.domain.model.BodyPart
import com.sportlife.records.domain.model.defaultBodyPartValues
import com.sportlife.records.domain.model.displayBodyPartName
import com.sportlife.records.ui.component.AppScaffold
import com.sportlife.records.ui.theme.EvolveBackground
import com.sportlife.records.ui.theme.EvolveMuted
import com.sportlife.records.ui.theme.EvolveNeon
import com.sportlife.records.ui.theme.EvolveSurfaceHigh
import com.sportlife.records.ui.theme.EvolveSurfaceLow

@Composable
fun EditTrainingPlanScreen(
    uiState: EditTrainingPlanUiState,
    onStartAddDay: (Long, Int) -> Unit,
    onStartEditDay: (TrainingDayEntity) -> Unit,
    onDeleteDay: (TrainingDayEntity) -> Unit,
    onDayNameChange: (String) -> Unit,
    onDayFocusChange: (String) -> Unit,
    onSaveDay: () -> Unit,
    onClearDayForm: () -> Unit,
    onStartAdd: (Long, Long?, String) -> Unit,
    onStartEdit: (TrainingPlanExerciseEntity) -> Unit,
    onDeleteExercise: (TrainingPlanExerciseEntity) -> Unit,
    onStartAddSection: (Long, String) -> Unit,
    onDeleteSection: (TrainingDaySectionEntity) -> Unit,
    onSectionNameChange: (String) -> Unit,
    onSectionBodyPartChange: (String) -> Unit,
    onSaveSection: () -> Unit,
    onClearSectionForm: () -> Unit,
    onNameChange: (String) -> Unit,
    onBodyPartChange: (String) -> Unit,
    onSectionChange: (Long?) -> Unit,
    onSetsChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSaveForm: () -> Unit,
    onClearForm: () -> Unit,
    onBack: () -> Unit,
) {
    val plan = uiState.plan
    AppScaffold(title = "编辑训练计划", onBack = onBack) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (plan == null) {
                item {
                    Text("暂无可编辑计划", color = EvolveMuted)
                }
            } else {
                item {
                    EditPlanHeader(
                        dayCount = plan.days.size,
                        onAddDay = { onStartAddDay(plan.plan.id, plan.days.size) },
                    )
                }
                items(plan.days.sortedBy { it.day.dayIndex }, key = { it.day.id }) { day ->
                    EditableTrainingDayCard(
                        day = day,
                        canDelete = plan.days.size > 1,
                        onStartEditDay = onStartEditDay,
                        onDeleteDay = onDeleteDay,
                        onStartAdd = onStartAdd,
                        onStartAddSection = onStartAddSection,
                        onStartEdit = onStartEdit,
                        onDeleteExercise = onDeleteExercise,
                        onDeleteSection = onDeleteSection,
                    )
                }
            }
        }
    }

    if (uiState.dayForm.planId != null) {
        TrainingDayFormDialog(
            form = uiState.dayForm,
            onNameChange = onDayNameChange,
            onFocusChange = onDayFocusChange,
            onSave = onSaveDay,
            onCancel = onClearDayForm,
        )
    }

    if (uiState.sectionForm.trainingDayId != null) {
        SectionFormDialog(
            form = uiState.sectionForm,
            onNameChange = onSectionNameChange,
            onBodyPartChange = onSectionBodyPartChange,
            onSave = onSaveSection,
            onCancel = onClearSectionForm,
        )
    }

    if (uiState.form.trainingDayId != null) {
        val daySections = plan?.days
            ?.firstOrNull { it.day.id == uiState.form.trainingDayId }
            ?.sections
            .orEmpty()
        PlanExerciseFormDialog(
            form = uiState.form,
            sections = daySections.map { it.section },
            onNameChange = onNameChange,
            onBodyPartChange = onBodyPartChange,
            onSectionChange = onSectionChange,
            onSetsChange = onSetsChange,
            onWeightChange = onWeightChange,
            onRepsChange = onRepsChange,
            onNoteChange = onNoteChange,
            onSave = onSaveForm,
            onCancel = onClearForm,
        )
    }
}

@Composable
private fun EditPlanHeader(
    dayCount: Int,
    onAddDay: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = EvolveSurfaceLow),
        border = BorderStroke(1.dp, EvolveNeon),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("自定义训练结构", color = EvolveMuted, style = MaterialTheme.typography.labelLarge)
                Text("$dayCount 个训练日", fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onAddDay) {
                Icon(Icons.Default.Add, contentDescription = null, tint = EvolveNeon)
                Text("训练日", color = EvolveNeon)
            }
        }
    }
}

@Composable
private fun EditableTrainingDayCard(
    day: TrainingDayWithExercises,
    canDelete: Boolean,
    onStartEditDay: (TrainingDayEntity) -> Unit,
    onDeleteDay: (TrainingDayEntity) -> Unit,
    onStartAdd: (Long, Long?, String) -> Unit,
    onStartAddSection: (Long, String) -> Unit,
    onStartEdit: (TrainingPlanExerciseEntity) -> Unit,
    onDeleteExercise: (TrainingPlanExerciseEntity) -> Unit,
    onDeleteSection: (TrainingDaySectionEntity) -> Unit,
) {
    val fallbackBodyPart = day.day.focusBodyPart.orEmpty().ifBlank { BodyPart.Back.name }
    val unsectioned = day.exercises.filter { it.sectionId == null }
    Card(
        modifier = Modifier.animateContentSize(),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(day.day.name, fontWeight = FontWeight.SemiBold)
                    Text(displayBodyPartName(day.day.focusBodyPart), color = EvolveMuted, style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onStartEditDay(day.day) }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑训练日", tint = EvolveNeon)
                    }
                    if (canDelete) {
                        IconButton(onClick = { onDeleteDay(day.day) }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除训练日")
                        }
                    }
                }
            }
            TextButton(onClick = { onStartAddSection(day.day.id, fallbackBodyPart) }) {
                Icon(Icons.Default.Add, contentDescription = null, tint = EvolveNeon)
                Text("新增小板块", color = EvolveNeon)
            }
            day.sections.sortedBy { it.section.sortOrder }.forEach { section ->
                EditableSectionCard(
                    dayId = day.day.id,
                    section = section,
                    onStartAdd = onStartAdd,
                    onStartEdit = onStartEdit,
                    onDeleteExercise = onDeleteExercise,
                    onDeleteSection = onDeleteSection,
                )
            }
            if (unsectioned.isNotEmpty()) {
                EditableSectionCard(
                    dayId = day.day.id,
                    sectionTitle = "未分组",
                    sectionId = null,
                    fallbackBodyPart = fallbackBodyPart,
                    exercises = unsectioned.sortedBy { it.sortOrder },
                    onStartAdd = onStartAdd,
                    onStartEdit = onStartEdit,
                    onDeleteExercise = onDeleteExercise,
                )
            }
        }
    }
}

@Composable
private fun EditableSectionCard(
    dayId: Long,
    section: TrainingDaySectionWithExercises,
    onStartAdd: (Long, Long?, String) -> Unit,
    onStartEdit: (TrainingPlanExerciseEntity) -> Unit,
    onDeleteExercise: (TrainingPlanExerciseEntity) -> Unit,
    onDeleteSection: (TrainingDaySectionEntity) -> Unit,
) {
    EditableSectionCard(
        dayId = dayId,
        sectionTitle = section.section.name,
        sectionId = section.section.id,
        fallbackBodyPart = section.section.bodyPart.orEmpty().ifBlank { BodyPart.Back.name },
        exercises = section.exercises.sortedBy { it.sortOrder },
        onStartAdd = onStartAdd,
        onStartEdit = onStartEdit,
        onDeleteExercise = onDeleteExercise,
        onDeleteSection = { onDeleteSection(section.section) },
    )
}

@Composable
private fun EditableSectionCard(
    dayId: Long,
    sectionTitle: String,
    sectionId: Long?,
    fallbackBodyPart: String,
    exercises: List<TrainingPlanExerciseEntity>,
    onStartAdd: (Long, Long?, String) -> Unit,
    onStartEdit: (TrainingPlanExerciseEntity) -> Unit,
    onDeleteExercise: (TrainingPlanExerciseEntity) -> Unit,
    onDeleteSection: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(EvolveSurfaceHigh, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(sectionTitle, color = EvolveNeon, fontWeight = FontWeight.Bold)
                Text(displayBodyPartName(fallbackBodyPart), color = EvolveMuted, style = MaterialTheme.typography.bodyMedium)
            }
            Row {
                TextButton(onClick = { onStartAdd(dayId, sectionId, fallbackBodyPart) }) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = EvolveNeon)
                    Text("动作", color = EvolveNeon)
                }
                if (onDeleteSection != null) {
                    IconButton(onClick = onDeleteSection) {
                        Icon(Icons.Default.Delete, contentDescription = "删除小板块")
                    }
                }
            }
        }
        if (exercises.isEmpty()) {
            Text("还没有动作", color = EvolveMuted)
        } else {
            exercises.forEach { exercise ->
                EditableExerciseLine(
                    exercise = exercise,
                    onEdit = { onStartEdit(exercise) },
                    onDelete = { onDeleteExercise(exercise) },
                )
            }
        }
    }
}

@Composable
private fun EditableExerciseLine(
    exercise: TrainingPlanExerciseEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(exercise.exerciseName, fontWeight = FontWeight.Medium)
            Text(
                "${displayBodyPartName(exercise.bodyPart)} · ${exercise.sets} 组 x ${exercise.defaultReps} 次",
                color = EvolveMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "编辑动作", tint = EvolveNeon)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "删除动作")
        }
    }
}

@Composable
private fun TrainingDayFormDialog(
    form: TrainingDayFormState,
    onNameChange: (String) -> Unit,
    onFocusChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    EditDialogFrame(
        title = if (form.editingDay == null) "新增训练日" else "编辑训练日",
        onSave = onSave,
        onCancel = onCancel,
    ) {
        OutlinedTextField(
            value = form.name,
            onValueChange = onNameChange,
            label = { Text("训练日名称，例如：第一天：背") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        FocusInput(
            value = form.focusBodyPart,
            onValueChange = onFocusChange,
            label = "主训练部位，可自定义",
            options = defaultBodyPartValues(),
        )
        if (form.message != null) {
            Text(form.message, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun SectionFormDialog(
    form: SectionFormState,
    onNameChange: (String) -> Unit,
    onBodyPartChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    EditDialogFrame(
        title = "新增小板块",
        onSave = onSave,
        onCancel = onCancel,
    ) {
        OutlinedTextField(
            value = form.name,
            onValueChange = onNameChange,
            label = { Text("小板块名称，例如：肩前束") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        FocusInput(
            value = form.bodyPart,
            onValueChange = onBodyPartChange,
            label = "所属部位，可自定义",
            options = defaultBodyPartValues(),
        )
        if (form.message != null) {
            Text(form.message, color = MaterialTheme.colorScheme.error)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlanExerciseFormDialog(
    form: PlanExerciseFormState,
    sections: List<TrainingDaySectionEntity>,
    onNameChange: (String) -> Unit,
    onBodyPartChange: (String) -> Unit,
    onSectionChange: (Long?) -> Unit,
    onSetsChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    val bodyPartOptions = (defaultBodyPartValues() + sections.mapNotNull { it.bodyPart }).distinct()
    EditDialogFrame(
        title = if (form.editingExercise == null) "新增计划动作" else "编辑计划动作",
        onSave = onSave,
        onCancel = onCancel,
    ) {
        OutlinedTextField(
            value = form.exerciseName,
            onValueChange = onNameChange,
            label = { Text("动作名称") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        FocusInput(
            value = form.bodyPart,
            onValueChange = onBodyPartChange,
            label = "动作部位，可自定义",
            options = bodyPartOptions,
        )
        Text("所属小板块", fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FocusChip(
                label = "未分组",
                selected = form.sectionId == null,
                onClick = { onSectionChange(null) },
            )
            sections.sortedBy { it.sortOrder }.forEach { section ->
                FocusChip(
                    label = section.name,
                    selected = form.sectionId == section.id,
                    onClick = { onSectionChange(section.id) },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = form.sets,
                onValueChange = onSetsChange,
                label = { Text("组数") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = form.weightKg,
                onValueChange = onWeightChange,
                label = { Text("重量") },
                suffix = { Text("kg") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = form.reps,
                onValueChange = onRepsChange,
                label = { Text("次数") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
        }
        OutlinedTextField(
            value = form.note,
            onValueChange = onNoteChange,
            label = { Text("备注") },
            modifier = Modifier.fillMaxWidth(),
        )
        if (form.message != null) {
            Text(form.message, color = MaterialTheme.colorScheme.error)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDialogFrame(
    title: String,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = EvolveSurfaceLow,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = "关闭")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = EvolveNeon, contentColor = EvolveBackground),
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Text("保存", modifier = Modifier.padding(start = 8.dp))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("取消", color = EvolveMuted)
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FocusInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    options: List<String>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.distinct().forEach { option ->
                FocusChip(
                    label = displayBodyPartName(option),
                    selected = value == option || value == displayBodyPartName(option),
                    onClick = { onValueChange(option) },
                )
            }
        }
    }
}

@Composable
private fun FocusChip(
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
