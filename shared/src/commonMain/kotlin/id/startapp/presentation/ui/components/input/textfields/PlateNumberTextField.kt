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
 * Plate Number Text Field
 *
 * Specialized text field for Indonesian vehicle plate numbers.
 *
 * Format: B 1234 ABC (Jakarta) or AB 1234 XY (other regions)
 * Supports both old and new plate formats
 *
 * @param value Current plate number value
 * @param onValueChange Callback when plate changes
 * @param modifier Modifier
 * @param externalLabel Label
 * @param enabled Whether enabled
 * @param error Error message
 * @param testTag Test tag for UI testing
 * @param contentDescription Content description for accessibility
 */
@Composable
fun PlateNumberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = "License Plate",
    placeholder: String? = "B 1234 ABC",
    enabled: Boolean = true,
    error: String? = null,
    testTag: String? = null,
    contentDescription: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }

    // Auto-format: uppercase and add spaces
    fun formatPlate(input: String): String {
        val clean = input.filter { !it.isWhitespace() }.uppercase()
        return when {
            clean.length <= 2 -> clean
            clean.length <= 6 -> clean.take(2) + " " + clean.drop(2)
            clean.length <= 10 -> clean.take(2) + " " + clean.drop(2).take(4) + " " + clean.drop(6)
            else -> clean
        }
    }

    val displayValue = formatPlate(value)

    AppTextField(
        value = displayValue,
        onValueChange = onValueChange,
        modifier = modifier,
        externalLabel = externalLabel,
        placeholder = placeholder,
        enabled = enabled,
        error = error ?: internalError,
        testTag = testTag,
        contentDescription = contentDescription,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        helperText = "Format: B 1234 ABC or AB 1234 XY",
        onBlur = {
            val cleanValue = value.replace(" ", "")
            internalError = if (cleanValue.isNotBlank() && !isValidPlate(cleanValue)) {
                "Invalid plate format"
            } else null
        }
    )
}

/**
 * Validates Indonesian vehicle plate number format.
 *
 * Supported formats:
 * - B 1234 ABC (Jakarta, single letter region code)
 * - AB 1234 XY (Other regions, two letter region code)
 * - Old format: B 1234 XX (shorter suffix)
 *
 * Format breakdown:
 * - Region code: 1-2 letters (A-Z)
 * - Number: 1-4 digits
 * - Suffix: 1-3 letters (A-Z)
 *
 * Examples of valid plates:
 * - B1234ABC (Jakarta)
 * - D1234XX (East Java)
 * - AB1234CD (West Java)
 *
 * @param plate Plate number WITHOUT spaces (already cleaned)
 * @return true if matches Indonesian plate number format
 */
private fun isValidPlate(plate: String): Boolean {
    val plateRegex = Regex("""^[A-Z]{1,2}\d{1,4}[A-Z]{1,3}$""")
    return plateRegex.matches(plate)
}
