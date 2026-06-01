package id.startapp.presentation.ui.components.input.textfields

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import id.startapp.presentation.ui.components.input.AppTextField

/**
 * Name Text Field
 *
 * Specialized text field for person/business names with validation.
 *
 * Features:
 * - Text keyboard type
 * - Auto-capitalization (sentences)
 * - Name validation (letters, spaces, and common punctuation)
 * - Min length validation
 * - Person icon
 *
 * Allowed characters: letters, spaces, ampersand, period, comma, apostrophe, hyphen
 *
 * @param value Current name value
 * @param onValueChange Callback when name changes
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param externalLabelRequired Whether to show required asterisk
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param readOnly Whether the field is read-only
 * @param minLength Minimum name length (default: 2)
 * @param allowEmpty Whether empty value is allowed
 * @param onError Callback when validation fails
 * @param error External error message (overrides internal validation)
 */
@Composable
fun NameTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = null,
    externalLabelRequired: Boolean = true,
    placeholder: String? = "John Doe",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    minLength: Int = 2,
    allowEmpty: Boolean = false,
    onError: ((String) -> Unit)? = null,
    error: String? = null,
    testTag: String? = null,
    contentDescription: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }

    AppTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        externalLabel = externalLabel ?: "Name",
        externalLabelRequired = externalLabelRequired,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = readOnly,
        error = error ?: internalError,
        testTag = testTag,
        contentDescription = contentDescription,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = "Name",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        ),
        onBlur = {
            // Simple name validation - letters, spaces, and common punctuation only
            val namePattern = Regex("""^[a-zA-Z\s\-'.]+$""")
            val result = when {
                value.isBlank() && !allowEmpty -> "Name is required"
                value.isNotBlank() && value.length < minLength -> "Name must be at least $minLength characters"
                value.isNotBlank() && !namePattern.matches(value.trim()) -> "Name contains invalid characters"
                else -> null
            }

            internalError = result
            if (result != null) {
                onError?.invoke(result)
            }
        }
    )
}

/**
 * First Name Text Field
 *
 * Specialized for first names (typically shorter, single word).
 *
 * @param value Current first name value
 * @param onValueChange Callback when first name changes
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param externalLabelRequired Whether to show required asterisk
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param readOnly Whether the field is read-only
 * @param allowEmpty Whether empty value is allowed
 * @param onError Callback when validation fails
 * @param error External error message (overrides internal validation)
 */
@Composable
fun FirstNameTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = "First Name",
    externalLabelRequired: Boolean = true,
    placeholder: String? = "John",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    allowEmpty: Boolean = false,
    onError: ((String) -> Unit)? = null,
    error: String? = null,
    testTag: String? = null,
    contentDescription: String? = null
) {
    NameTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        externalLabel = externalLabel,
        externalLabelRequired = externalLabelRequired,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = readOnly,
        minLength = 2,
        allowEmpty = allowEmpty,
        onError = onError,
        error = error,
        testTag = testTag,
        contentDescription = contentDescription
    )
}

/**
 * Last Name Text Field
 *
 * Specialized for last names (may include hyphens, apostrophes).
 *
 * @param value Current last name value
 * @param onValueChange Callback when last name changes
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param externalLabelRequired Whether to show required asterisk
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param readOnly Whether the field is read-only
 * @param allowEmpty Whether empty value is allowed
 * @param onError Callback when validation fails
 * @param error External error message (overrides internal validation)
 */
@Composable
fun LastNameTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = "Last Name",
    externalLabelRequired: Boolean = true,
    placeholder: String? = "Doe",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    allowEmpty: Boolean = false,
    onError: ((String) -> Unit)? = null,
    error: String? = null,
    testTag: String? = null,
    contentDescription: String? = null
) {
    NameTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        externalLabel = externalLabel,
        externalLabelRequired = externalLabelRequired,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = readOnly,
        minLength = 2,
        allowEmpty = allowEmpty,
        onError = onError,
        error = error,
        testTag = testTag,
        contentDescription = contentDescription
    )
}

/**
 * Business Name Text Field
 *
 * Specialized for business names (allows more characters, longer).
 *
 * @param value Current business name value
 * @param onValueChange Callback when business name changes
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param externalLabelRequired Whether to show required asterisk
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param readOnly Whether the field is read-only
 * @param allowEmpty Whether empty value is allowed
 * @param onError Callback when validation fails
 * @param error External error message (overrides internal validation)
 */
@Composable
fun BusinessNameTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = "Business Name",
    externalLabelRequired: Boolean = true,
    placeholder: String? = "Backbone",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    allowEmpty: Boolean = false,
    onError: ((String) -> Unit)? = null,
    error: String? = null,
    testTag: String? = null,
    contentDescription: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }

    AppTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        externalLabel = externalLabel,
        externalLabelRequired = externalLabelRequired,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = readOnly,
        error = error ?: internalError,
        testTag = testTag,
        contentDescription = contentDescription,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = "Business Name",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        ),
        onBlur = {
            val result = when {
                value.isBlank() && !allowEmpty -> "Business name is required"
                value.trim().length < 3 -> "Business name must be at least 3 characters"
                else -> null
            }

            internalError = result
            if (result != null) {
                onError?.invoke(result)
            }
        }
    )
}
