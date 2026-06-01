package id.startapp.presentation.ui.components.input.textfields

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
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import id.startapp.presentation.ui.components.input.AppTextField

/**
 * Postal Code Text Field
 *
 * Specialized text field for Indonesia postal code input (5 digits).
 *
 * Features:
 * - Number keyboard type
 * - 5-digit validation (Indonesia postal code format)
 * - Max length enforcement (5 characters)
 * - Auto-numeric filtering
 * - Location icon
 *
 * @param value Current postal code value
 * @param onValueChange Callback when postal code changes
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param externalLabelRequired Whether to show required asterisk
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param readOnly Whether the field is read-only
 * @param allowEmpty Whether empty value is allowed (postal code is often optional)
 * @param onError Callback when validation fails
 * @param error External error message (overrides internal validation)
 */
@Composable
fun PostalCodeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = null,
    externalLabelRequired: Boolean = false,
    placeholder: String? = "12345",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    allowEmpty: Boolean = true,
    onError: ((String) -> Unit)? = null,
    error: String? = null,
    testTag: String? = null,
    contentDescription: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }

    // Enforce max length of 5 characters
    val filteredValue = value.take(5)

    SideEffect {
        if (value != filteredValue) {
            onValueChange(filteredValue)
        }
    }

    AppTextField(
        value = filteredValue,
        onValueChange = { newValue ->
            // Only allow digits
            val digitsOnly = newValue.filter { it.isDigit() }
            onValueChange(digitsOnly.take(5))
        },
        modifier = modifier,
        externalLabel = externalLabel ?: "Postal Code",
        externalLabelRequired = externalLabelRequired,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = readOnly,
        error = error ?: internalError,
        testTag = testTag,
        contentDescription = contentDescription,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = "Postal Code",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next,
            autoCorrectEnabled = false
        ),
        onBlur = {
            val result = when {
                // Optional field - empty is valid
                filteredValue.isBlank() -> ValidationResult.Success
                // Non-empty - validate
                else -> PostalCodeValidation.validate(filteredValue)
            }

            internalError = if (result.isValid) null else result.errorMessage
            if (!result.isValid && result.errorMessage != null) {
                onError?.invoke(result.errorMessage)
            }
        }
    )
}
