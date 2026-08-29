/**
 * Repository for completed sessions, set history, PRs, and CSV row assembly.
 */
package com.overloadtracker.data.repository

import com.overloadtracker.data.local.db.ExerciseDao
import com.overloadtracker.data.local.db.WorkoutSessionDao
import com.overloadtracker.data.local.entity.SessionSet
import com.overloadtracker.data.local.entity.SessionWithSets
import com.overloadtracker.data.local.entity.WorkoutSession
import com.overloadtracker.data.model.CsvExportRow
import com.overloadtracker.data.model.ExerciseProgressPoint
import com.overloadtracker.data.model.PreviousSetInfo
import com.overloadtracker.data.model.WorkoutSummary
import com.overloadtracker.util.titleCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutSessionRepository @Inject constructor(
    private val sessionDao: WorkoutSessionDao,
    private val exerciseDao: ExerciseDao
) {
    fun observeSessions(): Flow<List<WorkoutSession>> = sessionDao.getAllSessions()

    fun observeSession(id: Long): Flow<SessionWithSets?> = sessionDao.getSessionWithSets(id)

    fun observeSummaries(): Flow<List<WorkoutSummary>> = combine(
        sessionDao.getAllSessionsWithSets(),
        exerciseDao.getAll()
    ) { sessions, exercises ->
        val categoriesById = exercises.associate { it.id to it.category }
        sessions.map { sws ->
            WorkoutSummary(
                sessionId = sws.session.id,
                groupName = sws.session.groupName,
                startTime = sws.session.startTime,
                endTime = sws.session.endTime,
                totalVolume = sws.session.totalVolume,
                muscleGroups = sws.sets.mapNotNull { categoriesById[it.exerciseId] }
                    .distinct()
                    .map(::titleCase),
                exerciseCount = sws.sets.map { it.exerciseId }.distinct().size,
                setCount = sws.sets.size
            )
        }
    }

    /** Summaries enriched with muscle-group chips (category per exercise). */
    suspend fun getSummariesWithMuscles(): List<WorkoutSummary> {
        return sessionDao.getAllSessionsWithSetsSync().map { sws ->
            val muscles = sws.sets.mapNotNull { set ->
                exerciseDao.getById(set.exerciseId)?.category
            }.distinct().map { titleCase(it) }
            WorkoutSummary(
                sessionId = sws.session.id,
                groupName = sws.session.groupName,
                startTime = sws.session.startTime,
                endTime = sws.session.endTime,
                totalVolume = sws.session.totalVolume,
                muscleGroups = muscles,
                exerciseCount = sws.sets.map { it.exerciseId }.distinct().size,
                setCount = sws.sets.size
            )
        }
    }

    fun observeProgress(exerciseId: String): Flow<List<ExerciseProgressPoint>> =
        sessionDao.getProgressForExercise(exerciseId)

    suspend fun getLastSet(exerciseId: String): PreviousSetInfo? =
        sessionDao.getLastSet(exerciseId)

    suspend fun getPRWeight(exerciseId: String): Double? =
        sessionDao.getPRWeight(exerciseId)

    suspend fun finishWorkout(
        groupId: Long?,
        groupName: String,
        startTime: Long,
        endTime: Long,
        sets: List<SessionSetDraft>
    ): Long {
        val volume = sets.filter { it.isCompleted }.sumOf { it.weight * it.reps }
        val sessionId = sessionDao.insertSession(
            WorkoutSession(
                groupId = groupId,
                groupName = groupName,
                startTime = startTime,
                endTime = endTime,
                totalVolume = volume
            )
        )
        val entities = sets.filter { it.isCompleted }.map {
            SessionSet(
                sessionId = sessionId,
                exerciseId = it.exerciseId,
                exerciseName = it.exerciseName,
                setNumber = it.setNumber,
                weight = it.weight,
                reps = it.reps,
                timeSeconds = it.timeSeconds,
                count = it.count,
                rpe = it.rpe,
                isCompleted = true,
                restSeconds = it.restSeconds
            )
        }
        if (entities.isNotEmpty()) sessionDao.insertSets(entities)
        return sessionId
    }

    suspend fun clearHistory() = sessionDao.clearAllSessions()

    suspend fun buildCsvRows(sessionId: Long? = null, startTimeCutoff: Long? = null): List<CsvExportRow> {
        val loaded = if (sessionId != null) {
            listOfNotNull(sessionDao.getSessionWithSetsOnce(sessionId))
        } else {
            sessionDao.getAllSessionsWithSetsSync()
        }.filter { sws ->
            startTimeCutoff == null || sws.session.endTime >= startTimeCutoff
        }
        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return loaded.flatMap { sws ->
            val muscleGroups = sws.sets.mapNotNull { set ->
                exerciseDao.getById(set.exerciseId)?.category
            }.distinct().joinToString(",") { titleCase(it) }
            sws.sets.map { set ->
                val equipment = exerciseDao.getById(set.exerciseId)?.equipment.orEmpty()
                CsvExportRow(
                    date = dateFmt.format(Date(sws.session.endTime)),
                    workoutName = sws.session.groupName,
                    muscleGroups = muscleGroups,
                    exerciseName = set.exerciseName,
                    equipment = titleCase(equipment),
                    setNumber = set.setNumber,
                    weightKg = set.weight,
                    reps = set.reps,
                    timeSeconds = set.timeSeconds,
                    count = set.count,
                    rpe = set.rpe,
                    totalVolume = set.weight * set.reps,
                    notes = sws.session.notes.orEmpty()
                )
            }
        }
    }

    suspend fun getExportPreview(sessionId: Long? = null, startTimeCutoff: Long? = null): Pair<Int, Int> {
        val loaded = if (sessionId != null) {
            listOfNotNull(sessionDao.getSessionWithSetsOnce(sessionId))
        } else {
            sessionDao.getAllSessionsWithSetsSync()
        }.filter { sws ->
            startTimeCutoff == null || sws.session.endTime >= startTimeCutoff
        }
        val totalSets = loaded.sumOf { it.sets.size }
        return Pair(loaded.size, totalSets)
    }

    suspend fun isPersonalRecord(exerciseId: String, weight: Double, excludeSessionId: Long?): Boolean {
        val pr = sessionDao.getPRWeight(exerciseId) ?: return true
        if (excludeSessionId == null) return weight >= pr
        // For historical display, PR means this set equals the all-time max
        return weight >= pr
    }
}

/** In-memory draft set used while a live session is active. */
data class SessionSetDraft(
    val exerciseId: String,
    val exerciseName: String,
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
    val timeSeconds: Int? = null,
    val count: Int? = null,
    val rpe: Int?,
    val isCompleted: Boolean,
    val restSeconds: Int?
)

