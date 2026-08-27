/**
 * Workout history list with session cards, activity consistency heatmap, and bulk CSV export.
 * Styled to Apex Athletic Liquid Glass specification.
 */
package com.overloadtracker.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.overloadtracker.R
import com.overloadtracker.data.model.WorkoutSummary
import com.overloadtracker.ui.components.GlassCard
import com.overloadtracker.ui.theme.GlassBorder
import com.overloadtracker.ui.theme.HeadlineLargeMobile
import com.overloadtracker.ui.theme.LabelCaps
import com.overloadtracker.ui.theme.OnSurface
import com.overloadtracker.ui.theme.OnSurfaceVariant
import com.overloadtracker.ui.theme.SecondaryText
import com.overloadtracker.ui.theme.StravaOrange
import com.overloadtracker.ui.theme.SurfaceContainerHighest
import com.overloadtracker.util.WeightUtils
import com.overloadtracker.util.formatDuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.style.TextOverflow
import com.overloadtracker.util.WeightUnit
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

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
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with CSV Export Action
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "History",
                        style = HeadlineLargeMobile,
                        color = StravaOrange
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SurfaceContainerHighest)
                            .border(1.dp, GlassBorder, CircleShape)
                            .clickable(onClick = viewModel::exportAll)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = stringResource(R.string.export_all),
                                tint = StravaOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.export_all),
                                style = LabelCaps.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                        }
                    }
                }
            }

            // Consistency Heatmap Visualizer Card (GitHub Style)
            item {
                val heatmapState by viewModel.heatmapState.collectAsState()
                ConsistencyHeatmapCard(
                    heatmapState = heatmapState,
                    onSelectDay = viewModel::selectDay
                )
            }

            // Recent Sessions List
            item {
                Text(
                    text = "Recent Sessions",
                    style = MaterialTheme.typography.headlineMedium,
                    color = OnSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (summaries.isEmpty()) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.empty_history),
                                style = MaterialTheme.typography.bodyLarge,
                                color = OnSurfaceVariant
                            )
                        }
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

            item {
                Spacer(Modifier.height(40.dp))
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
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = StravaOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = summary.groupName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                    Text(
                        text = dateFmt.format(Date(summary.endTime)),
                        style = LabelCaps.copy(fontSize = 11.sp),
                        color = SecondaryText
                    )
                }
                Text(
                    text = WeightUtils.formatWeight(summary.totalVolume, com.overloadtracker.util.WeightUnit.KG),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = StravaOrange
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${summary.exerciseCount} EXERCISES · ${summary.setCount} SETS · " +
                        formatDuration(summary.endTime - summary.startTime).uppercase(),
                    style = LabelCaps.copy(fontSize = 10.sp),
                    color = OnSurfaceVariant
                )
            }

            if (summary.muscleGroups.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    summary.muscleGroups.forEach { muscle ->
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SurfaceContainerHighest)
                                .border(1.dp, GlassBorder, CircleShape)
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = muscle.uppercase(),
                                style = LabelCaps.copy(fontSize = 9.sp),
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConsistencyHeatmapCard(
    heatmapState: ConsistencyHeatmapState,
    onSelectDay: (HeatmapDay) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Auto scroll to latest week on initial render
    LaunchedEffect(heatmapState.weeks) {
        if (heatmapState.weeks.isNotEmpty()) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Title + Streak & Session Counters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Consistency Map",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                    if (heatmapState.currentStreak > 0) {
                        Text(
                            text = "🔥 ${heatmapState.currentStreak} Day Streak",
                            style = LabelCaps.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                            color = StravaOrange
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SurfaceContainerHighest)
                        .border(1.dp, GlassBorder, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${heatmapState.totalWorkouts} Sessions",
                        style = LabelCaps.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = StravaOrange
                    )
                }
            }

            // GitHub Contribution Grid layout (Left Day Labels + Horizontally Scrollable Weeks)
            Row(modifier = Modifier.fillMaxWidth()) {
                // Fixed Day Labels Column (M, W, F)
                Column(
                    modifier = Modifier
                        .padding(top = 16.dp, end = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    val dayLabels = listOf("M", "", "W", "", "F", "", "")
                    dayLabels.forEach { label ->
                        Box(
                            modifier = Modifier.size(width = 12.dp, height = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = LabelCaps.copy(fontSize = 9.sp, fontWeight = FontWeight.Medium),
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }

                // Scrollable Matrix (Month Headers top + 7 rows x N weeks)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    heatmapState.weeks.forEach { week ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            // Month Header
                            Box(
                                modifier = Modifier
                                    .size(width = 14.dp, height = 13.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (week.monthLabel != null) {
                                    Text(
                                        text = week.monthLabel,
                                        style = LabelCaps.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        color = OnSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Clip
                                    )
                                }
                            }

                            // 7 Days (Mon..Sun)
                            week.days.forEach { day ->
                                val isSelected = heatmapState.selectedDay?.date == day.date
                                val tileColor = when (day.level) {
                                    1 -> StravaOrange.copy(alpha = 0.25f)
                                    2 -> StravaOrange.copy(alpha = 0.50f)
                                    3 -> StravaOrange.copy(alpha = 0.75f)
                                    4 -> StravaOrange
                                    else -> SurfaceContainerHighest.copy(alpha = 0.35f)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(tileColor)
                                        .then(
                                            when {
                                                isSelected -> Modifier.border(1.5.dp, StravaOrange, RoundedCornerShape(3.dp))
                                                day.isToday -> Modifier.border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                                                else -> Modifier
                                            }
                                        )
                                        .clickable { onSelectDay(day) }
                                )
                            }
                        }
                    }
                }
            }

            // Day Detail Inspector Banner
            heatmapState.selectedDay?.let { selectedDay ->
                val dateFmt = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.getDefault())
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerHighest.copy(alpha = 0.6f))
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateFmt.format(selectedDay.date) + if (selectedDay.isToday) " (Today)" else "",
                                style = LabelCaps.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                            if (selectedDay.workoutCount > 0) {
                                Text(
                                    text = WeightUtils.formatWeight(selectedDay.totalVolume, WeightUnit.KG),
                                    style = LabelCaps.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                    color = StravaOrange
                                )
                            }
                        }

                        if (selectedDay.workoutCount > 0) {
                            Text(
                                text = "${selectedDay.workoutCount} Workout${if (selectedDay.workoutCount > 1) "s" else ""} · " +
                                        "${selectedDay.exerciseCount} Exercises · ${selectedDay.setCount} Sets",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                            if (selectedDay.workoutNames.isNotEmpty()) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    selectedDay.workoutNames.distinct().forEach { name ->
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(StravaOrange.copy(alpha = 0.15f))
                                                .border(1.dp, StravaOrange.copy(alpha = 0.4f), CircleShape)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = name,
                                                style = LabelCaps.copy(fontSize = 9.sp),
                                                color = StravaOrange
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = if (selectedDay.isFuture) "Upcoming Day" else "Rest Day — No workouts logged",
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondaryText
                            )
                        }
                    }
                }
            }

            // GitHub Legend (Less -> More)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Less",
                    style = LabelCaps.copy(fontSize = 9.sp),
                    color = SecondaryText,
                    modifier = Modifier.padding(end = 4.dp)
                )
                val legendLevels = listOf(0, 1, 2, 3, 4)
                legendLevels.forEach { lvl ->
                    val color = when (lvl) {
                        1 -> StravaOrange.copy(alpha = 0.25f)
                        2 -> StravaOrange.copy(alpha = 0.50f)
                        3 -> StravaOrange.copy(alpha = 0.75f)
                        4 -> StravaOrange
                        else -> SurfaceContainerHighest.copy(alpha = 0.35f)
                    }
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 1.5.dp)
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                }
                Text(
                    text = "More",
                    style = LabelCaps.copy(fontSize = 9.sp),
                    color = SecondaryText,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

