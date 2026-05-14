package com.sportlife.records.ui.screen.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import com.sportlife.records.data.local.entity.TrainingDaySectionEntity
import com.sportlife.records.data.local.entity.TrainingPlanExerciseEntity
import com.sportlife.records.data.local.relation.TrainingDaySectionWithExercises
import com.sportlife.records.data.local.relation.TrainingDayWithExercises
import com.sportlife.records.domain.model.BodyPart
import com.sportlife.records.ui.component.AppScaffold
import com.sportlife.records.ui.screen.strength.BodyPartChips

@Composable
fun EditTrainingPlanScreen(
    uiState: EditTrainingPlanUiState,
    onStartAdd: (Long, Long?, BodyPart) -> Unit,
    onStartEdit: (TrainingPlanExerciseEntity) -> Unit,
    onDeleteExercise: (TrainingPlanExerciseEntity) -> Unit,
    onStartAddSection: (Long, BodyPart) -> Unit,
    onDeleteSection: (TrainingDaySectionEntity) -> Unit,
    onSectionNameChange: (String) -> Unit,
    onSectionBodyPartChange: (BodyPart) -> Unit,
    onSaveSection: () -> Unit,
    onClearSectionForm: () -> Unit,
    onNameChange: (String) -> Unit,
    onBodyPartChange: (BodyPart) -> Unit,
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
            if (uiState.sectionForm.trainingDayId != null) {
                item {
                    SectionForm(
                        form = uiState.sectionForm,
                        onNameChange = onSectionNameChange,
                        onBodyPartChange = onSectionBodyPartChange,
                        onSave = onSaveSection,
                        onCancel = onClearSectionForm,
                    )
                }
            }
            if (uiState.form.trainingDayId != null) {
                item {
                    val daySections = plan?.days
                        ?.firstOrNull { it.day.id == uiState.form.trainingDayId }
                        ?.sections
                        .orEmpty()
                    PlanExerciseForm(
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
            if (plan == null) {
                item {
                    Text("暂无可编辑计划", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(plan.days.sortedBy { it.day.dayIndex }, key = { it.day.id }) { day ->
                    EditableTrainingDayCard(
                        day = day,
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
}

@Composable
private fun EditableTrainingDayCard(
    day: TrainingDayWithExercises,
    onStartAdd: (Long, Long?, BodyPart) -> Unit,
    onStartAddSection: (Long, BodyPart) -> Unit,
    onStartEdit: (TrainingPlanExerciseEntity) -> Unit,
    onDeleteExercise: (TrainingPlanExerciseEntity) -> Unit,
    onDeleteSection: (TrainingDaySectionEntity) -> Unit,
) {
    val fallbackBodyPart = day.day.focusBodyPart?.let { BodyPart.fromName(it) } ?: BodyPart.Back
    val unsectioned = day.exercises.filter { it.sectionId == null }
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                Text(day.day.name, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = { onStartAddSection(day.day.id, fallbackBodyPart) }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("小板块")
                }
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
    onStartAdd: (Long, Long?, BodyPart) -> Unit,
    onStartEdit: (TrainingPlanExerciseEntity) -> Unit,
    onDeleteExercise: (TrainingPlanExerciseEntity) -> Unit,
    onDeleteSection: (TrainingDaySectionEntity) -> Unit,
) {
    EditableSectionCard(
        dayId = dayId,
        sectionTitle = section.section.name,
        sectionId = section.section.id,
        fallbackBodyPart = section.section.bodyPart?.let { BodyPart.fromName(it) } ?: BodyPart.Back,
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
    fallbackBodyPart: BodyPart,
    exercises: List<TrainingPlanExerciseEntity>,
    onStartAdd: (Long, Long?, BodyPart) -> Unit,
    onStartEdit: (TrainingPlanExerciseEntity) -> Unit,
    onDeleteExercise: (TrainingPlanExerciseEntity) -> Unit,
    onDeleteSection: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(sectionTitle, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(fallbackBodyPart.label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
            Row {
                TextButton(onClick = { onStartAdd(dayId, sectionId, fallbackBodyPart) }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("动作")
                }
                if (onDeleteSection != null) {
                    IconButton(onClick = onDeleteSection) {
                        Icon(Icons.Default.Delete, contentDescription = "删除小板块")
                    }
                }
            }
        }
        if (exercises.isEmpty()) {
            Text("还没有动作", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                "${BodyPart.fromName(exercise.bodyPart).label} · ${exercise.sets} 组 x ${exercise.defaultReps} 次",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "编辑动作")
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "删除动作")
        }
    }
}

@Composable
private fun SectionForm(
    form: SectionFormState,
    onNameChange: (String) -> Unit,
    onBodyPartChange: (BodyPart) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
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
                Text("新增小板块", fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = "关闭表单")
                }
            }
            OutlinedTextField(
                value = form.name,
                onValueChange = onNameChange,
                label = { Text("小板块名称，例如：肩前束") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            BodyPartChips(selected = form.bodyPart, onSelected = onBodyPartChange)
            if (form.message != null) {
                Text(form.message, color = MaterialTheme.colorScheme.error)
            }
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Save, contentDescription = null)
                Text("保存小板块", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PlanExerciseForm(
    form: PlanExerciseFormState,
    sections: List<TrainingDaySectionEntity>,
    onNameChange: (String) -> Unit,
    onBodyPartChange: (BodyPart) -> Unit,
    onSectionChange: (Long?) -> Unit,
    onSetsChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
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
                Text(
                    if (form.editingExercise == null) "新增计划动作" else "编辑计划动作",
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = "关闭表单")
                }
            }
            OutlinedTextField(
                value = form.exerciseName,
                onValueChange = onNameChange,
                label = { Text("动作名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            BodyPartChips(selected = form.bodyPart, onSelected = onBodyPartChange)
            Text("所属小板块", fontWeight = FontWeight.SemiBold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = form.sectionId == null,
                    onClick = { onSectionChange(null) },
                    label = { Text("未分组") },
                )
                sections.sortedBy { it.sortOrder }.forEach { section ->
                    FilterChip(
                        selected = form.sectionId == section.id,
                        onClick = { onSectionChange(section.id) },
                        label = { Text(section.name) },
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
                    label = { Text("默认重量") },
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
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Save, contentDescription = null)
                Text("保存动作", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
