/**
 * App settings: units, rest timer, theme, and maintenance actions.
 */
package com.overloadtracker.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.overloadtracker.R
import com.overloadtracker.data.preferences.AppThemeMode
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
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_settings)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Weight Unit", style = MaterialTheme.typography.titleMedium)
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

            HorizontalDivider()

            Text(
                text = "${stringResource(R.string.rest_timer)}: ${uiState.restSeconds}s",
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = uiState.restSeconds.toFloat(),
                onValueChange = { viewModel.setRestSeconds(it.toInt()) },
                valueRange = Constants.MIN_REST_SECONDS.toFloat()..Constants.MAX_REST_SECONDS.toFloat(),
                steps = ((Constants.MAX_REST_SECONDS - Constants.MIN_REST_SECONDS) / 15) - 1,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            Text("Theme", style = MaterialTheme.typography.titleMedium)
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

            HorizontalDivider()

            OutlinedButton(
                onClick = { showClearDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text(stringResource(R.string.clear_history))
            }
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text(stringResource(R.string.reset_database))
            }
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
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, modifier = Modifier.padding(start = 8.dp))
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
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
