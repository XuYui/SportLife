package com.sportlife.records.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportlife.records.ui.component.AppScaffold
import com.sportlife.records.ui.component.EvolveLogoHeader
import com.sportlife.records.ui.component.WorkoutRecordCard
import com.sportlife.records.ui.theme.EvolveBackground
import com.sportlife.records.ui.theme.EvolveMuted
import com.sportlife.records.ui.theme.EvolveNeon
import com.sportlife.records.ui.theme.EvolveSurfaceHigh
import com.sportlife.records.ui.theme.EvolveSurfaceLow
import java.time.LocalDate

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onRunningClick: () -> Unit,
    onStrengthClick: () -> Unit,
    onPlanClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onStatsClick: () -> Unit,
    onDataMigrationClick: () -> Unit,
    onEditSloganClick: () -> Unit,
    onSloganDraftChange: (String) -> Unit,
    onSaveSlogan: () -> Unit,
    onCancelSlogan: () -> Unit,
) {
    val today = remember { LocalDate.now() }

    AppScaffold(title = "SportLife", showTopBar = false) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            item {
                EvolveLogoHeader(
                    modifier = Modifier.padding(horizontal = 0.dp),
                    trailing = {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = EvolveNeon, modifier = Modifier.size(28.dp))
                    },
                )
            }
            item {
                DateHero(
                    today = today,
                    slogan = uiState.slogan,
                    onEditSloganClick = onEditSloganClick,
                )
            }
            item {
                TodayCheckInCard(
                    checked = uiState.checkedInToday,
                    count = uiState.todayCount,
                    onRunningClick = onRunningClick,
                    onStrengthClick = onStrengthClick,
                )
            }
            item {
                WeekStrip(today = today, checkedToday = uiState.checkedInToday)
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    MetricTile(
                        title = "今日记录",
                        value = "${uiState.todayCount}",
                        unit = "次",
                        highlighted = uiState.checkedInToday,
                        modifier = Modifier.weight(1f),
                    )
                    MetricTile(
                        title = "最近记录",
                        value = "${uiState.recentCheckIns.size}",
                        unit = "条",
                        highlighted = false,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            item {
                Text("功能入口", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    FeatureCard("健身计划", "训练日、小板块、动作", Icons.Default.CalendarMonth, onPlanClick, Modifier.weight(1f))
                    FeatureCard("历史记录", "日历查看与修改", Icons.Default.History, onHistoryClick, Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    FeatureCard("统计图表", "跑步与健身趋势", Icons.Default.Analytics, onStatsClick, Modifier.weight(1f))
                    FeatureCard("数据迁移", "备份、恢复、更新保护", Icons.Default.Sync, onDataMigrationClick, Modifier.weight(1f))
                }
            }
            item {
                Text("最近记录", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            }
            if (uiState.recentCheckIns.isEmpty()) {
                item {
                    EmptyRecentCard()
                }
            } else {
                items(uiState.recentCheckIns, key = { it.checkIn.id }) { record ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(260)) + expandVertically(tween(260)),
                    ) {
                        WorkoutRecordCard(record = record)
                    }
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (uiState.isEditingSlogan) {
        EditSloganDialog(
            draft = uiState.sloganDraft,
            onDraftChange = onSloganDraftChange,
            onSave = onSaveSlogan,
            onCancel = onCancelSlogan,
        )
    }
}

@Composable
private fun DateHero(
    today: LocalDate,
    slogan: String,
    onEditSloganClick: () -> Unit,
) {
    val weekLabel = listOf("一", "二", "三", "四", "五", "六", "日")[today.dayOfWeek.value - 1]

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "今天 · 周$weekLabel",
                color = EvolveMuted,
                style = MaterialTheme.typography.labelLarge,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = today.dayOfMonth.toString(),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 104.sp,
                    lineHeight = 104.sp,
                )
                Column(modifier = Modifier.padding(start = 14.dp, bottom = 16.dp)) {
                    Text(
                        text = "${today.monthValue}月",
                        color = EvolveNeon,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = today.year.toString(),
                        color = EvolveMuted,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = slogan,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                )
                IconButton(onClick = onEditSloganClick) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑标语", tint = EvolveNeon)
                }
            }
        }
        CheckRing()
    }
}

@Composable
private fun EditSloganDialog(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("自定义首页标语") },
        text = {
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                label = { Text("标语") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = EvolveNeon, contentColor = EvolveBackground),
            ) {
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

@Composable
private fun CheckRing() {
    Box(modifier = Modifier.size(86.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            drawArc(EvolveSurfaceHigh, -90f, 360f, false, style = stroke)
            drawArc(EvolveNeon, -90f, 260f, false, style = stroke)
        }
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EvolveNeon)
    }
}

@Composable
private fun TodayCheckInCard(
    checked: Boolean,
    count: Int,
    onRunningClick: () -> Unit,
    onStrengthClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = EvolveSurfaceLow),
        border = BorderStroke(1.dp, if (checked) EvolveNeon else MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("今日打卡", color = EvolveMuted, style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = if (checked) "已完成 $count 次记录" else "还没有打卡",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = "记录跑步公里数、配速，或选择四分化健身训练。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = EvolveNeon,
                    modifier = Modifier.size(38.dp),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SportActionButton(
                    text = "跑步打卡",
                    icon = Icons.Default.DirectionsRun,
                    onClick = onRunningClick,
                    primary = true,
                    modifier = Modifier.weight(1f),
                )
                SportActionButton(
                    text = "健身打卡",
                    icon = Icons.Default.FitnessCenter,
                    onClick = onStrengthClick,
                    primary = false,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SportActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    primary: Boolean,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 520f),
        label = "sport-action-scale",
    )

    if (primary) {
        Button(
            onClick = onClick,
            interactionSource = interactionSource,
            modifier = modifier
                .height(54.dp)
                .scale(scale),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EvolveNeon, contentColor = EvolveBackground),
        ) {
            Icon(icon, contentDescription = null)
            Text(text, modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.ExtraBold)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            interactionSource = interactionSource,
            modifier = modifier
                .height(54.dp)
                .scale(scale),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        ) {
            Icon(icon, contentDescription = null)
            Text(text, modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun WeekStrip(
    today: LocalDate,
    checkedToday: Boolean,
) {
    val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
    val days = (0..6).map { monday.plusDays(it.toLong()) }
    val labels = listOf("一", "二", "三", "四", "五", "六", "日")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("本周", color = EvolveMuted, style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            days.forEachIndexed { index, date ->
                val selected = date == today
                val active = selected && checkedToday
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .background(
                            when {
                                active -> EvolveNeon
                                selected -> EvolveSurfaceHigh
                                else -> EvolveSurfaceLow
                            },
                            RoundedCornerShape(8.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = labels[index],
                        color = if (active) EvolveBackground else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricTile(
    title: String,
    value: String,
    unit: String,
    highlighted: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = EvolveSurfaceLow),
        border = if (highlighted) BorderStroke(1.dp, EvolveNeon) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, color = EvolveMuted, style = MaterialTheme.typography.labelLarge)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = if (highlighted) EvolveNeon else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineLarge)
                Text(unit, modifier = Modifier.padding(start = 5.dp, bottom = 5.dp), color = EvolveMuted)
            }
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(132.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = EvolveSurfaceLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(icon, contentDescription = null, tint = EvolveNeon)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(subtitle, color = EvolveMuted, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun EmptyRecentCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = EvolveSurfaceLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Text(
            text = "还没有打卡记录，先完成一次跑步或健身打卡。",
            color = EvolveMuted,
            modifier = Modifier.padding(20.dp),
        )
    }
}
