/**
 * Circular rest countdown overlay with skip, extend, and reset controls.
 */
package com.overloadtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.overloadtracker.ui.theme.CyanAccent
import com.overloadtracker.ui.theme.DisplayMetrics
import com.overloadtracker.ui.theme.ElectricViolet
import com.overloadtracker.ui.theme.GlassBorderTopLeft
import com.overloadtracker.ui.theme.GlassOverlayDark
import com.overloadtracker.ui.theme.LabelCaps
import com.overloadtracker.ui.theme.MidnightSurfaceContainer
import com.overloadtracker.ui.theme.MidnightSurfaceContainerHigh
import com.overloadtracker.ui.theme.TextOnSurface
import com.overloadtracker.ui.theme.TextOnSurfaceVariant

/**
 * Full-screen rest timer overlay in Liquid Glass style.
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
            .background(GlassOverlayDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Text(
                text = stringResource(R.string.rest_timer).uppercase(),
                style = LabelCaps.copy(fontSize = 14.sp),
                color = TextOnSurfaceVariant
            )

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.size(220.dp),
                    strokeWidth = 10.dp,
                    trackColor = MidnightSurfaceContainerHigh.copy(alpha = 0.5f),
                    color = ElectricViolet
                )

                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(CircleShape)
                        .background(MidnightSurfaceContainer.copy(alpha = 0.9f))
                        .border(width = 1.dp, color = GlassBorderTopLeft, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatRestTime(secondsRemaining),
                        style = DisplayMetrics.copy(
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = CyanAccent
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                LiquidSecondaryButton(
                    text = stringResource(R.string.skip),
                    onClick = onSkip
                )

                LiquidPrimaryButton(
                    text = stringResource(R.string.add_30s),
                    onClick = onAdd30
                )

                LiquidSecondaryButton(
                    text = stringResource(R.string.reset),
                    onClick = onReset
                )
            }
        }
    }
}

private fun formatRestTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%d:%02d", m, s)
}
