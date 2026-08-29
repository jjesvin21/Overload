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

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Exercise::class,
        WorkoutGroup::class,
        GroupExercise::class,
        WorkoutSession::class,
        SessionSet::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutGroupDao(): WorkoutGroupDao
    abstract fun workoutSessionDao(): WorkoutSessionDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE session_sets ADD COLUMN timeSeconds INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE session_sets ADD COLUMN count INTEGER DEFAULT NULL")
            }
        }
    }
}

