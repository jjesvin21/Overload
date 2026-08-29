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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = LocalClipboardManager.current
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

            // Agentic MCP Integration Section
            val context = androidx.compose.ui.platform.LocalContext.current
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
                        Column {
                            Text("AGENTIC MCP INTEGRATION", style = LabelCaps, color = StravaOrange)
                            Text(
                                text = if (uiState.mcpEnabled) "Server Active" else "Server Disabled",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (uiState.mcpEnabled) Color(0xFF4CAF50) else SecondaryText
                            )
                        }
                        androidx.compose.material3.Switch(
                            checked = uiState.mcpEnabled,
                            onCheckedChange = { viewModel.setMcpEnabled(context, it) },
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = StravaOrange
                            )
                        )
                    }

                    if (uiState.mcpEnabled) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceContainerHighest, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isWifiMode = !uiState.mcpBindLocalOnly
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("CONNECT OVER WI-FI", style = LabelCaps.copy(fontSize = 10.sp), color = StravaOrange)
                                    Text(
                                        text = if (isWifiMode) "Enabled (LAN Subnet Access)" else "Disabled (USB ADB Cable Only)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurface
                                    )
                                }
                                androidx.compose.material3.Switch(
                                    checked = isWifiMode,
                                    onCheckedChange = { viewModel.setMcpBindLocalOnly(context, !it) },
                                    colors = androidx.compose.material3.SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = StravaOrange
                                    )
                                )
                            }

                            if (isWifiMode) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0x22FF9800), RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFFFF9800), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("⚠️ ", fontSize = 14.sp)
                                        Text(
                                            text = "Port ${uiState.mcpPort} is exposed to all devices on this Wi-Fi network. Please turn this off after completing your AI analysis session.",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = Color(0xFFFFCC80)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Active Endpoint:", style = LabelCaps.copy(fontSize = 10.sp), color = SecondaryText)
                                    Text(
                                        text = "http://${uiState.mcpIpAddress}:${uiState.mcpPort}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = OnSurface
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString("http://${uiState.mcpIpAddress}:${uiState.mcpPort}"))
                                        android.widget.Toast.makeText(context, "Endpoint copied", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Copy", style = LabelCaps.copy(fontSize = 10.sp), color = StravaOrange)
                                }
                            }

                            Spacer(Modifier.height(4.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Master Secret (Handshake Key):", style = LabelCaps.copy(fontSize = 10.sp), color = SecondaryText)
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0x22000000), RoundedCornerShape(8.dp))
                                        .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                                        .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = uiState.mcpToken,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            color = StravaOrange,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(uiState.mcpToken))
                                                android.widget.Toast.makeText(context, "Master Secret copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                                            },
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Copy Key", style = LabelCaps.copy(fontSize = 10.sp), color = StravaOrange)
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = {
                                            viewModel.revokeMcpSessions()
                                            android.widget.Toast.makeText(context, "All active sessions revoked", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Revoke Sessions", style = LabelCaps.copy(fontSize = 10.sp), color = OnSurfaceVariant)
                                    }
                                    Spacer(Modifier.width(4.dp))
                                    TextButton(
                                        onClick = {
                                            viewModel.regenerateMcpToken(context)
                                            android.widget.Toast.makeText(context, "New Master Secret generated", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Roll Secret", style = LabelCaps.copy(fontSize = 10.sp), color = StravaOrange)
                                    }
                                }
                            }

                            Spacer(Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("USB Cable ADB Forward Command:", style = LabelCaps.copy(fontSize = 10.sp), color = SecondaryText)
                                    Text(
                                        text = "adb forward tcp:${uiState.mcpPort} tcp:${uiState.mcpPort}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                        color = OnSurfaceVariant
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString("adb forward tcp:${uiState.mcpPort} tcp:${uiState.mcpPort}"))
                                        android.widget.Toast.makeText(context, "ADB command copied", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Copy", style = LabelCaps.copy(fontSize = 10.sp), color = OnSurfaceVariant)
                                }
                            }
                        }
                    }
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
