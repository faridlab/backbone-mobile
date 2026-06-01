package id.startapp.presentation.ui.components.input.textfields

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
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
 * Constants for NPWPTextField default values
 */
object NPWPTextFieldDefaults {
    const val NPWP_REQUIRED_DIGIT_COUNT = 15
}

/**
 * NPWP Text Field (Indonesian Tax ID)
 *
 * Specialized text field for Indonesian Tax ID (NPWP) number.
 *
 * Format: XX.XXX.XXX.X-XXX.XXX
 * - 2 digits: province code
 * - 3-6 digits: registration number
 * - Check digit (0-9)
 * - 3 digits: tax office code
 *
 * @param value Current NPWP value
 * @param onValueChange Callback when NPWP changes
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param externalLabelRequired Whether to show required asterisk
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param allowEmpty Whether empty value is allowed
 * @param error Error message
 */
@Composable
fun NPWPTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = "NPWP",
    externalLabelRequired: Boolean = false,
    placeholder: String? = "XX.XXX.XXX.X-XXX.XXX",
    enabled: Boolean = true,
    allowEmpty: Boolean = false,
    error: String? = null,
    testTag: String? = null,
    contentDescription: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }

    // Auto-format NPWP as user types
    fun formatNPWP(input: String): String {
        val digits = input.filter { it.isDigit() }
        return when {
            digits.isEmpty() -> ""
            digits.length <= 2 -> digits
            digits.length <= 5 -> "${digits.take(2)}.${digits.drop(2)}"
            digits.length <= 8 -> "${digits.take(2)}.${digits.drop(2).take(3)}.${digits.drop(5)}"
            digits.length <= 9 -> "${digits.take(2)}.${digits.drop(2).take(3)}.${digits.drop(5).take(3)}.${digits.drop(8)}"
            digits.length <= 12 -> "${digits.take(2)}.${digits.drop(2).take(3)}.${digits.drop(5).take(3)}.${digits.drop(8).take(1)}-${digits.drop(9)}"
            else -> "${digits.take(2)}.${digits.drop(2).take(3)}.${digits.drop(5).take(3)}.${digits.drop(8).take(1)}-${digits.drop(9).take(3)}.${digits.drop(12).take(3)}"
        }
    }

    AppTextField(
        value = value,
        onValueChange = { newValue ->
            onValueChange(formatNPWP(newValue))
        },
        modifier = modifier,
        externalLabel = externalLabel,
        externalLabelRequired = externalLabelRequired,
        placeholder = placeholder,
        enabled = enabled,
        error = error ?: internalError,
        testTag = testTag,
        contentDescription = contentDescription,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        helperText = "Format: XX.XXX.XXX.X-XXX.XXX",
        onBlur = {
            val result = if (value.isBlank() && !allowEmpty) {
                "NPWP is required"
            } else if (value.isNotBlank() && !isValidNPWP(value)) {
                "Invalid NPWP format"
            } else {
                null
            }
            internalError = result
        }
    )
}

private fun isValidNPWP(npwp: String): Boolean {
    val npwpRegex = Regex("""^\d{2}\.\d{3}\.\d{3}\.\d-\d{3}\.\d{3}$""")
    return npwpRegex.matches(npwp)
}
