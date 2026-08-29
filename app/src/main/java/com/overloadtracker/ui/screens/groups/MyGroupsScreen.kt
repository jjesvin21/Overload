/**
 * Workout groups list with Apex Athletic / Liquid Glass dashboard design.
 * Features Today's Focus quick-start banner, Weekly Progress liquid chart with real-time data, and My Groups cards.
 */
package com.overloadtracker.ui.screens.groups

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.overloadtracker.util.formatDuration

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.overloadtracker.R
import com.overloadtracker.ui.components.GlassCard
import com.overloadtracker.ui.theme.Charcoal
import com.overloadtracker.ui.theme.DisplayMetrics
import com.overloadtracker.ui.theme.HeadlineLargeMobile
import com.overloadtracker.ui.theme.LabelCaps
import com.overloadtracker.ui.theme.OnSurface
import com.overloadtracker.ui.theme.OnSurfaceVariant
import com.overloadtracker.ui.theme.SecondaryText
import com.overloadtracker.ui.theme.StravaOrange
import com.overloadtracker.ui.theme.SurfaceContainerHighest
import com.overloadtracker.ui.theme.TrueBlack
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyGroupsScreen(
    onStartWorkout: (Long) -> Unit,
    onEditGroup: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GroupsViewModel = hiltViewModel()
) {
    val groups by viewModel.groups.collectAsState()
    val weeklyProgress by viewModel.weeklyProgress.collectAsState()
    val deletedGroup by viewModel.deletedGroup.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateDialog by remember { mutableStateOf(false) }
    var pendingGroupIdToStart by remember { mutableStateOf<Long?>(null) }
    val groupDeletedMessage = stringResource(R.string.group_deleted)
    val undoLabel = stringResource(R.string.undo)

    fun requestStartWorkout(targetGroupId: Long) {
        if (activeSession != null && activeSession?.groupId != targetGroupId) {
            pendingGroupIdToStart = targetGroupId
        } else {
            onStartWorkout(targetGroupId)
        }
    }

    LaunchedEffect(deletedGroup) {
        if (deletedGroup != null) {
            val result = kotlinx.coroutines.withTimeoutOrNull(2500L) {
                snackbarHostState.showSnackbar(
                    message = groupDeletedMessage,
                    actionLabel = undoLabel,
                    duration = androidx.compose.material3.SnackbarDuration.Indefinite
                )
            }
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            } else {
                viewModel.clearDeleted()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = StravaOrange,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.shadow(12.dp, CircleShape, spotColor = StravaOrange)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.create_group))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Top Bar Header
            item {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHighest)
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
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
                        Text(
                            text = "Overload",
                            style = HeadlineLargeMobile,
                            color = StravaOrange
                        )
                    }
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = OnSurfaceVariant
                        )
                    }
                }
            }

            // Active Session Banner if workout in progress
            item {
                activeSession?.let { session ->
                    ActiveSessionBannerCard(
                        session = session,
                        onResume = { onStartWorkout(session.groupId) },
                        onDiscard = { viewModel.discardActiveSession() }
                    )
                }
            }


            // Today's Focus / Quick Start Card
            item {
                val topGroup = groups.firstOrNull()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Ready to crush it?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = "Today's Focus",
                        style = HeadlineLargeMobile,
                        color = OnSurface
                    )
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        hasGlow = true,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(StravaOrange.copy(alpha = 0.12f))
                                    .border(1.dp, StravaOrange.copy(alpha = 0.3f), CircleShape)
                                    .padding(horizontal = 14.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "START NOW",
                                    style = LabelCaps.copy(fontWeight = FontWeight.Bold),
                                    color = StravaOrange
                                )
                            }
                            Text(
                                text = topGroup?.group?.name ?: "Full Body",
                                style = DisplayMetrics.copy(fontSize = 36.sp, lineHeight = 40.sp),
                                color = OnSurface
                            )
                            Text(
                                text = topGroup?.group?.notes ?: "Heavy compound movements.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )

                            // Start Workout Pill Button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .shadow(
                                        elevation = 16.dp,
                                        shape = RoundedCornerShape(28.dp),
                                        spotColor = StravaOrange
                                    )
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(StravaOrange)
                                    .clickable {
                                        topGroup?.group?.id?.let { requestStartWorkout(it) }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = TrueBlack,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Start Workout",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = TrueBlack
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Real-Time Weekly Progress Bar Chart Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Weekly Progress",
                            style = MaterialTheme.typography.headlineMedium,
                            color = OnSurface
                        )
                        Text(
                            text = "${weeklyProgress.completedSessionsThisWeek} / ${weeklyProgress.targetSessionsPerWeek} Sessions",
                            style = LabelCaps,
                            color = StravaOrange
                        )
                    }
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                weeklyProgress.days.forEach { dayUi ->
                                    val animatedFraction by animateFloatAsState(
                                        targetValue = dayUi.fraction,
                                        label = "weekly_bar_${dayUi.dayLabel}"
                                    )
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(22.dp)
                                                .height(80.dp)
                                                .clip(RoundedCornerShape(11.dp))
                                                .background(
                                                    if (dayUi.isToday) SurfaceContainerHighest.copy(alpha = 0.8f) else SurfaceContainerHighest
                                                )
                                                .then(
                                                    if (dayUi.isToday) {
                                                        Modifier.border(1.dp, StravaOrange.copy(alpha = 0.5f), RoundedCornerShape(11.dp))
                                                    } else {
                                                        Modifier
                                                    }
                                                ),
                                            contentAlignment = Alignment.BottomCenter
                                        ) {
                                            if (animatedFraction > 0f) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height((80 * animatedFraction).dp)
                                                        .clip(RoundedCornerShape(11.dp))
                                                        .background(
                                                            Brush.verticalGradient(
                                                                colors = listOf(StravaOrange, StravaOrange.copy(alpha = 0.4f))
                                                            )
                                                        )
                                                )
                                            }
                                        }
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = dayUi.dayLabel,
                                            style = LabelCaps,
                                            color = if (dayUi.isToday) StravaOrange else if (dayUi.sessionCount > 0) OnSurface else OnSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Exercise Splits Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Exercise Splits",
                        style = MaterialTheme.typography.headlineMedium,
                        color = OnSurface
                    )
                    TextButton(onClick = { showCreateDialog = true }) {
                        Text(
                            text = "+ New Split",
                            style = LabelCaps,
                            color = StravaOrange
                        )
                    }
                }
            }

            // Workout Group Items List
            if (groups.isEmpty()) {
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
                                text = stringResource(R.string.empty_groups),
                                style = MaterialTheme.typography.bodyLarge,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(groups, key = { _, item -> item.group.id }) { index, item ->
                    GroupCard(
                        item = item,
                        isPrimary = index == 0,
                        onStart = { requestStartWorkout(item.group.id) },
                        onEdit = { onEditGroup(item.group.id) },
                        onDelete = { viewModel.deleteGroup(item.group) },
                        modifier = Modifier.animateItem()
                    )
                }
            }

            item {
                Spacer(Modifier.height(40.dp))
            }
        }
    }

    if (showCreateDialog) {
        CreateGroupDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, notes ->
                viewModel.createGroup(name, notes)
                showCreateDialog = false
            }
        )
    }

    pendingGroupIdToStart?.let { targetId ->
        AlertDialog(
            onDismissRequest = { pendingGroupIdToStart = null },
            containerColor = Charcoal,
            titleContentColor = OnSurface,
            textContentColor = OnSurfaceVariant,
            title = { Text("Active Workout Session In Progress") },
            text = {
                Text("You have an active workout session running. Starting a new session will discard your current progress. Do you want to replace it?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val idToStart = targetId
                        pendingGroupIdToStart = null
                        viewModel.discardActiveSession()
                        onStartWorkout(idToStart)
                    }
                ) {
                    Text("Discard & Start New", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingGroupIdToStart = null }) {
                    Text("Cancel", color = OnSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun GroupCard(
    item: GroupItemUi,
    isPrimary: Boolean,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onStart,
        shape = RoundedCornerShape(16.dp),
        backgroundColor = Charcoal,
        hasGlow = isPrimary
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = StravaOrange,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.group.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = OnSurface
                )
                Text(
                    text = if (item.exerciseCount == 0) "No exercises added in split" else "${item.exerciseCount} exercises",
                    style = LabelCaps,
                    color = if (item.exerciseCount == 0) SecondaryText else StravaOrange
                )
                item.lastPerformed?.let { ts ->
                    val fmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    Text(
                        text = "Last: ${fmt.format(Date(ts))}",
                        style = LabelCaps.copy(fontSize = 11.sp),
                        color = SecondaryText
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit_group),
                        tint = OnSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Charcoal,
        titleContentColor = OnSurface,
        textContentColor = OnSurfaceVariant,
        title = { Text(stringResource(R.string.create_group)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.group_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.notes_optional)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name, notes.ifBlank { null }) },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.save), color = StravaOrange)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = OnSurfaceVariant)
            }
        }
    )
}

