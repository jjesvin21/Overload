package com.overloadtracker.data.manager

import com.overloadtracker.ui.screens.workout.LiveExercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class ActiveWorkoutSession(
    val groupId: Long,
    val groupName: String,
    val startTime: Long,
    val exercises: List<LiveExercise> = emptyList()
)

@Singleton
class ActiveSessionManager @Inject constructor() {
    private val _activeSession = MutableStateFlow<ActiveWorkoutSession?>(null)
    val activeSession: StateFlow<ActiveWorkoutSession?> = _activeSession.asStateFlow()

    fun startSession(groupId: Long, groupName: String, startTime: Long = System.currentTimeMillis()) {
        _activeSession.value = ActiveWorkoutSession(
            groupId = groupId,
            groupName = groupName,
            startTime = startTime
        )
    }

    fun updateExercises(exercises: List<LiveExercise>) {
        _activeSession.value = _activeSession.value?.copy(exercises = exercises)
    }

    fun updateGroupName(groupName: String) {
        _activeSession.value = _activeSession.value?.copy(groupName = groupName)
    }

    fun clearSession() {
        _activeSession.value = null
    }
}
