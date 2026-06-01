package id.startapp.presentation.ui.components.input.textfields

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Phone
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
 * Phone Text Field
 *
 * Specialized text field for phone number input with Indonesia format validation.
 *
 * Features:
 * - Phone keyboard type
 * - Indonesia phone format validation (08xx, +628xx, 628xx)
 * - Auto-formatting to +62... prefix
 * - Phone icon
 *
 * Accepted formats:
 * - 081234567890
 * - 6281234567890
 * - +6281234567890
 *
 * @param value Current phone value
 * @param onValueChange Callback when phone changes
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param externalLabelRequired Whether to show required asterisk
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param readOnly Whether the field is read-only
 * @param allowEmpty Whether empty value is allowed
 * @param autoFormat Whether to auto-format with +62 prefix
 * @param onError Callback when validation fails
 * @param error External error message (overrides internal validation)
 */
@Composable
fun PhoneTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = null,
    externalLabelRequired: Boolean = true,
    placeholder: String? = "812 3456 7890",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    allowEmpty: Boolean = false,
    autoFormat: Boolean = true,
    onError: ((String) -> Unit)? = null,
    error: String? = null,
    testTag: String? = null,
    contentDescription: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }

    AppTextField(
        value = value,
        onValueChange = { newValue ->
            if (autoFormat) {
                // Auto-format as user types
                val formatted = if (newValue.length <= 3) {
                    newValue
                } else {
                    PhoneValidation.format(newValue)
                }
                onValueChange(formatted)
            } else {
                onValueChange(newValue)
            }
        },
        modifier = modifier,
        externalLabel = externalLabel ?: "Phone Number",
        externalLabelRequired = externalLabelRequired,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = readOnly,
        error = error ?: internalError,
        testTag = testTag,
        contentDescription = contentDescription,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Phone,
                contentDescription = "Phone",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next,
            autoCorrectEnabled = false
        ),
        onBlur = {
            val result = when {
                // Empty and not allowed
                value.isBlank() && !allowEmpty -> PhoneValidation.validate("")
                // Empty and allowed - skip validation
                value.isBlank() -> ValidationResult.Success
                // Non-empty - validate
                else -> PhoneValidation.validate(value)
            }

            internalError = if (result.isValid) null else result.errorMessage
            if (!result.isValid && result.errorMessage != null) {
                onError?.invoke(result.errorMessage)
            }
        }
    )
}

/**
 * WhatsApp Phone Text Field
 *
 * Specialized variant for WhatsApp numbers with WhatsApp branding.
 *
 * @param value Current WhatsApp number value
 * @param onValueChange Callback when WhatsApp number changes
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
fun WhatsAppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = "WhatsApp Number",
    externalLabelRequired: Boolean = false,
    placeholder: String? = "812 3456 7890",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    allowEmpty: Boolean = true, // WhatsApp is often optional
    onError: ((String) -> Unit)? = null,
    error: String? = null,
    testTag: String? = null,
    contentDescription: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }

    AppTextField(
        value = value,
        onValueChange = { newValue ->
            val formatted = PhoneValidation.format(newValue)
            onValueChange(formatted)
        },
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
                imageVector = Icons.Rounded.Phone,
                contentDescription = "WhatsApp",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next,
            autoCorrectEnabled = false
        ),
        helperText = "Used for order notifications",
        onBlur = {
            val result = when {
                // Empty and not allowed
                value.isBlank() && !allowEmpty -> PhoneValidation.validate("")
                // Empty and allowed - skip validation
                value.isBlank() -> ValidationResult.Success
                // Non-empty - validate
                else -> PhoneValidation.validate(value)
            }

            internalError = if (result.isValid) null else result.errorMessage
            if (!result.isValid && result.errorMessage != null) {
                onError?.invoke(result.errorMessage)
            }
        }
    )
}
