/**
 * ViewModel for the exercise library with debounced search and filters.
 */
package com.overloadtracker.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.overloadtracker.data.local.entity.Exercise
import com.overloadtracker.data.local.entity.WorkoutGroup
import com.overloadtracker.data.repository.ExerciseRepository
import com.overloadtracker.data.repository.WorkoutGroupRepository
import com.overloadtracker.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
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

data class ExerciseLibraryUiState(
    val exercises: List<Exercise> = emptyList(),
    val equipmentOptions: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val query: String = "",
    val selectedCategories: Set<String> = emptySet(),
    val selectedEquipment: String? = null
)

/**
 * Manages search, category multi-select, and equipment filtering for the library.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExerciseLibraryViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val groupRepository: WorkoutGroupRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _categories = MutableStateFlow<Set<String>>(emptySet())
    private val _equipment = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(true)

    val query: StateFlow<String> = _query.asStateFlow()
    val selectedCategories: StateFlow<Set<String>> = _categories.asStateFlow()
    val selectedEquipment: StateFlow<String?> = _equipment.asStateFlow()

    val equipmentOptions: StateFlow<List<String>> = exerciseRepository.observeEquipment()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val groups: StateFlow<List<WorkoutGroup>> = groupRepository.observeGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _debouncedQuery = _query.debounce { query ->
        if (query.isEmpty()) 0L else Constants.SEARCH_DEBOUNCE_MS
    }

    private val _filteredExercises: Flow<List<Exercise>> = combine(
        _debouncedQuery,
        _categories,
        _equipment
    ) { debouncedQuery, categories, equipment ->
        Triple(debouncedQuery, categories, equipment)
    }.flatMapLatest { (debouncedQuery, categories, equipment) ->
        exerciseRepository.observeFiltered(
            query = debouncedQuery,
            categories = categories,
            equipment = equipment
        )
    }

    val uiState: StateFlow<ExerciseLibraryUiState> = combine(
        _query,
        _categories,
        combine(_equipment, equipmentOptions) { eq, opts -> eq to opts },
        _isLoading,
        _filteredExercises
    ) { query, categories, (equipment, equipmentList), loading, exercises ->
        ExerciseLibraryUiState(
            query = query,
            selectedCategories = categories,
            selectedEquipment = equipment,
            isLoading = loading,
            equipmentOptions = equipmentList,
            exercises = exercises
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ExerciseLibraryUiState(isLoading = true)
    )

    init {
        viewModelScope.launch {
            exerciseRepository.seedIfNeeded()
            _isLoading.value = false
        }
    }

    fun setQuery(value: String) {
        _query.value = value
    }

    fun toggleCategory(category: String) {
        _categories.update { current ->
            if (category in current) current - category else current + category
        }
    }

    fun setEquipment(value: String?) {
        _equipment.value = value?.takeIf { it.isNotBlank() }
    }

    fun addExerciseToGroup(groupId: Long, exerciseId: String) {
        viewModelScope.launch {
            groupRepository.addExercises(groupId, listOf(exerciseId))
        }
    }
}
