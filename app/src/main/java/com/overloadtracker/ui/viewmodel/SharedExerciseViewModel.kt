/**
 * Shared exercise selection state for detail sheet across screens.
 */
package com.overloadtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.overloadtracker.data.local.entity.Exercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Holds the currently selected exercise for the shared detail bottom sheet.
 */
@HiltViewModel
class SharedExerciseViewModel @Inject constructor() : ViewModel() {

    private val _selectedExercise = MutableStateFlow<Exercise?>(null)
    val selectedExercise: StateFlow<Exercise?> = _selectedExercise.asStateFlow()

    fun showExercise(exercise: Exercise) {
        _selectedExercise.value = exercise
    }

    fun clearSelection() {
        _selectedExercise.value = null
    }
}
