package id.startapp.pheromone.presentation.ui.components.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import id.startapp.pheromone.presentation.ui.theme.PheromoneBlue
import id.startapp.pheromone.presentation.ui.theme.PheromoneDimensions

/**
 * Pheromone Loading Indicator
 *
 * Circular progress indicator following Pheromone design system.
 *
 * Specs:
 * - Size: 48dp (default)
 * - Stroke: 4dp
 * - Color: Brand Blue
 * - Animation: Rotate
 *
 * @param modifier Modifier for the indicator
 * @param color Progress color (default: Brand Blue)
 * @param strokeWidth Width of the progress stroke
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = PheromoneBlue,
    strokeWidth: Dp = 4.dp
) {
    CircularProgressIndicator(
        modifier = modifier.size(PheromoneDimensions.FabSize.dp),
        color = color,
        strokeWidth = strokeWidth,
        trackColor = Color.Transparent
    )
}

/**
 * Small Loading Indicator
 *
 * Smaller version (24dp) for inline loading.
 *
 * @param modifier Modifier for the indicator
 * @param color Progress color
 */
@Composable
fun LoadingIndicatorSmall(
    modifier: Modifier = Modifier,
    color: Color = PheromoneBlue
) {
    CircularProgressIndicator(
        modifier = modifier.size(24.dp),
        color = color,
        strokeWidth = 2.dp,
        trackColor = Color.Transparent
    )
}

/**
 * Linear Loading Indicator
 *
 * Horizontal progress bar for loading states.
 *
 * @param modifier Modifier for the indicator
 * @param color Progress color
 * @param trackColor Track/background color
 */
@Composable
fun LinearLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = PheromoneBlue,
    trackColor: Color = Color.Transparent
) {
    LinearProgressIndicator(
        modifier = modifier.height(4.dp),
        color = color,
        trackColor = trackColor
    )
}

/**
 * Loading Indicator with Label
 *
 * Circular indicator with text label below.
 *
 * @param label Text to display below indicator
 * @param modifier Modifier for the container
 * @param color Progress color
 */
@Composable
fun LoadingIndicatorWithLabel(
    label: String,
    modifier: Modifier = Modifier,
    color: Color = PheromoneBlue
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LoadingIndicator(color = color)

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Full-screen Loading
 *
 * Loading overlay covering entire screen.
 *
 * @param modifier Modifier for the container
 * @param color Progress color
 * @param label Optional text label
 */
@Composable
fun LoadingOverlay(
    modifier: Modifier = Modifier,
    color: Color = PheromoneBlue,
    label: String? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LoadingIndicator(color = color)

            if (label != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Button Loading Indicator
 *
 * Loading indicator shown inside a button during loading.
 *
 * @param modifier Modifier for the indicator
 * @param color Progress color (default: white for button)
 */
@Composable
fun ButtonLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    CircularProgressIndicator(
        modifier = modifier.size(20.dp),
        color = color,
        strokeWidth = 2.dp,
        trackColor = Color.Transparent
    )
}

/**
 * Refresh Loading Indicator
 *
 * Loading indicator for pull-to-refresh functionality.
 *
 * @param isRefreshing Whether refresh is active
 * @param modifier Modifier for the container
 */
@Composable
fun RefreshLoadingIndicator(
    isRefreshing: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(PheromoneDimensions.MD.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isRefreshing) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LoadingIndicatorSmall()
                Text(
                    text = "Refreshing...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
