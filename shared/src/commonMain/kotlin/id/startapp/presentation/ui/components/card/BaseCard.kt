package id.startapp.presentation.ui.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import id.startapp.presentation.ui.theme.BackboneDimensions

/**
 * Backbone Base Card
 *
 * Standard card component following Backbone design system.
 *
 * Specs:
 * - Border Radius: 12dp
 * - Elevation: 1 (default)
 * - Padding: 16dp
 * - Background: White
 * - Shadow: Subtle drop shadow
 *
 * @param modifier Modifier for the card
 * @param onClick Optional click handler
 * @param enabled Whether card is enabled (for clickable cards)
 * @param shape Card shape (default: rounded corners)
 * @param backgroundColor Card background color
 * @param border Optional border
 * @param elevation Card elevation
 * @param content Card content
 */
@Composable
fun BaseCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    border: BorderStroke? = null,
    elevation: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Column(
                modifier = Modifier.padding(BackboneDimensions.MD.dp),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Column(
                modifier = Modifier.padding(BackboneDimensions.MD.dp),
                content = content
            )
        }
    }
}

/**
 * Elevated Card
 *
 * Card with higher elevation for emphasis.
 *
 * Specs:
 * - Elevation: 2
 * - Shadow: Medium drop shadow
 *
 * @param modifier Modifier for the card
 * @param onClick Optional click handler
 * @param enabled Whether card is enabled
 * @param content Card content
 */
@Composable
fun ElevatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        onClick = onClick ?: {},
        modifier = modifier,
        enabled = onClick != null && enabled,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(BackboneDimensions.MD.dp),
            content = content
        )
    }
}

/**
 * Outlined Card
 *
 * Card with border instead of elevation.
 *
 * Specs:
 * - Border: 1dp Gray-300
 * - Elevation: 0
 * - Background: Gray-50
 *
 * @param modifier Modifier for the card
 * @param onClick Optional click handler
 * @param enabled Whether card is enabled
 * @param borderColor Border color
 * @param content Card content
 */
@Composable
fun OutlinedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    borderColor: Color = Color(0xFFE0E0E0),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        onClick = onClick ?: {},
        modifier = modifier,
        enabled = onClick != null && enabled,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFAFAFA)
        ),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(BackboneDimensions.MD.dp),
            content = content
        )
    }
}

/**
 * Card with custom padding
 *
 * Base card with customizable inner padding.
 *
 * @param modifier Modifier for the card
 * @param padding Inner padding
 * @param onClick Optional click handler
 * @param enabled Whether card is enabled
 * @param content Card content
 */
@Composable
fun PaddedCard(
    modifier: Modifier = Modifier,
    padding: Dp = BackboneDimensions.MD.dp,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(padding),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(padding),
                content = content
            )
        }
    }
}
