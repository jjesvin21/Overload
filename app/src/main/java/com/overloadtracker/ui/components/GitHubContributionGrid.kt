/**
 * GitHub-style contribution heatmap grid component for tracking workout consistency over time.
 * Styled with Liquid Glass / Liquid Vitality dark obsidian and neon accent aesthetics.
 */
package com.overloadtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.overloadtracker.data.model.WorkoutSummary
import com.overloadtracker.ui.theme.CyanAccent
import com.overloadtracker.ui.theme.ElectricViolet
import com.overloadtracker.ui.theme.GlassBorderTopLeft
import com.overloadtracker.ui.theme.GlassSurfaceHigh
import com.overloadtracker.ui.theme.LabelCaps
import com.overloadtracker.ui.theme.TextOnSurface
import com.overloadtracker.ui.theme.TextOnSurfaceVariant
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * GitHub-style heatmap grid showing workout frequency over the past [weeksCount] weeks.
 */
@Composable
fun GitHubContributionGrid(
    summaries: List<WorkoutSummary>,
    modifier: Modifier = Modifier,
    weeksCount: Int = 16
) {
    val today = remember { LocalDate.now() }
    val zoneId = remember { ZoneId.systemDefault() }

    // Map LocalDate to workout count
    val workoutMap = remember(summaries) {
        summaries.map { summary ->
            Instant.ofEpochMilli(summary.endTime).atZone(zoneId).toLocalDate()
        }.groupingBy { it }.eachCount()
    }

    // Calculate grid start date (starting on Sunday `weeksCount` weeks ago)
    val totalDays = weeksCount * 7
    val startDate = remember(today, weeksCount) {
        val daysToSunday = today.dayOfWeek.value % 7
        today.minusDays(daysToSunday.toLong()).minusWeeks((weeksCount - 1).toLong())
    }

    // Calculate streak and total active days
    val activeDays = workoutMap.size
    val currentStreak = remember(workoutMap, today) {
        var streak = 0
        var checkDate = today
        if (!workoutMap.containsKey(checkDate)) {
            checkDate = today.minusDays(1)
        }
        while (workoutMap.containsKey(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }
        streak
    }

    var selectedTileInfo by remember { mutableStateOf<Pair<LocalDate, Int>?>(null) }

    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        padding = 16.dp
    ) {
        Column {
            // Header stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CONSISTENCY MATRIX",
                        style = LabelCaps.copy(fontSize = 11.sp),
                        color = CyanAccent
                    )
                    Text(
                        text = "$activeDays Active Days",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextOnSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassSurfaceHigh)
                        .border(width = 1.dp, color = GlassBorderTopLeft, shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "⚡ STREAK: $currentStreak DAYS",
                        style = LabelCaps.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = ElectricViolet
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Heatmap matrix
            val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
            Row(modifier = Modifier.fillMaxWidth()) {
                // Day labels column
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(end = 6.dp, top = 2.dp)
                ) {
                    daysOfWeek.forEach { day ->
                        Text(
                            text = day,
                            style = LabelCaps.copy(fontSize = 9.sp),
                            color = TextOnSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Grid columns for weeks
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (w in 0 until weeksCount) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (d in 0 until 7) {
                                val cellDate = startDate.plusDays((w * 7 + d).toLong())
                                val count = workoutMap[cellDate] ?: 0
                                val isFuture = cellDate.isAfter(today)

                                ContributionTile(
                                    date = cellDate,
                                    count = count,
                                    isFuture = isFuture,
                                    onClick = {
                                        if (!isFuture) {
                                            selectedTileInfo = cellDate to count
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer legend & selected tile detail
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedTileInfo != null) {
                    val dateStr = selectedTileInfo!!.first.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                    val count = selectedTileInfo!!.second
                    Text(
                        text = "$dateStr: $count ${if (count == 1) "workout" else "workouts"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanAccent
                    )
                } else {
                    Text(
                        text = "Past $weeksCount weeks",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextOnSurfaceVariant
                    )
                }

                // Legend
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Less",
                        style = LabelCaps.copy(fontSize = 9.sp),
                        color = TextOnSurfaceVariant
                    )
                    listOf(0, 1, 2, 3).forEach { lvl ->
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(tileColorForCount(lvl, isFuture = false))
                        )
                    }
                    Text(
                        text = "More",
                        style = LabelCaps.copy(fontSize = 9.sp),
                        color = TextOnSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ContributionTile(
    date: LocalDate,
    count: Int,
    isFuture: Boolean,
    onClick: () -> Unit
) {
    val tileColor = tileColorForCount(count, isFuture)

    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(tileColor)
            .border(
                width = 0.5.dp,
                color = if (isFuture) Color.Transparent else GlassBorderTopLeft,
                shape = RoundedCornerShape(3.dp)
            )
            .clickable(enabled = !isFuture, onClick = onClick)
    )
}

private fun tileColorForCount(count: Int, isFuture: Boolean): Color {
    if (isFuture) return Color(0xFF0F1524).copy(alpha = 0.3f)
    return when (count) {
        0 -> Color(0xFF14192B)
        1 -> ElectricViolet.copy(alpha = 0.45f)
        2 -> ElectricViolet
        else -> CyanAccent
    }
}
