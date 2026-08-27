/**
 * Horizontal scrollable body-part filter chips backed by [Constants.BODY_PART_FILTERS].
 */
package com.overloadtracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.overloadtracker.ui.theme.ElectricViolet
import com.overloadtracker.ui.theme.ElectricVioletOnPrimary
import com.overloadtracker.ui.theme.GlassBorderTopLeft
import com.overloadtracker.ui.theme.GlassSurfaceHigh
import com.overloadtracker.ui.theme.LabelCaps
import com.overloadtracker.ui.theme.PrimaryGradientBrush
import com.overloadtracker.ui.theme.ShapeChip
import com.overloadtracker.ui.theme.TextOnSurfaceVariant
import com.overloadtracker.util.Constants

/**
 * Multi-select glass filter chips for exercise body-part categories.
 */
@Composable
fun FilterChips(
    selectedCategories: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Constants.BODY_PART_FILTERS.forEach { (label, value) ->
            val selected = value in selectedCategories

            val labelColor by animateColorAsState(
                targetValue = if (selected) ElectricVioletOnPrimary else TextOnSurfaceVariant,
                label = "chip_text_color"
            )

            Box(
                modifier = Modifier
                    .clip(ShapeChip)
                    .then(
                        if (selected) {
                            Modifier.background(PrimaryGradientBrush)
                        } else {
                            Modifier
                                .background(GlassSurfaceHigh.copy(alpha = 0.6f))
                                .border(width = 1.dp, color = GlassBorderTopLeft, shape = ShapeChip)
                        }
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onToggle(value) }
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label.uppercase(),
                    style = LabelCaps.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold),
                    color = labelColor
                )
            }
        }
    }
}
