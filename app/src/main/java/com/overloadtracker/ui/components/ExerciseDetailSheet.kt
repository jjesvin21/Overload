/**
 * Modal bottom sheet showing full exercise details with optional actions.
 */
package com.overloadtracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.overloadtracker.R
import com.overloadtracker.data.local.entity.Exercise
import com.overloadtracker.util.titleCase

/**
 * Bottom sheet with GIF, metadata, instructions, and optional CTAs.
 *
 * @param exercise exercise to display; when null the sheet is hidden.
 * @param onDismiss invoked when the sheet is dismissed.
 * @param onAddToGroup optional handler for "Add to Group".
 * @param onViewProgress optional handler for "View Progress".
 * @param modifier optional layout modifier.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExerciseDetailSheet(
    exercise: Exercise?,
    onDismiss: () -> Unit,
    onAddToGroup: (() -> Unit)? = null,
    onViewProgress: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (exercise == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.headlineMedium
            )
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("file:///android_asset/${exercise.gifPath}")
                    .crossfade(true)
                    .build(),
                contentDescription = exercise.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(titleCase(exercise.category)) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(titleCase(exercise.equipment)) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(titleCase(exercise.target)) }
                )
            }
            Text(
                text = exercise.instructions,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (onAddToGroup != null || onViewProgress != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    onAddToGroup?.let { handler ->
                        Button(
                            onClick = handler,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Text(stringResource(R.string.add_to_group))
                        }
                    }
                    onViewProgress?.let { handler ->
                        OutlinedButton(
                            onClick = handler,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.view_progress))
                        }
                    }
                }
            }
        }
    }
}
