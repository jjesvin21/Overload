/**
 * Repository for workout group templates and ordered exercise membership.
 */
package com.overloadtracker.data.repository

import com.overloadtracker.data.local.db.WorkoutGroupDao
import com.overloadtracker.data.local.db.WorkoutSessionDao
import com.overloadtracker.data.local.entity.GroupExercise
import com.overloadtracker.data.local.entity.GroupExerciseCrossRef
import com.overloadtracker.data.local.entity.WorkoutGroup
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

data class GroupListItem(
    val group: WorkoutGroup,
    val exerciseCount: Int,
    val lastPerformed: Long?
)

@Singleton
class WorkoutGroupRepository @Inject constructor(
    private val groupDao: WorkoutGroupDao,
    private val sessionDao: WorkoutSessionDao
) {
    fun observeGroups(): Flow<List<WorkoutGroup>> = groupDao.getAllGroups()

    fun observeGroup(id: Long): Flow<WorkoutGroup?> = groupDao.observeGroup(id)

    fun observeGroupExercises(groupId: Long): Flow<List<GroupExerciseCrossRef>> =
        groupDao.getGroupExerciseCrossRefs(groupId)

    fun observeExerciseCount(groupId: Long): Flow<Int> = groupDao.observeExerciseCount(groupId)

    fun observeLastPerformed(groupId: Long): Flow<Long?> = sessionDao.getLastPerformed(groupId)

    suspend fun createGroup(name: String, notes: String?): Long =
        groupDao.insertGroup(WorkoutGroup(name = name.trim(), notes = notes?.trim()?.ifBlank { null }))

    suspend fun updateGroup(group: WorkoutGroup) = groupDao.updateGroup(group)

    suspend fun deleteGroup(id: Long) = groupDao.deleteGroupById(id)

    suspend fun addExercises(groupId: Long, exerciseIds: List<String>) {
        var order = groupDao.maxSortOrder(groupId) + 1
        val rows = exerciseIds.distinct().map { id ->
            GroupExercise(groupId = groupId, exerciseId = id, sortOrder = order++)
        }
        if (rows.isNotEmpty()) groupDao.insertGroupExercises(rows)
    }

    suspend fun removeExercise(groupId: Long, exerciseId: String) =
        groupDao.removeExercise(groupId, exerciseId)

    suspend fun reorder(groupId: Long, orderedExerciseIds: List<String>) {
        groupDao.clearGroupExercises(groupId)
        val rows = orderedExerciseIds.mapIndexed { index, id ->
            GroupExercise(groupId = groupId, exerciseId = id, sortOrder = index)
        }
        groupDao.insertGroupExercises(rows)
    }

    suspend fun getGroup(id: Long): WorkoutGroup? = groupDao.getGroupById(id)

    suspend fun getAllGroupsWithExercisesSync(): List<Pair<WorkoutGroup, List<GroupExercise>>> {
        val groups = groupDao.getAllGroupsSync()
        return groups.map { group ->
            val exercises = groupDao.getGroupExercisesSync(group.id)
            Pair(group, exercises)
        }
    }

    suspend fun replaceAllSplits(newSplits: List<NewSplitRequest>): List<Long> {
        groupDao.deleteAllGroupExercises()
        groupDao.deleteAllGroups()

        val createdIds = mutableListOf<Long>()
        for (split in newSplits) {
            val groupId = groupDao.insertGroup(
                WorkoutGroup(
                    name = split.name.trim(),
                    notes = split.notes?.trim()?.ifBlank { null }
                )
            )
            val rows = split.exerciseIds.distinct().mapIndexed { index, exerciseId ->
                GroupExercise(groupId = groupId, exerciseId = exerciseId, sortOrder = index)
            }
            if (rows.isNotEmpty()) {
                groupDao.insertGroupExercises(rows)
            }
            createdIds.add(groupId)
        }
        return createdIds
    }
}

data class NewSplitRequest(
    val name: String,
    val notes: String? = null,
    val exerciseIds: List<String> = emptyList()
)
