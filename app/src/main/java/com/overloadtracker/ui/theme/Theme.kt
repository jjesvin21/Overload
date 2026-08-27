/**
 * Material3 theme with fitness brand colors and [AppThemeMode] support.
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
    primary = OrangeAccent,
    onPrimary = TextPrimaryLight,
    primaryContainer = OrangeAccentDark,
    onPrimaryContainer = TextPrimaryLight,
    secondary = OrangeAccentLight,
    onSecondary = TextPrimaryLight,
    background = NavyDeep,
    onBackground = TextPrimaryLight,
    surface = NavySurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = NavySurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = OrangeAccent,
    onPrimary = TextPrimaryLight,
    primaryContainer = OrangeAccentLight,
    onPrimaryContainer = TextPrimaryDark,
    secondary = NavyDeep,
    onSecondary = TextPrimaryLight,
    background = Color(0xFFF5F7FA),
    onBackground = TextPrimaryDark,
    surface = Color.White,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFFE8EDF2),
    onSurfaceVariant = TextSecondaryDark,
    error = ErrorRed
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
        content = content
    )
}
