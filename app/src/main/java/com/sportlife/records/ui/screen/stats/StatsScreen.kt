package com.sportlife.records.ui.screen.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportlife.records.domain.util.formatPace
import com.sportlife.records.ui.component.AppScaffold
import com.sportlife.records.ui.theme.EvolveBackground
import com.sportlife.records.ui.theme.EvolveMuted
import com.sportlife.records.ui.theme.EvolveNeon
import com.sportlife.records.ui.theme.EvolveSurfaceHigh
import com.sportlife.records.ui.theme.EvolveSurfaceLow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@Composable
fun StatsScreen(
    uiState: StatsUiState,
    onBack: () -> Unit,
) {
    AppScaffold(title = "统计图表", onBack = onBack) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                StatsHero(uiState)
            }
            item {
                LineChartCard(
                    title = "跑步里程趋势",
                    subtitle = "每次跑步的公里数",
                    emptyText = "还没有跑步数据",
                    values = uiState.runningPoints.map { it.distanceKm },
                    valueLabel = { "%.1f km".format(it) },
                    lineColor = EvolveNeon,
                )
            }
            item {
                LineChartCard(
                    title = "跑步配速趋势",
                    subtitle = "数值越低代表速度越快",
                    emptyText = "还没有配速数据",
                    values = uiState.runningPoints.mapNotNull { it.paceSecondsPerKm?.toDouble() },
                    valueLabel = { formatPace(it.toInt()) },
                    lineColor = EvolveNeon,
                )
            }
            item {
                BarChartCard(
                    title = "周跑量",
                    subtitle = "最近 8 周",
                    values = uiState.weeklyRunning,
                    barColor = EvolveNeon,
                )
            }
            item {
                BarChartCard(
                    title = "月跑量",
                    subtitle = "最近 6 个月",
                    values = uiState.monthlyRunning,
                    barColor = EvolveNeon,
                )
            }
            item {
                RadarChartCard(
                    title = "健身训练频率",
                    subtitle = "不同部位打卡次数",
                    values = uiState.strengthFrequency,
                )
            }
        }
    }
}

@Composable
private fun StatsHero(uiState: StatsUiState) {
    val totalDistance = uiState.runningPoints.sumOf { it.distanceKm }
    val totalStrength = uiState.strengthFrequency.sumOf { it.value }.toInt()
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = EvolveSurfaceHigh),
        border = BorderStroke(1.dp, EvolveNeon),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "训练概览",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "跑步 %.1f km · 健身 %d 次".format(totalDistance, totalStrength),
                    color = EvolveMuted,
                )
            }
            Box(
                modifier = Modifier
                    .background(EvolveNeon, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Analytics, contentDescription = null, tint = EvolveBackground)
            }
        }
    }
}

