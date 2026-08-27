/**
 * Multi-select exercise picker to add exercises to a workout group.
 */
package com.overloadtracker.ui.screens.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import com.overloadtracker.data.local.entity.Exercise
import com.overloadtracker.data.repository.ExerciseRepository
import com.overloadtracker.data.repository.WorkoutGroupRepository
import com.overloadtracker.ui.components.FilterChips
import com.overloadtracker.ui.navigation.AddExercisesRoute
import com.overloadtracker.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddExercisesViewModel @Inject constructor(
    savedStateHandle: androidx.lifecycle.SavedStateHandle,
    private val exerciseRepository: ExerciseRepository,
    private val groupRepository: WorkoutGroupRepository
) : ViewModel() {

    private val groupId = savedStateHandle.toRoute<AddExercisesRoute>().groupId

    private val _query = MutableStateFlow("")
    private val _categories = MutableStateFlow<Set<String>>(emptySet())
    private val _selected = MutableStateFlow<Set<String>>(emptySet())

    val selected: StateFlow<Set<String>> = _selected.asStateFlow()
    val categories: StateFlow<Set<String>> = _categories.asStateFlow()

    val exercises: StateFlow<List<Exercise>> = combine(
        _query.debounce(Constants.SEARCH_DEBOUNCE_MS),
        _categories
    ) { q, cats -> q to cats }
        .flatMapLatest { (q, cats) ->
            exerciseRepository.observeFiltered(q, cats, null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) {
        _query.value = value
    }

    fun toggleCategory(category: String) {
        _categories.update { if (category in it) it - category else it + category }
    }

    fun toggleSelection(exerciseId: String) {
        _selected.update { if (exerciseId in it) it - exerciseId else it + exerciseId }
    }

    fun addSelected(onDone: () -> Unit) {
        viewModelScope.launch {
            val ids = _selected.value.toList()
            if (ids.isNotEmpty()) {
                groupRepository.addExercises(groupId, ids)
            }
            onDone()
        }
    }
}

/**
 * Multi-select library screen with bottom bar confirmation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExercisesToGroupScreen(
    onBack: () -> Unit,
    onAdded: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddExercisesViewModel = hiltViewModel()
) {
    val exercises by viewModel.exercises.collectAsState()
    val selected by viewModel.selected.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var query by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_exercises)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = { viewModel.addSelected(onAdded) },
                enabled = selected.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(min = 48.dp)
            ) {
                Text(stringResource(R.string.add_selected, selected.size))
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 280.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = {
                            query = it
                            viewModel.setQuery(it)
                        },
                        label = { Text(stringResource(R.string.search_exercises)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    FilterChips(
                        selectedCategories = categories,
                        onToggle = viewModel::toggleCategory
                    )
                }
                items(exercises, key = { it.id }) { exercise ->
                    SelectableExerciseRow(
                        exercise = exercise,
                        checked = exercise.id in selected,
                        onToggle = { viewModel.toggleSelection(exercise.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectableExerciseRow(
    exercise: Exercise,
    checked: Boolean,
    onToggle: () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onToggle
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Checkbox(checked = checked, onCheckedChange = { onToggle() })
            Text(
                text = exercise.name,
                modifier = Modifier.padding(start = 48.dp)
            )
        }
    }
}
