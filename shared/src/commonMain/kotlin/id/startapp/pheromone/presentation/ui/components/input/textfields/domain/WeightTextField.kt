package id.startapp.pheromone.presentation.ui.components.input.textfields.domain

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Scale
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
 * Weight Text Field
 *
 * Specialized text field for weight input (kilograms).
 *
 * Features:
 * - Decimal keyboard type
 * - kg suffix display
 * - Min/max validation (0.1kg - 50kg default)
 * - Scale icon
 * - Decimal support (up to 2 places)
 *
 * @param value Current weight value
 * @param onValueChange Callback when weight changes
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param externalLabelRequired Whether to show required asterisk
 * @param placeholder Placeholder text
 * @param unit Weight unit suffix
 * @param enabled Whether the field is enabled
 * @param readOnly Whether the field is read-only
 * @param min Minimum weight in kg
 * @param max Maximum weight in kg
 * @param allowEmpty Whether empty value is allowed
 * @param onError Callback when validation fails
 * @param error External error message (overrides internal validation)
 */
@Composable
fun WeightTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = null,
    externalLabelRequired: Boolean = true,
    placeholder: String? = "0.0",
    unit: String = "kg",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    min: Double = 0.1,
    max: Double = 50.0,
    allowEmpty: Boolean = false,
    onError: ((String) -> Unit)? = null,
    error: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }

    // Filter to allow only valid decimal input
    val filteredValue = remember(value) {
        val clean = value.trim()
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
            // Allow only digits and one decimal point
            val clean = newValue.trim()
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
        externalLabel = externalLabel ?: "Weight",
        externalLabelRequired = externalLabelRequired,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = readOnly,
        error = error ?: internalError,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Scale,
                contentDescription = "Weight",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            Text(
                text = unit,
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
                filteredValue.isBlank() && !allowEmpty -> "Weight is required"
                filteredValue.isNotBlank() -> {
                    val weight = filteredValue.toDoubleOrNull()
                    when {
                        weight == null -> "Invalid weight"
                        weight < min -> "Minimum weight is $min$unit"
                        weight > max -> "Maximum weight is $max$unit"
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
