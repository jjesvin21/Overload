/**
 * Main activity — edge-to-edge Compose host with theme and navigation.
 */
package com.overloadtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.overloadtracker.data.preferences.UserPreferencesRepository
import com.overloadtracker.data.repository.ExerciseRepository
import com.overloadtracker.ui.navigation.AppNavigation
import com.overloadtracker.ui.theme.OverloadTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var prefs: UserPreferencesRepository
    @Inject lateinit var exerciseRepository: ExerciseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            exerciseRepository.seedIfNeeded()
        }

        setContent {
            val themeMode by prefs.themeMode.collectAsState(
                initial = com.overloadtracker.data.preferences.AppThemeMode.SYSTEM
            )
            val hasOnboarded by prefs.hasOnboarded.collectAsState(
                initial = false
            )
            val isSeeded by prefs.isSeeded.collectAsState(
                initial = false
            )

            OverloadTrackerTheme(themeMode = themeMode) {
                if (!isSeeded) {
                    SeedingSplash()
                } else {
                    AppNavigation(
                        hasOnboarded = hasOnboarded,
                        onMarkOnboarded = {
                            lifecycleScope.launch { prefs.setOnboarded(true) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SeedingSplash() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                text = stringResource(R.string.seeding),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
