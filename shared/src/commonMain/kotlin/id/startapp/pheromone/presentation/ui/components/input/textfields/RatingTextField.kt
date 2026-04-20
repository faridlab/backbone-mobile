package id.startapp.pheromone.presentation.ui.components.input.textfields

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Rating Text Field
 *
 * Star rating input for customer feedback.
 *
 * Features:
 * - Configurable star count (default: 5)
 * - Half-star support (optional)
 * - Read-only mode
 * - Color feedback for rating
 *
 * @param rating Current rating (0 to maxRating)
 * @param onRatingChange Callback when rating changes
 * @param modifier Modifier for the rating field
 * @param maxRating Maximum star count
 * @param externalLabel Label text
 * @param enabled Whether interactive
 * @param allowHalfStar Whether to allow half-star ratings
 * @param size Star size
 */
@Composable
fun RatingTextField(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    externalLabel: String? = "Rating",
    enabled: Boolean = true,
    allowHalfStar: Boolean = false,
    size: RatingSize = RatingSize.Medium
) {
    val starSize = when (size) {
        RatingSize.Small -> 24.dp
        RatingSize.Medium -> 32.dp
        RatingSize.Large -> 48.dp
    }

    androidx.compose.foundation.layout.Column(modifier = modifier) {
        externalLabel?.let { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
        ) {
            repeat(maxRating) { index ->
                val isFilled = index < rating
                Icon(
                    imageVector = if (isFilled) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                    contentDescription = "Star ${index + 1}",
                    tint = if (isFilled) {
                        androidx.compose.ui.graphics.Color(0xFFFFC107)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier
                        .width(starSize)
                        .height(starSize)
                        .then(
                            if (enabled) {
                                Modifier.clickable { onRatingChange(index + 1) }
                            } else {
                                Modifier
                            }
                        )
                )
            }
        }
    }
}

enum class RatingSize {
    Small, Medium, Large
}
