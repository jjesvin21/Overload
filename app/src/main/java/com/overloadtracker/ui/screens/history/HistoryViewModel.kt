/**
 * ViewModel for workout history list and CSV export.
 */
package com.overloadtracker.ui.screens.history

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.overloadtracker.data.model.ExportTimeRange
import com.overloadtracker.data.model.WorkoutSummary
import com.overloadtracker.data.repository.WorkoutSessionRepository
import com.overloadtracker.util.CsvExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject

data class HeatmapDay(
    val date: LocalDate,
    val level: Int, // 0..4
    val workoutCount: Int,
    val totalVolume: Double,
    val setCount: Int,
    val exerciseCount: Int,
    val workoutNames: List<String>,
    val isToday: Boolean,
    val isFuture: Boolean
)

data class HeatmapWeek(
    val weekStartDate: LocalDate,
    val monthLabel: String?,
    val days: List<HeatmapDay>
)

data class ConsistencyHeatmapState(
    val weeks: List<HeatmapWeek> = emptyList(),
    val totalWorkouts: Int = 0,
    val activeDaysCount: Int = 0,
    val currentStreak: Int = 0,
    val selectedDay: HeatmapDay? = null
)

/**
 * Observes session summaries and handles dynamic GitHub-style heatmap & bulk CSV export.
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val sessionRepository: WorkoutSessionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _exportMessage = MutableStateFlow<String?>(null)
    val exportMessage: StateFlow<String?> = _exportMessage.asStateFlow()

    private val _selectedDay = MutableStateFlow<HeatmapDay?>(null)
    val selectedDay: StateFlow<HeatmapDay?> = _selectedDay.asStateFlow()

    private val _showExportSheet = MutableStateFlow(false)
    val showExportSheet: StateFlow<Boolean> = _showExportSheet.asStateFlow()

    private val _selectedTimeRange = MutableStateFlow(ExportTimeRange.ALL_TIME)
    val selectedTimeRange: StateFlow<ExportTimeRange> = _selectedTimeRange.asStateFlow()

    private val _exportPreviewStats = MutableStateFlow<Pair<Int, Int>?>(null)
    val exportPreviewStats: StateFlow<Pair<Int, Int>?> = _exportPreviewStats.asStateFlow()

    val summaries: StateFlow<List<WorkoutSummary>> = sessionRepository.observeSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val heatmapState: StateFlow<ConsistencyHeatmapState> = combine(
        summaries,
        _selectedDay
    ) { summaryList, userSelectedDay ->
        buildHeatmapState(summaryList, userSelectedDay)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConsistencyHeatmapState())

    fun selectDay(day: HeatmapDay) {
        _selectedDay.value = day
    }

    fun openExportSheet() {
        _showExportSheet.value = true
        updatePreviewStats(_selectedTimeRange.value)
    }

    fun dismissExportSheet() {
        _showExportSheet.value = false
    }

    fun setTimeRange(range: ExportTimeRange) {
        _selectedTimeRange.value = range
        updatePreviewStats(range)
    }

    private fun updatePreviewStats(range: ExportTimeRange) {
        viewModelScope.launch {
            val cutoff = range.getStartTimestampEpochMs()
            _exportPreviewStats.value = sessionRepository.getExportPreview(startTimeCutoff = cutoff)
        }
    }

    fun shareCsv(range: ExportTimeRange) {
        viewModelScope.launch {
            val cutoff = range.getStartTimestampEpochMs()
            val rows = sessionRepository.buildCsvRows(startTimeCutoff = cutoff)
            val csv = CsvExporter.buildCsv(rows)
            val prefix = if (range == ExportTimeRange.ALL_TIME) "WorkoutHistory_AllTime" else "WorkoutHistory_${range.name}"
            CsvExporter.shareCsvFile(context, csv, prefix = prefix)
            dismissExportSheet()
        }
    }

    fun saveToDownloads(range: ExportTimeRange) {
        viewModelScope.launch {
            val cutoff = range.getStartTimestampEpochMs()
            val rows = sessionRepository.buildCsvRows(startTimeCutoff = cutoff)
            val csv = CsvExporter.buildCsv(rows)
            val prefix = if (range == ExportTimeRange.ALL_TIME) "WorkoutHistory_AllTime" else "WorkoutHistory_${range.name}"
            val uri = CsvExporter.saveToDownloads(context, csv, prefix = prefix)
            _exportMessage.value = if (uri != null) "exported" else "failed"
            dismissExportSheet()
        }
    }

    fun exportAll() {
        saveToDownloads(ExportTimeRange.ALL_TIME)
    }

    fun clearExportMessage() {
        _exportMessage.value = null
    }

    companion object {
        fun buildHeatmapState(
            summaries: List<WorkoutSummary>,
            userSelectedDay: HeatmapDay? = null,
            weeksCount: Int = 20,
            referenceDate: LocalDate = LocalDate.now(ZoneId.systemDefault())
        ): ConsistencyHeatmapState {
            val zoneId = ZoneId.systemDefault()
            val workoutsByDate = summaries.groupBy { summary ->
                Instant.ofEpochMilli(summary.endTime).atZone(zoneId).toLocalDate()
            }

            val currentWeekMonday = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val windowStartMonday = currentWeekMonday.minusWeeks((weeksCount - 1).toLong())

            // Compute maximum daily volume across window for level thresholds
            var maxDailyVolume = 0.0
            var activeDays = 0
            var totalWorkoutsInWindow = 0

            val allDatesInWindow = mutableListOf<LocalDate>()
            var curr = windowStartMonday
            val windowEndSunday = currentWeekMonday.plusDays(6)
            while (!curr.isAfter(windowEndSunday)) {
                allDatesInWindow.add(curr)
                val daySummaries = workoutsByDate[curr].orEmpty()
                if (daySummaries.isNotEmpty()) {
                    activeDays++
                    totalWorkoutsInWindow += daySummaries.size
                    val vol = daySummaries.sumOf { it.totalVolume }
                    if (vol > maxDailyVolume) {
                        maxDailyVolume = vol
                    }
                }
                curr = curr.plusDays(1)
            }

            // Calculate current streak working backwards from referenceDate
            var streak = 0
            var streakCheckDate = referenceDate
            // If referenceDate (today) has no workout, check if yesterday had one to maintain active streak
            if (workoutsByDate[streakCheckDate].isNullOrEmpty() && workoutsByDate[streakCheckDate.minusDays(1)]?.isNotEmpty() == true) {
                streakCheckDate = streakCheckDate.minusDays(1)
            }
            while (workoutsByDate[streakCheckDate]?.isNotEmpty() == true) {
                streak++
                streakCheckDate = streakCheckDate.minusDays(1)
            }

            // Build weeks
            val monthFmt = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())
            var lastMonthName = ""

            val weeks = (0 until weeksCount).map { weekIndex ->
                val weekStartDate = windowStartMonday.plusWeeks(weekIndex.toLong())
                
                // Determine month label for this week if month changes or is start of window
                var monthLabelToDisplay: String? = null
                val firstDayOfMonthInWeek = (0..6).map { weekStartDate.plusDays(it.toLong()) }
                    .firstOrNull { it.dayOfMonth == 1 }
                
                if (firstDayOfMonthInWeek != null) {
                    val mName = monthFmt.format(firstDayOfMonthInWeek)
                    if (mName != lastMonthName) {
                        monthLabelToDisplay = mName
                        lastMonthName = mName
                    }
                } else if (weekIndex == 0) {
                    val mName = monthFmt.format(weekStartDate)
                    monthLabelToDisplay = mName
                    lastMonthName = mName
                }

                val days = (0..6).map { dayIndex ->
                    val date = weekStartDate.plusDays(dayIndex.toLong())
                    val daySummaries = workoutsByDate[date].orEmpty()
                    val totalVol = daySummaries.sumOf { it.totalVolume }
                    val totalSets = daySummaries.sumOf { it.setCount }
                    val totalExercises = daySummaries.sumOf { it.exerciseCount }
                    val workoutNames = daySummaries.map { it.groupName }

                    val level = when {
                        daySummaries.isEmpty() -> 0
                        maxDailyVolume <= 0 -> 2 // fallback if volume is 0 (e.g. bodyweight exercises)
                        else -> {
                            val ratio = totalVol / maxDailyVolume
                            when {
                                ratio <= 0.25 -> 1
                                ratio <= 0.50 -> 2
                                ratio <= 0.75 -> 3
                                else -> 4
                            }
                        }
                    }

                    HeatmapDay(
                        date = date,
                        level = level,
                        workoutCount = daySummaries.size,
                        totalVolume = totalVol,
                        setCount = totalSets,
                        exerciseCount = totalExercises,
                        workoutNames = workoutNames,
                        isToday = date == referenceDate,
                        isFuture = date.isAfter(referenceDate)
                    )
                }

                HeatmapWeek(
                    weekStartDate = weekStartDate,
                    monthLabel = monthLabelToDisplay,
                    days = days
                )
            }

            // Default selected day to referenceDate (today) or user's selected day
            val allDays = weeks.flatMap { it.days }
            val resolvedSelectedDay = if (userSelectedDay != null) {
                allDays.find { it.date == userSelectedDay.date } ?: allDays.find { it.isToday } ?: allDays.lastOrNull()
            } else {
                allDays.find { it.isToday } ?: allDays.lastOrNull()
            }

            return ConsistencyHeatmapState(
                weeks = weeks,
                totalWorkouts = totalWorkoutsInWindow,
                activeDaysCount = activeDays,
                currentStreak = streak,
                selectedDay = resolvedSelectedDay
            )
        }
    }
}

