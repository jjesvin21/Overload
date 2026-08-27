/**
 * Material3 theme with Apex Athletic Liquid Glass brand colors.
 */
package com.overloadtracker.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.overloadtracker.data.preferences.AppThemeMode

private val ApexDarkColorScheme = darkColorScheme(
    primary = StravaOrange,
    onPrimary = TrueBlack,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnSurface,
    secondary = SecondaryText,
    onSecondary = TrueBlack,
    secondaryContainer = SurfaceContainerHigh,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = PRGold,
    onTertiary = TrueBlack,
    background = TrueBlack,
    onBackground = OnSurface,
    surface = SurfaceDark,
    onSurface = OnSurface,
    surfaceVariant = SurfaceContainerHighest,
    onSurfaceVariant = OnSurfaceVariant,
    error = ErrorRed,
    onError = TrueBlack,
    outline = GlassBorder,
    outlineVariant = SurfaceContainerHigh
)

/**
 * Root theme composable using Apex Athletic Liquid Glass aesthetic.
 */
@Composable
fun OverloadTrackerTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = ApexDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = TrueBlack.toArgb()
            window.navigationBarColor = TrueBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
