/**
 * Material3 theme configured with Liquid Glass / Liquid Vitality design tokens.
 * Supports dark and light modes with system preference fallback.
 */
package com.overloadtracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.overloadtracker.data.preferences.AppThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = ElectricViolet,
    onPrimary = ElectricVioletOnPrimary,
    primaryContainer = ElectricVioletContainer,
    onPrimaryContainer = ElectricVioletOnContainer,
    inversePrimary = ElectricVioletInverse,
    secondary = CyanAccent,
    onSecondary = CyanOnSecondary,
    secondaryContainer = CyanAccentContainer,
    onSecondaryContainer = CyanOnContainer,
    tertiary = SunsetRose,
    onTertiary = SunsetRoseOnTertiary,
    tertiaryContainer = SunsetRoseContainer,
    onTertiaryContainer = SunsetRoseOnContainer,
    background = MidnightBackground,
    onBackground = TextOnSurface,
    surface = MidnightSurface,
    onSurface = TextOnSurface,
    surfaceVariant = MidnightSurfaceVariant,
    onSurfaceVariant = TextOnSurfaceVariant,
    surfaceTint = ElectricViolet,
    inverseSurface = TextOnSurface,
    inverseOnSurface = TextInverseOnSurface,
    outline = OutlineColor,
    outlineVariant = OutlineVariantColor,
    error = ErrorRed,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricVioletInverse,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9DDFF),
    onPrimaryContainer = Color(0xFF23005C),
    secondary = Color(0xFF00687A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFACEDFF),
    onSecondaryContainer = Color(0xFF001F26),
    tertiary = Color(0xFF92002A),
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFCBD5E1),
    error = Color(0xFFBA1A1A)
)

/**
 * Root theme composable honoring user [themeMode] preference.
 */
@Composable
fun OverloadTrackerTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
