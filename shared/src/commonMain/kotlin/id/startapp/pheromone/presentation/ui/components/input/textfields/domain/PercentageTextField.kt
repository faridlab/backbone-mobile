package id.startapp.pheromone.presentation.ui.components.input.textfields.domain

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
 * Percentage Text Field
 *
 * Specialized text field for percentage input (discounts, tax, etc.).
 *
 * Features:
 * - Decimal keyboard type
 * - % suffix display
 * - 0-100 range validation
 * - Decimal support (up to 2 places)
 * - Percent icon
 *
 * @param value Current percentage value (without % symbol)
 * @param onValueChange Callback when percentage changes
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
fun PercentageTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = null,
    externalLabelRequired: Boolean = true,
    placeholder: String? = "0",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    allowEmpty: Boolean = false,
    onError: ((String) -> Unit)? = null,
    error: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }

    // Filter to allow only valid percentage input (0-100, decimal allowed)
    val filteredValue = remember(value) {
        val clean = value.trim().replace("%", "")
        if (clean.isEmpty()) return@remember clean
        if (clean == ".") return@remember "0."

        // Only digits and one decimal point
        val parts = clean.split(".")
        if (parts.size > 2) return@remember parts[0] + "." + parts[1]

        // Limit decimal places to 2
        val formatted = if (parts.size == 2 && parts[1].length > 2) {
            "${parts[0]}.${parts[1].take(2)}"
        } else {
            clean
        }

        formatted
    }

    AppTextField(
        value = filteredValue,
        onValueChange = { newValue ->
            val clean = newValue.trim().replace("%", "")
            if (clean.isEmpty() || clean == ".") {
                onValueChange(clean)
                return@AppTextField
            }

            val parts = clean.split(".")
            if (parts.size > 2) {
                onValueChange("${parts[0]}.${parts[1]}")
                return@AppTextField
            }

            if (parts.size == 2 && parts[1].length > 2) {
                onValueChange("${parts[0]}.${parts[1].take(2)}")
                return@AppTextField
            }

            onValueChange(clean)
        },
        modifier = modifier,
        externalLabel = externalLabel ?: "Percentage",
        externalLabelRequired = externalLabelRequired,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = readOnly,
        error = error ?: internalError,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Percent,
                contentDescription = "Percentage",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            Text(
                text = "%",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next
        ),
        onBlur = {
            val errorMsg = when {
                filteredValue.isBlank() && !allowEmpty -> "Percentage is required"
                filteredValue.isNotBlank() -> {
                    val parsed = filteredValue.toDoubleOrNull()
                    when {
                        parsed == null -> "Invalid percentage"
                        parsed < 0 -> "Percentage cannot be negative"
                        parsed > 100 -> "Percentage cannot exceed 100"
                        else -> null
                    }
                }
                else -> null
            }

            internalError = errorMsg
            if (errorMsg != null) {
                onError?.invoke(errorMsg)
            }
        }
    )
}

/**
 * Percentage Text Field (Double version)
 *
 * Convenience version that works directly with Double values (0.0-100.0).
 *
 * @param value Current percentage value
 * @param onValueChange Callback when percentage changes
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
fun PercentageDoubleTextField(
    value: Double,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = null,
    externalLabelRequired: Boolean = true,
    placeholder: String? = "0",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    allowEmpty: Boolean = false,
    onError: ((String) -> Unit)? = null,
    error: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }

    // Format double to string (remove unnecessary decimal places)
    val displayValue = remember(value) {
        val asString = value.toString()
        if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            // For decimal values, limit to 2 decimal places and trim trailing zeros
            val parts = asString.split(".")
            if (parts.size > 1) {
                val decimals = parts[1].take(2).trimEnd('0')
                if (decimals.isEmpty()) parts[0] else "${parts[0]}.$decimals"
            } else {
                asString
            }
        }
    }

    AppTextField(
        value = displayValue,
        onValueChange = { newValue ->
            val parsed = newValue.trim().replace("%", "").toDoubleOrNull() ?: 0.0
            val clamped = parsed.coerceIn(0.0, 100.0)
            onValueChange(clamped)
        },
        modifier = modifier,
        externalLabel = externalLabel ?: "Percentage",
        externalLabelRequired = externalLabelRequired,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = readOnly,
        error = error ?: internalError,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Percent,
                contentDescription = "Percentage",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            Text(
                text = "%",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next
        ),
        onBlur = {
            val errorMsg = when {
                value < 0 -> "Percentage cannot be negative"
                value > 100 -> "Percentage cannot exceed 100"
                else -> null
            }

            internalError = errorMsg
            if (errorMsg != null) {
                onError?.invoke(errorMsg)
            }
        }
    )
}
