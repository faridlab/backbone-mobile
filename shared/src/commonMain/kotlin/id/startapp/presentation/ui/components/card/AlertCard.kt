package id.startapp.presentation.ui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.startapp.presentation.ui.theme.BackboneBlue
import id.startapp.presentation.ui.theme.BackboneDimensions
import id.startapp.presentation.ui.theme.ErrorRed
import id.startapp.presentation.ui.theme.ErrorRedBg
import id.startapp.presentation.ui.theme.InfoBlue
import id.startapp.presentation.ui.theme.InfoBlueBg
import id.startapp.presentation.ui.theme.SuccessGreen
import id.startapp.presentation.ui.theme.SuccessGreenBg
import id.startapp.presentation.ui.theme.WarningOrange
import id.startapp.presentation.ui.theme.WarningOrangeBg

/**
 * Backbone Alert Card
 *
 * Card displaying alerts/notifications with severity-based styling.
 *
 * Specs:
 * - Border Radius: 12dp
 * - Elevation: 1
 * - Padding: 16dp
 * - Left border: 4dp colored strip (severity)
 * - Icon: 24dp, colored by severity
 *
 * @param message Alert message
 * @param severity Alert severity level
 * @param modifier Modifier for the card
 * @param title Optional title
 * @param time Optional timestamp
 * @param onDismiss Optional dismiss callback
 * @param onClick Optional click handler
 */
@Composable
fun AlertCard(
    message: String,
    severity: AlertSeverity = AlertSeverity.Info,
    modifier: Modifier = Modifier,
    title: String? = null,
    time: String? = null,
    onDismiss: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = severity.getColors()

    Card(
        onClick = onClick ?: {},
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.bg, RoundedCornerShape(
                    topStart = 12.dp,
                    bottomStart = 12.dp,
                    topEnd = 0.dp,
                    bottomEnd = 0.dp
                ))
                .padding(BackboneDimensions.MD.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Severity strip and icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Colored strip
                Spacer(
                    modifier = Modifier
                        .width(4.dp)
                        .height(40.dp)
                        .background(colors.main, RoundedCornerShape(2.dp))
                )

                // Icon
                Icon(
                    imageVector = severity.icon,
                    contentDescription = "${severity.name} alert",
                    tint = colors.main,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (time != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Dismiss button
            if (onDismiss != null) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Alert severity levels
 */
enum class AlertSeverity(
    val icon: ImageVector,
    private val mainColor: Color,
    private val bgColor: Color
) {
    /**
     * Critical/Error severity
     */
    Error(
        icon = Icons.Rounded.Error,
        mainColor = ErrorRed,
        bgColor = ErrorRedBg
    ),

    /**
     * Warning severity
     */
    Warning(
        icon = Icons.Rounded.Warning,
        mainColor = WarningOrange,
        bgColor = WarningOrangeBg
    ),

    /**
     * Info/Success severity
     */
    Info(
        icon = Icons.Rounded.Info,
        mainColor = InfoBlue,
        bgColor = InfoBlueBg
    ),

    /**
     * Success severity
     */
    Success(
        icon = Icons.Default.CheckCircle,
        mainColor = SuccessGreen,
        bgColor = SuccessGreenBg
    );

    fun getColors(): AlertColors = AlertColors(mainColor, bgColor)
}

/**
 * Alert colors container
 */
data class AlertColors(
    val main: Color,
    val bg: Color
)

/**
 * Compact Alert Card (for dashboard alerts section)
 *
 * Smaller version for use in compact list views.
 *
 * @param message Alert message
 * @param severity Alert severity level
 * @param modifier Modifier for the card
 * @param time Optional timestamp
 * @param onClick Optional click handler
 */
@Composable
fun CompactAlertCard(
    message: String,
    severity: AlertSeverity = AlertSeverity.Info,
    modifier: Modifier = Modifier,
    time: String? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = severity.getColors()

    Card(
        onClick = onClick ?: {},
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BackboneDimensions.MD.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with colored background
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(colors.bg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = severity.icon,
                    contentDescription = "${severity.name} alert",
                    tint = colors.main,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Message
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (time != null) {
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Chevron
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Alert Section (for dashboard)
 *
 * Grouped alerts with expand/collapse functionality.
 *
 * @param alerts List of alerts to display
 * @param title Section title
 * @param modifier Modifier for the section
 * @param maxVisible Maximum alerts to show before collapsing
 */
@Composable
fun AlertSection(
    alerts: List<AlertItem>,
    title: String = "⚠️ Alerts",
    modifier: Modifier = Modifier,
    maxVisible: Int = 3
) {
    var expanded by remember { mutableStateOf(false) }
    val visibleAlerts = if (expanded) alerts else alerts.take(maxVisible)

    Column(modifier = modifier) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$title (${alerts.size})",
                style = MaterialTheme.typography.titleMedium
            )

            if (alerts.size > maxVisible) {
                Text(
                    text = if (expanded) "Show less" else "View all",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BackboneBlue,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(BackboneDimensions.SM.dp))

        // Alert list
        visibleAlerts.forEach { alert ->
            CompactAlertCard(
                message = alert.message,
                severity = alert.severity,
                time = alert.time,
                onClick = alert.onClick
            )
            Spacer(modifier = Modifier.height(BackboneDimensions.SM.dp))
        }
    }
}

/**
 * Alert item data class
 */
data class AlertItem(
    val message: String,
    val severity: AlertSeverity = AlertSeverity.Info,
    val time: String? = null,
    val onClick: (() -> Unit)? = null
)
