package id.startapp.presentation.ui.components.input.textfields.domain

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import id.startapp.presentation.ui.components.input.AppTextField

/**
 * Time Text Field
 *
 * Specialized text field for time input in HH:MM format (24-hour).
 *
 * Features:
 * - Number keyboard type
 * - HH:MM format validation (24-hour)
 * - Auto-formatting as user types
 * - Clock icon
 * - Used for operating hours, opening times, etc.
 *
 * @param value Current time value (HH:MM format)
 * @param onValueChange Callback when time changes
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
fun TimeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = null,
    externalLabelRequired: Boolean = true,
    placeholder: String? = "HH:MM",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    allowEmpty: Boolean = false,
    onError: ((String) -> Unit)? = null,
    error: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }

    // Auto-format time as user types (HH:MM format)
    fun formatTime(input: String): String {
        val digits = input.filter { it.isDigit() }
        return when {
            digits.isEmpty() -> ""
            digits.length <= 2 -> digits
            digits.length == 3 -> "${digits.take(2)}:${digits.last()}"
            digits.length >= 4 -> {
                val hour = digits.take(2).toIntOrNull()?.coerceIn(0, 23)?.toString() ?: digits.take(2)
                val minute = digits.drop(2).take(2).toIntOrNull()?.coerceIn(0, 59)?.toString() ?: digits.drop(2).take(2)
                "$hour:$minute"
            }
            else -> input
        }
    }

    // Validate HH:MM format
    fun isValidTime(time: String): Boolean {
        val timeRegex = Regex("""^([01]\d|2[0-3]):([0-5]\d)$""")
        return timeRegex.matches(time)
    }

    AppTextField(
        value = value,
        onValueChange = { newValue ->
            val formatted = formatTime(newValue)
            onValueChange(formatted)
        },
        modifier = modifier,
        externalLabel = externalLabel ?: "Time",
        externalLabelRequired = externalLabelRequired,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = readOnly,
        error = error ?: internalError,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.AccessTime,
                contentDescription = "Time",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        helperText = "24-hour format (e.g., 09:00, 14:30)",
        onBlur = {
            val errorMsg = when {
                value.isBlank() && !allowEmpty -> "Time is required"
                value.isNotBlank() && !isValidTime(value) -> "Invalid time format (use HH:MM)"
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
 * Opening Hours Time Range Text Field
 *
 * Combined field for opening and closing times.
 *
 * @param openTime Opening time value
 * @param closeTime Closing time value
 * @param onOpenTimeChange Callback when opening time changes
 * @param onCloseTimeChange Callback when closing time changes
 * @param modifier Modifier for the text fields
 * @param externalLabel Label text shown above fields
 * @param enabled Whether the fields are enabled
 * @param onError Callback when validation fails
 * @param openTimeError External error message for opening time
 * @param closeTimeError External error message for closing time
 */
@Composable
fun OperatingHoursField(
    openTime: String,
    closeTime: String,
    onOpenTimeChange: (String) -> Unit,
    onCloseTimeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = null,
    enabled: Boolean = true,
    onError: ((String) -> Unit)? = null,
    openTimeError: String? = null,
    closeTimeError: String? = null
) {
    var internalOpenError by remember { mutableStateOf<String?>(null) }
    var internalCloseError by remember { mutableStateOf<String?>(null) }

    Row(modifier = modifier) {
        // Opening Time
        TimeTextField(
            value = openTime,
            onValueChange = onOpenTimeChange,
            modifier = Modifier.weight(1f),
            externalLabel = externalLabel ?: "Operating Hours",
            placeholder = "09:00",
            enabled = enabled,
            error = openTimeError ?: internalOpenError,
            onError = onError
        )

        Spacer(modifier = Modifier.fillMaxWidth(0.05f))

        // Separator
        Text(
            text = "—",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterVertically)
        )

        Spacer(modifier = Modifier.fillMaxWidth(0.05f))

        // Closing Time
        TimeTextField(
            value = closeTime,
            onValueChange = onCloseTimeChange,
            modifier = Modifier.weight(1f),
            externalLabel = "", // Only show label on first field
            placeholder = "17:00",
            enabled = enabled,
            error = closeTimeError ?: internalCloseError,
            onError = onError
        )
    }
}
