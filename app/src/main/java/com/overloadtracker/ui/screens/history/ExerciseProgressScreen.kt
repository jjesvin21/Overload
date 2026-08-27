/**
 * Exercise progress chart showing max weight over time.
 * Refactored with Liquid Glass / Liquid Vitality visual aesthetic.
 */
package com.overloadtracker.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.overloadtracker.R
import com.overloadtracker.data.model.ExerciseProgressPoint
import com.overloadtracker.ui.components.LiquidGlassCard
import com.overloadtracker.ui.components.LiquidMetricCard
import com.overloadtracker.ui.components.LiquidTopAppBar
import com.overloadtracker.ui.theme.CyanAccent
import com.overloadtracker.ui.theme.ElectricViolet
import com.overloadtracker.ui.theme.MidnightBackground
import com.overloadtracker.ui.theme.TextOnSurfaceVariant
import com.overloadtracker.util.WeightUnit
import com.overloadtracker.util.WeightUtils
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

/**
 * Line chart of max weight per session for one exercise in Liquid Glass style.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseProgressScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExerciseProgressViewModel = hiltViewModel()
) {
    val progress by viewModel.progress.collectAsState()
    val exerciseName by viewModel.exerciseName.collectAsState()

    val maxWeightEver = progress.maxOfOrNull { it.maxWeight } ?: 0.0
    val lastWeight = progress.lastOrNull()?.maxWeight ?: 0.0

    Scaffold(
        modifier = modifier.background(MidnightBackground),
        containerColor = MidnightBackground,
        topBar = {
            LiquidTopAppBar(
                title = exerciseName.orEmpty().ifEmpty { "EXERCISE PROGRESS" },
                subtitle = "PROGRESSION ANALYTICS",
                onBackClick = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (progress.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No logged sets yet for this exercise.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextOnSurfaceVariant
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LiquidMetricCard(
                        label = "ALL-TIME PEAK",
                        value = WeightUtils.formatWeight(maxWeightEver, WeightUnit.KG),
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        accentColor = ElectricViolet,
                        modifier = Modifier.weight(1f)
                    )
                    LiquidMetricCard(
                        label = "LATEST BEST",
                        value = WeightUtils.formatWeight(lastWeight, WeightUnit.KG),
                        icon = Icons.AutoMirrored.Filled.ShowChart,
                        accentColor = CyanAccent,
                        modifier = Modifier.weight(1f)
                    )
                }

                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    padding = 16.dp
                ) {
                    Text(
                        text = "MAX WEIGHT OVER TIME",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextOnSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    ProgressChart(
                        points = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressChart(
    points: List<ExerciseProgressPoint>,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(points) {
        modelProducer.runTransaction {
            lineSeries { series(points.map { it.maxWeight.toFloat() }) }
        }
    }
    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis()
        ),
        modelProducer = modelProducer,
        modifier = modifier
    )
}
