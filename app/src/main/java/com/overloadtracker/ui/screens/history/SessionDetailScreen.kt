/**
 * Detailed view of a single workout session with PR badges and CSV export.
 * Styled to Liquid Glass specification.
 */
package com.overloadtracker.ui.screens.history

import android.content.Context
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
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
import com.overloadtracker.ui.components.GlassCard
import com.overloadtracker.ui.navigation.SessionDetailRoute
import com.overloadtracker.ui.theme.GlassBorder
import com.overloadtracker.ui.theme.HeadlineLargeMobile
import com.overloadtracker.ui.theme.LabelCaps
import com.overloadtracker.ui.theme.OnSurface
import com.overloadtracker.ui.theme.OnSurfaceVariant
import com.overloadtracker.ui.theme.PRGold
import com.overloadtracker.ui.theme.SecondaryText
import com.overloadtracker.ui.theme.StravaOrange
import com.overloadtracker.ui.theme.SurfaceContainerHighest
import com.overloadtracker.ui.theme.TrueBlack
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

import androidx.compose.material.icons.filled.Share

import com.overloadtracker.util.formatCardioDisplay

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
        if ((set.timeSeconds ?: 0) > 0 || (set.count ?: 0) > 0) return false
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

    fun shareSession() {
        viewModelScope.launch {
            val rows = sessionRepository.buildCsvRows(sessionId)
            val csv = CsvExporter.buildCsv(rows)
            CsvExporter.shareCsvFile(context, csv, prefix = "WorkoutSession")
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
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Text(
                        session?.session?.groupName.orEmpty(),
                        style = HeadlineLargeMobile,
                        color = OnSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = OnSurfaceVariant
                        )
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Share Action Button
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(StravaOrange)
                                .clickable { viewModel.shareSession() }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Share",
                                    style = LabelCaps.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }

                        // Save Download Action Button
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SurfaceContainerHighest)
                                .border(1.dp, GlassBorder, CircleShape)
                                .clickable {
                                    viewModel.exportSession { success ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                if (success) exportedMessage else "Export failed"
                                            )
                                        }
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = StravaOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
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
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("DURATION", style = LabelCaps.copy(fontSize = 10.sp), color = SecondaryText)
                                Text(
                                    formatDuration(sws.session.endTime - sws.session.startTime),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("TOTAL VOLUME", style = LabelCaps.copy(fontSize = 10.sp), color = SecondaryText)
                                Text(
                                    WeightUtils.formatWeight(sws.session.totalVolume, WeightUnit.KG),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = StravaOrange
                                )
                            }
                        }
                    }
                }

                grouped.forEach { (name, sets) ->
                    item(key = "header-$name") {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(sets, key = { it.id }) { set ->
                        SetDetailRow(set = set, isPr = set.id in prSets)
                    }
                }

                item {
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun SetDetailRow(set: SessionSet, isPr: Boolean) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${stringResource(R.string.set_number)} ${set.setNumber}",
                    style = LabelCaps.copy(fontSize = 11.sp),
                    color = SecondaryText
                )
                Spacer(Modifier.height(2.dp))
                val isCardio = (set.timeSeconds ?: 0) > 0 || (set.count ?: 0) > 0
                val displayText = if (isCardio) {
                    formatCardioDisplay(set.timeSeconds, set.count)
                } else {
                    "${WeightUtils.formatWeight(set.weight, WeightUnit.KG)} × ${set.reps}"
                }
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = OnSurface
                )
                set.rpe?.let {
                    Text("RPE $it", style = LabelCaps.copy(fontSize = 10.sp), color = StravaOrange)
                }
            }
            if (isPr) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(PRGold.copy(alpha = 0.2f))
                        .border(1.dp, PRGold, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = PRGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.pr_badge),
                            style = LabelCaps.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = PRGold
                        )
                    }
                }
            }
        }
    }
}

