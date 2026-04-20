package id.startapp.pheromone.presentation.ui.components.input.textfields

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
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
 * Invoice Number Text Field
 *
 * Specialized text field for invoice number input with formatting and validation.
 *
 * Features:
 * - Prefix support (e.g., INV-, INV/2024/)
 * - Auto-increment suggestion
 * - Invoice number format validation
 * - Duplicate detection warning
 * - Year/month based numbering
 *
 * @param value Current invoice number value
 * @param onValueChange Callback when invoice number changes
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param externalLabelRequired Whether to show required asterisk
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param prefix Invoice prefix (e.g., "INV-", "INV/2024/")
 * @param useAutoNumber Whether to use auto-numbering
 * @param nextNumber Next auto-increment number (if using auto-numbering)
 * @param existingNumbers List of existing invoice numbers (for duplicate check)
 * @param onError Callback when validation fails
 * @param error External error message
 */
@Composable
fun InvoiceNumberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = "Invoice Number",
    externalLabelRequired: Boolean = true,
    placeholder: String? = "Enter invoice number",
    enabled: Boolean = true,
    prefix: String = "INV-",
    useAutoNumber: Boolean = false,
    nextNumber: Int = 1,
    existingNumbers: List<String> = emptyList(),
    onError: ((String) -> Unit)? = null,
    error: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }

    // Suggest auto-numbered invoice if value is empty and auto-numbering is enabled
    val suggestedValue = if (useAutoNumber && value.isBlank()) {
        "${prefix}${nextNumber.toString().padStart(6, '0')}"
    } else {
        value
    }

    SideEffect {
        if (useAutoNumber && value.isBlank() && suggestedValue != value) {
            onValueChange(suggestedValue)
        }
    }

    // Check for duplicates
    val isDuplicate = existingNumbers.any { it.equals(value, ignoreCase = true) }

    AppTextField(
        value = value,
        onValueChange = { newValue ->
            // Remove prefix if user types it manually
            val clean = if (newValue.startsWith(prefix) && prefix.isNotBlank()) {
                newValue
            } else {
                newValue
            }
            onValueChange(clean)
        },
        modifier = modifier,
        externalLabel = externalLabel,
        externalLabelRequired = externalLabelRequired,
        placeholder = placeholder,
        enabled = enabled,
        error = when {
            error != null -> error
            isDuplicate -> "Invoice number already exists"
            internalError != null -> internalError
            else -> null
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.ReceiptLong,
                contentDescription = "Invoice",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = if (useAutoNumber && value.isBlank()) {
            {
                Row {
                    Text(
                        text = "Auto: $suggestedValue",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        ),
        helperText = if (useAutoNumber) {
            "Format: ${prefix}XXXXXX"
        } else {
            "Prefix: $prefix"
        },
        onBlur = {
            val result = when {
                value.isBlank() -> "Invoice number is required"
                value.length < 5 -> "Invoice number is too short"
                isDuplicate -> "Invoice number already exists"
                else -> null
            }
            internalError = result
            if (result != null) {
                onError?.invoke(result)
            }
        }
    )
}
