/**
 * Edit workout group: rename, reorder exercises, remove, and add more.
 */
package com.overloadtracker.ui.screens.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.overloadtracker.R
import com.overloadtracker.data.local.entity.GroupExerciseCrossRef
import com.overloadtracker.data.local.entity.WorkoutGroup
import com.overloadtracker.data.repository.WorkoutGroupRepository
import com.overloadtracker.ui.navigation.GroupEditorRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupEditorViewModel @Inject constructor(
    savedStateHandle: androidx.lifecycle.SavedStateHandle,
    private val groupRepository: WorkoutGroupRepository
) : ViewModel() {

    private val groupId = savedStateHandle.toRoute<GroupEditorRoute>().groupId

    val group: StateFlow<WorkoutGroup?> = groupRepository.observeGroup(groupId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val exercises: StateFlow<List<GroupExerciseCrossRef>> =
        groupRepository.observeGroupExercises(groupId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun rename(name: String, notes: String?) {
        viewModelScope.launch {
            val current = groupRepository.getGroup(groupId) ?: return@launch
            groupRepository.updateGroup(
                current.copy(
                    name = name.trim(),
                    notes = notes?.trim()?.ifBlank { null }
                )
            )
        }
    }

    fun removeExercise(exerciseId: String) {
        viewModelScope.launch {
            groupRepository.removeExercise(groupId, exerciseId)
        }
    }

    fun moveUp(index: Int, items: List<GroupExerciseCrossRef>) {
        if (index <= 0) return
        val ids = items.map { it.exercise.id }.toMutableList()
        val tmp = ids[index - 1]
        ids[index - 1] = ids[index]
        ids[index] = tmp
        viewModelScope.launch { groupRepository.reorder(groupId, ids) }
    }

    fun moveDown(index: Int, items: List<GroupExerciseCrossRef>) {
        if (index >= items.lastIndex) return
        val ids = items.map { it.exercise.id }.toMutableList()
        val tmp = ids[index + 1]
        ids[index + 1] = ids[index]
        ids[index] = tmp
        viewModelScope.launch { groupRepository.reorder(groupId, ids) }
    }
}

/**
 * Screen to rename a group and manage its exercise order.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupEditorScreen(
    onBack: () -> Unit,
    onAddExercises: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GroupEditorViewModel = hiltViewModel()
) {
    val group by viewModel.group.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    var name by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(group) {
        group?.let {
            name = it.name
            notes = it.notes.orEmpty()
        }
    }

    LaunchedEffect(name, notes) {
        if (group != null && (name != group?.name || notes != group?.notes.orEmpty())) {
            kotlinx.coroutines.delay(400)
            viewModel.rename(name, notes.ifBlank { null })
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_group)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { group?.let { onAddExercises(it.id) } }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_exercises))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.group_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.notes_optional)) },
                modifier = Modifier.fillMaxWidth()
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(exercises, key = { _, item -> item.exercise.id }) { index, item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .heightIn(min = 48.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.exercise.name,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.moveUp(index, exercises) },
                                enabled = index > 0
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Move up")
                            }
                            IconButton(
                                onClick = { viewModel.moveDown(index, exercises) },
                                enabled = index < exercises.lastIndex
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Move down")
                            }
                            IconButton(onClick = { viewModel.removeExercise(item.exercise.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                            }
                        }
                    }
                }
            }
        }
    }
}
