/**
 * Single workout set row with weight, reps, optional RPE, and completion checkbox styled with Liquid Glass aesthetic.
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.overloadtracker.R
import com.overloadtracker.ui.theme.EtchedInputBackground
import com.overloadtracker.ui.theme.GlassBorder
import com.overloadtracker.ui.theme.LabelCaps
import com.overloadtracker.ui.theme.OnSurface
import com.overloadtracker.ui.theme.OnSurfaceVariant
import com.overloadtracker.ui.theme.SecondaryText
import com.overloadtracker.ui.theme.StravaOrange
import com.overloadtracker.ui.theme.SurfaceContainerHighest
import com.overloadtracker.ui.theme.TrueBlack

/**
 * Editable set row used during live workouts.
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
        // Set Indicator Badge (Circle)
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted) StravaOrange
                    else EtchedInputBackground
                )
                .border(
                    width = 1.dp,
                    color = if (isCompleted) StravaOrange else GlassBorder,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = TrueBlack,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = "$setNumber",
                    style = LabelCaps.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    color = if (isCompleted) TrueBlack else SecondaryText,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Weight Input Field
        OutlinedTextField(
            value = weightDisplay,
            onValueChange = onWeightChange,
            placeholder = { Text(stringResource(R.string.weight), color = OnSurfaceVariant, fontSize = 12.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = EtchedInputBackground,
                unfocusedContainerColor = EtchedInputBackground,
                focusedBorderColor = StravaOrange,
                unfocusedBorderColor = GlassBorder,
                focusedTextColor = OnSurface,
                unfocusedTextColor = OnSurface
            ),
            modifier = Modifier.weight(1f)
        )

        // Reps Input Field
        OutlinedTextField(
            value = repsDisplay,
            onValueChange = onRepsChange,
            placeholder = { Text(stringResource(R.string.reps), color = OnSurfaceVariant, fontSize = 12.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = EtchedInputBackground,
                unfocusedContainerColor = EtchedInputBackground,
                focusedBorderColor = StravaOrange,
                unfocusedBorderColor = GlassBorder,
                focusedTextColor = OnSurface,
                unfocusedTextColor = OnSurface
            ),
            modifier = Modifier.weight(0.9f)
        )

        if (showRpe) {
            RpeDropdown(
                rpe = rpe,
                onRpeChange = onRpeChange,
                modifier = Modifier.weight(0.7f)
            )
        }

        // Completion Toggle Button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted) StravaOrange.copy(alpha = 0.2f)
                    else Color.Transparent
                )
                .border(
                    width = 1.dp,
                    color = if (isCompleted) StravaOrange else GlassBorder,
                    shape = CircleShape
                )
                .clickable { onCompletedChange(!isCompleted) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Complete Set",
                tint = if (isCompleted) StravaOrange else SecondaryText.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
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
        OutlinedTextField(
            value = rpe?.toString().orEmpty(),
            onValueChange = {},
            readOnly = true,
            placeholder = { Text(stringResource(R.string.rpe), color = OnSurfaceVariant, fontSize = 12.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = EtchedInputBackground,
                unfocusedContainerColor = EtchedInputBackground,
                focusedBorderColor = StravaOrange,
                unfocusedBorderColor = GlassBorder,
                focusedTextColor = OnSurface,
                unfocusedTextColor = OnSurface
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(SurfaceContainerHighest)
        ) {
            DropdownMenuItem(
                text = { Text("—", color = OnSurface) },
                onClick = {
                    onRpeChange(null)
                    expanded = false
                }
            )
            (1..10).forEach { value ->
                DropdownMenuItem(
                    text = { Text(value.toString(), color = OnSurface) },
                    onClick = {
                        onRpeChange(value)
                        expanded = false
                    }
                )
            }
        }
    }
}
