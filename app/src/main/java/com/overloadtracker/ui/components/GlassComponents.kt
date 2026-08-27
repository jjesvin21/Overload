/**
 * Reusable Glassmorphism & Apex Athletic UI Components.
 */
package com.overloadtracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.overloadtracker.ui.theme.Charcoal
import com.overloadtracker.ui.theme.GlassBackground
import com.overloadtracker.ui.theme.GlassBorder
import com.overloadtracker.ui.theme.LabelCaps
import com.overloadtracker.ui.theme.OnSurface
import com.overloadtracker.ui.theme.SecondaryText
import com.overloadtracker.ui.theme.StravaOrange
import com.overloadtracker.ui.theme.TrueBlack

/**
 * Atmospheric Level 0 Background with glowing Strava Orange blur blobs.
 */
@Composable
fun AtmosphericBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TrueBlack)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Top-left orange ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        StravaOrange.copy(alpha = 0.15f),
                        StravaOrange.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.1f, size.height * 0.1f),
                    radius = size.width * 0.6f
                ),
                center = Offset(size.width * 0.1f, size.height * 0.1f),
                radius = size.width * 0.6f
            )

            // Bottom-right subtle secondary glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF393939).copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.9f, size.height * 0.85f),
                    radius = size.width * 0.7f
                ),
                center = Offset(size.width * 0.9f, size.height * 0.85f),
                radius = size.width * 0.7f
            )
        }
        content()
    }
}

/**
 * Glassmorphic container panel (Level 1 / Level 2 Surface).
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = GlassBackground,
    borderColor: Color = GlassBorder,
    borderWidth: Dp = 1.dp,
    hasGlow: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val glowModifier = if (hasGlow) {
        Modifier.shadow(
            elevation = 12.dp,
            shape = shape,
            ambientColor = StravaOrange.copy(alpha = 0.3f),
            spotColor = StravaOrange.copy(alpha = 0.4f)
        )
    } else Modifier

    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Box(
        modifier = modifier
            .then(glowModifier)
            .clip(shape)
            .background(backgroundColor)
            .border(borderWidth, borderColor, shape)
            .then(clickModifier)
    ) {
        content()
    }
}

/**
 * Floating Liquid Glass Navigation Item definition.
 */
data class NavItem(
    val route: Any,
    val title: String,
    val icon: ImageVector,
    val isSelected: Boolean,
    val onClick: () -> Unit
)

/**
 * Floating pill-shaped bottom navigation bar with Liquid Glass aesthetic.
 */
@Composable
fun LiquidGlassBottomBar(
    items: List<NavItem>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 12.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            shape = RoundedCornerShape(32.dp),
            backgroundColor = Charcoal.copy(alpha = 0.85f),
            borderColor = Color.White.copy(alpha = 0.12f),
            hasGlow = true,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(
                                if (item.isSelected) StravaOrange.copy(alpha = 0.15f)
                                else Color.Transparent
                            )
                            .clickable(onClick = item.onClick)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (item.isSelected) StravaOrange else SecondaryText.copy(alpha = 0.7f)
                            )
                            if (item.isSelected) {
                                Text(
                                    text = item.title,
                                    style = LabelCaps.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                    color = StravaOrange,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
