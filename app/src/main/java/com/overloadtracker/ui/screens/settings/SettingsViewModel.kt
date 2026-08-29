/**
 * ViewModel for app settings and destructive maintenance actions.
 */
package com.overloadtracker.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.overloadtracker.data.preferences.AppThemeMode
import com.overloadtracker.data.preferences.UserPreferencesRepository
import com.overloadtracker.data.repository.ExerciseRepository
import com.overloadtracker.data.repository.WorkoutSessionRepository
import com.overloadtracker.util.Constants
import com.overloadtracker.util.WeightUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import android.content.Context
import com.overloadtracker.mcp.McpServerManager
import com.overloadtracker.mcp.McpService

data class SettingsUiState(
    val weightUnit: WeightUnit = WeightUnit.KG,
    val restSeconds: Int = Constants.DEFAULT_REST_SECONDS,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val mcpEnabled: Boolean = false,
    val mcpPort: Int = 8080,
    val mcpToken: String = "",
    val mcpBindLocalOnly: Boolean = false,
    val mcpIpAddress: String = "127.0.0.1",
    val message: String? = null
)

/**
 * Exposes user preferences and database maintenance operations.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
    private val sessionRepository: WorkoutSessionRepository,
    private val exerciseRepository: ExerciseRepository,
    private val mcpServerManager: McpServerManager
) : ViewModel() {

    private val basicPrefs = combine(
        prefs.weightUnit,
        prefs.defaultRestSeconds,
        prefs.themeMode
    ) { unit, rest, theme -> Triple(unit, rest, theme) }

private data class McpPrefsData(
    val enabled: Boolean,
    val port: Int,
    val token: String,
    val bindLocalOnly: Boolean
)

    private val mcpPrefs = combine(
        prefs.mcpEnabled,
        prefs.mcpPort,
        prefs.mcpToken,
        prefs.mcpBindLocalOnly
    ) { enabled, port, token, localOnly ->
        McpPrefsData(enabled, port, token, localOnly)
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        basicPrefs,
        mcpPrefs
    ) { (unit, rest, theme), (mcpEnabled, mcpPort, mcpToken, mcpBindLocalOnly) ->
        val ip = if (mcpBindLocalOnly) "127.0.0.1" else (mcpServerManager.getLocalIpAddress() ?: "localhost")
        SettingsUiState(
            weightUnit = unit,
            restSeconds = rest,
            themeMode = theme,
            mcpEnabled = mcpEnabled,
            mcpPort = mcpPort,
            mcpToken = mcpToken,
            mcpBindLocalOnly = mcpBindLocalOnly,
            mcpIpAddress = ip
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SettingsUiState()
    )

    fun setWeightUnit(unit: WeightUnit) {
        viewModelScope.launch { prefs.setWeightUnit(unit) }
    }

    fun setRestSeconds(seconds: Int) {
        viewModelScope.launch { prefs.setDefaultRestSeconds(seconds) }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch { prefs.setThemeMode(mode) }
    }

    fun setMcpEnabled(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            prefs.setMcpEnabled(enabled)
            if (enabled) {
                McpService.startService(context)
            } else {
                McpService.stopService(context)
            }
        }
    }

    fun setMcpBindLocalOnly(context: Context, localOnly: Boolean) {
        viewModelScope.launch {
            prefs.setMcpBindLocalOnly(localOnly)
            if (uiState.value.mcpEnabled) {
                McpService.stopService(context)
                McpService.startService(context)
            }
        }
    }

    fun regenerateMcpToken(context: Context) {
        viewModelScope.launch {
            mcpServerManager.revokeAllSessions()
            prefs.regenerateMcpToken()
            if (uiState.value.mcpEnabled) {
                McpService.stopService(context)
                McpService.startService(context)
            }
        }
    }

    fun revokeMcpSessions() {
        mcpServerManager.revokeAllSessions()
    }

    fun clearHistory(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            sessionRepository.clearHistory()
            onDone()
        }
    }

    fun resetDatabase(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            exerciseRepository.resetDatabase()
            onDone()
        }
    }
}
