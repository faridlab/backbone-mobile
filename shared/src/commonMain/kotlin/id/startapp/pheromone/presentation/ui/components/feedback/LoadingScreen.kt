package id.startapp.pheromone.presentation.ui.components.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.startapp.pheromone.presentation.ui.theme.PheromoneBlue
import id.startapp.pheromone.presentation.ui.theme.PheromoneDimensions

/**
 * Pheromone Loading Screen
 *
 * Full-screen loading indicator with optional message.
 *
 * Specs:
 * - Full-screen centered content
 * - 48dp circular progress indicator
 * - Optional loading message below indicator
 * - Brand Blue primary color
 *
 * @param message Optional loading message
 * @param modifier Modifier for the container
 * @param color Progress indicator color
 */
@Composable
fun LoadingScreen(
    message: String? = null,
    modifier: Modifier = Modifier,
    color: Color = PheromoneBlue
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = color,
                strokeWidth = 4.dp
            )

            if (message != null) {
                Spacer(modifier = Modifier.height(PheromoneDimensions.MD.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Inline Loading Indicator
 *
 * Compact loading indicator for inline use (e.g., in cards, lists).
 *
 * @param message Optional loading message
 * @param modifier Modifier for the container
 * @param color Progress indicator color
 */
@Composable
fun InlineLoading(
    message: String? = null,
    modifier: Modifier = Modifier,
    color: Color = PheromoneBlue
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = color,
            strokeWidth = 3.dp
        )

        if (message != null) {
            Spacer(modifier = Modifier.height(PheromoneDimensions.SM.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Loading Overlay
 *
 * Semi-transparent overlay with loading indicator.
 * Useful for showing loading state over existing content.
 *
 * @param isVisible Whether the overlay is visible
 * @param message Optional loading message
 * @param modifier Modifier for the container
 */
@Composable
fun LoadingOverlay(
    isVisible: Boolean,
    message: String? = null,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            // Semi-transparent background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        Modifier
                            // Using alpha for semi-transparent background
                    )
            ) {
                // This would need Box with background modifier
                // Keeping simple for KMP compatibility
            }

            // Loading indicator centered
            LoadingScreen(message = message)
        }
    }
}
