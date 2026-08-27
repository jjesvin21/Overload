/**
 * Room database hosting the offline Progressive Overload Tracker schema.
 */
package com.overloadtracker.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.overloadtracker.data.local.entity.Exercise
import com.overloadtracker.data.local.entity.GroupExercise
import com.overloadtracker.data.local.entity.SessionSet
import com.overloadtracker.data.local.entity.WorkoutGroup
import com.overloadtracker.data.local.entity.WorkoutSession

@Database(
    entities = [
        Exercise::class,
        WorkoutGroup::class,
        GroupExercise::class,
        WorkoutSession::class,
        SessionSet::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutGroupDao(): WorkoutGroupDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
}
