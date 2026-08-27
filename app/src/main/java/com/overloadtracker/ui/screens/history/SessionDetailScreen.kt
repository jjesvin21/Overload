/**
 * Detailed view of a single workout session with PR badges and CSV export.
 */
package com.overloadtracker.ui.screens.history

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.overloadtracker.R
import com.overloadtracker.data.local.entity.SessionSet
import com.overloadtracker.data.local.entity.SessionWithSets
import com.overloadtracker.data.repository.WorkoutSessionRepository
import com.overloadtracker.ui.navigation.SessionDetailRoute
import com.overloadtracker.util.CsvExporter
import com.overloadtracker.util.WeightUnit
import com.overloadtracker.util.WeightUtils
import com.overloadtracker.util.formatDuration
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    savedStateHandle: androidx.lifecycle.SavedStateHandle,
    private val sessionRepository: WorkoutSessionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val sessionId = savedStateHandle.toRoute<SessionDetailRoute>().sessionId

    val session: StateFlow<SessionWithSets?> = sessionRepository.observeSession(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val prCache = mutableMapOf<String, Double?>()

    suspend fun isPr(set: SessionSet): Boolean {
        val pr = prCache.getOrPut(set.exerciseId) {
            sessionRepository.getPRWeight(set.exerciseId)
        }
        return pr != null && set.weight >= pr
    }

    fun exportSession(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val rows = sessionRepository.buildCsvRows(sessionId)
            val csv = CsvExporter.buildCsv(rows)
            val uri = CsvExporter.saveToDownloads(context, csv, prefix = "WorkoutSession")
            onResult(uri != null)
        }
    }
}

/**
 * Session detail with sets table and single-session CSV export.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SessionDetailViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var prSets by remember { mutableStateOf<Set<Long>>(emptySet()) }
    val exportedMessage = stringResource(R.string.csv_exported)

    LaunchedEffect(session) {
        session?.sets?.let { sets ->
            val prs = mutableSetOf<Long>()
            sets.forEach { set ->
                if (viewModel.isPr(set)) prs.add(set.id)
            }
            prSets = prs
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(session?.session?.groupName.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.exportSession { success ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (success) exportedMessage else "Export failed"
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .heightIn(min = 48.dp)
                    ) {
                        Text(stringResource(R.string.export_session))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        session?.let { sws ->
            val grouped = sws.sets.groupBy { it.exerciseName }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.duration) + ": " +
                            formatDuration(sws.session.endTime - sws.session.startTime),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stringResource(R.string.total_volume) + ": " +
                            WeightUtils.formatWeight(sws.session.totalVolume, WeightUnit.KG),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
                grouped.forEach { (name, sets) ->
                    item(key = "header-$name") {
                        Text(name, style = MaterialTheme.typography.titleMedium)
                    }
                    items(sets, key = { it.id }) { set ->
                        SetDetailRow(set = set, isPr = set.id in prSets)
                    }
                }
            }
        }
    }
}

@Composable
private fun SetDetailRow(set: SessionSet, isPr: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${stringResource(R.string.set_number)} ${set.setNumber}",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "${WeightUtils.formatWeight(set.weight, WeightUnit.KG)} × ${set.reps}",
                    style = MaterialTheme.typography.bodyLarge
                )
                set.rpe?.let {
                    Text("RPE $it", style = MaterialTheme.typography.bodySmall)
                }
            }
            if (isPr) {
                AssistChip(
                    onClick = {},
                    label = { Text(stringResource(R.string.pr_badge)) }
                )
            }
        }
    }
}

