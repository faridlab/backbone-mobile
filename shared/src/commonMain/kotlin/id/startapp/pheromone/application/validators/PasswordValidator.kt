package id.startapp.pheromone.application.validators

/**
 * Password validation requirements and rules.
 *
 * Enforces strong password policies for user security.
 *
 * Security Best Practices:
 * - Password values are NEVER included in error messages
 * - Password values are NEVER logged - use maskPassword() for debugging
 * - Error messages describe requirements only, never actual password content
 * - Validation results contain error codes, not the password itself
 */
object PasswordValidator {

    /**
     * Minimum password length.
     */
    const val MIN_LENGTH = 8

    /**
     * Maximum password length.
     */
    const val MAX_LENGTH = 128

    /**
     * Mask character for hiding password content in logs/debug output.
     */
    private const val MASK_CHAR = '*'

    /**
     * Result of password validation.
     *
     * Security Note: This result type never contains the password value itself
     * to prevent accidental exposure in logs, error messages, or debug output.
     */
    sealed class ValidationResult {
        data object Valid : ValidationResult()
        data class Invalid(val errors: List<PasswordError>) : ValidationResult()
    }

    /**
     * Password validation errors.
     *
     * Security: Messages describe requirements only and never include
     * the actual password value or any portion of it (e.g., no "your password
     * is too short" with the password length shown).
     */
    enum class PasswordError(val message: String) {
        TOO_SHORT("Password must be at least $MIN_LENGTH characters"),
        TOO_LONG("Password must be less than $MAX_LENGTH characters"),
        NO_UPPERCASE("Password must contain at least one uppercase letter"),
        NO_LOWERCASE("Password must contain at least one lowercase letter"),
        NO_DIGIT("Password must contain at least one digit"),
        NO_SPECIAL_CHAR("Password must contain at least one special character (@#\$%^&+=)")
    }

    /**
     * Validate a password against security requirements.
     *
     * @param password The password to validate
     * @return ValidationResult indicating success or specific errors
     */
    fun validate(password: String): ValidationResult {
        val errors = mutableListOf<PasswordError>()

        if (password.length < MIN_LENGTH) {
            errors.add(PasswordError.TOO_SHORT)
        }
        if (password.length > MAX_LENGTH) {
            errors.add(PasswordError.TOO_LONG)
        }
        if (!password.any { it.isUpperCase() }) {
            errors.add(PasswordError.NO_UPPERCASE)
        }
        if (!password.any { it.isLowerCase() }) {
            errors.add(PasswordError.NO_LOWERCASE)
        }
        if (!password.any { it.isDigit() }) {
            errors.add(PasswordError.NO_DIGIT)
        }
        if (!password.any { it in SPECIAL_CHARS }) {
            errors.add(PasswordError.NO_SPECIAL_CHAR)
        }

        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }

    /**
     * Check if a password meets minimum requirements.
     *
     * @param password The password to check
     * @return true if valid, false otherwise
     */
    fun isValid(password: String): Boolean {
        return validate(password) is ValidationResult.Valid
    }

    /**
     * Get a user-friendly error message from validation errors.
     *
     * Single-line format for better UI display. Multiple errors are
     * combined with commas and end with a period.
     *
     * @param errors List of validation errors
     * @return Formatted error message
     */
    fun getErrorMessage(errors: List<PasswordError>): String {
        return if (errors.size == 1) {
            errors.first().message
        } else {
            // Build a comprehensive message showing all requirements
            val requirements = mutableListOf<String>()

            if (errors.contains(PasswordError.TOO_SHORT)) {
                requirements.add("at least $MIN_LENGTH characters")
            }
            if (errors.contains(PasswordError.NO_UPPERCASE)) {
                requirements.add("an uppercase letter (A-Z)")
            }
            if (errors.contains(PasswordError.NO_LOWERCASE)) {
                requirements.add("a lowercase letter (a-z)")
            }
            if (errors.contains(PasswordError.NO_DIGIT)) {
                requirements.add("a number (0-9)")
            }
            if (errors.contains(PasswordError.NO_SPECIAL_CHAR)) {
                requirements.add("a special character (@#\$%^&+=)")
            }

            // For TOO_LONG, keep the original message since it's rare
            if (errors.contains(PasswordError.TOO_LONG)) {
                return "Password must be less than $MAX_LENGTH characters"
            }

            // Format: "Password must contain: uppercase, lowercase, number, special character."
            "Password must contain: ${requirements.joinToString(", ", ".", "")}"
        }
    }

    /**
     * Get the first error message from validation.
     *
     * @param password The password to validate
     * @return First error message or null if valid
     */
    fun getFirstError(password: String): String? {
        val result = validate(password)
        return if (result is ValidationResult.Invalid) {
            result.errors.firstOrNull()?.message
        } else {
            null
        }
    }

    private val SPECIAL_CHARS = setOf('@', '#', '$', '%', '^', '&', '+', '=', '!', '?', '*', '.', '~')

    /**
     * Mask a password for safe logging/debugging.
     *
     * Always use this function when logging password-related operations.
     * Never log the actual password value.
     *
     * Usage:
     * ```
     * println("Validating password: ${maskPassword(password)}")
     * ```
     *
     * @param password The password to mask
     * @param showLength If true, show length in format "**** (8 chars)"
     * @return Masked password string
     */
    fun maskPassword(password: String?, showLength: Boolean = false): String {
        if (password == null) return "null"
        val masked = MASK_CHAR.toString().repeat(password.length.coerceAtMost(8))
        return if (showLength) "$masked (${password.length} chars)" else masked
    }

    /**
     * Check if two password values match without exposing them in error messages.
     *
     * Use this for password confirmation checks.
     *
     * @param password The password
     * @param confirmation The confirmation password
     * @return true if passwords match, false otherwise
     */
    fun passwordsMatch(password: String, confirmation: String): Boolean {
        return password == confirmation
    }
}
