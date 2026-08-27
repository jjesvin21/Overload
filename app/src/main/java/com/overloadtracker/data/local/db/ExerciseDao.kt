/**
 * Data-access object for the seeded [Exercise] catalog.
 */
package com.overloadtracker.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.overloadtracker.data.local.entity.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAll(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Exercise?

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<Exercise?>

    @Query(
        """
        SELECT * FROM exercises
        WHERE (:query = '' OR name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR target LIKE '%' || :query || '%' OR muscleGroup LIKE '%' || :query || '%' OR equipment LIKE '%' || :query || '%')
          AND (:categoryCount = 0 OR category IN (:categories))
          AND (:equipment = '' OR equipment = :equipment)
        ORDER BY name ASC
        """
    )
    fun searchFiltered(
        query: String,
        categories: List<String>,
        categoryCount: Int,
        equipment: String
    ): Flow<List<Exercise>>

    @Query("SELECT DISTINCT equipment FROM exercises ORDER BY equipment ASC")
    fun getEquipmentList(): Flow<List<String>>

    @Query("SELECT DISTINCT category FROM exercises ORDER BY category ASC")
    fun getCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<Exercise>)

    @Query("DELETE FROM exercises")
    suspend fun clearAll()
}
