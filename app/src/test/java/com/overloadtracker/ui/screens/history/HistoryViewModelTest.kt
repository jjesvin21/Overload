package com.overloadtracker.ui.screens.history

import com.overloadtracker.data.model.WorkoutSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class HistoryViewModelTest {

    @Test
    fun `buildHeatmapState with empty summaries returns valid empty grid`() {
        val refDate = LocalDate.of(2026, 10, 15) // Thursday
        val state = HistoryViewModel.buildHeatmapState(
            summaries = emptyList(),
            weeksCount = 20,
            referenceDate = refDate
        )

        assertEquals(20, state.weeks.size)
        assertEquals(0, state.totalWorkouts)
        assertEquals(0, state.activeDaysCount)
        assertEquals(0, state.currentStreak)
        assertNotNull(state.selectedDay)
        assertEquals(refDate, state.selectedDay?.date)

        val allDays = state.weeks.flatMap { it.days }
        assertEquals(140, allDays.size)
        assertTrue(allDays.all { it.level == 0 })
    }

    @Test
    fun `buildHeatmapState aggregates volume and computes levels correctly`() {
        val refDate = LocalDate.of(2026, 10, 15)
        val zoneId = ZoneId.systemDefault()

        // Create sample workout session today and 2 days ago
        val todayMs = refDate.atStartOfDay(zoneId).toInstant().toEpochMilli() + 3600_000
        val twoDaysAgoMs = refDate.minusDays(2).atStartOfDay(zoneId).toInstant().toEpochMilli() + 3600_000

        val summaries = listOf(
            WorkoutSummary(
                sessionId = 1L,
                groupName = "Push Day",
                startTime = todayMs - 3600_000,
                endTime = todayMs,
                totalVolume = 8000.0,
                muscleGroups = listOf("Chest", "Triceps"),
                exerciseCount = 4,
                setCount = 12
            ),
            WorkoutSummary(
                sessionId = 2L,
                groupName = "Pull Day",
                startTime = twoDaysAgoMs - 3600_000,
                endTime = twoDaysAgoMs,
                totalVolume = 4000.0,
                muscleGroups = listOf("Back", "Biceps"),
                exerciseCount = 5,
                setCount = 15
            )
        )

        val state = HistoryViewModel.buildHeatmapState(
            summaries = summaries,
            weeksCount = 20,
            referenceDate = refDate
        )

        assertEquals(2, state.totalWorkouts)
        assertEquals(2, state.activeDaysCount)

        val todayHeatmapDay = state.weeks.flatMap { it.days }.find { it.date == refDate }
        assertNotNull(todayHeatmapDay)
        assertEquals(1, todayHeatmapDay?.workoutCount)
        assertEquals(8000.0, todayHeatmapDay?.totalVolume ?: 0.0, 0.01)
        assertEquals(4, todayHeatmapDay?.level) // Max volume -> Level 4

        val twoDaysAgoHeatmapDay = state.weeks.flatMap { it.days }.find { it.date == refDate.minusDays(2) }
        assertNotNull(twoDaysAgoHeatmapDay)
        assertEquals(1, twoDaysAgoHeatmapDay?.workoutCount)
        assertEquals(4000.0, twoDaysAgoHeatmapDay?.totalVolume ?: 0.0, 0.01)
        assertEquals(2, twoDaysAgoHeatmapDay?.level) // 50% max volume -> Level 2
    }

    @Test
    fun `buildHeatmapState calculates consecutive active day streak`() {
        val refDate = LocalDate.of(2026, 10, 15)
        val zoneId = ZoneId.systemDefault()

        // Create consecutive workouts for today, yesterday, and 2 days ago
        val day0Ms = refDate.atStartOfDay(zoneId).toInstant().toEpochMilli() + 1000
        val day1Ms = refDate.minusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() + 1000
        val day2Ms = refDate.minusDays(2).atStartOfDay(zoneId).toInstant().toEpochMilli() + 1000

        val summaries = listOf(
            WorkoutSummary(1L, "Workout A", day0Ms, day0Ms + 100, 1000.0, emptyList(), 1, 3),
            WorkoutSummary(2L, "Workout B", day1Ms, day1Ms + 100, 1000.0, emptyList(), 1, 3),
            WorkoutSummary(3L, "Workout C", day2Ms, day2Ms + 100, 1000.0, emptyList(), 1, 3)
        )

        val state = HistoryViewModel.buildHeatmapState(
            summaries = summaries,
            weeksCount = 20,
            referenceDate = refDate
        )

        assertEquals(3, state.currentStreak)
    }

    @Test
    fun `ExportTimeRange computes correct start timestamp epoch ms`() {
        val refDate = LocalDate.of(2026, 10, 15)
        val zoneId = ZoneId.systemDefault()

        val allTimeCutoff = com.overloadtracker.data.model.ExportTimeRange.ALL_TIME.getStartTimestampEpochMs(refDate)
        org.junit.Assert.assertNull(allTimeCutoff)

        val sevenDaysCutoff = com.overloadtracker.data.model.ExportTimeRange.LAST_7_DAYS.getStartTimestampEpochMs(refDate)
        val expectedSevenDays = refDate.minusDays(7).atStartOfDay(zoneId).toInstant().toEpochMilli()
        assertEquals(expectedSevenDays, sevenDaysCutoff)

        val thirtyDaysCutoff = com.overloadtracker.data.model.ExportTimeRange.LAST_30_DAYS.getStartTimestampEpochMs(refDate)
        val expectedThirtyDays = refDate.minusDays(30).atStartOfDay(zoneId).toInstant().toEpochMilli()
        assertEquals(expectedThirtyDays, thirtyDaysCutoff)

        val thisMonthCutoff = com.overloadtracker.data.model.ExportTimeRange.THIS_MONTH.getStartTimestampEpochMs(refDate)
        val expectedThisMonth = refDate.withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        assertEquals(expectedThisMonth, thisMonthCutoff)
    }
}
