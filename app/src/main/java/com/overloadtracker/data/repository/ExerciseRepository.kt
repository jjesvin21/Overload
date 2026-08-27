/**
 * Repository for the offline exercise catalog (search, filter, seeding).
 */
package com.overloadtracker.data.repository

import android.content.Context
import com.overloadtracker.data.local.db.ExerciseDao
import com.overloadtracker.data.local.entity.Exercise
import com.overloadtracker.data.preferences.UserPreferencesRepository
import com.overloadtracker.util.JsonSeeder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepository @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val prefs: UserPreferencesRepository,
    @ApplicationContext private val context: Context
) {
    fun observeFiltered(
        query: String,
        categories: Set<String>,
        equipment: String?
    ): Flow<List<Exercise>> {
        val cats = categories.toList().ifEmpty { listOf("__none__") }
        return exerciseDao.searchFiltered(
            query = query.trim(),
            categories = cats,
            categoryCount = categories.size,
            equipment = equipment.orEmpty()
        )
    }

    fun observeAll(): Flow<List<Exercise>> = exerciseDao.getAll()

    fun observeEquipment(): Flow<List<String>> = exerciseDao.getEquipmentList()

    suspend fun getById(id: String): Exercise? = exerciseDao.getById(id)

    fun observeById(id: String): Flow<Exercise?> = exerciseDao.observeById(id)

    /**
     * Seeds the database from assets when empty or when [force] is true.
     *
     * @return number of exercises inserted.
     */
    suspend fun seedIfNeeded(force: Boolean = false): Int = withContext(Dispatchers.IO) {
        val count = exerciseDao.count()
        val marked = prefs.isSeeded.first()
        if (!force && count > 0 && marked) return@withContext count
        if (force) exerciseDao.clearAll()
        val exercises = JsonSeeder.loadExercisesFromAssets(context)
        // Batch insert in chunks to keep memory stable
        exercises.chunked(200).forEach { exerciseDao.insertAll(it) }
        prefs.setSeeded(true)
        exerciseDao.count()
    }

    suspend fun resetDatabase(): Int = seedIfNeeded(force = true)
}
