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
import androidx.compose.material.icons.rounded.AccountBalance
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
import androidx.compose.ui.unit.dp
import id.startapp.presentation.ui.components.input.AppTextField

/**
 * Bank Account Text Field
 *
 * Specialized text field for bank account number input with formatting.
 *
 * Features:
 * - Numeric only input
 * - Grouped display (optional spacing)
 * - Min length validation (typically 10-16 digits)
 * - Bank icon
 *
 * @param value Current account number value
 * @param onValueChange Callback when account number changes
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param externalLabelRequired Whether to show required asterisk
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param minLength Minimum account number length (default: 10)
 * @param maxLength Maximum account number length (default: 16)
 * @param showGrouping Whether to show grouping (e.g., XXXX XXXX XXXX XXXX)
 * @param onError Callback when validation fails
 * @param error External error message
 * @param testTag Test tag for UI testing
 * @param contentDescription Content description for accessibility
 */
@Composable
fun BankAccountTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = "Bank Account Number",
    externalLabelRequired: Boolean = true,
    placeholder: String? = "Enter account number",
    enabled: Boolean = true,
    minLength: Int = 10,
    maxLength: Int = 16,
    showGrouping: Boolean = true,
    onError: ((String) -> Unit)? = null,
    error: String? = null,
    testTag: String? = null,
    contentDescription: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }

    // Filter to digits only
    val digitsOnly = value.filter { it.isDigit() }.take(maxLength)

    // Format for display with grouping
    val displayValue = if (showGrouping && digitsOnly.isNotEmpty()) {
        digitsOnly.chunked(4).joinToString(" ")
    } else {
        digitsOnly
    }

    SideEffect {
        if (value != digitsOnly) {
            onValueChange(digitsOnly)
        }
    }

    AppTextField(
        value = displayValue,
        onValueChange = { newValue ->
            val digits = newValue.filter { it.isDigit() }.take(maxLength)
            onValueChange(digits)
        },
        modifier = modifier,
        externalLabel = externalLabel,
        externalLabelRequired = externalLabelRequired,
        placeholder = placeholder,
        enabled = enabled,
        error = error ?: internalError,
        testTag = testTag,
        contentDescription = contentDescription,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.AccountBalance,
                contentDescription = "Bank Account",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        helperText = "$minLength-$maxLength digits",
        onBlur = {
            val result = when {
                digitsOnly.isBlank() -> "Account number is required"
                digitsOnly.length < minLength -> "Account number must be at least $minLength digits"
                else -> null
            }
            internalError = result
            if (result != null) {
                onError?.invoke(result)
            }
        }
    )
}
