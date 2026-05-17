package com.sportlife.records.ui.screen.plan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportlife.records.data.local.entity.TrainingPlanExerciseEntity
import com.sportlife.records.data.local.relation.TrainingDaySectionWithExercises
import com.sportlife.records.data.local.relation.TrainingDayWithExercises
import com.sportlife.records.domain.model.BodyPart
import com.sportlife.records.domain.model.TrainingSplitType
import com.sportlife.records.ui.component.AppScaffold
import com.sportlife.records.ui.theme.EvolveBackground
import com.sportlife.records.ui.theme.EvolveMuted
import com.sportlife.records.ui.theme.EvolveNeon
import com.sportlife.records.ui.theme.EvolveSurfaceHigh
import com.sportlife.records.ui.theme.EvolveSurfaceLow
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TrainingPlanScreen(
    uiState: TrainingPlanUiState,
    onSplitSelected: (TrainingSplitType) -> Unit,
    onEditClick: () -> Unit,
    onBack: () -> Unit,
) {
    val plan = uiState.plan
    val days = plan?.days?.sortedBy { it.day.dayIndex }.orEmpty()
    val pagerState = rememberPagerState(pageCount = { days.size })
    LaunchedEffect(days.size) {
        if (days.isNotEmpty() && pagerState.currentPage >= days.size) {
            pagerState.scrollToPage(days.lastIndex)
        }
    }
    AppScaffold(
        title = "健身计划",
        onBack = onBack,
        actions = {
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "编辑计划")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TrainingSplitType.entries.forEach { split ->
                    FilterChip(
                        selected = plan?.plan?.splitId == split.id,
                        onClick = { onSplitSelected(split) },
                        label = { Text(split.label) },
                    )
                }
            }
            if (plan == null) {
                Text("暂无训练计划", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                PlanHeaderCard(
                    planName = plan.plan.name,
                    splitName = TrainingSplitType.entries
                        .firstOrNull { it.id == plan.plan.splitId }
                        ?.label
                        .orEmpty(),
                )
                if (days.isEmpty()) {
                    Text("暂无训练日", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    TrainingDayPagerHeader(
                        currentDayName = days[pagerState.currentPage].day.name,
                        currentPage = pagerState.currentPage,
                        pageCount = days.size,
                    )
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        pageSpacing = 12.dp,
                    ) { page ->
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 24.dp),
                        ) {
                            item(key = days[page].day.id) {
                                TrainingDayCard(day = days[page])
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrainingDayPagerHeader(
    currentDayName: String,
    currentPage: Int,
    pageCount: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "训练日",
                color = EvolveMuted,
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = currentDayName,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(pageCount) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == currentPage) 18.dp else 8.dp, 8.dp)
                        .background(
                            color = if (index == currentPage) EvolveNeon else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(999.dp),
                        ),
                )
            }
        }
    }
}

@Composable
private fun PlanHeaderCard(
    planName: String,
    splitName: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = EvolveSurfaceLow),
        border = BorderStroke(1.dp, EvolveNeon),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "当前健身计划",
                        color = EvolveMuted,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = planName,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = if (splitName.isNotBlank()) splitName else "当前训练计划",
                        color = EvolveMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Text(
                    text = "计划",
                    color = EvolveBackground,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .background(EvolveNeon, RoundedCornerShape(999.dp))
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                )
            }
        }
    }
}

@Composable
private fun TrainingDayCard(day: TrainingDayWithExercises) {
    val unsectioned = day.exercises.filter { it.sectionId == null }
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(day.day.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            val sections = day.sections.sortedBy { it.section.sortOrder }
            if (sections.isEmpty() && unsectioned.isEmpty()) {
                Text("还没有配置动作", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            sections.forEach { section ->
                SectionCard(section)
            }
            if (unsectioned.isNotEmpty()) {
                SectionCard(
                    title = "未分组",
                    bodyPart = null,
                    exercises = unsectioned.sortedBy { it.sortOrder },
                )
            }
        }
    }
}

@Composable
private fun SectionCard(section: TrainingDaySectionWithExercises) {
    SectionCard(
        title = section.section.name,
        bodyPart = section.section.bodyPart?.let { BodyPart.fromName(it) },
        exercises = section.exercises.sortedBy { it.sortOrder },
    )
}

@Composable
private fun SectionCard(
    title: String,
    bodyPart: BodyPart?,
    exercises: List<TrainingPlanExerciseEntity>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(EvolveSurfaceHigh, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            if (bodyPart != null) {
                Text(bodyPart.label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (exercises.isEmpty()) {
            Text("这个小板块还没有动作", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        } else {
            exercises.forEach { exercise ->
                PlanExerciseLine(exercise)
            }
        }
    }
}

@Composable
private fun PlanExerciseLine(exercise: TrainingPlanExerciseEntity) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(exercise.exerciseName, fontWeight = FontWeight.Medium)
            Text("${exercise.sets} 组 x ${exercise.defaultReps} 次")
        }
        Text(
            if (exercise.defaultWeightKg > 0.0) {
                "重量 ${formatWeightKg(exercise.defaultWeightKg)} kg"
            } else {
                "自重"
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        if (exercise.note.isNotBlank()) {
            Text(exercise.note, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun formatWeightKg(weightKg: Double): String =
    String.format(Locale.US, "%.2f", weightKg)
        .trimEnd('0')
        .trimEnd('.')
