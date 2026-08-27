/**
 * Active workout screen with expandable exercises, sets, and rest timer.
 */
package com.overloadtracker.ui.screens.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.overloadtracker.R
import com.overloadtracker.ui.components.RestTimer
import com.overloadtracker.ui.components.SetRow
import com.overloadtracker.ui.screens.library.ExerciseLibraryScreen
import com.overloadtracker.util.formatDuration

/**
 * Live workout logging screen.
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
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(uiState.groupName)
                            Text(
                                text = formatDuration(uiState.elapsedMillis),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { showDiscardDialog = true }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = { viewModel.finishWorkout { onFinish() } },
                            enabled = !uiState.isFinishing
                        ) {
                            Text(stringResource(R.string.finish_workout))
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddExercise = true }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_exercises))
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.discard_confirm_title)) },
            text = { Text(stringResource(R.string.discard_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onDiscard()
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
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
    androidx.compose.material3.ModalBottomSheet(onDismissRequest = onDismiss) {
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
    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
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
    androidx.compose.material3.Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(exercise.exerciseName, style = MaterialTheme.typography.titleMedium)
                    exercise.prevBestLabel?.let { label ->
                        AssistChip(
                            onClick = {},
                            label = { Text("Prev: $label") }
                        )
                    }
                }
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        if (exercise.expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            }
            AnimatedVisibility(visible = exercise.expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    exercise.sets.forEach { set ->
                        SetRow(
                            setNumber = set.setNumber,
                            weightDisplay = set.weightDisplay,
                            repsDisplay = set.repsDisplay,
                            rpe = set.rpe,
                            isCompleted = set.isCompleted,
                            onWeightChange = { onWeightChange(set.setNumber, it) },
                            onRepsChange = { onRepsChange(set.setNumber, it) },
                            onRpeChange = { onRpeChange(set.setNumber, it) },
                            onCompletedChange = { onCompleteChange(set.setNumber, it) }
                        )
                    }
                    OutlinedButton(
                        onClick = onAddSet,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Text(stringResource(R.string.add_set))
                    }
                }
            }
        }
    }
}
