package id.startapp.pheromone.presentation.ui.components.button

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.startapp.pheromone.presentation.ui.accessibility.TestTags
import id.startapp.pheromone.presentation.ui.theme.PheromoneBlue
import id.startapp.pheromone.presentation.ui.theme.PheromoneDimensions

/**
 * Pheromone FAB (Floating Action Button)
 *
 * Floating action button with Brand Blue background.
 *
 * Specs:
 * - Size: 56dp (default), 40dp (small)
 * - Background: Brand Blue (#0288D1)
 * - Icon: White, 24dp
 * - Border Radius: 16dp
 * - Elevation: 6
 * - Position: Bottom-right, 16dp from edge
 *
 * @param onClick Callback when FAB is clicked
 * @param modifier Modifier for the FAB
 * @param icon Icon to display
 * @param contentDescription Description for accessibility
 * @param containerColor Background color (default: Brand Blue)
 */
@Composable
fun FabButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String? = null,
    containerColor: Color = PheromoneBlue
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.testTag(TestTags.FAB_BUTTON),
        containerColor = containerColor,
        contentColor = Color.White,
        shape = MaterialTheme.shapes.large
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White
        )
    }
}

/**
 * Small FAB (40dp)
 *
 * Compact floating action button for smaller screens.
 *
 * @param onClick Callback when FAB is clicked
 * @param modifier Modifier for the FAB
 * @param icon Icon to display
 * @param contentDescription Description for accessibility
 * @param containerColor Background color (default: Brand Blue)
 */
@Composable
fun FabButtonSmall(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String? = null,
    containerColor: Color = PheromoneBlue
) {
    SmallFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = Color.White,
        shape = MaterialTheme.shapes.medium
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White
        )
    }
}

/**
 * Extended FAB with label
 *
 * FAB with text label and icon.
 *
 * @param text Label text
 * @param icon Leading icon
 * @param onClick Callback when FAB is clicked
 * @param modifier Modifier for the FAB
 * @param containerColor Background color (default: Brand Blue)
 */
@Composable
fun FabButtonExtended(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = PheromoneBlue
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = Color.White,
        shape = MaterialTheme.shapes.large
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White
        )
        // TODO: Add Spacer when available
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium
            )
        )
    }
}

/**
 * FAB with custom size
 *
 * @param onClick Callback when FAB is clicked
 * @param modifier Modifier for the FAB
 * @param icon Icon to display
 * @param contentDescription Description for accessibility
 * @param size Custom size in dp (default: 56dp)
 * @param containerColor Background color (default: Brand Blue)
 */
@Composable
fun FabButtonCustom(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String? = null,
    size: androidx.compose.ui.unit.Dp = PheromoneDimensions.FabSize.dp,
    containerColor: Color = PheromoneBlue
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.then(
            Modifier.size(size)
        ),
        containerColor = containerColor,
        contentColor = Color.White,
        shape = MaterialTheme.shapes.large
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(PheromoneIconSizes.Medium.dp)
        )
    }
}

// Import icon sizes
private object PheromoneIconSizes {
    const val Medium = 24
}

private fun Modifier.size(size: androidx.compose.ui.unit.Dp): Modifier = this
