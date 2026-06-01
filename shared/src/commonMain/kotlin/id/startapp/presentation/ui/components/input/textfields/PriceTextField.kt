package id.startapp.presentation.ui.components.input.textfields

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import id.startapp.presentation.ui.components.input.AppTextField

/**
 * Constants for PriceTextField default values
 */
object PriceTextFieldDefaults {
    const val DEFAULT_MAX_PRICE = 1_000_000_000.0
}

/**
 * Price Text Field
 *
 * Specialized text field for price/amount input with currency formatting.
 *
 * Features:
 * - Decimal keyboard type
 * - Currency prefix (Rp, $, etc.)
 * - Thousand separator formatting
 * - Min/max amount validation
 * - Decimal support
 *
 * @param value Current price value as Double
 * @param onValueChange Callback when price changes (returns Double)
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param externalLabelRequired Whether to show required asterisk
 * @param placeholder Placeholder text
 * @param currency Currency symbol prefix
 * @param enabled Whether the field is enabled
 * @param readOnly Whether the field is read-only
 * @param min Minimum allowed value
 * @param max Maximum allowed value
 * @param allowEmpty Whether empty value is allowed
 * @param onError Callback when validation fails
 * @param error External error message (overrides internal validation)
 */
@Composable
fun PriceTextField(
    value: Double,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = null,
    externalLabelRequired: Boolean = true,
    placeholder: String? = "0",
    currency: String = "Rp",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    min: Double = 0.0,
    max: Double = PriceTextFieldDefaults.DEFAULT_MAX_PRICE,
    allowEmpty: Boolean = false,
    onError: ((String) -> Unit)? = null,
    error: String? = null,
    testTag: String? = null,
    contentDescription: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }

    // Format value for display (empty string if zero and allowEmpty)
    val displayValue = if (value == 0.0 && allowEmpty) "" else {
        PriceValidation.format(value)
    }

    AppTextField(
        value = displayValue,
        onValueChange = { newValue ->
            val parsed = PriceValidation.parse(newValue)
            onValueChange(parsed)
        },
        modifier = modifier,
        externalLabel = externalLabel ?: "Price",
        externalLabelRequired = externalLabelRequired,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = readOnly,
        error = error ?: internalError,
        testTag = testTag,
        contentDescription = contentDescription,
        leadingIcon = {
            Text(
                text = currency,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next,
            autoCorrectEnabled = false
        ),
        onBlur = {
            val result = when {
                // Empty and not allowed
                displayValue.isBlank() && !allowEmpty -> PriceValidation.validate("")
                // Empty and allowed - skip validation
                displayValue.isBlank() -> ValidationResult.Success
                // Non-empty - validate
                else -> PriceValidation.validate(displayValue, min, max)
            }

            internalError = if (result.isValid) null else result.errorMessage
            if (!result.isValid && result.errorMessage != null) {
                onError?.invoke(result.errorMessage)
            }
        }
    )
}

/**
 * Price Text Field with TextFieldValue (for cursor control)
 *
 * Version that accepts TextFieldValue for better cursor management.
 *
 * @param value Current TextFieldValue
 * @param onValueChange Callback when value changes
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param externalLabelRequired Whether to show required asterisk
 * @param placeholder Placeholder text
 * @param currency Currency symbol prefix
 * @param enabled Whether the field is enabled
 * @param readOnly Whether the field is read-only
 * @param min Minimum allowed value
 * @param max Maximum allowed value
 * @param onError Callback when validation fails
 * @param error External error message (overrides internal validation)
 */
@Composable
fun PriceTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = null,
    externalLabelRequired: Boolean = true,
    placeholder: String? = "0",
    currency: String = "Rp",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    min: Double = 0.0,
    max: Double = PriceTextFieldDefaults.DEFAULT_MAX_PRICE,
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
        externalLabel = externalLabel ?: "Price",
        externalLabelRequired = externalLabelRequired,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = readOnly,
        error = error ?: internalError,
        testTag = testTag,
        contentDescription = contentDescription,
        leadingIcon = {
            Text(
                text = currency,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next,
            autoCorrectEnabled = false
        ),
        onBlur = {
            val result = when {
                // Empty - validate as required
                value.text.isBlank() -> PriceValidation.validate("")
                // Non-empty - validate with range
                else -> PriceValidation.validate(value.text, min, max)
            }

            internalError = if (result.isValid) null else result.errorMessage
            if (!result.isValid && result.errorMessage != null) {
                onError?.invoke(result.errorMessage)
            }
        }
    )
}
