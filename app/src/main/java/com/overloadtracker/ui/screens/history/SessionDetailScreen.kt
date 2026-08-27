/**
 * Detailed view of a single workout session with PR badges and CSV export.
 * Refactored with Liquid Glass / Liquid Vitality visual aesthetic.
 */
package com.overloadtracker.ui.screens.history

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.overloadtracker.R
import com.overloadtracker.data.local.entity.SessionSet
import com.overloadtracker.data.local.entity.SessionWithSets
import com.overloadtracker.data.repository.WorkoutSessionRepository
import com.overloadtracker.ui.components.LiquidGlassCard
import com.overloadtracker.ui.components.LiquidMetricCard
import com.overloadtracker.ui.components.LiquidPrimaryButton
import com.overloadtracker.ui.components.LiquidTopAppBar
import com.overloadtracker.ui.navigation.SessionDetailRoute
import com.overloadtracker.ui.theme.CyanAccent
import com.overloadtracker.ui.theme.ElectricViolet
import com.overloadtracker.ui.theme.GlassBorderHighlight
import com.overloadtracker.ui.theme.GlassBorderTopLeft
import com.overloadtracker.ui.theme.GlassSurfaceHigh
import com.overloadtracker.ui.theme.LabelCaps
import com.overloadtracker.ui.theme.MidnightBackground
import com.overloadtracker.ui.theme.NumericData
import com.overloadtracker.ui.theme.ShapeChip
import com.overloadtracker.ui.theme.SunsetRose
import com.overloadtracker.ui.theme.TextOnSurface
import com.overloadtracker.ui.theme.TextOnSurfaceVariant
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
 * Session detail with sets table and single-session CSV export in Liquid Glass style.
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
        modifier = modifier.background(MidnightBackground),
        containerColor = MidnightBackground,
        topBar = {
            LiquidTopAppBar(
                title = session?.session?.groupName.orEmpty().ifEmpty { "SESSION SUMMARY" },
                subtitle = "DETAILED METRICS & LOGS",
                onBackClick = onBack,
                actions = {
                    LiquidPrimaryButton(
                        text = stringResource(R.string.export_session),
                        onClick = {
                            viewModel.exportSession { success ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (success) exportedMessage else "Export failed"
                                    )
                                }
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LiquidMetricCard(
                            label = stringResource(R.string.duration),
                            value = formatDuration(sws.session.endTime - sws.session.startTime),
                            icon = Icons.Default.Timer,
                            accentColor = ElectricViolet,
                            modifier = Modifier.weight(1f)
                        )
                        LiquidMetricCard(
                            label = stringResource(R.string.total_volume),
                            value = WeightUtils.formatWeight(sws.session.totalVolume, WeightUnit.KG),
                            icon = Icons.Default.Speed,
                            accentColor = CyanAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                grouped.forEach { (name, sets) ->
                    item(key = "header-$name") {
                        Text(
                            text = name.uppercase(),
                            style = LabelCaps.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                            color = CyanAccent,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
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
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        highlightBorder = isPr,
        padding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SET ${set.setNumber}".uppercase(),
                    style = LabelCaps.copy(fontSize = 11.sp),
                    color = TextOnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${WeightUtils.formatWeight(set.weight, WeightUnit.KG)} × ${set.reps}",
                    style = NumericData.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    color = TextOnSurface
                )
                set.rpe?.let {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "RPE $it",
                        style = LabelCaps.copy(fontSize = 10.sp),
                        color = ElectricViolet
                    )
                }
            }
            if (isPr) {
                Box(
                    modifier = Modifier
                        .clip(ShapeChip)
                        .background(SunsetRose.copy(alpha = 0.2f))
                        .border(width = 1.dp, color = SunsetRose, shape = ShapeChip)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = SunsetRose,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = stringResource(R.string.pr_badge).uppercase(),
                            style = LabelCaps.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = SunsetRose
                        )
                    }
                }
            }
        }
    }
}
