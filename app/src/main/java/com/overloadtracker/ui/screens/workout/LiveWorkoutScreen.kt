/**
 * Active workout screen with expandable exercises, set logging, rest timer, and Liquid Glass design.
 */
package com.overloadtracker.ui.screens.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.overloadtracker.R
import com.overloadtracker.ui.components.GlassCard
import com.overloadtracker.ui.components.RestTimer
import com.overloadtracker.ui.components.SetRow
import com.overloadtracker.ui.screens.library.ExerciseLibraryScreen
import com.overloadtracker.ui.theme.Charcoal
import com.overloadtracker.ui.theme.GlassBorder
import com.overloadtracker.ui.theme.HeadlineLargeMobile
import com.overloadtracker.ui.theme.LabelCaps
import com.overloadtracker.ui.theme.OnSurface
import com.overloadtracker.ui.theme.OnSurfaceVariant
import com.overloadtracker.ui.theme.SecondaryText
import com.overloadtracker.ui.theme.StravaOrange
import com.overloadtracker.ui.theme.SurfaceContainerHighest
import com.overloadtracker.ui.theme.TrueBlack
import com.overloadtracker.util.formatDuration

import androidx.compose.material.icons.filled.Info
import com.overloadtracker.ui.components.ExerciseDetailSheet

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
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = OnSurface
                    ),
                    title = {
                        Column {
                            Text(
                                text = uiState.groupName.uppercase(),
                                style = LabelCaps.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = StravaOrange,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = formatDuration(uiState.elapsedMillis),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { showDiscardDialog = true }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = OnSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .shadow(8.dp, CircleShape, spotColor = StravaOrange)
                                .clip(CircleShape)
                                .background(StravaOrange)
                                .clickable(enabled = !uiState.isFinishing) {
                                    viewModel.finishWorkout { onFinish() }
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.finish_workout),
                                style = LabelCaps.copy(fontWeight = FontWeight.Bold),
                                color = TrueBlack
                            )
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddExercise = true },
                    containerColor = StravaOrange,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.shadow(12.dp, CircleShape, spotColor = StravaOrange)
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_exercises))
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.exercises.isEmpty()) {
                    item {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceContainerHighest),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = StravaOrange,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Text(
                                    text = "No Exercises Added",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurface
                                )
                                Text(
                                    text = "This split has no exercises configured. Tap below to select exercises from your library.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = StravaOrange)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(StravaOrange)
                                        .clickable { showAddExercise = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = TrueBlack,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.add_exercises),
                                            style = LabelCaps.copy(fontWeight = FontWeight.Bold),
                                            color = TrueBlack
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(uiState.exercises, key = { it.exerciseId }) { exercise ->
                        ExerciseSection(
                            exercise = exercise,
                            onToggleExpand = { viewModel.toggleExpanded(exercise.exerciseId) },
                            onSelectExercise = { viewModel.selectExerciseDetail(exercise.exerciseId) },
                            onWeightChange = { setNum, value ->
                                viewModel.updateWeight(exercise.exerciseId, setNum, value)
                            },
                            onRepsChange = { setNum, value ->
                                viewModel.updateReps(exercise.exerciseId, setNum, value)
                            },
                            onTimeChange = { setNum, value ->
                                viewModel.updateTime(exercise.exerciseId, setNum, value)
                            },
                            onCountChange = { setNum, value ->
                                viewModel.updateCount(exercise.exerciseId, setNum, value)
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

                item {
                    Spacer(Modifier.height(40.dp))
                }
            }

        }
    }

    if (uiState.selectedExerciseDetail != null) {
        ExerciseDetailSheet(
            detailWithHistory = uiState.selectedExerciseDetail,
            weightUnit = uiState.weightUnit,
            onDismiss = viewModel::dismissExerciseDetail
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            containerColor = Charcoal,
            titleContentColor = OnSurface,
            textContentColor = OnSurfaceVariant,
            title = { Text(stringResource(R.string.discard_confirm_title)) },
            text = { Text(stringResource(R.string.discard_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onDiscard()
                }) {
                    Text(stringResource(R.string.confirm), color = StravaOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.cancel), color = OnSurfaceVariant)
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Charcoal
    ) {
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
    onSelectExercise: () -> Unit,
    onWeightChange: (Int, String) -> Unit,
    onRepsChange: (Int, String) -> Unit,
    onTimeChange: (Int, String) -> Unit,
    onCountChange: (Int, String) -> Unit,
    onRpeChange: (Int, Int?) -> Unit,
    onCompleteChange: (Int, Boolean) -> Unit,
    onAddSet: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Exercise Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSelectExercise),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = StravaOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.exerciseName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                    exercise.prevBestLabel?.let { label ->
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "PREV BEST: $label",
                            style = LabelCaps.copy(fontSize = 10.sp),
                            color = StravaOrange
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val completedCount = exercise.sets.count { it.isCompleted }
                    val totalCount = exercise.sets.size
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StravaOrange.copy(alpha = 0.15f))
                            .border(1.dp, StravaOrange.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$completedCount/$totalCount SETS",
                            style = LabelCaps.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = StravaOrange
                        )
                    }
                    IconButton(onClick = onSelectExercise) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Exercise Details & History",
                            tint = OnSurfaceVariant
                        )
                    }
                    IconButton(onClick = onToggleExpand) {
                        Icon(
                            imageVector = if (exercise.expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = OnSurfaceVariant
                        )
                    }
                }
            }

            AnimatedVisibility(visible = exercise.expanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Set Table Column Header Labels
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("SET", style = LabelCaps.copy(fontSize = 10.sp), color = SecondaryText, modifier = Modifier.width(28.dp))
                        if (exercise.isCardio) {
                            Text(stringResource(R.string.cardio_time_short).uppercase(), style = LabelCaps.copy(fontSize = 10.sp), color = SecondaryText, modifier = Modifier.weight(1f))
                            Text(stringResource(R.string.cardio_count).uppercase(), style = LabelCaps.copy(fontSize = 10.sp), color = SecondaryText, modifier = Modifier.weight(1f))
                        } else {
                            Text("WEIGHT", style = LabelCaps.copy(fontSize = 10.sp), color = SecondaryText, modifier = Modifier.weight(1f))
                            Text("REPS", style = LabelCaps.copy(fontSize = 10.sp), color = SecondaryText, modifier = Modifier.weight(1f))
                        }
                        Text("", modifier = Modifier.size(36.dp))
                    }

                    // Set Rows List
                    exercise.sets.forEach { set ->
                        SetRow(
                            setNumber = set.setNumber,
                            isCardio = exercise.isCardio,
                            weightDisplay = set.weightDisplay,
                            repsDisplay = set.repsDisplay,
                            timeDisplay = set.timeDisplay,
                            countDisplay = set.countDisplay,
                            rpe = set.rpe,
                            isCompleted = set.isCompleted,
                            onWeightChange = { onWeightChange(set.setNumber, it) },
                            onRepsChange = { onRepsChange(set.setNumber, it) },
                            onTimeChange = { onTimeChange(set.setNumber, it) },
                            onCountChange = { onCountChange(set.setNumber, it) },
                            onRpeChange = { onRpeChange(set.setNumber, it) },
                            onCompletedChange = { onCompleteChange(set.setNumber, it) }
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // Add Set Pill Action
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerHighest)
                            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                            .clickable(onClick = onAddSet),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = StravaOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.add_set),
                                style = LabelCaps.copy(fontWeight = FontWeight.Bold),
                                color = StravaOrange
                            )
                        }
                    }
                }
            }
        }
    }
}

