/**
 * Shared Glass Dialog & Bottom Sheet primitives for Liquid Glass design system.
 */
package com.overloadtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.overloadtracker.ui.theme.GlassBorderTopLeft
import com.overloadtracker.ui.theme.GlassOverlayDark
import com.overloadtracker.ui.theme.MidnightSurfaceContainerLow
import com.overloadtracker.ui.theme.ShapeBottomSheet
import com.overloadtracker.ui.theme.ShapeDialog
import com.overloadtracker.ui.theme.TextOnSurface
import com.overloadtracker.ui.theme.TextOnSurfaceVariant

/**
 * Liquid Glass Alert / Confirmation Dialog.
 */
@Composable
fun LiquidAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    bodyText: String? = null,
    confirmButtonText: String = "Confirm",
    onConfirm: () -> Unit = onDismissRequest,
    dismissButtonText: String? = "Cancel",
    onDismiss: (() -> Unit)? = onDismissRequest,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(ShapeDialog)
                .background(MidnightSurfaceContainerLow.copy(alpha = 0.95f))
                .border(width = 1.dp, color = GlassBorderTopLeft, shape = ShapeDialog)
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextOnSurface
                )

                if (!bodyText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = bodyText ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOnSurfaceVariant
                    )
                }

                if (content != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    content()
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (dismissButtonText != null && onDismiss != null) {
                        LiquidSecondaryButton(
                            text = dismissButtonText,
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    LiquidPrimaryButton(
                        text = confirmButtonText,
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Liquid Glass Modal Bottom Sheet component with frosted container and drag handle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MidnightSurfaceContainerLow.copy(alpha = 0.96f),
        scrimColor = GlassOverlayDark,
        shape = ShapeBottomSheet,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(48.dp)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(GlassBorderTopLeft)
            )
        },
        modifier = modifier
            .border(width = 1.dp, color = GlassBorderTopLeft, shape = ShapeBottomSheet)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            content()
        }
    }
}
