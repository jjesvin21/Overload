/**
 * ViewModel for workout history list and CSV export.
 */
package com.overloadtracker.ui.screens.history

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.overloadtracker.data.model.WorkoutSummary
import com.overloadtracker.data.repository.WorkoutSessionRepository
import com.overloadtracker.util.CsvExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val summaries: List<WorkoutSummary> = emptyList(),
    val exportMessage: String? = null
)

/**
 * Observes session summaries and handles bulk CSV export.
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val sessionRepository: WorkoutSessionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _exportMessage = MutableStateFlow<String?>(null)
    val exportMessage: StateFlow<String?> = _exportMessage.asStateFlow()

    val summaries: StateFlow<List<WorkoutSummary>> = sessionRepository.observeSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun exportAll() {
        viewModelScope.launch {
            val rows = sessionRepository.buildCsvRows()
            val csv = CsvExporter.buildCsv(rows)
            val uri = CsvExporter.saveToDownloads(context, csv)
            _exportMessage.value = if (uri != null) "exported" else "failed"
        }
    }

    fun clearExportMessage() {
        _exportMessage.value = null
    }
}
