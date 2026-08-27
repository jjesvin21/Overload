/**
 * Workout groups list with create, swipe-delete, and quick actions.
 * Refactored with Liquid Glass design system aesthetic.
 */
package com.overloadtracker.ui.screens.groups

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.overloadtracker.R
import com.overloadtracker.ui.components.LiquidAlertDialog
import com.overloadtracker.ui.components.LiquidGlassCard
import com.overloadtracker.ui.components.LiquidIconButton
import com.overloadtracker.ui.components.LiquidTextField
import com.overloadtracker.ui.components.LiquidTopAppBar
import com.overloadtracker.ui.theme.CyanAccent
import com.overloadtracker.ui.theme.ElectricViolet
import com.overloadtracker.ui.theme.ElectricVioletOnPrimary
import com.overloadtracker.ui.theme.ErrorRed
import com.overloadtracker.ui.theme.LabelCaps
import com.overloadtracker.ui.theme.MidnightBackground
import com.overloadtracker.ui.theme.PrimaryGradientBrush
import com.overloadtracker.ui.theme.ShapePill
import com.overloadtracker.ui.theme.TextOnSurface
import com.overloadtracker.ui.theme.TextOnSurfaceVariant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Lists workout groups with FAB create, swipe delete + undo, and navigation actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyGroupsScreen(
    onStartWorkout: (Long) -> Unit,
    onEditGroup: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GroupsViewModel = hiltViewModel()
) {
    val groups by viewModel.groups.collectAsState()
    val deletedGroup by viewModel.deletedGroup.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateDialog by remember { mutableStateOf(false) }
    val groupDeletedMessage = stringResource(R.string.group_deleted)
    val undoLabel = stringResource(R.string.undo)

    LaunchedEffect(deletedGroup) {
        if (deletedGroup != null) {
            val result = snackbarHostState.showSnackbar(
                message = groupDeletedMessage,
                actionLabel = undoLabel
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            } else {
                viewModel.clearDeleted()
            }
        }
    }

    Scaffold(
        modifier = modifier.background(MidnightBackground),
        containerColor = MidnightBackground,
        topBar = {
            LiquidTopAppBar(
                title = "MY WORKOUTS",
                subtitle = "APEX ROUTINES & GROUPS"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Color.Transparent,
                contentColor = ElectricVioletOnPrimary,
                shape = ShapePill,
                modifier = Modifier.background(PrimaryGradientBrush, shape = ShapePill)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.create_group))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (groups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.empty_groups),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextOnSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(groups, key = { it.group.id }) { item ->
                    GroupSwipeCard(
                        item = item,
                        onStart = { onStartWorkout(item.group.id) },
                        onEdit = { onEditGroup(item.group.id) },
                        onDelete = { viewModel.deleteGroup(item.group) }
                    )
                }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupSwipeCard(
    item: GroupItemUi,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = ErrorRed
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onStart,
            padding = 16.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.group.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextOnSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${item.exerciseCount} EXERCISES",
                        style = LabelCaps.copy(fontSize = 11.sp),
                        color = TextOnSurfaceVariant
                    )
                    item.lastPerformed?.let { ts ->
                        val fmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "LAST: ${fmt.format(Date(ts))}".uppercase(),
                            style = LabelCaps.copy(fontSize = 11.sp),
                            color = CyanAccent
                        )
                    }
                }
                LiquidIconButton(
                    icon = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_group),
                    onClick = onEdit
                )
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

    LiquidAlertDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.create_group),
        confirmButtonText = stringResource(R.string.save),
        onConfirm = {
            if (name.isNotBlank()) {
                onCreate(name, notes.ifBlank { null })
            }
        },
        dismissButtonText = stringResource(R.string.cancel),
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LiquidTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.group_name),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            LiquidTextField(
                value = notes,
                onValueChange = { notes = it },
                label = stringResource(R.string.notes_optional),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
