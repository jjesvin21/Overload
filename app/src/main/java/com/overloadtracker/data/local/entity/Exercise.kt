/**
 * Seeded exercise entity — read-only for the user; populated from assets/data/exercises.json.
 */
package com.overloadtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val equipment: String,
    val target: String,
    val muscleGroup: String,
    /** JSON array stored as string, e.g. ["triceps","chest"] */
    val secondaryMuscles: String,
    val instructions: String,
    /** Relative asset path, e.g. images/0001-xxx.jpg */
    val imagePath: String,
    /** Relative asset path, e.g. videos/0001-xxx.gif */
    val gifPath: String
)
