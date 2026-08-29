/**
 * Hilt module wiring Room database, DAOs, and singleton app services.
 */
package com.overloadtracker.di

import android.content.Context
import androidx.room.Room
import com.overloadtracker.data.local.db.AppDatabase
import com.overloadtracker.data.local.db.ExerciseDao
import com.overloadtracker.data.local.db.WorkoutGroupDao
import com.overloadtracker.data.local.db.WorkoutSessionDao
import com.overloadtracker.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, Constants.DB_NAME)
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()


    @Provides
    fun provideExerciseDao(db: AppDatabase): ExerciseDao = db.exerciseDao()

    @Provides
    fun provideWorkoutGroupDao(db: AppDatabase): WorkoutGroupDao = db.workoutGroupDao()

    @Provides
    fun provideWorkoutSessionDao(db: AppDatabase): WorkoutSessionDao = db.workoutSessionDao()
}
