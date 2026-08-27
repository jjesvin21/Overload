/**
 * Single workout set row with weight, reps, optional RPE, and completion checkbox.
 */
package com.overloadtracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.overloadtracker.R

/**
 * Editable set row used during live workouts.
 *
 * @param setNumber 1-based set index displayed to the user.
 * @param weightDisplay weight string in the user's preferred unit.
 * @param repsDisplay reps string.
 * @param rpe optional rate-of-perceived-exertion (1–10).
 * @param isCompleted whether the set is marked done.
 * @param showRpe when true, shows the RPE dropdown.
 * @param onWeightChange callback when weight text changes.
 * @param onRepsChange callback when reps text changes.
 * @param onRpeChange callback when RPE selection changes.
 * @param onCompletedChange callback when the done checkbox toggles.
 * @param modifier optional layout modifier.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetRow(
    setNumber: Int,
    weightDisplay: String,
    repsDisplay: String,
    rpe: Int?,
    isCompleted: Boolean,
    showRpe: Boolean = true,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onRpeChange: (Int?) -> Unit,
    onCompletedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$setNumber",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(24.dp)
        )
        OutlinedTextField(
            value = weightDisplay,
            onValueChange = onWeightChange,
            label = { Text(stringResource(R.string.weight)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = repsDisplay,
            onValueChange = onRepsChange,
            label = { Text(stringResource(R.string.reps)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(0.8f)
        )
        if (showRpe) {
            RpeDropdown(
                rpe = rpe,
                onRpeChange = onRpeChange,
                modifier = Modifier.weight(0.7f)
            )
        }
        Checkbox(
            checked = isCompleted,
            onCheckedChange = onCompletedChange,
            modifier = Modifier.heightIn(min = 48.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RpeDropdown(
    rpe: Int?,
    onRpeChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = rpe?.toString().orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.rpe)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("—") },
                onClick = {
                    onRpeChange(null)
                    expanded = false
                }
            )
            (1..10).forEach { value ->
                DropdownMenuItem(
                    text = { Text(value.toString()) },
                    onClick = {
                        onRpeChange(value)
                        expanded = false
                    }
                )
            }
        }
    }
}
