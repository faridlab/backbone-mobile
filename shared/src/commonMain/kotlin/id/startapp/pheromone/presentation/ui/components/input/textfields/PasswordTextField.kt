package id.startapp.pheromone.presentation.ui.components.input.textfields

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import id.startapp.pheromone.presentation.ui.components.input.AppTextField
import id.startapp.pheromone.presentation.ui.theme.ErrorRed
import id.startapp.pheromone.presentation.ui.theme.SuccessGreen
import id.startapp.pheromone.presentation.ui.theme.WarningOrange

/**
 * Password Text Field
 *
 * Specialized text field for password input with strength indicator.
 *
 * Features:
 * - Password keyboard type
 * - Show/hide toggle
 * - Real-time strength meter
 * - Password requirements display
 * - Min strength enforcement
 *
 * @param value Current password value
 * @param onValueChange Callback when password changes
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param externalLabelRequired Whether to show required asterisk
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param readOnly Whether the field is read-only
 * @param showStrengthIndicator Whether to show password strength meter
 * @param showRequirements Whether to show password requirements list
 * @param minStrength Minimum required password strength
 * @param onStrengthChange Callback when password strength changes
 * @param error External error message (overrides internal validation)
 */
@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = null,
    externalLabelRequired: Boolean = true,
    placeholder: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    showStrengthIndicator: Boolean = true,
    showRequirements: Boolean = false,
    minStrength: PasswordValidation.Strength = PasswordValidation.Strength.FAIR,
    onStrengthChange: ((PasswordValidation.Strength) -> Unit)? = null,
    error: String? = null,
    testTag: String? = null,
    contentDescription: String? = null
) {
    var isVisible by remember { mutableStateOf(false) }
    var internalError by remember { mutableStateOf<String?>(null) }
    val strength = remember(value) { PasswordValidation.calculateStrength(value) }

    // Notify parent of strength changes
    onStrengthChange?.let { it(strength) }

    Column(modifier = modifier) {
        AppTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            externalLabel = externalLabel ?: "Password",
            externalLabelRequired = externalLabelRequired,
            placeholder = placeholder,
            enabled = enabled,
            readOnly = readOnly,
            error = error ?: internalError,
            testTag = testTag,
            contentDescription = contentDescription,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = "Password",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                IconButton(onClick = { isVisible = !isVisible }) {
                    Icon(
                        imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isVisible) "Hide password" else "Show password",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                autoCorrectEnabled = false
            ),
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
            onBlur = {
                val result = PasswordValidation.validate(value, minStrength)
                internalError = if (result.isValid) null else result.errorMessage
            }
        )

        // Strength indicator
        if (showStrengthIndicator && value.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            PasswordStrengthIndicator(strength = strength)
        }

        // Requirements list
        if (showRequirements && value.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            PasswordRequirementsList(value)
        }
    }
}

/**
 * Password strength indicator with colored progress bar
 */
@Composable
private fun ColumnScope.PasswordStrengthIndicator(strength: PasswordValidation.Strength) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = when (strength) {
                PasswordValidation.Strength.WEAK -> "Weak password"
                PasswordValidation.Strength.FAIR -> "Fair password"
                PasswordValidation.Strength.GOOD -> "Good password"
                PasswordValidation.Strength.STRONG -> "Strong password"
            },
            style = MaterialTheme.typography.bodySmall,
            color = when (strength) {
                PasswordValidation.Strength.WEAK -> ErrorRed
                PasswordValidation.Strength.FAIR -> WarningOrange
                PasswordValidation.Strength.GOOD -> Color(0xFF4FC3F7)
                PasswordValidation.Strength.STRONG -> SuccessGreen
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { strengthProgress(strength) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(2.dp)
                ),
            color = when (strength) {
                PasswordValidation.Strength.WEAK -> ErrorRed
                PasswordValidation.Strength.FAIR -> WarningOrange
                PasswordValidation.Strength.GOOD -> Color(0xFF4FC3F7)
                PasswordValidation.Strength.STRONG -> SuccessGreen
            },
        )
    }
}

private fun strengthProgress(strength: PasswordValidation.Strength): Float {
    return when (strength) {
        PasswordValidation.Strength.WEAK -> 0.25f
        PasswordValidation.Strength.FAIR -> 0.5f
        PasswordValidation.Strength.GOOD -> 0.75f
        PasswordValidation.Strength.STRONG -> 1f
    }
}

/**
 * Password requirements checklist
 */
@Composable
private fun PasswordRequirementsList(password: String) {
    val hasUppercase = password.any { it.isUpperCase() }
    val hasLowercase = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasMinLength = password.length >= 6

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        RequirementItem(text = "At least 6 characters", satisfied = hasMinLength)
        RequirementItem(text = "Contains uppercase letter", satisfied = hasUppercase)
        RequirementItem(text = "Contains lowercase letter", satisfied = hasLowercase)
        RequirementItem(text = "Contains number", satisfied = hasDigit)
    }
}

@Composable
private fun RequirementItem(text: String, satisfied: Boolean) {
    val color = if (satisfied) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
    val icon = if (satisfied) "✓" else "○"

    Text(
        text = "$icon $text",
        style = MaterialTheme.typography.bodySmall,
        color = color
    )
}

/**
 * Confirm Password Text Field
 *
 * Variant that validates against a master password field.
 *
 * @param value Current confirm password value
 * @param onValueChange Callback when confirm password changes
 * @param masterPassword The password to match against
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param enabled Whether the field is enabled
 * @param error External error message
 */
@Composable
fun ConfirmPasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    masterPassword: String,
    modifier: Modifier = Modifier,
    externalLabel: String? = "Confirm Password",
    externalLabelRequired: Boolean = true,
    enabled: Boolean = true,
    error: String? = null,
    testTag: String? = null,
    contentDescription: String? = null
) {
    var isVisible by remember { mutableStateOf(false) }
    var internalError by remember { mutableStateOf<String?>(null) }

    AppTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        externalLabel = externalLabel,
        externalLabelRequired = externalLabelRequired,
        enabled = enabled,
        error = error ?: internalError,
        testTag = testTag,
        contentDescription = contentDescription,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = "Confirm Password",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            IconButton(onClick = { isVisible = !isVisible }) {
                Icon(
                    imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (isVisible) "Hide password" else "Show password",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        onBlur = {
            internalError = when {
                value.isBlank() -> "Please confirm your password"
                value != masterPassword -> "Passwords do not match"
                else -> null
            }
        }
    )
}
