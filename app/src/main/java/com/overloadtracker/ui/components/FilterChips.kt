/**
 * Horizontal scrollable body-part filter chips backed by [Constants.BODY_PART_FILTERS].
 */
package com.overloadtracker.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.overloadtracker.util.Constants

/**
 * Multi-select filter chips for exercise body-part categories.
 *
 * @param selectedCategories currently selected category values (lowercase).
 * @param onToggle invoked with the category value when a chip is toggled.
 * @param modifier optional layout modifier.
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
            FilterChip(
                selected = selected,
                onClick = { onToggle(value) },
                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}
