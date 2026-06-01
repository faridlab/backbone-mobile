package id.startapp.presentation.ui.components.input.textfields

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import id.startapp.presentation.ui.components.input.AppTextField

/**
 * Constants for NumberTextField default values
 */
object NumberTextFieldDefaults {
    const val DEFAULT_STEP = 1
}

/**
 * Number Text Field
 *
 * Specialized text field for numeric input with validation.
 *
 * Features:
 * - Number keyboard type
 * - Min/max validation
 * - Optional stepper buttons (+/-)
 * - Integer-only input
 *
 * @param value Current numeric value
 * @param onValueChange Callback when value changes
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param externalLabelRequired Whether to show required asterisk
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param readOnly Whether the field is read-only
 * @param min Minimum allowed value
 * @param max Maximum allowed value
 * @param showSteppers Whether to show +/- stepper buttons
 * @param step Step increment for steppers
 * @param allowEmpty Whether empty value is allowed
 * @param onError Callback when validation fails
 * @param error External error message (overrides internal validation)
 */
@Composable
fun NumberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = null,
    externalLabelRequired: Boolean = true,
    placeholder: String? = "0",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    min: Int? = null,
    max: Int? = null,
    showSteppers: Boolean = false,
    step: Int = NumberTextFieldDefaults.DEFAULT_STEP,
    allowEmpty: Boolean = false,
    onError: ((String) -> Unit)? = null,
    error: String? = null,
    testTag: String? = null,
    contentDescription: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }

    // Filter non-numeric characters
    val numericValue = value.filter { it.isDigit() || it == '-' }

    AppTextField(
        value = numericValue,
        onValueChange = { newValue ->
            val filtered = if (newValue.startsWith("-")) {
                "-" + newValue.filter { it.isDigit() }
            } else {
                newValue.filter { it.isDigit() }
            }
            onValueChange(filtered)
        },
        modifier = modifier,
        externalLabel = externalLabel ?: "Number",
        externalLabelRequired = externalLabelRequired,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = readOnly,
        error = error ?: internalError,
        testTag = testTag,
        contentDescription = contentDescription,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next,
            autoCorrectEnabled = false
        ),
        trailingIcon = if (showSteppers && enabled && !readOnly) {
            {
                Row {
                    IconButton(
                        onClick = {
                            val current = numericValue.toIntOrNull() ?: (min ?: 0)
                            val newValue = current - step
                            if (min == null || newValue >= min) {
                                onValueChange(newValue.toString())
                            }
                        },
                        modifier = Modifier.width(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = "Decrease",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = {
                            val current = numericValue.toIntOrNull() ?: (min ?: 0)
                            val newValue = current + step
                            if (max == null || newValue <= max) {
                                onValueChange(newValue.toString())
                            }
                        },
                        modifier = Modifier.width(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Increase",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else null,
        onBlur = {
            val result = if (numericValue.isBlank() && !allowEmpty) {
                NumberValidation.validate("", min, max)
            } else if (numericValue.isNotBlank()) {
                NumberValidation.validate(numericValue, min, max)
            } else {
                ValidationResult.Success
            }

            internalError = if (result.isValid) null else result.errorMessage
            if (!result.isValid && result.errorMessage != null) {
                onError?.invoke(result.errorMessage)
            }
        }
    )
}

/**
 * Integer Text Field (value as Int)
 *
 * Convenience version that works directly with Int values.
 *
 * @param value Current integer value
 * @param onValueChange Callback when value changes
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param externalLabelRequired Whether to show required asterisk
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param readOnly Whether the field is read-only
 * @param min Minimum allowed value
 * @param max Maximum allowed value
 * @param showSteppers Whether to show +/- stepper buttons
 * @param step Step increment for steppers
 * @param onError Callback when validation fails
 * @param error External error message (overrides internal validation)
 */
@Composable
fun IntegerTextField(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = null,
    externalLabelRequired: Boolean = true,
    placeholder: String? = "0",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    min: Int? = null,
    max: Int? = null,
    showSteppers: Boolean = false,
    step: Int = NumberTextFieldDefaults.DEFAULT_STEP,
    onError: ((String) -> Unit)? = null,
    error: String? = null,
    testTag: String? = null,
    contentDescription: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }
    val stringValue = value.toString()

    AppTextField(
        value = stringValue,
        onValueChange = { newValue ->
            val filtered = newValue.filter { it.isDigit() || (it == '-' && newValue.startsWith("-")) }
            filtered.toIntOrNull()?.let { onValueChange(it) }
        },
        modifier = modifier,
        externalLabel = externalLabel ?: "Number",
        externalLabelRequired = externalLabelRequired,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = readOnly,
        error = error ?: internalError,
        testTag = testTag,
        contentDescription = contentDescription,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next,
            autoCorrectEnabled = false
        ),
        trailingIcon = if (showSteppers && enabled && !readOnly) {
            {
                Row {
                    IconButton(
                        onClick = {
                            val newValue = value - step
                            if (min == null || newValue >= min) {
                                onValueChange(newValue)
                            }
                        },
                        modifier = Modifier.width(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = "Decrease",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = {
                            val newValue = value + step
                            if (max == null || newValue <= max) {
                                onValueChange(newValue)
                            }
                        },
                        modifier = Modifier.width(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Increase",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else null,
        onBlur = {
            val result = NumberValidation.validate(stringValue, min, max)
            internalError = if (result.isValid) null else result.errorMessage
            if (!result.isValid && result.errorMessage != null) {
                onError?.invoke(result.errorMessage)
            }
        }
    )
}
