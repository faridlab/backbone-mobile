package id.startapp.presentation.ui.components.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.startapp.presentation.ui.theme.BackboneBlue
import id.startapp.presentation.ui.theme.BackboneDimensions
import id.startapp.presentation.ui.theme.SurfaceGray200

/**
 * Filter Chips Row
 *
 * Horizontal scrollable filter chips following design spec.
 *
 * Specs:
 * - Height: 40dp
 * - Spacing: 8dp between chips
 * - Padding: 12dp horizontal
 * - Border Radius: 20dp
 * - Unselected: Gray-200 bg, Gray text
 * - Selected: Brand Blue bg, White text
 *
 * @param filters List of filter options
 * @param selectedFilter Currently selected filter
 * @param onFilterSelected Callback when filter is selected
 * @param modifier Modifier for the component
 */
@Composable
fun FilterChips(
    filters: List<FilterOption>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(BackboneDimensions.SM.dp)
    ) {
        items(filters) { filter ->
            FilterChip(
                label = filter.label,
                isSelected = filter.id == selectedFilter,
                count = filter.count,
                onClick = { onFilterSelected(filter.id) }
            )
        }
    }
}

/**
 * Individual Filter Chip
 */
@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    count: Int? = null,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        BackboneBlue
    } else {
        SurfaceGray200
    }

    val textColor = if (isSelected) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )

        if (count != null) {
            Text(
                text = " ($count)",
                style = MaterialTheme.typography.labelMedium,
                color = textColor
            )
        }
    }
}

/**
 * Pill Badge
 *
 * Small pill-shaped badge for status indicators.
 *
 * Specs:
 * - Height: 24dp
 * - Padding: 8dp horizontal
 * - Border Radius: 12dp
 * - Font: Label Small
 *
 * @param text Badge text
 * @param color Badge background color
 * @param modifier Modifier for the component
 */
@Composable
fun PillBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = Color.White
    )
}

/**
 * Data class for filter option
 */
data class FilterOption(
    val id: String,
    val label: String,
    val count: Int? = null
)
