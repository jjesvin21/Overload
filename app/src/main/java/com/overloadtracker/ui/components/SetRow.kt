/**
 * Single workout set row with weight, reps, optional RPE, and completion checkbox.
 */
package com.overloadtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.overloadtracker.R
import com.overloadtracker.ui.theme.ElectricVioletOnPrimary
import com.overloadtracker.ui.theme.GlassBorderTopLeft
import com.overloadtracker.ui.theme.GlassSurfaceHigh
import com.overloadtracker.ui.theme.LabelCaps
import com.overloadtracker.ui.theme.MidnightSurfaceContainerHigh
import com.overloadtracker.ui.theme.PrimaryGradientBrush
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.unit.sp
import com.overloadtracker.ui.theme.CyanAccent
import com.overloadtracker.ui.theme.ShapeChip
import com.overloadtracker.ui.theme.TextOnSurface
import com.overloadtracker.ui.theme.TextOnSurfaceVariant

/**
 * Editable set row formatted with Liquid Glass etched inputs and HUD styling.
 */
@Composable
fun SetRow(
    setNumber: Int,
    weightDisplay: String,
    repsDisplay: String,
    rpe: Int?,
    isCompleted: Boolean,
    showRpe: Boolean = true,
    prevWeightDisplay: String? = null,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onRpeChange: (Int?) -> Unit,
    onCompletedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (!prevWeightDisplay.isNullOrBlank()) {
            Row(
                modifier = Modifier.padding(start = 40.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PREV: $prevWeightDisplay".uppercase(),
                    style = LabelCaps.copy(fontSize = 10.sp),
                    color = CyanAccent
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
        // Set Index Badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(GlassSurfaceHigh)
                .border(width = 1.dp, color = GlassBorderTopLeft, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$setNumber",
                style = LabelCaps.copy(fontWeight = FontWeight.Bold),
                color = TextOnSurface
            )
        }

        LiquidTextField(
            value = weightDisplay,
            onValueChange = onWeightChange,
            label = stringResource(R.string.weight),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
        )

        LiquidTextField(
            value = repsDisplay,
            onValueChange = onRepsChange,
            label = stringResource(R.string.reps),
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

        // Custom Glass Checkbox Button
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(ShapeChip)
                .then(
                    if (isCompleted) {
                        Modifier.background(PrimaryGradientBrush)
                    } else {
                        Modifier
                            .background(GlassSurfaceHigh)
                            .border(width = 1.dp, color = GlassBorderTopLeft, shape = ShapeChip)
                    }
                )
                .clickable { onCompletedChange(!isCompleted) },
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = ElectricVioletOnPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
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
        LiquidTextField(
            value = rpe?.toString().orEmpty(),
            onValueChange = {},
            label = stringResource(R.string.rpe),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            enabled = false,
            modifier = Modifier
                .menuAnchor(type = androidx.compose.material3.MenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
                .clickable { expanded = true }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MidnightSurfaceContainerHigh)
        ) {
            DropdownMenuItem(
                text = { Text("—", color = TextOnSurface) },
                onClick = {
                    onRpeChange(null)
                    expanded = false
                }
            )
            (1..10).forEach { value ->
                DropdownMenuItem(
                    text = { Text("$value", color = TextOnSurface) },
                    onClick = {
                        onRpeChange(value)
                        expanded = false
                    }
                )
            }
        }
    }
}
