/**
 * Circular rest countdown overlay with skip, extend, and reset controls.
 */
package com.overloadtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.overloadtracker.R

/**
 * Full-screen rest timer overlay.
 *
 * @param secondsRemaining seconds left in the countdown.
 * @param totalSeconds total duration used for progress calculation.
 * @param onSkip dismiss the timer early.
 * @param onAdd30 add 30 seconds to the remaining time.
 * @param onReset restart the timer from [totalSeconds].
 * @param modifier optional layout modifier.
 */
@Composable
fun RestTimer(
    secondsRemaining: Int,
    totalSeconds: Int,
    onSkip: () -> Unit,
    onAdd30: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (totalSeconds > 0) {
        secondsRemaining.toFloat() / totalSeconds.toFloat()
    } else {
        0f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = stringResource(R.string.rest_timer),
                style = MaterialTheme.typography.headlineMedium
            )
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.size(180.dp),
                    strokeWidth = 8.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    color = MaterialTheme.colorScheme.primary
                )
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatRestTime(secondsRemaining),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.skip))
                }
                Button(
                    onClick = onAdd30,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.add_30s))
                }
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.reset))
                }
            }
        }
    }
}

private fun formatRestTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%d:%02d", m, s)
}
