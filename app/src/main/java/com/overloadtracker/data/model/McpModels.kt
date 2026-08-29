package com.overloadtracker.data.model

import kotlinx.serialization.Serializable

@Serializable
data class McpStatusResponse(
    val status: String = "ONLINE",
    val appName: String = "Overload Tracker",
    val version: String = "1.0.0",
    val activeIpAddress: String? = null,
    val port: Int = 8080
)

@Serializable
data class McpWorkoutHistoryResponse(
    val totalSessions: Int,
    val totalVolumeKg: Double,
    val muscleGroupBreakdown: Map<String, Double>,
    val sessions: List<McpWorkoutSessionDto>
)

@Serializable
data class McpWorkoutSessionDto(
    val sessionId: Long,
    val groupName: String,
    val startTime: Long,
    val endTime: Long,
    val totalVolume: Double,
    val muscleGroups: List<String>,
    val sets: List<McpSessionSetDto>
)

@Serializable
data class McpSessionSetDto(
    val exerciseId: String,
    val exerciseName: String,
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
    val timeSeconds: Int? = null,
    val count: Int? = null,
    val rpe: Int? = null,
    val restSeconds: Int? = null
)


@Serializable
data class McpExerciseDto(
    val id: String,
    val name: String,
    val category: String,
    val equipment: String,
    val instructions: String? = null
)

@Serializable
data class McpSplitDto(
    val id: Long,
    val name: String,
    val notes: String? = null,
    val exercises: List<McpExerciseDto>
)

@Serializable
data class McpReplaceSplitsRequest(
    val confirmReplace: Boolean = true,
    val splits: List<McpNewSplitDto>
)

@Serializable
data class McpNewSplitDto(
    val name: String,
    val notes: String? = null,
    val exerciseIds: List<String>
)

@Serializable
data class McpReplaceSplitsResponse(
    val status: String,
    val message: String,
    val createdSplitsCount: Int,
    val newSplitIds: List<Long>
)

@Serializable
data class McpAuthRequest(
    val masterSecret: String? = null
)

@Serializable
data class McpAuthResponse(
    val status: String = "AUTHENTICATED",
    val sessionToken: String,
    val expiresInSeconds: Long = 3600
)
