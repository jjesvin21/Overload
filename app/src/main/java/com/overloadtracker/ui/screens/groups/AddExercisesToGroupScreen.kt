/**
 * Multi-select exercise picker to add exercises to a workout group.
 * Refactored with Liquid Glass / Liquid Vitality visual aesthetic.
 */
package com.overloadtracker.ui.screens.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.overloadtracker.ui.components.ExerciseCard
import com.overloadtracker.ui.components.FilterChips
import com.overloadtracker.ui.components.LiquidPrimaryButton
import com.overloadtracker.ui.components.LiquidSearchField
import com.overloadtracker.ui.components.LiquidTopAppBar
import com.overloadtracker.ui.navigation.AddExercisesRoute
import com.overloadtracker.ui.theme.MidnightBackground
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
 * Multi-select library screen with bottom bar confirmation in Liquid Glass style.
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
        modifier = modifier.background(MidnightBackground),
        containerColor = MidnightBackground,
        topBar = {
            LiquidTopAppBar(
                title = "ADD TO ROUTINE",
                subtitle = "MULTI-SELECT EXERCISES",
                onBackClick = onBack
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                LiquidPrimaryButton(
                    text = stringResource(R.string.add_selected, selected.size),
                    onClick = { viewModel.addSelected(onAdded) },
                    enabled = selected.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LiquidSearchField(
                            query = query,
                            onQueryChange = {
                                query = it
                                viewModel.setQuery(it)
                            },
                            placeholder = stringResource(R.string.search_exercises),
                            modifier = Modifier.fillMaxWidth()
                        )
                        FilterChips(
                            selectedCategories = categories,
                            onToggle = viewModel::toggleCategory
                        )
                    }
                }
                items(exercises, key = { it.id }) { exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        selected = exercise.id in selected,
                        onClick = { viewModel.toggleSelection(exercise.id) }
                    )
                }
            }
        }
    }
}
