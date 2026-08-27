/**
 * Ordered junction between a [WorkoutGroup] and an [Exercise].
 */
package com.overloadtracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "group_exercises",
    primaryKeys = ["groupId", "exerciseId"],
    foreignKeys = [
        ForeignKey(
            entity = WorkoutGroup::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exerciseId"), Index("groupId")]
)
data class GroupExercise(
    val groupId: Long,
    val exerciseId: String,
    val sortOrder: Int
)
