/**
 * Session history, set logs, and progressive-overload queries (PR / last set).
 */
package com.overloadtracker.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.overloadtracker.data.local.entity.SessionSet
import com.overloadtracker.data.local.entity.SessionWithSets
import com.overloadtracker.data.local.entity.WorkoutSession
import com.overloadtracker.data.model.ExerciseProgressPoint
import com.overloadtracker.data.model.PreviousSetInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

    @Query("SELECT * FROM workout_sessions ORDER BY endTime DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :id LIMIT 1")
    fun getSessionWithSets(id: Long): Flow<SessionWithSets?>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionWithSetsOnce(id: Long): SessionWithSets?

    @Transaction
    @Query("SELECT * FROM workout_sessions ORDER BY endTime DESC")
    fun getAllSessionsWithSets(): Flow<List<SessionWithSets>>

    @Transaction
    @Query("SELECT * FROM workout_sessions ORDER BY endTime DESC")
    suspend fun getAllSessionsWithSetsSync(): List<SessionWithSets>

    @Insert
    suspend fun insertSession(session: WorkoutSession): Long

    @Insert
    suspend fun insertSets(sets: List<SessionSet>)

    @Query("DELETE FROM workout_sessions")
    suspend fun clearAllSessions()

    @Query("DELETE FROM workout_sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)

    @Query(
        """
        SELECT weight AS weight, reps AS reps, timeSeconds AS timeSeconds, count AS count
        FROM session_sets
        WHERE exerciseId = :exerciseId AND isCompleted = 1
        ORDER BY sessionId DESC, setNumber DESC
        LIMIT 1
        """
    )
    suspend fun getLastSet(exerciseId: String): PreviousSetInfo?


    @Query(
        """
        SELECT MAX(weight) FROM session_sets
        WHERE exerciseId = :exerciseId AND isCompleted = 1
        """
    )
    suspend fun getPRWeight(exerciseId: String): Double?

    @Query(
        """
        SELECT s.endTime AS dateMillis, MAX(ss.weight) AS maxWeight, MAX(ss.weight * ss.reps) AS maxVolume
        FROM session_sets ss
        INNER JOIN workout_sessions s ON s.id = ss.sessionId
        WHERE ss.exerciseId = :exerciseId AND ss.isCompleted = 1
        GROUP BY s.id
        ORDER BY s.endTime ASC
        """
    )
    fun getProgressForExercise(exerciseId: String): Flow<List<ExerciseProgressPoint>>

    @Query(
        """
        SELECT MAX(endTime) FROM workout_sessions WHERE groupId = :groupId
        """
    )
    fun getLastPerformed(groupId: Long): Flow<Long?>
}
