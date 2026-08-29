/**
 * CRUD for workout groups and their ordered exercises.
 */
package com.overloadtracker.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.overloadtracker.data.local.entity.GroupExercise
import com.overloadtracker.data.local.entity.GroupExerciseCrossRef
import com.overloadtracker.data.local.entity.WorkoutGroup
import kotlinx.coroutines.flow.Flow

data class GroupExerciseCount(
    val groupId: Long,
    val count: Int
)

@Dao
interface WorkoutGroupDao {

    @Query("SELECT groupId, COUNT(*) as count FROM group_exercises GROUP BY groupId")
    fun observeGroupExerciseCounts(): Flow<List<GroupExerciseCount>>

    @Query("SELECT * FROM workout_groups ORDER BY createdAt DESC")
    fun getAllGroups(): Flow<List<WorkoutGroup>>

    @Query("SELECT * FROM workout_groups WHERE id = :id LIMIT 1")
    suspend fun getGroupById(id: Long): WorkoutGroup?

    @Query("SELECT * FROM workout_groups WHERE id = :id LIMIT 1")
    fun observeGroup(id: Long): Flow<WorkoutGroup?>

    @Insert
    suspend fun insertGroup(group: WorkoutGroup): Long

    @Update
    suspend fun updateGroup(group: WorkoutGroup)

    @Delete
    suspend fun deleteGroup(group: WorkoutGroup)

    @Query("DELETE FROM workout_groups WHERE id = :id")
    suspend fun deleteGroupById(id: Long)

    @Query(
        """
        SELECT * FROM group_exercises
        WHERE groupId = :groupId
        ORDER BY sortOrder ASC
        """
    )
    fun getGroupExercises(groupId: Long): Flow<List<GroupExercise>>

    @Transaction
    @Query(
        """
        SELECT * FROM group_exercises
        WHERE groupId = :groupId
        ORDER BY sortOrder ASC
        """
    )
    fun getGroupExerciseCrossRefs(groupId: Long): Flow<List<GroupExerciseCrossRef>>

    @Query("SELECT COUNT(*) FROM group_exercises WHERE groupId = :groupId")
    fun observeExerciseCount(groupId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupExercises(items: List<GroupExercise>)

    @Query("DELETE FROM group_exercises WHERE groupId = :groupId AND exerciseId = :exerciseId")
    suspend fun removeExercise(groupId: Long, exerciseId: String)

    @Query("DELETE FROM group_exercises WHERE groupId = :groupId")
    suspend fun clearGroupExercises(groupId: Long)

    @Query("DELETE FROM group_exercises")
    suspend fun deleteAllGroupExercises()

    @Query("DELETE FROM workout_groups")
    suspend fun deleteAllGroups()

    @Query("SELECT * FROM workout_groups ORDER BY id ASC")
    suspend fun getAllGroupsSync(): List<WorkoutGroup>

    @Query("SELECT * FROM group_exercises WHERE groupId = :groupId ORDER BY sortOrder ASC")
    suspend fun getGroupExercisesSync(groupId: Long): List<GroupExercise>

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM group_exercises WHERE groupId = :groupId")
    suspend fun maxSortOrder(groupId: Long): Int
}
