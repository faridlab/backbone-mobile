package id.startapp.presentation.ui.components.input

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.startapp.application.validators.PasswordValidator

/**
 * Password requirement item showing check/cross icon with text.
 *
 * @param isMet Whether the requirement is met
 * @param text The requirement description
 */
@Composable
private fun PasswordRequirementItem(
    isMet: Boolean,
    text: String
) {
    val iconColor = if (isMet) {
        Color(0xFF4CAF50) // Green for success
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }
    val textColor = if (isMet) {
        Color(0xFF4CAF50)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.Check else Icons.Default.Close,
            contentDescription = if (isMet) "Requirement met" else "Requirement not met",
            tint = iconColor,
            modifier = Modifier.width(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = textColor
        )
    }
}

/**
 * Password requirements indicator that shows which requirements are met.
 *
 * Displays a checklist of password requirements with check/cross icons
 * indicating which ones are satisfied by the current password.
 *
 * @param password The current password value to validate
 * @param modifier Modifier for the component
 */
@Composable
fun PasswordRequirements(
    password: String,
    modifier: Modifier = Modifier
) {
    val validationResult = PasswordValidator.validate(password)
    val errors = if (validationResult is PasswordValidator.ValidationResult.Invalid) {
        validationResult.errors
    } else {
        emptyList()
    }

    val hasMinLength = !errors.contains(PasswordValidator.PasswordError.TOO_SHORT)
    val hasUppercase = !errors.contains(PasswordValidator.PasswordError.NO_UPPERCASE)
    val hasLowercase = !errors.contains(PasswordValidator.PasswordError.NO_LOWERCASE)
    val hasDigit = !errors.contains(PasswordValidator.PasswordError.NO_DIGIT)
    val hasSpecialChar = !errors.contains(PasswordValidator.PasswordError.NO_SPECIAL_CHAR)
    val isValid = errors.isEmpty()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Only show if user has started typing or there are errors
        if (password.isNotEmpty() || !isValid) {
            if (password.isNotEmpty()) {
                Text(
                    text = "Password requirements:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (password.isNotEmpty() || !hasMinLength) {
                PasswordRequirementItem(
                    isMet = hasMinLength,
                    text = "At least ${PasswordValidator.MIN_LENGTH} characters"
                )
            }
            if (password.isNotEmpty() || !hasUppercase) {
                PasswordRequirementItem(
                    isMet = hasUppercase,
                    text = "Uppercase letter (A-Z)"
                )
            }
            if (password.isNotEmpty() || !hasLowercase) {
                PasswordRequirementItem(
                    isMet = hasLowercase,
                    text = "Lowercase letter (a-z)"
                )
            }
            if (password.isNotEmpty() || !hasDigit) {
                PasswordRequirementItem(
                    isMet = hasDigit,
                    text = "Number (0-9)"
                )
            }
            if (password.isNotEmpty() || !hasSpecialChar) {
                PasswordRequirementItem(
                    isMet = hasSpecialChar,
                    text = "Special character (@#\$%^&+=)"
                )
            }
        }
    }
}

/**
 * Minimal password strength error message.
 *
 * Shows a single error message when password is invalid.
 * Use this when you want a more compact display instead of the full checklist.
 *
 * @param password The current password value to validate
 * @param modifier Modifier for the component
 */
@Composable
fun PasswordStrengthError(
    password: String,
    modifier: Modifier = Modifier
) {
    val validationResult = PasswordValidator.validate(password)

    if (password.isNotEmpty() && validationResult is PasswordValidator.ValidationResult.Invalid) {
        Text(
            text = PasswordValidator.getErrorMessage(validationResult.errors),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = modifier
        )
    }
}