@Composable
private fun ActiveSessionBannerCard(
    session: com.overloadtracker.data.manager.ActiveWorkoutSession,
    onResume: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(session.startTime) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val elapsedText = remember(currentTimeMillis, session.startTime) {
        formatDuration(currentTimeMillis - session.startTime)
    }
    val completedSetsCount = remember(session.exercises) {
        session.exercises.sumOf { ex -> ex.sets.count { it.isCompleted } }
    }
    val totalSetsCount = remember(session.exercises) {
        session.exercises.sumOf { it.sets.size }
    }

    val backgroundBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFFF4500),
                Color(0xFFFF6D00),
                Color(0xFFE63900)
            )
        )
    }
    val shineBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.22f),
                Color.Transparent
            )
        )
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onResume,
        hasGlow = true,
        shape = RoundedCornerShape(20.dp),
        backgroundBrush = backgroundBrush,
        borderColor = Color.White.copy(alpha = 0.35f),
        borderWidth = 1.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(shineBrush)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Row: Status badge & Horizontal Timer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Text(
                            text = "WORKOUT IN PROGRESS",
                            style = LabelCaps.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            ),
                            color = Color.White,
                            maxLines = 1
                        )
                    }

                    // Horizontal Timer Badge
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.25f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = elapsedText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Middle Section: Workout Details (Title & Exercise/Set Counters)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = session.groupName.ifBlank { "Live Session" },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${session.exercises.size} exercises",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        if (totalSetsCount > 0) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "$completedSetsCount/$totalSetsCount sets",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }
                }

                // Bottom Row: Equal-Sized Cancel & Resume Buttons Side-by-Side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel / Discard Button (Dark Glass / Red Tint)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f))
                            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                            .clickable { showDiscardDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.discard_workout),
                                tint = Color(0xFFFF6B6B),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Cancel",
                                style = LabelCaps.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = Color.White
                            )
                        }
                    }

                    // Resume Button (White Pill)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .shadow(6.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.3f))
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable(onClick = onResume),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Resume",
                                style = LabelCaps.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                ),
                                color = TrueBlack
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = TrueBlack,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
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
                    Text(stringResource(R.string.discard_workout), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.cancel), color = OnSurfaceVariant)
                }
            }
        )
    }
}
