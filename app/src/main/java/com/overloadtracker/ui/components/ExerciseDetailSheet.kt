/**
 * Modal bottom sheet showing full exercise details with optional actions.
 */
package com.overloadtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.overloadtracker.R
import com.overloadtracker.data.local.entity.Exercise
import com.overloadtracker.ui.theme.CyanAccent
import com.overloadtracker.ui.theme.GlassBorderTopLeft
import com.overloadtracker.ui.theme.GlassSurfaceHigh
import com.overloadtracker.ui.theme.LabelCaps
import com.overloadtracker.ui.theme.MidnightSurfaceContainerHighest
import com.overloadtracker.ui.theme.ShapeChip
import com.overloadtracker.ui.theme.ShapeGlassCard
import com.overloadtracker.ui.theme.TextOnSurface
import com.overloadtracker.ui.theme.TextOnSurfaceVariant
import com.overloadtracker.util.titleCase

/**
 * Bottom sheet with exercise GIF, metadata tags, instructions, and CTAs.
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

    LiquidBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextOnSurface
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(ShapeGlassCard)
                    .background(MidnightSurfaceContainerHighest.copy(alpha = 0.5f))
                    .border(width = 1.dp, color = GlassBorderTopLeft, shape = ShapeGlassCard),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data("file:///android_asset/${exercise.gifPath}")
                        .decoderFactory(
                            if (android.os.Build.VERSION.SDK_INT >= 28) {
                                coil.decode.ImageDecoderDecoder.Factory()
                            } else {
                                coil.decode.GifDecoder.Factory()
                            }
                        )
                        .crossfade(true)
                        .build(),
                    contentDescription = exercise.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(exercise.category, exercise.equipment, exercise.target).forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(ShapeChip)
                            .background(GlassSurfaceHigh)
                            .border(width = 1.dp, color = GlassBorderTopLeft, shape = ShapeChip)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = titleCase(tag).uppercase(),
                            style = LabelCaps,
                            color = CyanAccent
                        )
                    }
                }
            }

            Text(
                text = exercise.instructions,
                style = MaterialTheme.typography.bodyMedium,
                color = TextOnSurfaceVariant
            )

            if (onAddToGroup != null || onViewProgress != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    onAddToGroup?.let { handler ->
                        LiquidPrimaryButton(
                            text = stringResource(R.string.add_to_group),
                            onClick = handler,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    onViewProgress?.let { handler ->
                        LiquidSecondaryButton(
                            text = stringResource(R.string.view_progress),
                            onClick = handler,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
