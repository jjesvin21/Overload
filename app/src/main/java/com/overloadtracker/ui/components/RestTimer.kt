/**
 * Circular rest countdown overlay with Liquid Glass aesthetic and Strava Orange accents.
 */
package com.overloadtracker.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.overloadtracker.R
import com.overloadtracker.ui.theme.Charcoal
import com.overloadtracker.ui.theme.DisplayMetrics
import com.overloadtracker.ui.theme.GlassBorder
import com.overloadtracker.ui.theme.HeadlineLargeMobile
import com.overloadtracker.ui.theme.LabelCaps
import com.overloadtracker.ui.theme.OnSurface
import com.overloadtracker.ui.theme.OnSurfaceVariant
import com.overloadtracker.ui.theme.StravaOrange
import com.overloadtracker.ui.theme.SurfaceContainerHighest
import com.overloadtracker.ui.theme.TrueBlack

/**
 * Full-screen rest timer overlay styled with Liquid Glass design.
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
            .background(TrueBlack.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            hasGlow = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.rest_timer),
                    style = HeadlineLargeMobile,
                    color = OnSurface
                )
                Text(
                    text = "Stay warm. Focus on deep breathing.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )

                // Circular Progress Counter
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.size(190.dp),
                        strokeWidth = 10.dp,
                        trackColor = SurfaceContainerHighest,
                        color = StravaOrange
                    )
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(Charcoal)
                            .border(1.dp, GlassBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = formatRestTime(secondsRemaining),
                            style = DisplayMetrics.copy(fontSize = 38.sp, fontWeight = FontWeight.Bold),
                            color = StravaOrange
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Control Action Buttons Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Skip Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerHighest)
                            .border(1.dp, GlassBorder, CircleShape)
                            .clickable(onClick = onSkip),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.skip),
                            style = LabelCaps.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )
                    }

                    // +30s Button
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
                            .shadow(8.dp, CircleShape, spotColor = StravaOrange)
                            .clip(CircleShape)
                            .background(StravaOrange)
                            .clickable(onClick = onAdd30),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.add_30s),
                            style = LabelCaps.copy(fontWeight = FontWeight.Bold),
                            color = TrueBlack
                        )
                    }

                    // Reset Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerHighest)
                            .border(1.dp, GlassBorder, CircleShape)
                            .clickable(onClick = onReset),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.reset),
                            style = LabelCaps.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )
                    }
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
