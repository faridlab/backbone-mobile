package id.startapp.pheromone.presentation.ui.components.input.textfields

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import id.startapp.pheromone.presentation.ui.components.input.AppTextField

/**
 * Notes Text Field
 *
 * Specialized text field for notes, comments, and special instructions.
 *
 * Features:
 * - Multiline support
 * - Character counter
 * - Edit/Note icon
 * - Optional max length
 * - Used for order notes, special instructions, etc.
 *
 * @param value Current notes value
 * @param onValueChange Callback when notes change
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param externalLabelRequired Whether to show required asterisk
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param readOnly Whether the field is read-only
 * @param minLines Minimum number of visible lines
 * @param maxLines Maximum number of visible lines
 * @param maxLength Maximum character count (0 = unlimited)
 * @param showCounter Whether to show character counter
 * @param allowEmpty Whether empty value is allowed
 */
@Composable
fun NotesTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = null,
    externalLabelRequired: Boolean = false,
    placeholder: String? = "Enter your notes here...",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    minLines: Int = 3,
    maxLines: Int = 5,
    maxLength: Int = 0,
    showCounter: Boolean = true,
    allowEmpty: Boolean = true,
    testTag: String? = null,
    contentDescription: String? = null
) {
    // Enforce max length
    val truncatedValue = if (maxLength > 0 && value.length > maxLength) {
        value.take(maxLength)
    } else {
        value
    }

    SideEffect {
        if (maxLength > 0 && value.length > maxLength) {
            onValueChange(truncatedValue)
        }
    }

    AppTextField(
        value = truncatedValue,
        onValueChange = onValueChange,
        modifier = modifier,
        externalLabel = externalLabel ?: "Notes",
        externalLabelRequired = externalLabelRequired,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = readOnly,
        testTag = testTag,
        contentDescription = contentDescription,
        singleLine = false,
        minLines = minLines,
        maxLines = maxLines,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = "Notes",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Default
        ),
        helperText = buildHelperText(
            value = truncatedValue,
            maxLength = maxLength,
            showCounter = showCounter,
            placeholder = placeholder
        )
    )
}

/**
 * Build helper text with optional character counter
 */
@Composable
private fun buildHelperText(
    value: String,
    maxLength: Int,
    showCounter: Boolean,
    placeholder: String?
): String? {
    return when {
        showCounter && maxLength > 0 -> "${value.length}/$maxLength characters"
        showCounter -> "${value.length} characters"
        placeholder != null && value.isEmpty() -> placeholder
        else -> null
    }
}

/**
 * Order Notes Text Field
 *
 * Specialized for order notes with common options.
 *
 * @param value Current order notes value
 * @param onValueChange Callback when notes change
 * @param modifier Modifier for the text field
 * @param enabled Whether the field is enabled
 * @param maxLength Maximum character count
 */
@Composable
fun OrderNotesTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    maxLength: Int = 500
) {
    NotesTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        externalLabel = "Order Notes",
        placeholder = "Special instructions, delivery notes, preferences...",
        enabled = enabled,
        minLines = 3,
        maxLines = 6,
        maxLength = maxLength,
        showCounter = true
    )
}

/**
 * Special Instructions Text Field
 *
 * Specialized for special instructions (e.g., "Do not bleach", "Handle with care").
 *
 * @param value Current instructions value
 * @param onValueChange Callback when instructions change
 * @param modifier Modifier for the text field
 * @param enabled Whether the field is enabled
 * @param maxLength Maximum character count
 */
@Composable
fun SpecialInstructionsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    maxLength: Int = 300
) {
    NotesTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        externalLabel = "Special Instructions",
        placeholder = "e.g., Handle with care, Do not bleach, Hang dry only...",
        enabled = enabled,
        minLines = 2,
        maxLines = 4,
        maxLength = maxLength,
        showCounter = true
    )
}

/**
 * Address Text Field
 *
 * Specialized for address input with multiline support.
 *
 * @param value Current address value
 * @param onValueChange Callback when address changes
 * @param modifier Modifier for the text field
 * @param enabled Whether the field is enabled
 * @param required Whether the field is required
 */
@Composable
fun AddressTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    required: Boolean = true
) {
    NotesTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        externalLabel = "Street Address",
        externalLabelRequired = required,
        placeholder = "Street name, Building No, Area name...",
        enabled = enabled,
        minLines = 2,
        maxLines = 3,
        maxLength = 500,
        showCounter = false
    )
}

/**
 * Description Text Field
 *
 * General-purpose description field with longer support.
 *
 * @param value Current description value
 * @param onValueChange Callback when description changes
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param maxLength Maximum character count
 */
@Composable
fun DescriptionTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = null,
    placeholder: String? = "Enter description...",
    enabled: Boolean = true,
    maxLength: Int = 1000
) {
    NotesTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        externalLabel = externalLabel ?: "Description",
        placeholder = placeholder,
        enabled = enabled,
        minLines = 4,
        maxLines = 8,
        maxLength = maxLength,
        showCounter = true
    )
}
