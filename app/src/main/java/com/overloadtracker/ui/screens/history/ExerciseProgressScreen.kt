/**
 * Exercise progress chart showing max weight over time.
 */
package com.overloadtracker.ui.screens.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.overloadtracker.data.model.ExerciseProgressPoint
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

/**
 * Line chart of max weight per session for one exercise.
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

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(exerciseName ?: "Progress") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (progress.isEmpty()) {
                Text(
                    text = "No logged sets yet for this exercise.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
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
