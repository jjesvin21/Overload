/**
 * Type-safe navigation routes for Navigation Compose 2.8.
 */
package com.overloadtracker.ui.navigation

import kotlinx.serialization.Serializable

@Serializable data object GroupsRoute

@Serializable data object LibraryRoute

@Serializable data object HistoryRoute

@Serializable data object SettingsRoute

@Serializable data object OnboardingRoute

@Serializable data class GroupEditorRoute(val groupId: Long)

@Serializable data class AddExercisesRoute(val groupId: Long)

@Serializable data class LiveWorkoutRoute(val groupId: Long)

@Serializable data class SessionDetailRoute(val sessionId: Long)

@Serializable data class ExerciseProgressRoute(val exerciseId: String)
