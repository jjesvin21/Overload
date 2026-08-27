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

data class SettingsUiState(
    val weightUnit: WeightUnit = WeightUnit.KG,
    val restSeconds: Int = Constants.DEFAULT_REST_SECONDS,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val message: String? = null
)

/**
 * Exposes user preferences and database maintenance operations.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
    private val sessionRepository: WorkoutSessionRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        prefs.weightUnit,
        prefs.defaultRestSeconds,
        prefs.themeMode
    ) { unit, rest, theme ->
        SettingsUiState(weightUnit = unit, restSeconds = rest, themeMode = theme)
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
