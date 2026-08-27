/**
 * Active workout screen with expandable exercises, sets, and rest timer.
 * Refactored with Liquid Glass / Liquid Vitality visual aesthetic.
 */
package com.overloadtracker.ui.screens.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.overloadtracker.ui.components.LiquidAlertDialog
import com.overloadtracker.ui.components.LiquidBottomSheet
import com.overloadtracker.ui.components.LiquidGlassCard
import com.overloadtracker.ui.components.LiquidIconButton
import com.overloadtracker.ui.components.LiquidPrimaryButton
import com.overloadtracker.ui.components.LiquidSecondaryButton
import com.overloadtracker.ui.components.LiquidTopAppBar
import com.overloadtracker.ui.components.RestTimer
import com.overloadtracker.ui.components.SetRow
import com.overloadtracker.ui.screens.library.ExerciseLibraryScreen
import com.overloadtracker.ui.theme.CyanAccent
import com.overloadtracker.ui.theme.ElectricVioletOnPrimary
import com.overloadtracker.ui.theme.GlassBorderTopLeft
import com.overloadtracker.ui.theme.GlassSurfaceHigh
import com.overloadtracker.ui.theme.LabelCaps
import com.overloadtracker.ui.theme.MidnightBackground
import com.overloadtracker.ui.theme.PrimaryGradientBrush
import com.overloadtracker.ui.theme.ShapeChip
import com.overloadtracker.ui.theme.ShapePill
import com.overloadtracker.ui.theme.TextOnSurface
import com.overloadtracker.ui.theme.TextOnSurfaceVariant
import com.overloadtracker.util.formatDuration

/**
 * Live workout logging screen in Liquid Glass style.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveWorkoutScreen(
    onFinish: () -> Unit,
    onDiscard: () -> Unit,
    onViewProgress: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LiveWorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showAddExercise by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.validationMessage) {
        uiState.validationMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearValidationMessage()
        }
    }

    BoxWithRestTimer(
        restSeconds = uiState.restSecondsRemaining,
        restTotal = uiState.restTotalSeconds,
        onSkipRest = viewModel::skipRest,
        onAddRest30 = viewModel::addRest30,
        onResetRest = viewModel::resetRest
    ) {
        Scaffold(
            modifier = modifier.background(MidnightBackground),
            containerColor = MidnightBackground,
            topBar = {
                LiquidTopAppBar(
                    title = uiState.groupName.ifEmpty { "ACTIVE WORKOUT" },
                    subtitle = formatDuration(uiState.elapsedMillis),
                    onBackClick = { showDiscardDialog = true },
                    actions = {
                        LiquidPrimaryButton(
                            text = stringResource(R.string.finish_workout),
                            onClick = { viewModel.finishWorkout { onFinish() } },
                            enabled = !uiState.isFinishing
                        )
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddExercise = true },
                    containerColor = Color.Transparent,
                    contentColor = ElectricVioletOnPrimary,
                    shape = ShapePill,
                    modifier = Modifier.background(PrimaryGradientBrush, shape = ShapePill)
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_exercises))
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(uiState.exercises, key = { it.exerciseId }) { exercise ->
                    ExerciseSection(
                        exercise = exercise,
                        onToggleExpand = { viewModel.toggleExpanded(exercise.exerciseId) },
                        onWeightChange = { setNum, value ->
                            viewModel.updateWeight(exercise.exerciseId, setNum, value)
                        },
                        onRepsChange = { setNum, value ->
                            viewModel.updateReps(exercise.exerciseId, setNum, value)
                        },
                        onRpeChange = { setNum, rpe ->
                            viewModel.updateRpe(exercise.exerciseId, setNum, rpe)
                        },
                        onCompleteChange = { setNum, done ->
                            viewModel.toggleSetComplete(exercise.exerciseId, setNum, done)
                        },
                        onAddSet = { viewModel.addSet(exercise.exerciseId) }
                    )
                }
            }
        }
    }

    if (showDiscardDialog) {
        LiquidAlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = stringResource(R.string.discard_confirm_title),
            bodyText = stringResource(R.string.discard_confirm_body),
            confirmButtonText = stringResource(R.string.confirm),
            onConfirm = {
                showDiscardDialog = false
                onDiscard()
            },
            dismissButtonText = stringResource(R.string.cancel),
            onDismiss = { showDiscardDialog = false }
        )
    }

    if (showAddExercise) {
        AddExerciseBottomSheet(
            onDismiss = { showAddExercise = false },
            onSelect = { exercise ->
                viewModel.addExercise(exercise)
                showAddExercise = false
            },
            onViewProgress = { id ->
                showAddExercise = false
                onViewProgress(id)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExerciseBottomSheet(
    onDismiss: () -> Unit,
    onSelect: (com.overloadtracker.data.local.entity.Exercise) -> Unit,
    onViewProgress: (String) -> Unit
) {
    LiquidBottomSheet(onDismissRequest = onDismiss) {
        ExerciseLibraryScreen(
            onViewProgress = onViewProgress,
            onExerciseSelected = onSelect,
            modifier = Modifier.heightIn(min = 400.dp, max = 600.dp)
        )
    }
}

@Composable
private fun BoxWithRestTimer(
    restSeconds: Int?,
    restTotal: Int,
    onSkipRest: () -> Unit,
    onAddRest30: () -> Unit,
    onResetRest: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()
        if (restSeconds != null) {
            RestTimer(
                secondsRemaining = restSeconds,
                totalSeconds = restTotal,
                onSkip = onSkipRest,
                onAdd30 = onAddRest30,
                onReset = onResetRest
            )
        }
    }
}

@Composable
private fun ExerciseSection(
    exercise: LiveExercise,
    onToggleExpand: () -> Unit,
    onWeightChange: (Int, String) -> Unit,
    onRepsChange: (Int, String) -> Unit,
    onRpeChange: (Int, Int?) -> Unit,
    onCompleteChange: (Int, Boolean) -> Unit,
    onAddSet: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        padding = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.exerciseName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextOnSurface
                )
                exercise.prevBestLabel?.let { label ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(ShapeChip)
                            .background(GlassSurfaceHigh)
                            .border(width = 1.dp, color = GlassBorderTopLeft, shape = ShapeChip)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "PREV: $label".uppercase(),
                            style = LabelCaps.copy(fontSize = 10.sp),
                            color = CyanAccent
                        )
                    }
                }
            }
            LiquidIconButton(
                icon = if (exercise.expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Expand/Collapse",
                onClick = onToggleExpand
            )
        }
        AnimatedVisibility(visible = exercise.expanded) {
            Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                exercise.sets.forEach { set ->
                    SetRow(
                        setNumber = set.setNumber,
                        weightDisplay = set.weightDisplay,
                        repsDisplay = set.repsDisplay,
                        rpe = set.rpe,
                        isCompleted = set.isCompleted,
                        prevWeightDisplay = set.prevWeightDisplay,
                        onWeightChange = { onWeightChange(set.setNumber, it) },
                        onRepsChange = { onRepsChange(set.setNumber, it) },
                        onRpeChange = { onRpeChange(set.setNumber, it) },
                        onCompletedChange = { onCompleteChange(set.setNumber, it) }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LiquidSecondaryButton(
                    text = stringResource(R.string.add_set),
                    onClick = onAddSet,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
