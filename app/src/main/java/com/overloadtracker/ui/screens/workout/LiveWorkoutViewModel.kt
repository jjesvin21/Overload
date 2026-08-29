/**
 * ViewModel for an active live workout session.
 */
package com.overloadtracker.ui.screens.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.overloadtracker.ui.navigation.LiveWorkoutRoute
import com.overloadtracker.data.local.entity.Exercise
import com.overloadtracker.data.local.entity.GroupExerciseCrossRef
import com.overloadtracker.data.preferences.UserPreferencesRepository
import com.overloadtracker.data.repository.ExerciseRepository
import com.overloadtracker.data.repository.SessionSetDraft
import com.overloadtracker.data.repository.WorkoutGroupRepository
import com.overloadtracker.data.repository.WorkoutSessionRepository
import com.overloadtracker.util.WeightUnit
import com.overloadtracker.util.WeightUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.overloadtracker.util.formatCardioDisplay
import com.overloadtracker.util.formatSecondsToDisplay
import com.overloadtracker.util.parseTimeDisplayToSeconds

/** In-memory set during a live workout. */
data class LiveSet(
    val setNumber: Int,
    val weightKg: Double = 0.0,
    val reps: Int = 0,
    val timeSeconds: Int? = null,
    val count: Int? = null,
    val rpe: Int? = null,
    val isCompleted: Boolean = false,
    val weightDisplay: String = "",
    val repsDisplay: String = "",
    val timeDisplay: String = "",
    val countDisplay: String = ""
)

/** In-memory exercise block during a live workout. */
data class LiveExercise(
    val exerciseId: String,
    val exerciseName: String,
    val isCardio: Boolean = false,
    val sets: List<LiveSet>,
    val expanded: Boolean = true,
    val prevBestLabel: String? = null
)

data class LiveWorkoutUiState(
    val groupId: Long,
    val groupName: String = "",
    val exercises: List<LiveExercise> = emptyList(),
    val elapsedMillis: Long = 0L,
    val restSecondsRemaining: Int? = null,
    val restTotalSeconds: Int = 90,
    val isFinishing: Boolean = false,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val defaultRestSeconds: Int = 90,
    val validationMessage: String? = null,
    val selectedExerciseDetail: com.overloadtracker.data.model.ExerciseDetailWithHistory? = null
)

/**
 * Manages live workout state, rest timer, and session persistence.
 */
