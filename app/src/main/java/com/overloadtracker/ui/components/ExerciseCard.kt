package com.overloadtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.overloadtracker.data.local.entity.Exercise
import com.overloadtracker.ui.theme.CyanAccent
import com.overloadtracker.ui.theme.LabelCaps
import com.overloadtracker.ui.theme.MidnightSurfaceContainerHighest
import com.overloadtracker.ui.theme.TextOnSurface
import com.overloadtracker.ui.theme.TextOnSurfaceVariant
import com.overloadtracker.util.titleCase

/**
 * Grid card for an exercise: glass surface, thumbnail, name, target, equipment.
 */
@Composable
fun ExerciseCard(
    exercise: Exercise,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false
) {
    val context = LocalContext.current

    LiquidGlassCard(
        modifier = modifier,
        onClick = onClick,
        highlightBorder = selected,
        padding = 12.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.1f)
                .clip(RoundedCornerShape(16.dp))
                .background(MidnightSurfaceContainerHighest.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("file:///android_asset/${exercise.imagePath}")
                    .crossfade(true)
                    .build(),
                contentDescription = exercise.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = exercise.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = TextOnSurface
        )

        Spacer(Modifier.height(2.dp))

        Text(
            text = titleCase(exercise.target).uppercase(),
            style = LabelCaps,
            color = CyanAccent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = titleCase(exercise.equipment),
            style = MaterialTheme.typography.bodySmall,
            color = TextOnSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
