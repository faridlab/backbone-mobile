package id.startapp.pheromone.presentation.ui.components.input.textfields

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import id.startapp.pheromone.presentation.ui.theme.PheromoneBlue
import id.startapp.pheromone.presentation.ui.theme.ErrorRed
import id.startapp.pheromone.presentation.ui.theme.SurfaceGray300

/**
 * Constants for OTPTextField default values
 */
object OTPTextFieldDefaults {
    const val DEFAULT_OTP_DIGIT_COUNT = 6
}

/**
 * OTP Text Field
 *
 * Specialized text field for OTP (One-Time Password) input with individual digit boxes.
 *
 * Features:
 * - Configurable digit count (4-6 digits)
 * - Auto-focus to next digit
 * - Auto-backspace on delete
 * - OTP-style individual boxes
 * - Paste support
 *
 * @param otp Current OTP value
 * @param onOtpChange Callback when OTP changes
 * @param modifier Modifier for the text field
 * @param digitCount Number of OTP digits (default: 6)
 * @param enabled Whether the field is enabled
 * @param error Error message
 */
@Composable
fun OTPTextField(
    otp: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    digitCount: Int = OTPTextFieldDefaults.DEFAULT_OTP_DIGIT_COUNT,
    enabled: Boolean = true,
    error: String? = null,
    testTag: String? = null,
    contentDescription: String? = null
) {
    val focusRequesters = remember { List(digitCount) { FocusRequester() } }
    val otpValues = remember(otp) {
        List(digitCount) { index -> otp.getOrNull(index)?.toString() ?: "" }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (testTag != null) Modifier.testTag(testTag) else Modifier
            )
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                }
            ),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        repeat(digitCount) { index ->
            OTPDigitBox(
                value = otpValues[index],
                onFocusChange = { focused ->
                    if (focused && otpValues[index].isEmpty()) {
                        focusRequesters[index].requestFocus()
                    }
                },
                onClick = {
                    focusRequesters[index].requestFocus()
                },
                onValueChange = { newValue ->
                    val newOtp = otp.toMutableList()
                    if (newValue.isNotEmpty()) {
                        if (newOtp.size > index) {
                            newOtp[index] = newValue.last()
                        } else {
                            newOtp.add(newValue.last())
                        }
                        if (index < digitCount - 1) {
                            focusRequesters[index + 1].requestFocus()
                        }
                    } else {
                        if (newOtp.size > index) {
                            newOtp.removeAt(index)
                        }
                        if (index > 0 && newOtp.size <= index) {
                            focusRequesters[index - 1].requestFocus()
                        }
                    }
                    onOtpChange(newOtp.joinToString(""))
                },
                modifier = Modifier.weight(1f),
                focusRequester = focusRequesters[index],
                isError = error != null,
                enabled = enabled
            )

            if (index < digitCount - 1) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun OTPDigitBox(
    value: String,
    onFocusChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester,
    isError: Boolean,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .height(56.dp)
            .focusRequester(focusRequester),
        singleLine = true,
        textStyle = MaterialTheme.typography.headlineMedium,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = if (value.isNotEmpty()) ImeAction.Next else ImeAction.Default
        ),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isError) ErrorRed else PheromoneBlue,
            unfocusedBorderColor = if (isError) ErrorRed else SurfaceGray300,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            errorBorderColor = ErrorRed,
            cursorColor = PheromoneBlue,
            errorCursorColor = ErrorRed
        ),
        enabled = enabled
    )
}
