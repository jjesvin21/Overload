/**
 * DataStore-backed user preferences (units, rest timer, theme, onboarding).
 */
package com.overloadtracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.overloadtracker.util.Constants
import com.overloadtracker.util.WeightUnit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

enum class AppThemeMode { SYSTEM, LIGHT, DARK }

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val UNIT = stringPreferencesKey("weight_unit")
        val REST = intPreferencesKey("default_rest_seconds")
        val THEME = stringPreferencesKey("theme_mode")
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val SEEDED = booleanPreferencesKey("db_seeded")
        val DRAFT = stringPreferencesKey("workout_draft_json")
    }

    val weightUnit: Flow<WeightUnit> = context.dataStore.data.map {
        when (it[Keys.UNIT]) {
            "LB" -> WeightUnit.LB
            else -> WeightUnit.KG
        }
    }

    val defaultRestSeconds: Flow<Int> = context.dataStore.data.map {
        it[Keys.REST] ?: Constants.DEFAULT_REST_SECONDS
    }

    val themeMode: Flow<AppThemeMode> = context.dataStore.data.map {
        when (it[Keys.THEME]) {
            "LIGHT" -> AppThemeMode.LIGHT
            "DARK" -> AppThemeMode.DARK
            else -> AppThemeMode.SYSTEM
        }
    }

    val hasOnboarded: Flow<Boolean> = context.dataStore.data.map { it[Keys.ONBOARDED] ?: false }

    val isSeeded: Flow<Boolean> = context.dataStore.data.map { it[Keys.SEEDED] ?: false }

    val workoutDraft: Flow<String?> = context.dataStore.data.map { it[Keys.DRAFT] }

    suspend fun setWeightUnit(unit: WeightUnit) {
        context.dataStore.edit { it[Keys.UNIT] = unit.name }
    }

    suspend fun setDefaultRestSeconds(seconds: Int) {
        context.dataStore.edit {
            it[Keys.REST] = seconds.coerceIn(Constants.MIN_REST_SECONDS, Constants.MAX_REST_SECONDS)
        }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { it[Keys.THEME] = mode.name }
    }

    suspend fun setOnboarded(value: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDED] = value }
    }

    suspend fun setSeeded(value: Boolean) {
        context.dataStore.edit { it[Keys.SEEDED] = value }
    }

    suspend fun saveDraft(json: String?) {
        context.dataStore.edit {
            if (json == null) it.remove(Keys.DRAFT) else it[Keys.DRAFT] = json
        }
    }
}