@Composable
private fun LineChartCard(
    title: String,
    subtitle: String,
    emptyText: String,
    values: List<Double>,
    valueLabel: (Double) -> String,
    lineColor: Color,
) {
    ChartCard(title = title, subtitle = subtitle) {
        if (values.isEmpty()) {
            EmptyChartText(emptyText)
        } else {
            LineChart(
                values = values.takeLast(12),
                lineColor = lineColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.9f),
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("最低 ${valueLabel(values.min())}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                Text("最高 ${valueLabel(values.max())}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun BarChartCard(
    title: String,
    subtitle: String,
    values: List<BarChartValue>,
    barColor: Color,
) {
    ChartCard(title = title, subtitle = subtitle) {
        if (values.isEmpty()) {
            EmptyChartText("还没有跑步数据")
        } else {
            BarChart(
                values = values,
                barColor = barColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2.2f),
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                values.forEach { value ->
                    Text(value.label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun RadarChartCard(
    title: String,
    subtitle: String,
    values: List<RadarChartValue>,
) {
    ChartCard(title = title, subtitle = subtitle) {
        RadarChart(
            values = values,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.25f),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            values.forEach { value ->
                Text("${value.label} ${value.value.toInt()}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
            content()
        }
    }
}

@Composable
private fun EmptyChartText(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LineChart(
    values: List<Double>,
    lineColor: Color,
    modifier: Modifier = Modifier,
) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val pointColor = MaterialTheme.colorScheme.surface
    Canvas(modifier = modifier) {
        val padding = 18.dp.toPx()
        val left = padding
        val top = padding
        val right = size.width - padding
        val bottom = size.height - padding
        val chartWidth = right - left
        val chartHeight = bottom - top
        val minValue = values.min()
        val maxValue = values.max()
        val range = max(1.0, maxValue - minValue)

        repeat(4) { index ->
            val y = top + chartHeight * index / 3f
            drawLine(gridColor, Offset(left, y), Offset(right, y), strokeWidth = 1.dp.toPx())
        }

        val points = values.mapIndexed { index, value ->
            val x = if (values.size == 1) left + chartWidth / 2f else left + chartWidth * index / (values.lastIndex).toFloat()
            val normalized = ((value - minValue) / range).toFloat()
            val y = bottom - chartHeight * normalized
            Offset(x, y)
        }
        if (points.size > 1) {
            for (index in 0 until points.lastIndex) {
                drawLine(
                    color = lineColor,
                    start = points[index],
                    end = points[index + 1],
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
        points.forEach { point ->
            drawCircle(lineColor, radius = 5.dp.toPx(), center = point)
            drawCircle(pointColor, radius = 2.5.dp.toPx(), center = point)
        }
    }
}

@Composable
private fun BarChart(
    values: List<BarChartValue>,
    barColor: Color,
    modifier: Modifier = Modifier,
) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier = modifier) {
        val padding = 18.dp.toPx()
        val left = padding
        val top = padding
        val right = size.width - padding
        val bottom = size.height - padding
        val chartWidth = right - left
        val chartHeight = bottom - top
        val maxValue = max(1.0, values.maxOf { it.value })
        val slot = chartWidth / values.size
        val barWidth = slot * 0.56f

        repeat(4) { index ->
            val y = top + chartHeight * index / 3f
            drawLine(gridColor, Offset(left, y), Offset(right, y), strokeWidth = 1.dp.toPx())
        }
        values.forEachIndexed { index, value ->
            val height = (chartHeight * (value.value / maxValue)).toFloat()
            val x = left + index * slot + (slot - barWidth) / 2f
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, bottom - height),
                size = Size(barWidth, height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx()),
            )
        }
    }
}

@Composable
private fun RadarChart(
    values: List<RadarChartValue>,
    modifier: Modifier = Modifier,
) {
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    val fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
    val strokeColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = min(size.width, size.height) * 0.34f
        val maxValue = max(1.0, values.maxOf { it.value })
        val count = values.size

        repeat(4) { layer ->
            val layerRadius = radius * (layer + 1) / 4f
            val path = Path()
            repeat(count) { index ->
                val point = radarPoint(center, layerRadius, index, count)
                if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
            }
            path.close()
            drawPath(path, color = lineColor, style = Stroke(width = 1.dp.toPx()))
        }

        val valuePath = Path()
        values.forEachIndexed { index, value ->
            val valueRadius = radius * (value.value / maxValue).toFloat()
            val point = radarPoint(center, valueRadius, index, count)
            if (index == 0) valuePath.moveTo(point.x, point.y) else valuePath.lineTo(point.x, point.y)
            val axisEnd = radarPoint(center, radius, index, count)
            drawLine(lineColor, center, axisEnd, strokeWidth = 1.dp.toPx())
        }
        valuePath.close()
        drawPath(valuePath, color = fillColor)
        drawPath(valuePath, color = strokeColor, style = Stroke(width = 2.dp.toPx()))

        values.forEachIndexed { index, value ->
            val labelPoint = radarPoint(center, radius + 22.dp.toPx(), index, count)
            drawContext.canvas.nativeCanvas.drawText(
                value.label,
                labelPoint.x - 14.dp.toPx(),
                labelPoint.y + 4.dp.toPx(),
                android.graphics.Paint().apply {
                    color = labelColor.toArgb()
                    textSize = 12.dp.toPx()
                    isAntiAlias = true
                },
            )
        }
    }
}

private fun radarPoint(center: Offset, radius: Float, index: Int, count: Int): Offset {
    val angle = -PI / 2.0 + 2.0 * PI * index / count
    return Offset(
        x = center.x + radius * cos(angle).toFloat(),
        y = center.y + radius * sin(angle).toFloat(),
    )
}

private fun Color.toArgb(): Int =
    android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt(),
    )
