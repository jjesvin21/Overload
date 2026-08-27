/**
 * ViewModel for workout group list management.
 */
package com.overloadtracker.ui.screens.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.overloadtracker.data.local.entity.WorkoutGroup
import com.overloadtracker.data.repository.WorkoutGroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** UI model for a group row in the list. */
data class GroupItemUi(
    val group: WorkoutGroup,
    val exerciseCount: Int,
    val lastPerformed: Long?
)

/**
 * Loads groups with exercise counts and last-performed timestamps; handles undo delete.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val groupRepository: WorkoutGroupRepository
) : ViewModel() {

    private val _deletedGroup = MutableStateFlow<WorkoutGroup?>(null)
    val deletedGroup: StateFlow<WorkoutGroup?> = _deletedGroup.asStateFlow()

    val groups: StateFlow<List<GroupItemUi>> = groupRepository.observeGroups()
        .flatMapLatest { groupList ->
            if (groupList.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    groupList.map { group ->
                        combine(
                            groupRepository.observeExerciseCount(group.id),
                            groupRepository.observeLastPerformed(group.id)
                        ) { count, last ->
                            GroupItemUi(group, count, last)
                        }
                    }
                ) { items -> items.toList() }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
}
