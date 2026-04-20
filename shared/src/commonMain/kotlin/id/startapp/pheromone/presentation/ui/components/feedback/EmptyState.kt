package id.startapp.pheromone.presentation.ui.components.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.startapp.pheromone.presentation.ui.theme.PheromoneBlue
import id.startapp.pheromone.presentation.ui.theme.PheromoneDimensions
import id.startapp.pheromone.presentation.ui.theme.SurfaceGray300

/**
 * Pheromone Empty State
 *
 * Empty state display with illustration, message, and optional action.
 *
 * Specs:
 * - Illustration: 200dp × 200dp (or icon)
 * - Title: Headline Small
 * - Message: Body Medium
 * - CTA Button: Primary
 *
 * @param message Message to display
 * @param modifier Modifier for the container
 * @param title Optional title
 * @param icon Optional icon instead of illustration
 * @param actionLabel Optional action button label
 * @param onAction Optional action callback
 */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PheromoneDimensions.MD.dp),
            modifier = Modifier.padding(PheromoneDimensions.XL.dp)
        ) {
            // Icon or illustration placeholder
            val displayIcon = icon ?: Icons.Rounded.Inbox
            Icon(
                imageVector = displayIcon,
                contentDescription = null,
                tint = SurfaceGray300,
                modifier = Modifier.size(120.dp)
            )

            // Title
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }

            // Message
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Action button
            if (actionLabel != null && onAction != null) {
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PheromoneBlue
                    )
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

/**
 * Empty State for Lists
 *
 * Compact empty state for list views.
 *
 * @param message Message to display
 * @param modifier Modifier for the container
 * @param actionLabel Optional action button label
 * @param onAction Optional action callback
 */
@Composable
fun EmptyListState(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(PheromoneDimensions.XXL.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PheromoneDimensions.MD.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Inbox,
            contentDescription = null,
            tint = SurfaceGray300,
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (actionLabel != null && onAction != null) {
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PheromoneBlue
                )
            ) {
                Text(actionLabel)
            }
        }
    }
}

/**
 * Success Empty State
 *
 * Empty state with success styling (e.g., "All caught up!").
 *
 * @param message Message to display
 * @param modifier Modifier for the container
 * @param actionLabel Optional action button label
 * @param onAction Optional action callback
 */
@Composable
fun SuccessEmptyState(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PheromoneDimensions.MD.dp),
            modifier = Modifier.padding(PheromoneDimensions.XL.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            if (actionLabel != null && onAction != null) {
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PheromoneBlue
                    )
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

/**
 * Pheromone Status Badge
 *
 * Pill badge for status indicators.
 *
 * Specs:
 * - Height: 24dp
 * - Padding: 8dp horizontal
 * - Border Radius: 12dp (full pill)
 * - Font: Label Small
 * - Colors by status
 *
 * @param text Badge text
 * @param status Status type determining color
 * @param modifier Modifier for the badge
 */
@Composable
fun StatusBadge(
    text: String,
    status: BadgeStatus = BadgeStatus.Neutral,
    modifier: Modifier = Modifier
) {
    val colors = status.getColors()

    Surface(
        modifier = modifier,
        color = colors.background,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = colors.content
        )
    }
}

/**
 * Badge status types
 */
enum class BadgeStatus {
    Success,
    Warning,
    Error,
    Info,
    Neutral,
    Express;

    fun getColors(): BadgeColors = when (this) {
        Success -> BadgeColors(
            background = Color(0xFF4CAF50),
            content = Color.White
        )
        Warning -> BadgeColors(
            background = Color(0xFFFF9800),
            content = Color.White
        )
        Error -> BadgeColors(
            background = Color(0xFFF44336),
            content = Color.White
        )
        Info -> BadgeColors(
            background = Color(0xFF2196F3),
            content = Color.White
        )
        Neutral -> BadgeColors(
            background = Color(0xFFBDBDBD),
            content = Color.White
        )
        Express -> BadgeColors(
            background = Color(0xFF9C27B0),
            content = Color.White
        )
    }
}

/**
 * Badge colors container
 */
data class BadgeColors(
    val background: Color,
    val content: Color
)

/**
 * Status Badge for Order Status
 *
 * Pre-configured badge for common order statuses.
 *
 * @param status Order status string
 * @param modifier Modifier for the badge
 */
@Composable
fun OrderStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val badgeStatus = when (status.lowercase()) {
        "pending", "waiting" -> BadgeStatus.Warning
        "processing", "washing", "drying", "ironing" -> BadgeStatus.Info
        "ready", "completed" -> BadgeStatus.Success
        "delivered" -> BadgeStatus.Success
        "cancelled", "rejected", "failed" -> BadgeStatus.Error
        "express" -> BadgeStatus.Express
        else -> BadgeStatus.Neutral
    }

    StatusBadge(
        text = status.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
        status = badgeStatus,
        modifier = modifier
    )
}
