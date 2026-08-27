package com.overloadtracker.data.model

import java.time.LocalDate
import java.time.ZoneId

enum class ExportTimeRange(val label: String) {
    ALL_TIME("All Time"),
    LAST_7_DAYS("Last 7 Days"),
    LAST_30_DAYS("Last 30 Days"),
    LAST_90_DAYS("Last 90 Days"),
    THIS_MONTH("This Month");

    /**
     * Calculates the start epoch millisecond cutoff for filtering workout sessions.
     * Returns null for ALL_TIME.
     */
    fun getStartTimestampEpochMs(referenceDate: LocalDate = LocalDate.now(ZoneId.systemDefault())): Long? {
        val zoneId = ZoneId.systemDefault()
        val startDate = when (this) {
            ALL_TIME -> return null
            LAST_7_DAYS -> referenceDate.minusDays(7)
            LAST_30_DAYS -> referenceDate.minusDays(30)
            LAST_90_DAYS -> referenceDate.minusDays(90)
            THIS_MONTH -> referenceDate.withDayOfMonth(1)
        }
        return startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
    }
}
