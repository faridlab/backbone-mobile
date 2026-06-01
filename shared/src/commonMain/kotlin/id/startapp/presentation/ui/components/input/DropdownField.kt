package id.startapp.presentation.ui.components.input

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import id.startapp.presentation.ui.theme.BackboneDimensions

/**
 * Backbone Dropdown Field
 *
 * Select dropdown with options list.
 *
 * Specs:
 * - Height: 56dp
 * - Trailing icon: Arrow down/up
 * - Popup with options on click
 * - Selected option highlighted
 *
 * @param label Field label
 * @param selectedOption Currently selected option
 * @param options List of available options
 * @param onOptionSelected Callback when option is selected
 * @param modifier Modifier for the field
 * @param placeholder Text shown when no option is selected
 * @param enabled Whether the field is enabled
 */
@Composable
fun <T> DropdownField(
    label: String,
    selectedOption: T?,
    options: List<T>,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Select...",
    enabled: Boolean = true,
    displayText: (T) -> String = { it.toString() }
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // Dropdown trigger
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(BackboneDimensions.InputHeight.dp)
                .clickable(enabled = enabled) { expanded = !expanded },
            shape = MaterialTheme.shapes.small,
            colors = CardDefaults.cardColors(
                containerColor = if (enabled) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                }
            ),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = BackboneDimensions.MD.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Label and selected text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Label
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Selected value or placeholder
                        Text(
                            text = selectedOption?.let { displayText(it) } ?: placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selectedOption != null) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Arrow icon
                    Icon(
                        imageVector = if (expanded) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Dropdown options popup
        if (expanded) {
            DropdownPopup(
                options = options,
                selectedOption = selectedOption,
                onOptionSelected = { option ->
                    onOptionSelected(option)
                    expanded = false
                },
                onDismiss = { expanded = false },
                displayText = displayText
            )
        }
    }
}

/**
 * Dropdown Popup
 *
 * Popup showing dropdown options.
 */
@Composable
fun <T> DropdownPopup(
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    onDismiss: () -> Unit,
    displayText: (T) -> String = { it.toString() }
) {
    Popup(
        alignment = Alignment.TopCenter,
        properties = PopupProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            focusable = true
        ),
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = BackboneDimensions.MD.dp),
            shape = MaterialTheme.shapes.small,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(options) { option ->
                    val isSelected = option == selectedOption

                    DropdownOptionItem(
                        text = displayText(option),
                        isSelected = isSelected,
                        onClick = { onOptionSelected(option) }
                    )
                }
            }
        }
    }
}

/**
 * Dropdown Option Item
 *
 * Single option in dropdown list.
 */
@Composable
fun DropdownOptionItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(
                horizontal = BackboneDimensions.MD.dp,
                vertical = BackboneDimensions.SM.dp
            )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
