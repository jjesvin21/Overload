/**
 * Workout history list with session cards and bulk CSV export.
 */
package com.overloadtracker.ui.screens.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.overloadtracker.R
import com.overloadtracker.data.model.WorkoutSummary
import com.overloadtracker.util.WeightUtils
import com.overloadtracker.util.formatDuration
import com.overloadtracker.util.titleCase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Chronological list of completed workout sessions.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HistoryScreen(
    onSessionClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val summaries by viewModel.summaries.collectAsState()
    val exportMessage by viewModel.exportMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val csvExportedMessage = stringResource(R.string.csv_exported)

    LaunchedEffect(exportMessage) {
        when (exportMessage) {
            "exported" -> {
                snackbarHostState.showSnackbar(csvExportedMessage)
                viewModel.clearExportMessage()
            }
            "failed" -> {
                snackbarHostState.showSnackbar("Export failed")
                viewModel.clearExportMessage()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_history)) },
                actions = {
                    Button(
                        onClick = viewModel::exportAll,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .heightIn(min = 48.dp)
                    ) {
                        Text(stringResource(R.string.export_all))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (summaries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.empty_history),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(summaries, key = { it.sessionId }) { summary ->
                    HistorySessionCard(
                        summary = summary,
                        onClick = { onSessionClick(summary.sessionId) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HistorySessionCard(
    summary: WorkoutSummary,
    onClick: () -> Unit
) {
    val dateFmt = SimpleDateFormat("EEE, MMM d · h:mm a", Locale.getDefault())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(summary.groupName, style = MaterialTheme.typography.titleLarge)
            Text(
                text = dateFmt.format(Date(summary.endTime)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${summary.exerciseCount} exercises · ${summary.setCount} sets · " +
                    formatDuration(summary.endTime - summary.startTime),
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = stringResource(R.string.total_volume) + ": " +
                    WeightUtils.formatWeight(summary.totalVolume, com.overloadtracker.util.WeightUnit.KG),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            if (summary.muscleGroups.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    summary.muscleGroups.forEach { muscle ->
                        AssistChip(onClick = {}, label = { Text(muscle) })
                    }
                }
            }
        }
    }
}
