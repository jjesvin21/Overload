/**
 * App-wide constants for assets, defaults, and filter chip labels.
 */
package com.overloadtracker.util

object Constants {
    const val EXERCISES_JSON_PATH = "data/exercises.json"
    const val DB_NAME = "overload_tracker.db"
    const val DEFAULT_REST_SECONDS = 90
    const val MIN_REST_SECONDS = 30
    const val MAX_REST_SECONDS = 300
    const val DRAFT_AUTOSAVE_MS = 30_000L
    const val SEARCH_DEBOUNCE_MS = 300L
    const val LB_PER_KG = 2.2046226218

    /** Display labels mapped to dataset category values (lowercase). */
    val BODY_PART_FILTERS = listOf(
        "Chest" to "chest",
        "Back" to "back",
        "Upper Arms" to "upper arms",
        "Shoulders" to "shoulders",
        "Upper Legs" to "upper legs",
        "Lower Legs" to "lower legs",
        "Waist" to "waist",
        "Lower Arms" to "lower arms",
        "Cardio" to "cardio",
        "Neck" to "neck"
    )
}
