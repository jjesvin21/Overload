/**
 * One-time onboarding intro that marks the user as onboarded.
 * Refactored with Liquid Glass / Liquid Vitality visual aesthetic.
 */
package com.overloadtracker.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.overloadtracker.R
import com.overloadtracker.ui.components.LiquidGlassCard
import com.overloadtracker.ui.components.LiquidPrimaryButton
import com.overloadtracker.ui.theme.CyanAccent
import com.overloadtracker.ui.theme.DisplayMetrics
import com.overloadtracker.ui.theme.ElectricViolet
import com.overloadtracker.ui.theme.GlassBorderTopLeft
import com.overloadtracker.ui.theme.GlassSurfaceHigh
import com.overloadtracker.ui.theme.LabelCaps
import com.overloadtracker.ui.theme.MidnightBackground
import com.overloadtracker.ui.theme.TextOnSurface
import com.overloadtracker.ui.theme.TextOnSurfaceVariant

/**
 * First-launch onboarding screen with Liquid Glass aesthetic.
 *
 * @param onComplete invoked after the user taps Get Started.
 * @param modifier optional layout modifier.
 */
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            padding = 28.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Hero Icon Badge
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(GlassSurfaceHigh)
                        .border(width = 1.5.dp, color = GlassBorderTopLeft, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "OVERLOAD",
                    style = LabelCaps.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    color = CyanAccent,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.onboarding_title),
                    style = DisplayMetrics.copy(fontSize = 32.sp, lineHeight = 38.sp),
                    textAlign = TextAlign.Center,
                    color = TextOnSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.onboarding_body),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = TextOnSurfaceVariant
                )

                Spacer(modifier = Modifier.height(36.dp))

                LiquidPrimaryButton(
                    text = stringResource(R.string.get_started),
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
