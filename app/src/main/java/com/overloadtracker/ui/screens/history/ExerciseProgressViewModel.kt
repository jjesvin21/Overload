/**
 * ViewModel for exercise weight progress chart.
 */
package com.overloadtracker.ui.screens.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.overloadtracker.data.model.ExerciseProgressPoint
import com.overloadtracker.data.repository.ExerciseRepository
import com.overloadtracker.data.repository.WorkoutSessionRepository
import com.overloadtracker.ui.navigation.ExerciseProgressRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ExerciseProgressViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    exerciseRepository: ExerciseRepository,
    sessionRepository: WorkoutSessionRepository
) : ViewModel() {

    private val exerciseId = savedStateHandle.toRoute<ExerciseProgressRoute>().exerciseId

    val exerciseName: StateFlow<String?> = exerciseRepository.observeById(exerciseId)
        .map { it?.name }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val progress: StateFlow<List<ExerciseProgressPoint>> =
        sessionRepository.observeProgress(exerciseId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
