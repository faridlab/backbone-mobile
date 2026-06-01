package id.startapp.presentation.ui.components.feedback

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.startapp.presentation.ui.theme.BackboneBlue
import id.startapp.presentation.ui.theme.BackboneDimensions
import id.startapp.presentation.ui.theme.ErrorRed

/**
 * Backbone Loading Screen
 *
 * Full-screen loading state with logo and loading indicator.
 *
 * @param modifier Modifier for the screen
 * @param message Optional message to display
 */
@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(BackboneDimensions.LG.dp)
        ) {
            LoadingIndicator()

            if (message != null) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Error Screen
 *
 * Full-screen error state with icon, message, and retry button.
 *
 * Specs:
 * - Icon: 64dp, Error color
 * - Title: Headline Small
 * - Message: Body Medium
 * - Button: Primary (Retry)
 *
 * @param message Error message to display
 * @param modifier Modifier for the screen
 * @param onRetry Optional retry callback
 * @param title Optional title (default: "Something went wrong")
 */
@Composable
fun ErrorScreen(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    title: String? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(BackboneDimensions.MD.dp),
            modifier = Modifier.padding(BackboneDimensions.XL.dp)
        ) {
            // Error icon
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = ErrorRed,
                modifier = Modifier.size(64.dp)
            )

            // Title
            Text(
                text = title ?: "Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            // Message
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(BackboneDimensions.MD.dp))

            // Retry button
            if (onRetry != null) {
                // TODO: Use PrimaryButton when available
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BackboneBlue
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("Retry")
                    }
                }
            }
        }
    }
}

/**
 * Network Error Screen
 *
 * Specialized error screen for network connectivity issues.
 *
 * @param modifier Modifier for the screen
 * @param onRetry Callback for retry action
 */
@Composable
fun NetworkErrorScreen(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    ErrorScreen(
        message = "Please check your internet connection and try again.",
        title = "No Internet Connection",
        modifier = modifier,
        onRetry = onRetry
    )
}

/**
 * Server Error Screen
 *
 * Specialized error screen for server errors (5xx).
 *
 * @param modifier Modifier for the screen
 * @param onRetry Callback for retry action
 */
@Composable
fun ServerErrorScreen(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    ErrorScreen(
        message = "Our servers are experiencing issues. Please try again later.",
        title = "Server Error",
        modifier = modifier,
        onRetry = onRetry
    )
}

/**
 * Timeout Error Screen
 *
 * Specialized error screen for request timeouts.
 *
 * @param modifier Modifier for the screen
 * @param onRetry Callback for retry action
 */
@Composable
fun TimeoutErrorScreen(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    ErrorScreen(
        message = "The request took too long to complete. Please try again.",
        title = "Request Timeout",
        modifier = modifier,
        onRetry = onRetry
    )
}

/**
 * Not Found Screen
 *
 * Error screen for 404 / not found states.
 *
 * @param message Message to display
 * @param modifier Modifier for the screen
 * @param onBack Optional callback for back navigation
 */
@Composable
fun NotFoundScreen(
    message: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(BackboneDimensions.MD.dp),
            modifier = Modifier.padding(BackboneDimensions.XL.dp)
        ) {
            Text(
                text = "404",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Not Found",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (onBack != null) {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BackboneBlue
                    )
                ) {
                    Text("Go Back")
                }
            }
        }
    }
}
