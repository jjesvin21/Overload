package com.overloadtracker.ui.screens.library

import com.overloadtracker.data.local.entity.Exercise
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseLibraryViewModelTest {

    @Test
    fun `ExerciseLibraryUiState defaults to loading state`() {
        val uiState = ExerciseLibraryUiState()

        assertTrue(uiState.isLoading)
        assertEquals("", uiState.query)
        assertTrue(uiState.exercises.isEmpty())
        assertTrue(uiState.equipmentOptions.isEmpty())
        assertTrue(uiState.selectedCategories.isEmpty())
        assertNull(uiState.selectedEquipment)
    }

    @Test
    fun `ExerciseLibraryUiState copy updates query and equipment correctly`() {
        val initial = ExerciseLibraryUiState()
        val dummyExercise = Exercise(
            id = "0001",
            name = "Barbell Bench Press",
            category = "chest",
            equipment = "barbell",
            target = "pectorals",
            muscleGroup = "chest",
            secondaryMuscles = "[\"triceps\"]",
            instructions = "Push up",
            imagePath = "",
            gifPath = ""
        )

        val updated = initial.copy(
            query = "bench",
            selectedCategories = setOf("chest"),
            selectedEquipment = "barbell",
            isLoading = false,
            exercises = listOf(dummyExercise)
        )

        assertFalse(updated.isLoading)
        assertEquals("bench", updated.query)
        assertEquals(setOf("chest"), updated.selectedCategories)
        assertEquals("barbell", updated.selectedEquipment)
        assertEquals(1, updated.exercises.size)
        assertEquals("Barbell Bench Press", updated.exercises.first().name)
    }
}
