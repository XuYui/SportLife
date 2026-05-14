package com.sportlife.records.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportlife.records.ui.theme.EvolveBackground
import com.sportlife.records.ui.theme.EvolveNeon
import com.sportlife.records.ui.theme.EvolveSurfaceLow

@Composable
fun EvolveBottomBar(
    selected: String,
    onHome: () -> Unit,
    onTrain: () -> Unit,
    onWorkout: () -> Unit,
    onHistory: () -> Unit,
    onStats: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(EvolveSurfaceLow, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EvolveNavItem("首页", Icons.Default.GridView, selected == "home", onHome)
        EvolveNavItem("计划", Icons.Default.CalendarMonth, selected == "train", onTrain)
        EvolveNavItem("打卡", Icons.Default.AddBox, selected == "workout", onWorkout)
        EvolveNavItem("历史", Icons.Default.History, selected == "history", onHistory)
        EvolveNavItem("统计", Icons.Default.QueryStats, selected == "stats", onStats)
    }
}

@Composable
private fun EvolveNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val container = if (selected) EvolveNeon else Color.Transparent
    val content = if (selected) EvolveBackground else MaterialTheme.colorScheme.onSurfaceVariant

    androidx.compose.material3.Surface(
        onClick = onClick,
        color = container,
        shape = RoundedCornerShape(999.dp),
        modifier = Modifier.clip(RoundedCornerShape(999.dp)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = if (selected) 16.dp else 10.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(22.dp))
            Text(
                text = label,
                color = content,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun EvolveLogoHeader(
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(EvolveBackground, RoundedCornerShape(999.dp))
                .border(2.dp, EvolveNeon, RoundedCornerShape(999.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("SL", color = EvolveNeon, style = MaterialTheme.typography.labelLarge)
        }
        Text(
            text = "SportLife",
            color = EvolveNeon,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
        )
        Box(modifier = Modifier.size(42.dp), contentAlignment = Alignment.Center) {
            trailing()
        }
    }
}
