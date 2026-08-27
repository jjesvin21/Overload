/**
 * Shape scale for Overload Tracker — Liquid Glass design system.
 */
package com.overloadtracker.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

// Specific Shape Tokens
val ShapeGlassCard = RoundedCornerShape(24.dp)
val ShapeInputField = RoundedCornerShape(16.dp)
val ShapeChip = RoundedCornerShape(12.dp)
val ShapePill = CircleShape
val ShapeDialog = RoundedCornerShape(24.dp)
val ShapeBottomSheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
