/**
 * Individual set logged during a [WorkoutSession].
 */
package com.overloadtracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "session_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId"), Index("exerciseId")]
)
data class SessionSet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: String,
    val exerciseName: String,
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
    val rpe: Int? = null,
    val isCompleted: Boolean = true,
    val restSeconds: Int? = null,
    val timeSeconds: Int? = null,
    val count: Int? = null
)