@HiltViewModel
class LiveWorkoutViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: WorkoutGroupRepository,
    private val exerciseRepository: ExerciseRepository,
    private val sessionRepository: WorkoutSessionRepository,
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    private val groupId: Long = savedStateHandle.toRoute<LiveWorkoutRoute>().groupId
    private val startTime = System.currentTimeMillis()

    private val _uiState = MutableStateFlow(
        LiveWorkoutUiState(groupId = groupId)
    )
    val uiState: StateFlow<LiveWorkoutUiState> = _uiState.asStateFlow()

    val weightUnit: StateFlow<WeightUnit> = prefs.weightUnit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeightUnit.KG)

    init {
        viewModelScope.launch {
            val unit = prefs.weightUnit.first()
            val rest = prefs.defaultRestSeconds.first()
            _uiState.update { it.copy(weightUnit = unit, defaultRestSeconds = rest, restTotalSeconds = rest) }

            val group = groupRepository.getGroup(groupId)
            _uiState.update { it.copy(groupName = group?.name.orEmpty()) }

            groupRepository.observeGroupExercises(groupId).collect { refs ->
                loadExercises(refs, unit)
            }
        }
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1_000)
                _uiState.update {
                    it.copy(elapsedMillis = System.currentTimeMillis() - startTime)
                }
                tickRestTimer()
            }
        }
    }

    private suspend fun loadExercises(refs: List<GroupExerciseCrossRef>, unit: WeightUnit) {
        val current = _uiState.value.exercises.associateBy { it.exerciseId }
        val exercises = refs.mapNotNull { ref ->
            val ex = ref.exercise ?: return@mapNotNull null
            val existing = current[ex.id]
            if (existing != null) {
                existing
            } else {
                val isCardio = ex.category.equals("cardio", ignoreCase = true)
                val last = sessionRepository.getLastSet(ex.id)
                val prevLabel = last?.let { info ->
                    if (isCardio) {
                        formatCardioDisplay(info.timeSeconds, info.count)
                    } else {
                        val w = WeightUtils.formatWeight(info.weight, unit)
                        "$w × ${info.reps}"
                    }
                }
                LiveExercise(
                    exerciseId = ex.id,
                    exerciseName = ex.name,
                    isCardio = isCardio,
                    sets = (1..3).map { defaultSet(it, last, unit, isCardio) },
                    prevBestLabel = prevLabel
                )
            }
        }
        _uiState.update { it.copy(exercises = exercises) }
    }


    private fun defaultSet(
        number: Int,
        last: com.overloadtracker.data.model.PreviousSetInfo?,
        unit: WeightUnit,
        isCardio: Boolean = false
    ): LiveSet {
        if (isCardio) {
            val timeSec = last?.timeSeconds
            val cnt = last?.count
            return LiveSet(
                setNumber = number,
                timeSeconds = timeSec,
                count = cnt,
                timeDisplay = formatSecondsToDisplay(timeSec),
                countDisplay = if (cnt != null && cnt > 0) cnt.toString() else ""
            )
        } else {
            val kg = last?.weight ?: 0.0
            val reps = last?.reps ?: 0
            return LiveSet(
                setNumber = number,
                weightKg = kg,
                reps = reps,
                weightDisplay = if (kg > 0) WeightUtils.formatWeightNumber(kg, unit) else "",
                repsDisplay = if (reps > 0) reps.toString() else ""
            )
        }
    }

    private fun tickRestTimer() {
        val remaining = _uiState.value.restSecondsRemaining ?: return
        if (remaining <= 1) {
            _uiState.update { it.copy(restSecondsRemaining = null) }
        } else {
            _uiState.update { it.copy(restSecondsRemaining = remaining - 1) }
        }
    }

    fun toggleExpanded(exerciseId: String) {
        _uiState.update { state ->
            state.copy(
                exercises = state.exercises.map {
                    if (it.exerciseId == exerciseId) it.copy(expanded = !it.expanded) else it
                }
            )
        }
    }

    fun updateWeight(exerciseId: String, setNumber: Int, display: String) {
        val unit = _uiState.value.weightUnit
        val kg = display.toDoubleOrNull()?.let { WeightUtils.displayToKg(it, unit) } ?: 0.0
        updateSet(exerciseId, setNumber) { it.copy(weightDisplay = display, weightKg = kg) }
    }

    fun updateReps(exerciseId: String, setNumber: Int, display: String) {
        val reps = display.toIntOrNull() ?: 0
        updateSet(exerciseId, setNumber) { it.copy(repsDisplay = display, reps = reps) }
    }

    fun updateTime(exerciseId: String, setNumber: Int, display: String) {
        val seconds = parseTimeDisplayToSeconds(display)
        updateSet(exerciseId, setNumber) { it.copy(timeDisplay = display, timeSeconds = seconds) }
    }

    fun updateCount(exerciseId: String, setNumber: Int, display: String) {
        val count = display.toIntOrNull()
        updateSet(exerciseId, setNumber) { it.copy(countDisplay = display, count = count) }
    }

    fun updateRpe(exerciseId: String, setNumber: Int, rpe: Int?) {
        updateSet(exerciseId, setNumber) { it.copy(rpe = rpe) }
    }

    fun toggleSetComplete(exerciseId: String, setNumber: Int, completed: Boolean) {
        val exercise = _uiState.value.exercises.firstOrNull { it.exerciseId == exerciseId }
        val set = exercise?.sets?.firstOrNull { it.setNumber == setNumber }
        if (completed) {
            if (set == null) return
            if (exercise.isCardio) {
                val hasTime = (set.timeSeconds != null && set.timeSeconds > 0)
                val hasCount = (set.count != null && set.count > 0)
                if (!hasTime && !hasCount) {
                    _uiState.update { it.copy(validationMessage = "Enter time or count before completing a set.") }
                    return
                }
            } else {
                if (set.weightKg <= 0.0 || set.reps <= 0) {
                    _uiState.update { it.copy(validationMessage = "Enter a weight and reps before completing a set.") }
                    return
                }
            }
        }
        updateSet(exerciseId, setNumber) { it.copy(isCompleted = completed) }
        if (completed) {
            _uiState.update {
                it.copy(
                    restSecondsRemaining = it.defaultRestSeconds,
                    restTotalSeconds = it.defaultRestSeconds
                )
            }
        }
    }

    private fun updateSet(exerciseId: String, setNumber: Int, transform: (LiveSet) -> LiveSet) {
        _uiState.update { state ->
            state.copy(
                exercises = state.exercises.map { ex ->
                    if (ex.exerciseId != exerciseId) ex
                    else ex.copy(
                        sets = ex.sets.map { set ->
                            if (set.setNumber == setNumber) transform(set) else set
                        }
                    )
                }
            )
        }
    }

    fun addSet(exerciseId: String) {
        _uiState.update { state ->
            state.copy(
                exercises = state.exercises.map { ex ->
                    if (ex.exerciseId != exerciseId) ex
                    else {
                        val next = ex.sets.size + 1
                        val last = ex.sets.lastOrNull()
                        ex.copy(
                            sets = ex.sets + LiveSet(
                                setNumber = next,
                                weightKg = last?.weightKg ?: 0.0,
                                reps = last?.reps ?: 0,
                                timeSeconds = last?.timeSeconds,
                                count = last?.count,
                                weightDisplay = last?.weightDisplay.orEmpty(),
                                repsDisplay = last?.repsDisplay.orEmpty(),
                                timeDisplay = last?.timeDisplay.orEmpty(),
                                countDisplay = last?.countDisplay.orEmpty()
                            )
                        )
                    }
                }
            )
        }
    }

    fun addExercise(exercise: Exercise) {
        if (_uiState.value.exercises.any { it.exerciseId == exercise.id }) return
        viewModelScope.launch {
            val unit = _uiState.value.weightUnit
            val isCardio = exercise.category.equals("cardio", ignoreCase = true)
            val last = sessionRepository.getLastSet(exercise.id)
            val prevLabel = last?.let { info ->
                if (isCardio) {
                    formatCardioDisplay(info.timeSeconds, info.count)
                } else {
                    "${WeightUtils.formatWeight(info.weight, unit)} × ${info.reps}"
                }
            }
            _uiState.update { state ->
                state.copy(
                    exercises = state.exercises + LiveExercise(
                        exerciseId = exercise.id,
                        exerciseName = exercise.name,
                        isCardio = isCardio,
                        sets = (1..3).map { defaultSet(it, last, unit, isCardio) },
                        prevBestLabel = prevLabel
                    )
                )
            }
        }
    }

    fun skipRest() {
        _uiState.update { it.copy(restSecondsRemaining = null) }
    }

    fun addRest30() {
        _uiState.update { state ->
            val current = state.restSecondsRemaining ?: state.defaultRestSeconds
            state.copy(restSecondsRemaining = current + 30, restTotalSeconds = current + 30)
        }
    }

    fun resetRest() {
        _uiState.update { state ->
            state.copy(
                restSecondsRemaining = state.defaultRestSeconds,
                restTotalSeconds = state.defaultRestSeconds
            )
        }
    }

    fun clearValidationMessage() {
        _uiState.update { it.copy(validationMessage = null) }
    }

    fun selectExerciseDetail(exerciseId: String) {
        viewModelScope.launch {
            val detail = sessionRepository.getExerciseDetailWithHistory(exerciseId)
            _uiState.update { it.copy(selectedExerciseDetail = detail) }
        }
    }

    fun dismissExerciseDetail() {
        _uiState.update { it.copy(selectedExerciseDetail = null) }
    }

    fun finishWorkout(onComplete: (Long) -> Unit) {
        val hasCompletedSet = _uiState.value.exercises.any { exercise ->
            exercise.sets.any { set ->
                if (!set.isCompleted) false
                else if (exercise.isCardio) {
                    (set.timeSeconds != null && set.timeSeconds > 0) || (set.count != null && set.count > 0)
                } else {
                    set.weightKg > 0.0 && set.reps > 0
                }
            }
        }
        if (!hasCompletedSet) {
            _uiState.update { it.copy(validationMessage = "Complete at least one set before finishing your workout.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isFinishing = true) }
            val state = _uiState.value
            val drafts = state.exercises.flatMap { ex ->
                ex.sets.map { set ->
                    SessionSetDraft(
                        exerciseId = ex.exerciseId,
                        exerciseName = ex.exerciseName,
                        setNumber = set.setNumber,
                        weight = set.weightKg,
                        reps = set.reps,
                        timeSeconds = set.timeSeconds,
                        count = set.count,
                        rpe = set.rpe,
                        isCompleted = set.isCompleted,
                        restSeconds = null
                    )
                }
            }
            val sessionId = sessionRepository.finishWorkout(
                groupId = groupId,
                groupName = state.groupName,
                startTime = startTime,
                endTime = System.currentTimeMillis(),
                sets = drafts
            )
            _uiState.update { it.copy(isFinishing = false) }
            onComplete(sessionId)
        }
    }
}

