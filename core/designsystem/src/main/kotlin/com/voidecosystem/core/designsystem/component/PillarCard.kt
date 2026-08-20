package com.voidecosystem.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** What the pill button at the bottom of a [PillarCard] should say and do. */
enum class TileAction { GET, OPEN, UPDATE, WORKING }

/**
 * A single tappable app tile used on the dashboard/launcher gateway grid —
 * styled like an app-store listing card: gradient icon chip, a Play-Store
 * style action pill (Get / Open / Update), and a subtle press-scale so the
 * whole grid feels tactile rather than a static list of buttons.
 */
@Composable
fun PillarCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    action: TileAction,
    modifier: Modifier = Modifier,
    /** Non-null while the app is downloading, in [0f, 1f]; renders a progress bar. */
    downloadProgress: Float? = null,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, animationSpec = tween(120), label = "tileScale")
    val actionColor by animateColorAsState(
        targetValue = if (action == TileAction.UPDATE) MaterialTheme.colorScheme.secondary else accent,
        label = "actionColor",
    )

    Box(
        modifier = modifier
            .aspectRatio(0.92f)
            .scale(scale)
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, accent.copy(alpha = 0.14f), RoundedCornerShape(22.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(16.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(accent.copy(alpha = 0.32f), accent.copy(alpha = 0.10f)),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accent)
            }

            Column(modifier = Modifier.padding(top = 12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (downloadProgress != null) {
                LinearProgressIndicator(
                    progress = { downloadProgress },
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.15f),
                )
            } else {
                ActionPill(
                    action = action,
                    color = actionColor,
                    onClick = onClick,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun ActionPill(
    action: TileAction,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = when (action) {
        TileAction.GET -> "Get"
        TileAction.OPEN -> "Open"
        TileAction.UPDATE -> "Update"
        TileAction.WORKING -> "Installing…"
    }
    val filled = action != TileAction.OPEN

    Box(
        modifier = modifier
            .clip(CircleShape)
            .then(
                if (filled) {
                    Modifier.background(color.copy(alpha = if (action == TileAction.WORKING) 0.35f else 1f))
                } else {
                    Modifier.border(1.dp, color.copy(alpha = 0.5f), CircleShape)
                },
            )
            .clickable(enabled = action != TileAction.WORKING, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (filled) Color.White else color,
        )
    }
}
