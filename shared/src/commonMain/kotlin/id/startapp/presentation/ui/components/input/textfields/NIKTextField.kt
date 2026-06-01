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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import id.startapp.presentation.ui.components.input.AppTextField

/**
 * Constants for NIKTextField default values
 */
object NIKTextFieldDefaults {
    const val NIK_REQUIRED_LENGTH = 16
}

/**
 * NIK Text Field (Indonesian KTP Number)
 *
 * Specialized text field for Indonesian ID card (KTP) number.
 *
 * Format: 16 digits, no spaces, no dashes
 * Province code (first 2 digits) validation optional
 *
 * @param value Current NIK value
 * @param onValueChange Callback when NIK changes
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param externalLabelRequired Whether to show required asterisk
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param validateProvince Whether to validate province code
 * @param error Error message
 */
@Composable
fun NIKTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = "NIK (KTP)",
    externalLabelRequired: Boolean = true,
    placeholder: String? = "16 digit NIK",
    enabled: Boolean = true,
    validateProvince: Boolean = false,
    error: String? = null,
    testTag: String? = null,
    contentDescription: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }

    // Filter to only digits and max 16
    val filteredValue = value.filter { it.isDigit() }.take(NIKTextFieldDefaults.NIK_REQUIRED_LENGTH)

    SideEffect {
        if (value != filteredValue) {
            onValueChange(filteredValue)
        }
    }

    AppTextField(
        value = filteredValue,
        onValueChange = onValueChange,
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
        onBlur = {
            val result = when {
                filteredValue.isBlank() -> "NIK is required"
                filteredValue.length != NIKTextFieldDefaults.NIK_REQUIRED_LENGTH -> "NIK must be ${NIKTextFieldDefaults.NIK_REQUIRED_LENGTH} digits"
                validateProvince && !isValidProvinceCode(filteredValue.take(2)) -> "Invalid province code"
                else -> null
            }
            internalError = result
        }
    )
}

// Valid Indonesian province codes (first 2 digits of NIK)
private val VALID_PROVINCE_CODES = setOf(
    "11", "12", "13", "14", "15", "16", "17", "18", "19",
    "21", "22", "23", "24", "25", "26", "27", "28", "29",
    "31", "32", "33", "34", "35", "36", "37", "38", "39",
    "51", "52", "53", "54", "55", "56", "57", "58", "59",
    "61", "62", "63", "64", "65", "66", "67", "68", "69",
    "71", "72", "73", "74", "75", "76", "77", "78", "79",
    "81", "82", "83", "84", "85", "86", "87", "88", "89",
    "91", "92", "93", "94", "95", "96", "97", "98", "99"
)

private fun isValidProvinceCode(code: String): Boolean {
    return code in VALID_PROVINCE_CODES
}
