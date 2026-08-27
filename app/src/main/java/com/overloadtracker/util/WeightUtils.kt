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
