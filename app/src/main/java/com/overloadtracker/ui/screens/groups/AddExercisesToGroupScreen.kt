/**
 * Multi-select exercise picker to add exercises to a workout group styled with Liquid Glass design.
 */
package com.overloadtracker.ui.screens.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
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
import com.overloadtracker.data.local.entity.Exercise
import com.overloadtracker.data.repository.ExerciseRepository
import com.overloadtracker.data.repository.WorkoutGroupRepository
import com.overloadtracker.ui.components.FilterChips
import com.overloadtracker.ui.components.GlassCard
import com.overloadtracker.ui.navigation.AddExercisesRoute
import com.overloadtracker.ui.theme.Charcoal
import com.overloadtracker.ui.theme.GlassBorder
import com.overloadtracker.ui.theme.HeadlineLargeMobile
import com.overloadtracker.ui.theme.LabelCaps
import com.overloadtracker.ui.theme.OnSurface
import com.overloadtracker.ui.theme.OnSurfaceVariant
import com.overloadtracker.ui.theme.StravaOrange
import com.overloadtracker.ui.theme.SurfaceContainerHighest
import com.overloadtracker.ui.theme.TrueBlack
import com.overloadtracker.util.Constants
import com.overloadtracker.util.titleCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddExercisesViewModel @Inject constructor(
    savedStateHandle: androidx.lifecycle.SavedStateHandle,
    private val exerciseRepository: ExerciseRepository,
    private val groupRepository: WorkoutGroupRepository
) : ViewModel() {

    private val groupId = savedStateHandle.toRoute<AddExercisesRoute>().groupId

    private val _query = MutableStateFlow("")
    private val _categories = MutableStateFlow<Set<String>>(emptySet())
    private val _selected = MutableStateFlow<Set<String>>(emptySet())

    val selected: StateFlow<Set<String>> = _selected.asStateFlow()
    val categories: StateFlow<Set<String>> = _categories.asStateFlow()

    val exercises: StateFlow<List<Exercise>> = combine(
        _query.debounce(Constants.SEARCH_DEBOUNCE_MS),
        _categories
    ) { q, cats -> q to cats }
        .flatMapLatest { (q, cats) ->
            exerciseRepository.observeFiltered(q, cats, null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) {
        _query.value = value
    }

    fun toggleCategory(category: String) {
        _categories.update { if (category in it) it - category else it + category }
    }

    fun toggleSelection(exerciseId: String) {
        _selected.update { if (exerciseId in it) it - exerciseId else it + exerciseId }
    }

    fun addSelected(onDone: () -> Unit) {
        viewModelScope.launch {
            val ids = _selected.value.toList()
            if (ids.isNotEmpty()) {
                groupRepository.addExercises(groupId, ids)
            }
            onDone()
        }
    }
}

/**
 * Multi-select library screen with bottom bar confirmation styled to Liquid Glass spec.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExercisesToGroupScreen(
    onBack: () -> Unit,
    onAdded: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddExercisesViewModel = hiltViewModel()
) {
    val exercises by viewModel.exercises.collectAsState()
    val selected by viewModel.selected.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var query by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Text(
                        stringResource(R.string.add_exercises),
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
                }
            )
        },
        bottomBar = {
            if (selected.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .shadow(12.dp, CircleShape, spotColor = StravaOrange)
                            .clip(CircleShape)
                            .background(StravaOrange)
                            .clickable { viewModel.addSelected(onAdded) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.add_selected, selected.size),
                            style = LabelCaps.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                            color = TrueBlack
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = {
                            query = it
                            viewModel.setQuery(it)
                        },
                        placeholder = { Text(stringResource(R.string.search_exercises), color = OnSurfaceVariant) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = StravaOrange) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Charcoal,
                            unfocusedContainerColor = Charcoal,
                            focusedBorderColor = StravaOrange,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    FilterChips(
                        selectedCategories = categories,
                        onToggle = viewModel::toggleCategory
                    )
                }
            }
            items(exercises, key = { it.id }) { exercise ->
                SelectableExerciseRow(
                    exercise = exercise,
                    checked = exercise.id in selected,
                    onToggle = { viewModel.toggleSelection(exercise.id) }
                )
            }
        }
    }
}

@Composable
private fun SelectableExerciseRow(
    exercise: Exercise,
    checked: Boolean,
    onToggle: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onToggle,
        shape = RoundedCornerShape(14.dp),
        backgroundColor = if (checked) StravaOrange.copy(alpha = 0.18f) else Charcoal,
        borderColor = if (checked) StravaOrange else GlassBorder,
        hasGlow = checked
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (checked) StravaOrange else SurfaceContainerHighest)
                    .border(1.dp, if (checked) StravaOrange else GlassBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (checked) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = TrueBlack,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = OnSurface
                )
                Text(
                    text = titleCase(exercise.target),
                    style = LabelCaps.copy(fontSize = 10.sp),
                    color = StravaOrange
                )
            }
        }
    }
}
