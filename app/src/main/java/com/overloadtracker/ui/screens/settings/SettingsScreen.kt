/**
 * App settings: units, rest timer, theme, and maintenance actions.
 * Refactored with Liquid Glass / Liquid Vitality visual aesthetic.
 */
package com.overloadtracker.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.overloadtracker.R
import com.overloadtracker.data.preferences.AppThemeMode
import com.overloadtracker.ui.components.LiquidAlertDialog
import com.overloadtracker.ui.components.LiquidGlassCard
import com.overloadtracker.ui.components.LiquidSecondaryButton
import com.overloadtracker.ui.components.LiquidTopAppBar
import com.overloadtracker.ui.theme.CyanAccent
import com.overloadtracker.ui.theme.ElectricViolet
import com.overloadtracker.ui.theme.GlassBorderTopLeft
import com.overloadtracker.ui.theme.LabelCaps
import com.overloadtracker.ui.theme.MidnightBackground
import com.overloadtracker.ui.theme.SunsetRose
import com.overloadtracker.ui.theme.TextOnSurface
import com.overloadtracker.ui.theme.TextOnSurfaceVariant
import com.overloadtracker.util.Constants
import com.overloadtracker.util.WeightUnit

/**
 * Settings screen for user preferences and maintenance in Liquid Glass style.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.background(MidnightBackground),
        containerColor = MidnightBackground,
        topBar = {
            LiquidTopAppBar(
                title = "SYSTEM SETTINGS",
                subtitle = "PREFERENCES & MAINTENANCE"
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Weight Unit
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                padding = 16.dp
            ) {
                Text(
                    text = "WEIGHT UNIT",
                    style = LabelCaps.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                    color = CyanAccent,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                RadioRow(
                    label = stringResource(R.string.units_kg),
                    selected = uiState.weightUnit == WeightUnit.KG,
                    onClick = { viewModel.setWeightUnit(WeightUnit.KG) }
                )
                RadioRow(
                    label = stringResource(R.string.units_lb),
                    selected = uiState.weightUnit == WeightUnit.LB,
                    onClick = { viewModel.setWeightUnit(WeightUnit.LB) }
                )
            }

            // Section 2: Default Rest Timer
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                padding = 16.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.rest_timer).uppercase(),
                        style = LabelCaps.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                        color = CyanAccent
                    )
                    Text(
                        text = "${uiState.restSeconds} SECONDS",
                        style = LabelCaps.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                        color = ElectricViolet
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Slider(
                    value = uiState.restSeconds.toFloat(),
                    onValueChange = { viewModel.setRestSeconds(it.toInt()) },
                    valueRange = Constants.MIN_REST_SECONDS.toFloat()..Constants.MAX_REST_SECONDS.toFloat(),
                    steps = ((Constants.MAX_REST_SECONDS - Constants.MIN_REST_SECONDS) / 15) - 1,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanAccent,
                        activeTrackColor = ElectricViolet,
                        inactiveTrackColor = GlassBorderTopLeft
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Section 3: Appearance / Theme Mode
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                padding = 16.dp
            ) {
                Text(
                    text = "APPEARANCE MODE",
                    style = LabelCaps.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                    color = CyanAccent,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                RadioRow(
                    label = stringResource(R.string.theme_system),
                    selected = uiState.themeMode == AppThemeMode.SYSTEM,
                    onClick = { viewModel.setThemeMode(AppThemeMode.SYSTEM) }
                )
                RadioRow(
                    label = stringResource(R.string.theme_light),
                    selected = uiState.themeMode == AppThemeMode.LIGHT,
                    onClick = { viewModel.setThemeMode(AppThemeMode.LIGHT) }
                )
                RadioRow(
                    label = stringResource(R.string.theme_dark),
                    selected = uiState.themeMode == AppThemeMode.DARK,
                    onClick = { viewModel.setThemeMode(AppThemeMode.DARK) }
                )
            }

            // Section 4: Maintenance Actions
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                padding = 16.dp
            ) {
                Text(
                    text = "MAINTENANCE & RESET",
                    style = LabelCaps.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                    color = SunsetRose,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LiquidSecondaryButton(
                        text = stringResource(R.string.clear_history),
                        onClick = { showClearDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                    LiquidSecondaryButton(
                        text = stringResource(R.string.reset_database),
                        onClick = { showResetDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        LiquidAlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = stringResource(R.string.clear_history),
            bodyText = "This permanently deletes all workout history.",
            confirmButtonText = stringResource(R.string.confirm),
            onConfirm = {
                viewModel.clearHistory()
                showClearDialog = false
            },
            dismissButtonText = stringResource(R.string.cancel),
            onDismiss = { showClearDialog = false }
        )
    }

    if (showResetDialog) {
        LiquidAlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = stringResource(R.string.reset_database),
            bodyText = "This re-imports the exercise library from assets.",
            confirmButtonText = stringResource(R.string.confirm),
            onConfirm = {
                viewModel.resetDatabase()
                showResetDialog = false
            },
            dismissButtonText = stringResource(R.string.cancel),
            onDismiss = { showResetDialog = false }
        )
    }
}

@Composable
private fun RadioRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = ElectricViolet,
                unselectedColor = TextOnSurfaceVariant
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = TextOnSurface,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
