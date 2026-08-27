/**
 * Horizontal scrollable body-part filter chips styled with Liquid Glass aesthetic.
 */
package com.overloadtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.overloadtracker.ui.theme.Charcoal
import com.overloadtracker.ui.theme.GlassBorder
import com.overloadtracker.ui.theme.LabelCaps
import com.overloadtracker.ui.theme.OnSurface
import com.overloadtracker.ui.theme.SecondaryText
import com.overloadtracker.ui.theme.StravaOrange
import com.overloadtracker.ui.theme.TrueBlack
import com.overloadtracker.util.Constants

/**
 * Multi-select filter chips for exercise body-part categories.
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
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Constants.BODY_PART_FILTERS.forEach { (label, value) ->
            val selected = value in selectedCategories
            val shadowModifier = if (selected) {
                Modifier.shadow(8.dp, CircleShape, spotColor = StravaOrange)
            } else Modifier

            Box(
                modifier = Modifier
                    .then(shadowModifier)
                    .clip(CircleShape)
                    .background(if (selected) StravaOrange else Charcoal)
                    .border(
                        width = 1.dp,
                        color = if (selected) StravaOrange else GlassBorder,
                        shape = CircleShape
                    )
                    .clickable { onToggle(value) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = LabelCaps.copy(fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium),
                    color = if (selected) TrueBlack else SecondaryText
                )
            }
        }
    }
}
