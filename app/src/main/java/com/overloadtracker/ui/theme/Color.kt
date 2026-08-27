/**
 * Core Color Tokens for Overload Tracker — Liquid Glass / Liquid Vitality design system.
 * Inspired by high-performance obsidian dark mode with electric violet and cyan gradients.
 */
package com.overloadtracker.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Midnight Dark Surfaces & Backgrounds
val MidnightBackground = Color(0xFF0B1326)
val MidnightSurface = Color(0xFF0B1326)
val MidnightSurfaceDim = Color(0xFF0B1326)
val MidnightSurfaceBright = Color(0xFF31394D)
val MidnightSurfaceContainerLowest = Color(0xFF060E20)
val MidnightSurfaceContainerLow = Color(0xFF131B2E)
val MidnightSurfaceContainer = Color(0xFF171F33)
val MidnightSurfaceContainerHigh = Color(0xFF222A3D)
val MidnightSurfaceContainerHighest = Color(0xFF2D3449)
val MidnightSurfaceVariant = Color(0xFF2D3449)

// Primary Palette — Electric Violet
val ElectricViolet = Color(0xFFD0BCFF)
val ElectricVioletContainer = Color(0xFFA078FF)
val ElectricVioletOnPrimary = Color(0xFF3C0091)
val ElectricVioletOnContainer = Color(0xFF340080)
val ElectricVioletInverse = Color(0xFF6D3BD7)

// Secondary Palette — Cyan Accent
val CyanAccent = Color(0xFF4CD7F6)
val CyanAccentContainer = Color(0xFF03B5D3)
val CyanOnSecondary = Color(0xFF003640)
val CyanOnContainer = Color(0xFF00424E)

// Tertiary Palette — Sunset Rose
val SunsetRose = Color(0xFFFFB2B7)
val SunsetRoseContainer = Color(0xFFFF516A)
val SunsetRoseOnTertiary = Color(0xFF67001B)
val SunsetRoseOnContainer = Color(0xFF5B0017)

// Neutral Text & Outline Colors
val TextOnSurface = Color(0xFFDAE2FD)
val TextOnSurfaceVariant = Color(0xFFCBC3D7)
val TextInverseSurface = Color(0xFFDAE2FD)
val TextInverseOnSurface = Color(0xFF283044)
val OutlineColor = Color(0xFF958EA0)
val OutlineVariantColor = Color(0xFF494454)

// Functional Colors
val SuccessGreen = Color(0xFF4ADE80)
val ErrorRed = Color(0xFFFFB4AB)
val OnError = Color(0xFF690005)
val ErrorContainer = Color(0xFF93000A)
val OnErrorContainer = Color(0xFFFFDAD6)

// Glassmorphism & Liquid Tokens
val GlassSurface = Color(0x33171F33)
val GlassSurfaceHigh = Color(0x4D222A3D)
val GlassBorderTopLeft = Color(0x33FFFFFF)
val GlassBorderBottomRight = Color(0x14FFFFFF)
val GlassBorderHighlight = Color(0x55D0BCFF)
val GlassOverlayDark = Color(0xCC060E20)
val GlassEtchedInput = Color(0x20000000)

// Gradients
val PrimaryGradientBrush = Brush.linearGradient(
    colors = listOf(ElectricVioletContainer, CyanAccent)
)
val PrimaryGradientBrushFaded = Brush.linearGradient(
    colors = listOf(ElectricVioletContainer.copy(alpha = 0.3f), CyanAccent.copy(alpha = 0.3f))
)
val SurfaceGradientBrush = Brush.verticalGradient(
    colors = listOf(MidnightSurfaceContainerHigh.copy(alpha = 0.7f), MidnightSurfaceContainerLow.copy(alpha = 0.9f))
)

// Legacy Aliases for backward compatibility
val NavyDeep = MidnightBackground
val NavySurface = MidnightSurfaceContainer
val NavySurfaceVariant = MidnightSurfaceContainerHighest
val OrangeAccent = ElectricVioletContainer
val OrangeAccentLight = CyanAccent
val OrangeAccentDark = ElectricViolet
val TextPrimaryLight = TextOnSurface
val TextSecondaryLight = TextOnSurfaceVariant
val TextPrimaryDark = MidnightBackground
val TextSecondaryDark = OutlineColor
