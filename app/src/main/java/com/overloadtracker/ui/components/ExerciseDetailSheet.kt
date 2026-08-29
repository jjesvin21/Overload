/**
 * Modal bottom sheet showing full exercise details, instructions, personal records,
 * and previous logged history of weights, reps, and intensity across sessions.
 */
package com.overloadtracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.overloadtracker.R
import com.overloadtracker.data.local.entity.Exercise
import com.overloadtracker.data.model.ExerciseDetailWithHistory
import com.overloadtracker.data.model.ExerciseHistorySessionGroup
import com.overloadtracker.data.model.PastExerciseSetLog
import com.overloadtracker.ui.theme.Charcoal
import com.overloadtracker.ui.theme.GlassBorder
import com.overloadtracker.ui.theme.LabelCaps
import com.overloadtracker.ui.theme.OnSurface
import com.overloadtracker.ui.theme.OnSurfaceVariant
import com.overloadtracker.ui.theme.SecondaryText
import com.overloadtracker.ui.theme.StravaOrange
import com.overloadtracker.ui.theme.SurfaceContainerHighest
import com.overloadtracker.util.WeightUnit
import com.overloadtracker.util.WeightUtils
import com.overloadtracker.util.formatCardioDisplay
import com.overloadtracker.util.titleCase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Bottom sheet displaying exercise details, PR, and previous workout history (weight/reps/intensity).
 */
@Composable
fun ExerciseDetailSheet(
    exercise: Exercise?,
    onDismiss: () -> Unit,
    onAddToGroup: (() -> Unit)? = null,
    onViewProgress: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (exercise == null) return
    ExerciseDetailSheet(
        detailWithHistory = ExerciseDetailWithHistory(
            exercise = exercise,
            prWeightKg = null,
            pastSessions = emptyList()
        ),
        weightUnit = WeightUnit.KG,
        onDismiss = onDismiss,
        onAddToGroup = onAddToGroup,
        onViewProgress = onViewProgress,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExerciseDetailSheet(
    detailWithHistory: ExerciseDetailWithHistory?,
    weightUnit: WeightUnit = WeightUnit.KG,
    onDismiss: () -> Unit,
    onAddToGroup: (() -> Unit)? = null,
    onViewProgress: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (detailWithHistory == null) return

    val exercise = detailWithHistory.exercise
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val dateFmt = SimpleDateFormat("MMM d, yyyy", Locale.US)
    val isCardio = exercise.category.equals("cardio", ignoreCase = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Charcoal,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Name & Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = OnSurface
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text(titleCase(exercise.category)) }
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text(titleCase(exercise.equipment)) }
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text(titleCase(exercise.target)) }
                    )
                }
            }

            // Exercise GIF
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("file:///android_asset/${exercise.gifPath}")
                    .crossfade(true)
                    .build(),
                contentDescription = exercise.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerHighest)
            )

            // Personal Record (PR) Card
            detailWithHistory.prWeightKg?.let { pr ->
                if (!isCardio && pr > 0) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(StravaOrange.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = StravaOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "ALL-TIME PERSONAL RECORD",
                                    style = LabelCaps.copy(fontSize = 10.sp),
                                    color = StravaOrange
                                )
                                Text(
                                    text = WeightUtils.formatWeight(pr, weightUnit),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurface
                                )
                            }
                        }
                    }
                }
            }

            // Instructions
            if (exercise.instructions.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "INSTRUCTIONS",
                        style = LabelCaps.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = SecondaryText
                    )
                    Text(
                        text = exercise.instructions,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }

            // History Section Header
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = StravaOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "PAST PERFORMANCE HISTORY",
                        style = LabelCaps.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = StravaOrange
                    )
                }

                if (detailWithHistory.pastSessions.isEmpty()) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "No previous workout sessions recorded yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    detailWithHistory.pastSessions.take(5).forEach { sessionGroup ->
                        PastSessionHistoryCard(
                            sessionGroup = sessionGroup,
                            dateStr = dateFmt.format(Date(sessionGroup.sessionDateMillis)),
                            weightUnit = weightUnit,
                            isCardio = isCardio
                        )
                    }
                }
            }

            // Action Buttons
            if (onAddToGroup != null) {
                Column(
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                ) {
                    Button(
                        onClick = onAddToGroup,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Text(stringResource(R.string.add_to_group))
                    }
                }
            } else {
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}


@Composable
private fun PastSessionHistoryCard(
    sessionGroup: ExerciseHistorySessionGroup,
    dateStr: String,
    weightUnit: WeightUnit,
    isCardio: Boolean
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Session Name & Date Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sessionGroup.groupName.ifBlank { "Workout Session" },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceContainerHighest)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = dateStr,
                        style = LabelCaps.copy(fontSize = 10.sp),
                        color = SecondaryText
                    )
                }
            }

            // Table Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "SET",
                    style = LabelCaps.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                    color = SecondaryText,
                    modifier = Modifier.width(28.dp)
                )
                Text(
                    text = if (isCardio) "CARDIO LOG" else "PERFORMANCE",
                    style = LabelCaps.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                    color = SecondaryText,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "INTENSITY",
                    style = LabelCaps.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                    color = SecondaryText,
                    modifier = Modifier.width(100.dp),
                    textAlign = TextAlign.End
                )
            }

            HorizontalDivider(
                color = GlassBorder.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            // Sets Rows
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                sessionGroup.sets.forEach { setLog ->
                    PastSetRow(setLog = setLog, weightUnit = weightUnit, isCardio = isCardio)
                }
            }
        }
    }
}

@Composable
private fun PastSetRow(
    setLog: PastExerciseSetLog,
    weightUnit: WeightUnit,
    isCardio: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceContainerHighest.copy(alpha = 0.4f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Set Circle Pill
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(StravaOrange.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${setLog.setNumber}",
                style = LabelCaps.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = StravaOrange
            )
        }

        // Performance / Metrics Column
        if (isCardio) {
            Text(
                text = formatCardioDisplay(setLog.timeSeconds, setLog.count),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = OnSurface,
                modifier = Modifier.weight(1f)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = WeightUtils.formatWeight(setLog.weight, weightUnit),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = OnSurface
                )
                Text(
                    text = "  ×  ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText
                )
                Text(
                    text = "${setLog.reps} reps",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = OnSurfaceVariant
                )
            }
        }

        // Intensity / Volume Badge Column (Aligned to End)
        Box(
            modifier = Modifier.width(100.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (setLog.rpe != null && setLog.rpe > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(StravaOrange.copy(alpha = 0.18f))
                        .border(1.dp, StravaOrange.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "RPE ${setLog.rpe}",
                        style = LabelCaps.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = StravaOrange
                    )
                }
            } else if (!isCardio && setLog.weight > 0 && setLog.reps > 0) {
                val volKg = setLog.weight * setLog.reps
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceContainerHighest)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "VOL: ${WeightUtils.formatWeight(volKg, weightUnit)}",
                        style = LabelCaps.copy(fontSize = 9.sp, fontWeight = FontWeight.Medium),
                        color = SecondaryText
                    )
                }
            } else {
                Text(
                    text = "—",
                    style = LabelCaps.copy(fontSize = 11.sp),
                    color = SecondaryText
                )
            }
        }
    }
}
