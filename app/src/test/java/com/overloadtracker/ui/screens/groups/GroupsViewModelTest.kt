package com.overloadtracker.ui.screens.groups

import com.overloadtracker.data.local.entity.WorkoutGroup
import org.junit.Assert.assertEquals
import org.junit.Test

class GroupsViewModelTest {

    private fun sortGroupItems(items: List<GroupItemUi>): List<GroupItemUi> {
        return items.sortedWith(
            compareBy<GroupItemUi> { it.lastPerformed ?: 0L }
                .thenBy { it.group.createdAt }
        )
    }

    @Test
    fun `unperformed groups are sorted first by creation date`() {
        val group1 = GroupItemUi(
            group = WorkoutGroup(id = 1L, name = "Push A", createdAt = 1000L),
            exerciseCount = 5,
            lastPerformed = null
        )
        val group2 = GroupItemUi(
            group = WorkoutGroup(id = 2L, name = "Pull A", createdAt = 2000L),
            exerciseCount = 4,
            lastPerformed = null
        )
        val group3 = GroupItemUi(
            group = WorkoutGroup(id = 3L, name = "Legs A", createdAt = 3000L),
            exerciseCount = 6,
            lastPerformed = null
        )

        val sorted = sortGroupItems(listOf(group3, group1, group2))

        assertEquals(listOf(group1, group2, group3), sorted)
    }

    @Test
    fun `finished group moves to bottom and next group moves to top as start now`() {
        val now = 10000L
        val groupPush = GroupItemUi(
            group = WorkoutGroup(id = 1L, name = "Push A", createdAt = 1000L),
            exerciseCount = 5,
            lastPerformed = 5000L // performed 5 sec ago
        )
        val groupPull = GroupItemUi(
            group = WorkoutGroup(id = 2L, name = "Pull A", createdAt = 2000L),
            exerciseCount = 4,
            lastPerformed = null // never performed
        )
        val groupLegs = GroupItemUi(
            group = WorkoutGroup(id = 3L, name = "Legs A", createdAt = 3000L),
            exerciseCount = 6,
            lastPerformed = null // never performed
        )

        // Pull and Legs are unperformed, Push was performed at 5000L
        val initialOrder = sortGroupItems(listOf(groupPush, groupPull, groupLegs))
        assertEquals(listOf(groupPull, groupLegs, groupPush), initialOrder)
        assertEquals("Pull A", initialOrder.first().group.name)

        // Pull A workout is completed now (lastPerformed = 10000L)
        val groupPullFinished = groupPull.copy(lastPerformed = now)

        val updatedOrder = sortGroupItems(listOf(groupPush, groupPullFinished, groupLegs))

        // Order should now be: Legs A (null), Push A (5000L), Pull A (10000L - finished most recently)
        assertEquals(listOf(groupLegs, groupPush, groupPullFinished), updatedOrder)

        // The top group (Start Now widget) is now Legs A
        assertEquals("Legs A", updatedOrder.first().group.name)
        // The finished group (Pull A) moved to down / bottom
        assertEquals("Pull A", updatedOrder.last().group.name)
    }

    @Test
    fun `full rotation cycle rotates groups sequentially`() {
        var groupA = GroupItemUi(WorkoutGroup(id = 1L, name = "Group A", createdAt = 100L), 3, lastPerformed = null)
        var groupB = GroupItemUi(WorkoutGroup(id = 2L, name = "Group B", createdAt = 200L), 3, lastPerformed = null)
        var groupC = GroupItemUi(WorkoutGroup(id = 3L, name = "Group C", createdAt = 300L), 3, lastPerformed = null)

        // Step 1: Initial state (all unperformed) -> A, B, C
        var currentList = sortGroupItems(listOf(groupA, groupB, groupC))
        assertEquals("Group A", currentList[0].group.name)
        assertEquals("Group B", currentList[1].group.name)
        assertEquals("Group C", currentList[2].group.name)

        // Step 2: Group A completed at T=1000 -> B, C, A
        groupA = groupA.copy(lastPerformed = 1000L)
        currentList = sortGroupItems(listOf(groupA, groupB, groupC))
        assertEquals("Group B", currentList[0].group.name)
        assertEquals("Group C", currentList[1].group.name)
        assertEquals("Group A", currentList[2].group.name)

        // Step 3: Group B completed at T=2000 -> C, A, B
        groupB = groupB.copy(lastPerformed = 2000L)
        currentList = sortGroupItems(listOf(groupA, groupB, groupC))
        assertEquals("Group C", currentList[0].group.name)
        assertEquals("Group A", currentList[1].group.name)
        assertEquals("Group B", currentList[2].group.name)

        // Step 4: Group C completed at T=3000 -> A, B, C (Back to A!)
        groupC = groupC.copy(lastPerformed = 3000L)
        currentList = sortGroupItems(listOf(groupA, groupB, groupC))
        assertEquals("Group A", currentList[0].group.name)
        assertEquals("Group B", currentList[1].group.name)
        assertEquals("Group C", currentList[2].group.name)
    }
}
