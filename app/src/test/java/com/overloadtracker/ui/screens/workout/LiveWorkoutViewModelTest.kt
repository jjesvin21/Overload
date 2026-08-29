package com.overloadtracker.ui.screens.workout

import com.overloadtracker.data.local.entity.Exercise
import com.overloadtracker.data.model.ExerciseDetailWithHistory
import com.overloadtracker.data.model.ExerciseHistorySessionGroup
import com.overloadtracker.data.model.PastExerciseSetLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LiveWorkoutViewModelTest {

    @Test
    fun `LiveWorkoutUiState initial state has no selected exercise detail`() {
        val state = LiveWorkoutUiState(groupId = 100L)
        assertNull(state.selectedExerciseDetail)
    }

    @Test
    fun `LiveWorkoutUiState copy with selectedExerciseDetail retains detail and history`() {
        val dummyExercise = Exercise(
            id = "bench_press",
            name = "Barbell Bench Press",
            category = "chest",
            equipment = "barbell",
            target = "pectorals",
            muscleGroup = "chest",
            secondaryMuscles = "[\"triceps\"]",
            instructions = "Lie on bench and push bar upward",
            imagePath = "",
            gifPath = ""
        )

        val pastSet = PastExerciseSetLog(
            sessionId = 1L,
            sessionDateMillis = 1700000000000L,
            groupName = "Push Day",
            setNumber = 1,
            weight = 80.0,
            reps = 8,
            rpe = 8
        )

        val historyGroup = ExerciseHistorySessionGroup(
            sessionId = 1L,
            sessionDateMillis = 1700000000000L,
            groupName = "Push Day",
            sets = listOf(pastSet)
        )

        val detailWithHistory = ExerciseDetailWithHistory(
            exercise = dummyExercise,
            prWeightKg = 90.0,
            pastSessions = listOf(historyGroup)
        )

        val state = LiveWorkoutUiState(groupId = 100L, selectedExerciseDetail = detailWithHistory)

        assertEquals("bench_press", state.selectedExerciseDetail?.exercise?.id)
        assertEquals(90.0, state.selectedExerciseDetail?.prWeightKg ?: 0.0, 0.001)
        assertEquals(1, state.selectedExerciseDetail?.pastSessions?.size ?: 0)
        assertEquals(80.0, state.selectedExerciseDetail?.pastSessions?.first()?.sets?.first()?.weight ?: 0.0, 0.001)
        assertEquals(Integer.valueOf(8), state.selectedExerciseDetail?.pastSessions?.first()?.sets?.first()?.rpe)
    }
}
