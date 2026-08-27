/**
 * App settings: units, rest timer, theme, and maintenance actions.
 * Styled with Liquid Glass aesthetic.
 */
package com.overloadtracker.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.overloadtracker.R
import com.overloadtracker.data.preferences.AppThemeMode
import com.overloadtracker.ui.components.GlassCard
import com.overloadtracker.ui.theme.Charcoal
import com.overloadtracker.ui.theme.GlassBorder
import com.overloadtracker.ui.theme.HeadlineLargeMobile
import com.overloadtracker.ui.theme.LabelCaps
import com.overloadtracker.ui.theme.OnSurface
import com.overloadtracker.ui.theme.OnSurfaceVariant
import com.overloadtracker.ui.theme.SecondaryText
import com.overloadtracker.ui.theme.StravaOrange
import com.overloadtracker.ui.theme.SurfaceContainerHighest
import com.overloadtracker.util.Constants
import com.overloadtracker.util.WeightUnit

/**
 * Settings screen for user preferences and destructive maintenance.
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
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { Text(stringResource(R.string.nav_settings), style = HeadlineLargeMobile, color = StravaOrange) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Weight Unit Section
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("WEIGHT UNIT", style = LabelCaps, color = StravaOrange)
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
            }

            // Rest Timer Slider Section
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("DEFAULT REST TIMER", style = LabelCaps, color = StravaOrange)
                        Text(
                            text = "${uiState.restSeconds}s",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )
                    }
                    Slider(
                        value = uiState.restSeconds.toFloat(),
                        onValueChange = { viewModel.setRestSeconds(it.toInt()) },
                        valueRange = Constants.MIN_REST_SECONDS.toFloat()..Constants.MAX_REST_SECONDS.toFloat(),
                        steps = ((Constants.MAX_REST_SECONDS - Constants.MIN_REST_SECONDS) / 15) - 1,
                        colors = SliderDefaults.colors(
                            thumbColor = StravaOrange,
                            activeTrackColor = StravaOrange,
                            inactiveTrackColor = SurfaceContainerHighest
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Theme Options Section
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("THEME", style = LabelCaps, color = StravaOrange)
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
            }

            // Maintenance / Reset Section
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("DATA & MAINTENANCE", style = LabelCaps, color = StravaOrange)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerHighest)
                            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                            .clickable { showClearDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.clear_history),
                            style = LabelCaps.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerHighest)
                            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                            .clickable { showResetDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.reset_database),
                            style = LabelCaps.copy(fontWeight = FontWeight.Bold),
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    if (showClearDialog) {
        ConfirmDialog(
            title = stringResource(R.string.clear_history),
            body = "This permanently deletes all workout history.",
            onConfirm = {
                viewModel.clearHistory()
                showClearDialog = false
            },
            onDismiss = { showClearDialog = false }
        )
    }
    if (showResetDialog) {
        ConfirmDialog(
            title = stringResource(R.string.reset_database),
            body = "This re-imports the exercise library from assets.",
            onConfirm = {
                viewModel.resetDatabase()
                showResetDialog = false
            },
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
            .heightIn(min = 44.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = StravaOrange,
                unselectedColor = SecondaryText
            )
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = OnSurface
        )
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    body: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Charcoal,
        titleContentColor = OnSurface,
        textContentColor = OnSurfaceVariant,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.confirm), color = StravaOrange)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = OnSurfaceVariant)
            }
        }
    )
}
