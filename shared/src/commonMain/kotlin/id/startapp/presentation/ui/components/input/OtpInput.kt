package id.startapp.presentation.ui.components.input

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import id.startapp.presentation.ui.theme.BackboneBlue
import id.startapp.presentation.ui.theme.BackboneBlueTint
import id.startapp.presentation.ui.theme.BackboneDimensions
import id.startapp.presentation.ui.theme.ErrorRed
import id.startapp.presentation.ui.theme.SurfaceGray300

/**
 * Backbone OTP Input
 *
 * 6-box OTP input with auto-advance following Backbone design system.
 *
 * Specs:
 * - 6 boxes, 48dp × 48dp each
 * - 8dp spacing between boxes
 * - Centered text, Title Medium
 * - Border: 2dp Gray-300 → Brand Blue (focused) → Red (error)
 * - Background: Tint when focused
 * - Auto-advance to next box on input
 * - Backspace goes to previous box
 * - Auto-submit when all 6 filled
 *
 * @param otpValue Current OTP value (6 digits)
 * @param onOtpChange Callback when OTP changes
 * @param modifier Modifier for the OTP input
 * @param length Number of OTP digits (default: 6)
 * @param error Optional error message
 * @param enabled Whether inputs are enabled
 * @param onComplete Callback when all digits are entered
 */
@Composable
fun OtpInput(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    error: String? = null,
    enabled: Boolean = true,
    onComplete: (() -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    val focusRequesters = remember { List(length) { FocusRequester() } }

    // Split OTP into individual digits (no padding with 0 - empty stays empty)
    val otpDigits = otpValue.map { it.toString() }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(length) { index ->
            OtpBox(
                value = otpDigits.getOrNull(index) ?: "",
                isError = error != null,
                isEnabled = enabled,
                focusRequester = focusRequesters[index],
                onValueChange = { newValue ->
                    val newOtp = buildString {
                        for (i in 0 until length) {
                            if (i == index) {
                                append(newValue)
                            } else if (i < index) {
                                append(otpDigits.getOrNull(i) ?: "")
                            }
                        }
                    }.take(length)

                    onOtpChange(newOtp)

                    // Auto-advance to next box
                    if (newValue.isNotEmpty() && index < length - 1) {
                        focusRequesters[index + 1].requestFocus()
                    }

                    // Auto-submit when complete
                    if (newOtp.length == length && onComplete != null) {
                        onComplete()
                    }
                },
                onDelete = {
                    if (index > 0) {
                        focusRequesters[index - 1].requestFocus()
                    }
                },
                onPaste = { pastedValue ->
                    val digits = pastedValue.filter { it.isDigit() }.take(length)
                    onOtpChange(digits)
                    if (digits.isNotEmpty()) {
                        val nextIndex = (digits.length - 1).coerceAtMost(length - 1)
                        focusRequesters[nextIndex].requestFocus()
                    }
                    if (digits.length == length && onComplete != null) {
                        onComplete()
                    }
                }
            )

            if (index < length - 1) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }

    // Focus first box on initial load if OTP is empty
    LaunchedEffect(Unit) {
        if (otpValue.isEmpty()) {
            focusRequesters[0].requestFocus()
        }
    }
}

/**
 * Individual OTP Box
 */
@Composable
private fun OtpBox(
    value: String,
    isError: Boolean,
    isEnabled: Boolean,
    focusRequester: FocusRequester,
    onValueChange: (String) -> Unit,
    onDelete: () -> Unit,
    onPaste: (String) -> Unit
) {
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(0, 1)
            )
        )
    }

    // Update text field value when external value changes
    LaunchedEffect(value) {
        if (textFieldValue.text != value) {
            textFieldValue = TextFieldValue(
                text = value,
                selection = TextRange(value.length, value.length)
            )
        }
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            if (newValue.text.length <= 1) {
                textFieldValue = newValue
                onValueChange(newValue.text)
            }
        },
        modifier = Modifier
            .width(BackboneDimensions.OtpBoxSize.dp)
            .height(BackboneDimensions.OtpBoxSize.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                if (focusState.hasFocus && textFieldValue.text.isNotEmpty()) {
                    textFieldValue = textFieldValue.copy(
                        selection = TextRange(0, 1)
                    )
                }
            },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = isError,
        enabled = isEnabled,
        textStyle = MaterialTheme.typography.titleMedium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isError) ErrorRed else BackboneBlue,
            unfocusedBorderColor = if (isError) ErrorRed else SurfaceGray300,
            disabledBorderColor = if (isError) ErrorRed else SurfaceGray300,
            errorBorderColor = ErrorRed,
            focusedContainerColor = if (isError) ErrorRed.copy(alpha = 0.1f) else BackboneBlueTint,
            unfocusedContainerColor = if (value.isNotEmpty()) Color.Transparent else Color.Transparent,
            cursorColor = if (isError) ErrorRed else BackboneBlue
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * OTP Input with timer
 *
 * OTP input with countdown timer for resend functionality.
 *
 * @param otpValue Current OTP value
 * @param onOtpChange Callback when OTP changes
 * @param modifier Modifier for the OTP input
 * @param onResend Callback for resend OTP
 * @param countdownSeconds Countdown duration in seconds
 * @param error Optional error message
 * @param enabled Whether inputs are enabled
 * @param onComplete Callback when all digits are entered
 */
@Composable
fun OtpInputWithTimer(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onResend: () -> Unit,
    countdownSeconds: Int = BackboneDimensions.OtpCountdownSeconds,
    error: String? = null,
    enabled: Boolean = true,
    onComplete: (() -> Unit)? = null
) {
    var remainingSeconds by rememberSaveable { mutableStateOf(countdownSeconds) }

    // Countdown timer
    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            kotlinx.coroutines.delay(1000L)
            remainingSeconds--
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OtpInput(
            otpValue = otpValue,
            onOtpChange = onOtpChange,
            length = BackboneDimensions.OtpLength,
            error = error,
            enabled = enabled,
            onComplete = onComplete
        )

        ResendTimer(
            remainingSeconds = remainingSeconds,
            onResend = {
                remainingSeconds = countdownSeconds
                onResend()
            },
            enabled = remainingSeconds == 0
        )
    }
}

/**
 * Resend Timer Component
 *
 * Shows countdown timer with resend link.
 *
 * @param remainingSeconds Remaining seconds in countdown
 * @param onResend Callback for resend action
 * @param enabled Whether resend is enabled (timer expired)
 */
@Composable
private fun ResendTimer(
    remainingSeconds: Int,
    onResend: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Didn't receive code?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (remainingSeconds > 0) {
            Text(
                text = "Resend in ${formatTime(remainingSeconds)}",
                style = MaterialTheme.typography.bodyMedium,
                color = BackboneBlue
            )
        } else {
            Text(
                text = "Resend",
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) BackboneBlue else SurfaceGray300,
                modifier = if (enabled) Modifier.clickable { onResend() } else Modifier
            )
        }
    }
}

/**
 * Format seconds as MM:SS
 */
private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return "$minutes:${secs.toString().padStart(2, '0')}"
}
