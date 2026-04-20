package id.startapp.pheromone.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.startapp.pheromone.presentation.ui.components.button.PrimaryButton
import id.startapp.pheromone.presentation.ui.components.input.textfields.DateTextField
import id.startapp.pheromone.presentation.ui.theme.PheromoneBlue
import id.startapp.pheromone.presentation.ui.theme.PheromoneDimensions
import id.startapp.pheromone.presentation.ui.theme.SurfaceGray300
import kotlinx.datetime.LocalDate

/**
 * Shared date filter UI components used across Dashboard and Order List screens.
 */

// ============================================================================
// DateFilterChip
// ============================================================================

/**
 * A chip that shows the current date filter label and opens a selection sheet when clicked.
 *
 * @param label The text to display (e.g. "Today", "This Week")
 * @param onClick Called when the chip is tapped
 * @param backgroundColor Background color of the chip (defaults to surface)
 * @param iconTint Tint color for the icons (defaults to onSurfaceVariant)
 * @param modifier Optional modifier
 */
@Composable
internal fun DateFilterChip(
    label: String,
    onClick: () -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = iconTint,
        )
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = iconTint,
        )
    }
}

// ============================================================================
// DateFilterSelectionSheet
// ============================================================================

/**
 * A bottom sheet content that displays a list of date filter options with radio buttons.
 *
 * @param title The sheet title
 * @param subtitle The sheet subtitle
 * @param options The list of filter options to display
 * @param selectedOption The currently selected option
 * @param onOptionSelected Called when a filter option is tapped
 * @param displayText Produces the label string for each option
 * @param confirmButtonText Optional text for a confirm button; if null, no button is shown
 * @param onConfirm Called when the confirm button is tapped (only used when confirmButtonText is set)
 * @param modifier Optional modifier
 */
@Composable
internal fun <T> DateFilterSelectionSheet(
    title: String,
    subtitle: String,
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    displayText: (T) -> String,
    confirmButtonText: String? = null,
    onConfirm: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = PheromoneDimensions.LG.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PheromoneDimensions.MD.dp),
            verticalArrangement = Arrangement.spacedBy(PheromoneDimensions.XS.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(PheromoneDimensions.LG.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            options.forEach { option ->
                FilterOptionRow(
                    label = displayText(option),
                    isSelected = option == selectedOption,
                    onClick = { onOptionSelected(option) },
                )
            }
        }

        if (confirmButtonText != null) {
            Spacer(modifier = Modifier.height(PheromoneDimensions.LG.dp))

            PrimaryButton(
                text = confirmButtonText,
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PheromoneDimensions.MD.dp),
            )
        }
    }
}

// ============================================================================
// FilterOptionRow
// ============================================================================

/**
 * A single filter option row with a label and a radio button.
 *
 * @param label The option text
 * @param isSelected Whether this option is currently selected
 * @param onClick Called when the row is tapped
 */
@Composable
internal fun FilterOptionRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderModifier = if (isSelected) {
        Modifier.border(1.5.dp, PheromoneBlue, RoundedCornerShape(12.dp))
    } else {
        Modifier.border(1.dp, SurfaceGray300.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PheromoneDimensions.MD.dp, vertical = 4.dp)
            .then(borderModifier)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(PheromoneDimensions.MD.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        )
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = PheromoneBlue,
                unselectedColor = SurfaceGray300,
            ),
        )
    }
}

// ============================================================================
// CustomDateSheet
// ============================================================================

/**
 * A bottom sheet content for picking a custom date.
 *
 * @param date The currently selected date (or null)
 * @param onDateChange Called when the date changes
 * @param onConfirm Called when the user confirms the date selection
 * @param onBack Called when the user taps the back icon
 * @param title Title text shown in the header
 * @param subtitle Subtitle text shown below the title
 * @param dateLabel Label for the date text field
 * @param datePlaceholder Placeholder for the date text field
 * @param confirmText Text for the confirm button
 * @param modifier Optional modifier
 */
@Composable
internal fun CustomDateSheet(
    date: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
    title: String = "Pilih Tanggal",
    subtitle: String = "Pilih tanggal untuk filter laporan",
    dateLabel: String = "Tanggal",
    datePlaceholder: String = "Pilih tanggal",
    confirmText: String = "Terapkan",
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = PheromoneDimensions.LG.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PheromoneDimensions.MD.dp),
            verticalArrangement = Arrangement.spacedBy(PheromoneDimensions.XS.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp).clickable(onClick = onBack),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(PheromoneDimensions.LG.dp))

        DateTextField(
            date = date,
            onDateChange = onDateChange,
            externalLabel = dateLabel,
            placeholder = datePlaceholder,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PheromoneDimensions.MD.dp),
        )

        Spacer(modifier = Modifier.height(PheromoneDimensions.LG.dp))

        PrimaryButton(
            text = confirmText,
            onClick = { if (date != null) onConfirm() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PheromoneDimensions.MD.dp),
        )
    }
}
