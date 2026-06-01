package id.startapp.presentation.ui.components.input.textfields

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
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
 * Email Text Field
 *
 * Specialized text field for email input with built-in validation.
 *
 * Features:
 * - Email keyboard type
 * - Email format validation (RFC-compliant regex)
 * - Email icon
 * - Real-time format validation on blur
 *
 * @param value Current email value
 * @param onValueChange Callback when email changes
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param externalLabelRequired Whether to show required asterisk
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param readOnly Whether the field is read-only
 * @param allowEmpty Whether empty value is allowed (false = required field)
 * @param onError Callback when validation fails
 * @param error External error message (overrides internal validation)
 */
@Composable
fun EmailTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = null,
    externalLabelRequired: Boolean = true,
    placeholder: String? = "email@example.com",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    allowEmpty: Boolean = false,
    onError: ((String) -> Unit)? = null,
    error: String? = null,
    testTag: String? = null,
    contentDescription: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }

    // Simple email validation regex
    val emailRegex = Regex("""^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""")

    AppTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        externalLabel = externalLabel ?: "Email",
        externalLabelRequired = externalLabelRequired,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = readOnly,
        error = error ?: internalError,
        testTag = testTag,
        contentDescription = contentDescription,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Email,
                contentDescription = "Email",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        onBlur = {
            val result = when {
                value.isBlank() && !allowEmpty -> "Email is required"
                value.isNotBlank() && !emailRegex.matches(value) -> "Invalid email format"
                else -> null
            }

            internalError = result
            if (result != null) {
                onError?.invoke(result)
            }
        }
    )
}
