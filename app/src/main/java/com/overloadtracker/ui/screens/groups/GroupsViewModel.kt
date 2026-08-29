/**
 * ViewModel for workout group list management and real-time weekly progress calculation.
 */
package com.overloadtracker.ui.screens.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.overloadtracker.data.local.entity.WorkoutGroup
import com.overloadtracker.data.local.entity.WorkoutSession
import com.overloadtracker.data.repository.WorkoutGroupRepository
import com.overloadtracker.data.repository.WorkoutSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/** UI model for a group row in the list. */
data class GroupItemUi(
    val group: WorkoutGroup,
    val exerciseCount: Int,
    val lastPerformed: Long?
)

/** Daily progress UI model for weekly bar chart. */
data class DayProgressUi(
    val dayLabel: String,
    val sessionCount: Int,
    val fraction: Float,
    val isToday: Boolean
)

/** Weekly progress UI model. */
data class WeeklyProgressUi(
    val days: List<DayProgressUi>,
    val completedSessionsThisWeek: Int,
    val targetSessionsPerWeek: Int = 4
)

/**
 * Loads groups with exercise counts, last-performed timestamps, and real-time weekly training progress.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val groupRepository: WorkoutGroupRepository,
    private val sessionRepository: WorkoutSessionRepository,
    private val activeSessionManager: com.overloadtracker.data.manager.ActiveSessionManager
) : ViewModel() {

    private val _deletedGroup = MutableStateFlow<WorkoutGroup?>(null)
    val deletedGroup: StateFlow<WorkoutGroup?> = _deletedGroup.asStateFlow()
    val activeSession = activeSessionManager.activeSession

    val groups: StateFlow<List<GroupItemUi>> = combine(
        groupRepository.observeGroups(),
        groupRepository.observeGroupExerciseCounts(),
        sessionRepository.observeGroupLastPerformedMap()
    ) { groupList, countsMap, lastMap ->
        groupList.map { group ->
            GroupItemUi(
                group = group,
                exerciseCount = countsMap[group.id] ?: 0,
                lastPerformed = lastMap[group.id]
            )
        }.sortedWith(
            compareBy<GroupItemUi> { it.lastPerformed ?: 0L }
                .thenBy { it.group.createdAt }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val weeklyProgress: StateFlow<WeeklyProgressUi> = sessionRepository.observeSessions()
        .map { sessions -> calculateWeeklyProgress(sessions) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            calculateWeeklyProgress(emptyList())
        )

    fun createGroup(name: String, notes: String?) {
        viewModelScope.launch {
            groupRepository.createGroup(name, notes)
        }
    }

    fun deleteGroup(group: WorkoutGroup) {
        viewModelScope.launch {
            _deletedGroup.value = group
            groupRepository.deleteGroup(group.id)
        }
    }

    fun undoDelete() {
        val group = _deletedGroup.value ?: return
        viewModelScope.launch {
            groupRepository.createGroup(group.name, group.notes)
            _deletedGroup.value = null
        }
    }

    fun clearDeleted() {
        _deletedGroup.value = null
    }

    fun discardActiveSession() {
        activeSessionManager.clearSession()
    }

    private fun calculateWeeklyProgress(sessions: List<WorkoutSession>): WeeklyProgressUi {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        val todayDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
        cal.add(Calendar.DAY_OF_YEAR, -daysFromMonday)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
        val calDays = listOf(
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
            Calendar.SUNDAY
        )

        var totalCompletedThisWeek = 0
        val daysList = ArrayList<DayProgressUi>()
        val tempCal = cal.clone() as Calendar

        for (i in 0 until 7) {
            val startMillis = tempCal.timeInMillis
            tempCal.add(Calendar.DAY_OF_YEAR, 1)
            val endMillis = tempCal.timeInMillis - 1

            val count = sessions.count { it.endTime in startMillis..endMillis }
            totalCompletedThisWeek += count

            val fraction = count.coerceAtMost(1).toFloat()
            val isToday = (calDays[i] == todayDayOfWeek)

            daysList.add(
                DayProgressUi(
                    dayLabel = dayLabels[i],
                    sessionCount = count,
                    fraction = fraction,
                    isToday = isToday
                )
            )
        }

        return WeeklyProgressUi(
            days = daysList,
            completedSessionsThisWeek = totalCompletedThisWeek,
            targetSessionsPerWeek = 4
        )
    }
}
