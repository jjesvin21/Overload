/**
 * One completed (or historically finished) workout session.
 */
package com.overloadtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long?,
    val groupName: String,
    val startTime: Long,
    val endTime: Long,
    val totalVolume: Double,
    val notes: String? = null
)
