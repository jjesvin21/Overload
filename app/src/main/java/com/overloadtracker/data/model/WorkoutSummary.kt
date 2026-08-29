/**
 * Lightweight query projections used by analytics and previous-best chips.
 */
package com.overloadtracker.data.model

data class PreviousSetInfo(
    val weight: Double,
    val reps: Int,
    val timeSeconds: Int? = null,
    val count: Int? = null
)

data class ExerciseProgressPoint(
    val dateMillis: Long,
    val maxWeight: Double,
    val maxVolume: Double
)

data class WorkoutSummary(
    val sessionId: Long,
    val groupName: String,
    val startTime: Long,
    val endTime: Long,
    val totalVolume: Double,
    val muscleGroups: List<String>,
    val exerciseCount: Int,
    val setCount: Int
)

data class CsvExportRow(
    val date: String,
    val workoutName: String,
    val muscleGroups: String,
    val exerciseName: String,
    val equipment: String,
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val timeSeconds: Int? = null,
    val count: Int? = null,
    val rpe: Int?,
    val totalVolume: Double,
    val notes: String
)

data class PastExerciseSetLog(
    val sessionId: Long,
    val sessionDateMillis: Long,
    val groupName: String,
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
    val timeSeconds: Int? = null,
    val count: Int? = null,
    val rpe: Int? = null
)

data class ExerciseHistorySessionGroup(
    val sessionId: Long,
    val sessionDateMillis: Long,
    val groupName: String,
    val sets: List<PastExerciseSetLog>
)

data class ExerciseDetailWithHistory(
    val exercise: com.overloadtracker.data.local.entity.Exercise,
    val prWeightKg: Double?,
    val pastSessions: List<ExerciseHistorySessionGroup>
)

