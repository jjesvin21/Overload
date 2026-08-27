/**
 * Searchable exercise library with Liquid Glass filters, dark search bar, and grid.
 */
package com.overloadtracker.ui.screens.library

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.overloadtracker.R
import com.overloadtracker.data.local.entity.Exercise
import com.overloadtracker.data.local.entity.WorkoutGroup
import com.overloadtracker.ui.components.ExerciseCard
import com.overloadtracker.ui.components.ExerciseDetailSheet
import com.overloadtracker.ui.components.FilterChips
import com.overloadtracker.ui.theme.Charcoal
import com.overloadtracker.ui.theme.GlassBorder
import com.overloadtracker.ui.theme.HeadlineLargeMobile
import com.overloadtracker.ui.theme.OnSurface
import com.overloadtracker.ui.theme.OnSurfaceVariant
import com.overloadtracker.ui.theme.StravaOrange
import com.overloadtracker.ui.theme.SurfaceContainerHighest
import com.overloadtracker.ui.viewmodel.SharedExerciseViewModel
import com.overloadtracker.util.titleCase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLibraryScreen(
    onViewProgress: (String) -> Unit,
    modifier: Modifier = Modifier,
    onExerciseSelected: ((Exercise) -> Unit)? = null,
    viewModel: ExerciseLibraryViewModel = hiltViewModel(),
    sharedExerciseViewModel: SharedExerciseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedExercise by sharedExerciseViewModel.selectedExercise.collectAsState()
    val groups by viewModel.groups.collectAsState()
    var exerciseToAdd by remember { mutableStateOf<Exercise?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        if (onExerciseSelected == null) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Library",
                    style = HeadlineLargeMobile,
                    color = StravaOrange
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        // Search Input Field
        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::setQuery,
            placeholder = { Text(stringResource(R.string.search_exercises), color = OnSurfaceVariant) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = StravaOrange
                )
            },
            trailingIcon = {
                if (uiState.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = OnSurfaceVariant)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Charcoal,
                unfocusedContainerColor = Charcoal,
                focusedBorderColor = StravaOrange,
                unfocusedBorderColor = GlassBorder,
                focusedTextColor = OnSurface,
                unfocusedTextColor = OnSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // Filter Category Chips
        FilterChips(
            selectedCategories = uiState.selectedCategories,
            onToggle = viewModel::toggleCategory
        )

        // Equipment Dropdown Filter
        EquipmentDropdown(
            options = uiState.equipmentOptions,
            selected = uiState.selectedEquipment,
            onSelected = viewModel::setEquipment,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = StravaOrange)
                        Text(
                            text = stringResource(R.string.loading),
                            modifier = Modifier.padding(top = 12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }
            uiState.exercises.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_exercises_match),
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSurfaceVariant
                    )
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.exercises, key = { it.id }) { exercise ->
                        ExerciseCard(
                            exercise = exercise,
                            onClick = {
                                if (onExerciseSelected != null) {
                                    onExerciseSelected(exercise)
                                } else {
                                    sharedExerciseViewModel.showExercise(exercise)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    ExerciseDetailSheet(
        exercise = if (onExerciseSelected == null) selectedExercise else null,
        onDismiss = { sharedExerciseViewModel.clearSelection() },
        onAddToGroup = selectedExercise?.let { { exerciseToAdd = it } },
        onViewProgress = selectedExercise?.let { ex ->
            {
                sharedExerciseViewModel.clearSelection()
                onViewProgress(ex.id)
            }
        }
    )

    exerciseToAdd?.let { exercise ->
        AddExerciseToGroupDialog(
            exercise = exercise,
            groups = groups,
            onDismiss = { exerciseToAdd = null },
            onSelectGroup = { groupId ->
                viewModel.addExerciseToGroup(groupId, exercise.id)
                exerciseToAdd = null
                sharedExerciseViewModel.clearSelection()
            }
        )
    }
}

@Composable
private fun AddExerciseToGroupDialog(
    exercise: Exercise,
    groups: List<WorkoutGroup>,
    onDismiss: () -> Unit,
    onSelectGroup: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Charcoal,
        titleContentColor = OnSurface,
        textContentColor = OnSurfaceVariant,
        title = { Text(stringResource(R.string.add_exercise_to_group, exercise.name)) },
        text = {
            if (groups.isEmpty()) {
                Text(stringResource(R.string.no_groups_to_add))
            } else {
                Column {
                    groups.forEach { group ->
                        TextButton(
                            onClick = { onSelectGroup(group.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(group.name, color = StravaOrange)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = OnSurfaceVariant)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EquipmentDropdown(
    options: List<String>,
    selected: String?,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected?.let { titleCase(it) } ?: stringResource(R.string.all_equipment),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.equipment_filter), color = OnSurfaceVariant) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Charcoal,
                unfocusedContainerColor = Charcoal,
                focusedBorderColor = StravaOrange,
                unfocusedBorderColor = GlassBorder,
                focusedTextColor = OnSurface,
                unfocusedTextColor = OnSurface
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(SurfaceContainerHighest)
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.all_equipment), color = OnSurface) },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )
            options.forEach { equipment ->
                DropdownMenuItem(
                    text = { Text(titleCase(equipment), color = OnSurface) },
                    onClick = {
                        onSelected(equipment)
                        expanded = false
                    }
                )
            }
        }
    }
}
