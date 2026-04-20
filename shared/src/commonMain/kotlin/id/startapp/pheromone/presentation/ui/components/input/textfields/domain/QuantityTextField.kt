package id.startapp.pheromone.presentation.ui.components.input.textfields.domain

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import id.startapp.pheromone.presentation.ui.components.input.AppTextField

/**
 * Quantity Text Field
 *
 * Specialized text field for order item quantities with stepper buttons.
 *
 * Features:
 * - Number keyboard type
 * - Min/max validation (default: 1-100)
 * - Stepper buttons (+/-)
 * - Auto-sets to 1 if empty on blur
 * - Prevents zero or negative values
 *
 * @param value Current quantity value
 * @param onValueChange Callback when quantity changes
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param externalLabelRequired Whether to show required asterisk
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param readOnly Whether the field is read-only
 * @param min Minimum quantity (default: 1)
 * @param max Maximum quantity (default: 100)
 * @param step Step increment for steppers (default: 1)
 * @param onError Callback when validation fails
 * @param error External error message (overrides internal validation)
 */
@Composable
fun QuantityTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = null,
    externalLabelRequired: Boolean = true,
    placeholder: String? = "1",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    min: Int = 1,
    max: Int = 100,
    step: Int = 1,
    onError: ((String) -> Unit)? = null,
    error: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }

    // Filter to allow only positive integers
    val filteredValue = value.filter { it.isDigit() }.takeIf { it.isNotEmpty() } ?: ""

    AppTextField(
        value = filteredValue,
        onValueChange = { newValue ->
            val digitsOnly = newValue.filter { it.isDigit() }
            onValueChange(digitsOnly)
        },
        modifier = modifier,
        externalLabel = externalLabel ?: "Quantity",
        externalLabelRequired = externalLabelRequired,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = readOnly,
        error = error ?: internalError,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        trailingIcon = if (enabled && !readOnly) {
            {
                Row {
                    IconButton(
                        onClick = {
                            val current = filteredValue.toIntOrNull() ?: min
                            val newValue = (current - step).coerceAtLeast(min)
                            onValueChange(newValue.toString())
                        },
                        enabled = (filteredValue.toIntOrNull() ?: min) > min
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = "Decrease quantity",
                            tint = if ((filteredValue.toIntOrNull() ?: min) > min) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            }
                        )
                    }
                    IconButton(
                        onClick = {
                            val current = filteredValue.toIntOrNull() ?: min
                            val newValue = (current + step).coerceAtMost(max)
                            onValueChange(newValue.toString())
                        },
                        enabled = (filteredValue.toIntOrNull() ?: min) < max
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Increase quantity",
                            tint = if ((filteredValue.toIntOrNull() ?: min) < max) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            }
                        )
                    }
                }
            }
        } else null,
        onBlur = {
            val errorMsg = if (filteredValue.isBlank()) {
                // Auto-set to minimum if empty
                onValueChange(min.toString())
                null
            } else {
                val num = filteredValue.toIntOrNull()
                when {
                    num == null -> "Invalid quantity"
                    num < min -> "Minimum quantity is $min"
                    num > max -> "Maximum quantity is $max"
                    else -> null
                }
            }

            internalError = errorMsg
            if (errorMsg != null) {
                onError?.invoke(errorMsg)
            }
        }
    )
}

/**
 * Quantity Text Field (Int version)
 *
 * Convenience version that works directly with Int values.
 *
 * @param value Current quantity value
 * @param onValueChange Callback when quantity changes
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param externalLabelRequired Whether to show required asterisk
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param readOnly Whether the field is read-only
 * @param min Minimum quantity
 * @param max Maximum quantity
 * @param step Step increment for steppers
 * @param onError Callback when validation fails
 * @param error External error message (overrides internal validation)
 */
@Composable
fun QuantityIntTextField(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = null,
    externalLabelRequired: Boolean = true,
    placeholder: String? = "1",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    min: Int = 1,
    max: Int = 100,
    step: Int = 1,
    onError: ((String) -> Unit)? = null,
    error: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }
    val stringValue = value.toString()

    AppTextField(
        value = stringValue,
        onValueChange = { newValue ->
            val digitsOnly = newValue.filter { it.isDigit() }
            digitsOnly.toIntOrNull()?.let { num ->
                onValueChange(num.coerceIn(min, max))
            }
        },
        modifier = modifier,
        externalLabel = externalLabel ?: "Quantity",
        externalLabelRequired = externalLabelRequired,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = readOnly,
        error = error ?: internalError,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        trailingIcon = if (enabled && !readOnly) {
            {
                Row {
                    IconButton(
                        onClick = {
                            val newValue = (value - step).coerceAtLeast(min)
                            onValueChange(newValue)
                        },
                        enabled = value > min
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = "Decrease",
                            tint = if (value > min) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            }
                        )
                    }
                    IconButton(
                        onClick = {
                            val newValue = (value + step).coerceAtMost(max)
                            onValueChange(newValue)
                        },
                        enabled = value < max
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Increase",
                            tint = if (value < max) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            }
                        )
                    }
                }
            }
        } else null,
        onBlur = {
            val errorMsg = when {
                value < min -> "Minimum quantity is $min"
                value > max -> "Maximum quantity is $max"
                else -> null
            }

            internalError = errorMsg
            if (errorMsg != null) {
                onError?.invoke(errorMsg)
            }
        }
    )
}
