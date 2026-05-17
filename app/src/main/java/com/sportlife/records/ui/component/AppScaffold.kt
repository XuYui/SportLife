package com.sportlife.records.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportlife.records.ui.theme.EvolveBackground
import com.sportlife.records.ui.theme.EvolveNeon
import com.sportlife.records.ui.theme.EvolveSurfaceLow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
    showTopBar: Boolean = true,
    content: @Composable (PaddingValues) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EvolveBackground),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0.dp),
            topBar = {
                if (showTopBar) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = "SportLife",
                                color = EvolveNeon,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        },
                        navigationIcon = {
                            if (onBack != null) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .size(42.dp)
                                        .background(EvolveSurfaceLow, RoundedCornerShape(999.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(999.dp)),
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "返回",
                                        tint = EvolveNeon,
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .size(42.dp)
                                        .background(EvolveSurfaceLow, RoundedCornerShape(999.dp))
                                        .border(2.dp, EvolveNeon, RoundedCornerShape(999.dp)),
                                )
                            }
                        },
                        actions = {
                            if (actions == null) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = EvolveNeon,
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .size(26.dp),
                                )
                            } else {
                                actions()
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = EvolveBackground,
                            scrolledContainerColor = EvolveBackground,
                            navigationIconContentColor = EvolveNeon,
                            actionIconContentColor = EvolveNeon,
                        ),
                    )
                }
            },
            content = content,
        )
    }
}
