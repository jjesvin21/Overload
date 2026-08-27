/**
 * Workout history list with session cards and bulk CSV export.
 * Refactored with Liquid Glass / Liquid Vitality visual aesthetic.
 */
package com.overloadtracker.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.overloadtracker.R
import com.overloadtracker.data.model.WorkoutSummary
import com.overloadtracker.ui.components.LiquidGlassCard
import com.overloadtracker.ui.components.LiquidPrimaryButton
import com.overloadtracker.ui.components.LiquidTopAppBar
import com.overloadtracker.ui.theme.CyanAccent
import com.overloadtracker.ui.theme.ElectricViolet
import com.overloadtracker.ui.theme.GlassBorderTopLeft
import com.overloadtracker.ui.theme.GlassSurfaceHigh
import com.overloadtracker.ui.theme.LabelCaps
import com.overloadtracker.ui.theme.MidnightBackground
import com.overloadtracker.ui.theme.ShapeChip
import com.overloadtracker.ui.theme.TextOnSurface
import com.overloadtracker.ui.theme.TextOnSurfaceVariant
import com.overloadtracker.util.WeightUtils
import com.overloadtracker.util.formatDuration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Chronological list of completed workout sessions in Liquid Glass design system.
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
        modifier = modifier.background(MidnightBackground),
        containerColor = MidnightBackground,
        topBar = {
            LiquidTopAppBar(
                title = "TRAINING HISTORY",
                subtitle = "COMPLETED LOGS & EXPORTS",
                actions = {
                    LiquidPrimaryButton(
                        text = stringResource(R.string.export_all),
                        onClick = viewModel::exportAll,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                com.overloadtracker.ui.components.GitHubContributionGrid(
                    summaries = summaries,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (summaries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.empty_history),
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextOnSurfaceVariant
                        )
                    }
                }
            } else {
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
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        padding = 18.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = summary.groupName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextOnSurface
            )

            Text(
                text = dateFmt.format(Date(summary.endTime)).uppercase(),
                style = LabelCaps.copy(fontSize = 11.sp),
                color = TextOnSurfaceVariant
            )

            Text(
                text = "${summary.exerciseCount} EXERCISES · ${summary.setCount} SETS · " +
                    formatDuration(summary.endTime - summary.startTime).uppercase(),
                style = LabelCaps.copy(fontSize = 11.sp),
                color = ElectricViolet
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = stringResource(R.string.total_volume).uppercase() + ": " +
                    WeightUtils.formatWeight(summary.totalVolume, com.overloadtracker.util.WeightUnit.KG),
                style = LabelCaps.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                color = CyanAccent
            )

            if (summary.muscleGroups.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    summary.muscleGroups.forEach { muscle ->
                        Box(
                            modifier = Modifier
                                .clip(ShapeChip)
                                .background(GlassSurfaceHigh)
                                .border(width = 1.dp, color = GlassBorderTopLeft, shape = ShapeChip)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = muscle.uppercase(),
                                style = LabelCaps.copy(fontSize = 10.sp),
                                color = TextOnSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
