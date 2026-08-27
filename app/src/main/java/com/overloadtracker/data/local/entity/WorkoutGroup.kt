/**
 * User-created workout group template (e.g. "Push A", "Leg Day").
 */
package com.overloadtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_groups")
data class WorkoutGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
