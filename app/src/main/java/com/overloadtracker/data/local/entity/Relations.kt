/**
 * Room relation wrappers used by DAOs and repositories.
 */
package com.overloadtracker.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class GroupWithExercises(
    @Embedded val group: WorkoutGroup,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = GroupExercise::class,
            parentColumn = "groupId",
            entityColumn = "exerciseId"
        )
    )
    val exercises: List<Exercise>
)

data class GroupExerciseCrossRef(
    @Embedded val crossRef: GroupExercise,
    @Relation(parentColumn = "exerciseId", entityColumn = "id")
    val exercise: Exercise? = null
)


data class SessionWithSets(
    @Embedded val session: WorkoutSession,
    @Relation(parentColumn = "id", entityColumn = "sessionId")
    val sets: List<SessionSet>
)
