package id.startapp.pheromone.presentation.ui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.startapp.pheromone.presentation.ui.theme.PheromoneBlue
import id.startapp.pheromone.presentation.ui.theme.PheromoneDimensions
import id.startapp.pheromone.presentation.ui.theme.ChartGrowthGreen
import id.startapp.pheromone.presentation.ui.theme.ErrorRed
import id.startapp.pheromone.presentation.ui.theme.SurfaceGray300
import id.startapp.pheromone.presentation.ui.theme.SuccessGreen
import id.startapp.pheromone.presentation.ui.theme.WarningOrange

/**
 * Pheromone Stat Card
 *
 * Dashboard statistics card displaying a key metric with icon and trend.
 *
 * Specs:
 * - Height: 80dp (content), ~100dp with padding
 * - Border Radius: 12dp
 * - Icon: 32dp, colored circle background
 * - Value: Display Small (32sp), Bold
 * - Label: Body Small
 * - Trend: Small indicator with arrow
 *
 * @param value Stat value to display
 * @param label Stat label/description
 * @param icon Optional icon for the stat
 * @param iconTint Background tint color for icon
 * @param modifier Modifier for the card
 * @param onClick Optional click handler
 * @param trend Optional trend indicator
 * @param trendColor Color for trend indicator
 */
@Composable
fun StatCard(
    value: String,
    label: String,
    icon: ImageVector? = null,
    iconTint: Color = PheromoneBlue.copy(alpha = 0.1f),
    iconColor: Color = PheromoneBlue,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trend: String? = null,
    trendColor: Color? = null
) {
    val cardShape = RoundedCornerShape(12.dp)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .height(110.dp),
            shape = cardShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            StatCardContent(
                value = value,
                label = label,
                icon = icon,
                iconTint = iconTint,
                iconColor = iconColor,
                trend = trend,
                trendColor = trendColor
            )
        }
    } else {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(110.dp),
            shape = cardShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            StatCardContent(
                value = value,
                label = label,
                icon = icon,
                iconTint = iconTint,
                iconColor = iconColor,
                trend = trend,
                trendColor = trendColor
            )
        }
    }
}

/**
 * Internal stat card content
 *
 * Layout matches design: icon+label row at top, value+trend row at bottom.
 */
@Composable
private fun StatCardContent(
    value: String,
    label: String,
    icon: ImageVector?,
    iconTint: Color,
    iconColor: Color,
    trend: String?,
    trendColor: Color?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(PheromoneDimensions.MD.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top row: Icon + Label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                IconBackground(
                    icon = icon,
                    tint = iconTint,
                    iconColor = iconColor
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(PheromoneDimensions.SM.dp))

        // Bottom row: Value + Trend
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = value,
                modifier = Modifier.alignByBaseline(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (trend != null) {
                TrendIndicator(
                    trend = trend,
                    color = trendColor ?: ChartGrowthGreen,
                    modifier = Modifier.alignByBaseline()
                )
            }
        }
    }
}

/**
 * Icon with colored rounded rectangle background
 */
@Composable
private fun IconBackground(
    icon: ImageVector,
    tint: Color,
    iconColor: Color = PheromoneBlue
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(tint, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Trend indicator text
 */
@Composable
private fun TrendIndicator(
    trend: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = trend,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall,
        color = color,
        fontWeight = FontWeight.Medium
    )
}

/**
 * Stat Card preset types
 */

/**
 * Orders Stat Card
 */
@Composable
fun OrdersStatCard(
    value: String,
    trend: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    StatCard(
        value = value,
        label = "Orders",
        icon = Icons.Default.ShoppingBag,
        iconTint = PheromoneBlue.copy(alpha = 0.1f),
        modifier = modifier,
        onClick = onClick,
        trend = trend,
        trendColor = if (trend?.contains("-") == true) ErrorRed else ChartGrowthGreen
    )
}

/**
 * Revenue Stat Card
 */
@Composable
fun RevenueStatCard(
    value: String,
    trend: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    StatCard(
        value = value,
        label = "Revenue",
        icon = Icons.Default.Payments,
        iconTint = SuccessGreen.copy(alpha = 0.1f),
        iconColor = SuccessGreen,
        modifier = modifier,
        onClick = onClick,
        trend = trend,
        trendColor = if (trend?.contains("-") == true) ErrorRed else ChartGrowthGreen
    )
}

/**
 * Pickup Stat Card
 */
@Composable
fun PickupStatCard(
    value: String,
    trend: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    StatCard(
        value = value,
        label = "Pickup",
        icon = Icons.Default.LocalShipping,
        iconTint = WarningOrange.copy(alpha = 0.1f),
        iconColor = WarningOrange,
        modifier = modifier,
        onClick = onClick,
        trend = trend,
        trendColor = if (trend?.contains("-") == true) ErrorRed else ChartGrowthGreen
    )
}

/**
 * Ready Stat Card
 */
@Composable
fun ReadyStatCard(
    value: String,
    trend: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    StatCard(
        value = value,
        label = "Ready",
        icon = Icons.Default.CheckCircle,
        iconTint = SuccessGreen.copy(alpha = 0.1f),
        iconColor = SuccessGreen,
        modifier = modifier,
        onClick = onClick,
        trend = trend,
        trendColor = if (trend?.contains("-") == true) ErrorRed else ChartGrowthGreen
    )
}

/**
 * Queue Stat Card (for Operator dashboard)
 */
@Composable
fun QueueStatCard(
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    StatCard(
        value = value,
        label = "In Queue",
        icon = Icons.Default.ListAlt,
        iconTint = WarningOrange.copy(alpha = 0.1f),
        iconColor = WarningOrange,
        modifier = modifier,
        onClick = onClick
    )
}

/**
 * Timer Stat Card (for Operator dashboard)
 */
@Composable
fun TimerStatCard(
    value: String,
    modifier: Modifier = Modifier,
    isRunning: Boolean = false
) {
    StatCard(
        value = value,
        label = "Timer",
        icon = Icons.Default.Timer,
        iconTint = if (isRunning) PheromoneBlue.copy(alpha = 0.1f) else SurfaceGray300.copy(alpha = 0.3f),
        iconColor = if (isRunning) PheromoneBlue else SurfaceGray300,
        modifier = modifier
    )
}

/**
 * Completed Stat Card
 */
@Composable
fun CompletedStatCard(
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    StatCard(
        value = value,
        label = "Done",
        icon = Icons.Default.CheckCircle,
        iconTint = SuccessGreen.copy(alpha = 0.1f),
        iconColor = SuccessGreen,
        modifier = modifier,
        onClick = onClick
    )
}

