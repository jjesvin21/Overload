/**
 * Unit conversion helpers and formatting for weight displays / CSV export.
 */
package com.overloadtracker.util

import kotlin.math.roundToInt

enum class WeightUnit { KG, LB }

object WeightUtils {
    fun kgToDisplay(kg: Double, unit: WeightUnit): Double =
        if (unit == WeightUnit.LB) kg * Constants.LB_PER_KG else kg

    fun displayToKg(value: Double, unit: WeightUnit): Double =
        if (unit == WeightUnit.LB) value / Constants.LB_PER_KG else value

    fun formatWeight(kg: Double, unit: WeightUnit): String {
        val v = kgToDisplay(kg, unit)
        val label = if (unit == WeightUnit.LB) "lb" else "kg"
        return if (v % 1.0 == 0.0) "${v.toInt()}$label" else String.format("%.1f%s", v, label)
    }

    fun formatWeightNumber(kg: Double, unit: WeightUnit): String {
        val v = kgToDisplay(kg, unit)
        return if (v % 1.0 == 0.0) v.toInt().toString() else String.format("%.1f", v)
    }

    fun roundSmart(value: Double): Double =
        (value * 10).roundToInt() / 10.0
}

fun formatDuration(millis: Long): String {
    val totalSec = (millis / 1000).coerceAtLeast(0)
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s)
    else String.format("%d:%02d", m, s)
}

fun titleCase(value: String): String =
    value.split(" ").joinToString(" ") { part ->
        part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

fun parseTimeDisplayToSeconds(display: String): Int? {
    if (display.isBlank()) return null
    if (display.contains(":")) {
        val parts = display.split(":")
        if (parts.size == 2) {
            val m = parts[0].trim().toIntOrNull() ?: 0
            val s = parts[1].trim().toIntOrNull() ?: 0
            return m * 60 + s
        }
    }
    val minutes = display.toDoubleOrNull() ?: return null
    return (minutes * 60).toInt()
}

fun formatSecondsToDisplay(seconds: Int?): String {
    if (seconds == null || seconds <= 0) return ""
    val m = seconds / 60
    val s = seconds % 60
    return if (s == 0) m.toString() else String.format("%d:%02d", m, s)
}

fun formatCardioDisplay(timeSeconds: Int?, count: Int?): String {
    val timePart = if (timeSeconds != null && timeSeconds > 0) {
        val m = timeSeconds / 60
        val s = timeSeconds % 60
        if (s == 0) "${m}m" else "${m}m ${s}s"
    } else null

    val countPart = if (count != null && count > 0) "$count" else null

    return when {
        timePart != null && countPart != null -> "$timePart × $countPart"
        timePart != null -> timePart
        countPart != null -> "$countPart count"
        else -> "0m"
    }
}

