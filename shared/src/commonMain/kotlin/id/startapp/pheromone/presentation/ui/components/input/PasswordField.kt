package id.startapp.pheromone.presentation.ui.components.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import id.startapp.pheromone.presentation.ui.theme.PheromoneBlue
import id.startapp.pheromone.presentation.ui.theme.ErrorRed
import id.startapp.pheromone.presentation.ui.theme.SurfaceGray300

/**
 * Pheromone Password Field
 *
 * Password input field with visibility toggle following Pheromone design system.
 *
 * Specs:
 * - Height: 56dp
 * - Border Radius: 4dp (outlined)
 * - Font: Body Medium (masked with bullets)
 * - Toggle icon: Visibility/VisibilityOff
 *
 * @param value Current password value
 * @param onValueChange Callback when password changes
 * @param modifier Modifier for the password field
 * @param label Optional label text (floating label inside field)
 * @param placeholder Optional placeholder text
 * @param error Optional error message
 * @param helperText Optional helper text
 * @param enabled Whether the field is enabled
 * @param isVisible Whether password is visible (default: false/masked)
 * @param externalLabel Optional external label text (rendered above field, not floating)
 * @param externalLabelRequired Whether to show required asterisk (*) with external label
 * @param onBlur Callback when field loses focus (user clicks outside)
 */
@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    error: String? = null,
    helperText: String? = null,
    enabled: Boolean = true,
    isVisible: Boolean = false,
    onVisibilityToggle: (Boolean) -> Unit = {},
    externalLabel: String? = null,
    externalLabelRequired: Boolean = false,
    onBlur: () -> Unit = {}
) {
    var isPasswordVisible by rememberSaveable { mutableStateOf(isVisible) }

    AppTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = label,
        placeholder = placeholder,
        error = error,
        helperText = helperText,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (isPasswordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        externalLabel = externalLabel,
        externalLabelRequired = externalLabelRequired,
        onBlur = onBlur,
        trailingIcon = {
            IconButton(onClick = {
                isPasswordVisible = !isPasswordVisible
                onVisibilityToggle(isPasswordVisible)
            }) {
                Icon(
                    imageVector = if (isPasswordVisible) {
                        Icons.Default.Visibility
                    } else {
                        Icons.Default.VisibilityOff
                    },
                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                    tint = if (error != null) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

/**
 * Password Field with confirmation
 *
 * Two password fields for password creation and confirmation.
 *
 * @param password Current password value
 * @param onPasswordChange Callback when password changes
 * @param confirmPassword Current confirmation value
 * @param onConfirmPasswordChange Callback when confirmation changes
 * @param modifier Modifier for the column
 * @param passwordLabel Label for password field
 * @param confirmLabel Label for confirmation field
 * @param error Optional error message
 * @param enabled Whether fields are enabled
 */
@Composable
fun PasswordFieldWithConfirmation(
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    passwordLabel: String? = "Password",
    confirmLabel: String? = "Confirm Password",
    error: String? = null,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        PasswordField(
            value = password,
            onValueChange = onPasswordChange,
            label = passwordLabel,
            error = if (error != null && password.isNotEmpty()) error else null,
            enabled = enabled
        )

        PasswordField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = confirmLabel,
            error = if (error != null && confirmPassword.isNotEmpty()) error else null,
            enabled = enabled
        )
    }
}

/**
 * Password strength indicator
 *
 * Visual indicator showing password strength.
 *
 * @param password Current password value
 * @param modifier Modifier for the indicator
 */
@Composable
fun PasswordStrengthIndicator(
    password: String,
    modifier: Modifier = Modifier
) {
    val strength = calculatePasswordStrength(password)

    Column(modifier = modifier) {
        Text(
            text = "Password strength: ${strength.label}",
            style = MaterialTheme.typography.bodySmall,
            color = strength.color
        )
        // TODO: Add progress bar implementation
    }
}

/**
 * Password strength data class
 */
private data class PasswordStrength(
    val label: String,
    val color: androidx.compose.ui.graphics.Color,
    val score: Int
)

/**
 * Calculate password strength
 */
private fun calculatePasswordStrength(password: String): PasswordStrength {
    if (password.isEmpty()) {
        return PasswordStrength("", Color.Transparent, 0)
    }

    var score = 0

    // Length check
    if (password.length >= 8) score++
    if (password.length >= 12) score++

    // Complexity checks
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++

    return when {
        score <= 2 -> PasswordStrength(
            "Weak",
            ErrorRed,
            score
        )
        score <= 4 -> PasswordStrength(
            "Medium",
            androidx.compose.ui.graphics.Color(0xFFFF9800),
            score
        )
        else -> PasswordStrength(
            "Strong",
            androidx.compose.ui.graphics.Color(0xFF4CAF50),
            score
        )
    }
}
