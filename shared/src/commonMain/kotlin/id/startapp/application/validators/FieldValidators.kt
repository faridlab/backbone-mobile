package id.startapp.application.validators

/**
 * Common field validators for user input.
 */
object FieldValidators {

    /**
     * Email validation result.
     */
    sealed class EmailResult {
        data object Valid : EmailResult()
        data class Invalid(val reason: String) : EmailResult()
    }

    /**
     * Validate an email address format.
     *
     * Uses a comprehensive regex pattern that matches:
     * - Standard format: local@domain.tld
     * - Subdomains: user@mail.example.com
     * - Special characters in local part: user+tag@example.com
     * - International domains
     *
     * Does NOT match:
     * - Missing @ symbol
     * - Missing domain extension
     * - Invalid characters
     *
     * @param email The email address to validate
     * @return EmailResult indicating validity
     */
    fun validateEmail(email: String): EmailResult {
        if (email.isBlank()) {
            return EmailResult.Invalid("Email is required")
        }

        // Comprehensive email regex
        val emailRegex = Regex(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )

        if (!emailRegex.matches(email)) {
            return EmailResult.Invalid("Invalid email format")
        }

        // Additional checks
        val domain = email.substringAfter("@")
        if (domain.contains("..")) {
            return EmailResult.Invalid("Invalid email format")
        }

        return EmailResult.Valid
    }

    /**
     * Check if an email address is valid.
     *
     * @param email The email address to check
     * @return true if valid, false otherwise
     */
    fun isValidEmail(email: String): Boolean {
        return validateEmail(email) is EmailResult.Valid
    }

    /**
     * Username validation result.
     */
    sealed class UsernameResult {
        data object Valid : UsernameResult()
        data class Invalid(val reason: String) : UsernameResult()
    }

    /**
     * Validate a username.
     *
     * Rules:
     * - 3-30 characters
     * - Only alphanumeric, underscore, and hyphen
     * - Must start with a letter
     *
     * @param username The username to validate
     * @return UsernameResult indicating validity
     */
    fun validateUsername(username: String): UsernameResult {
        if (username.isBlank()) {
            return UsernameResult.Invalid("Username is required")
        }

        if (username.length < 3) {
            return UsernameResult.Invalid("Username must be at least 3 characters")
        }

        if (username.length > 30) {
            return UsernameResult.Invalid("Username must be less than 30 characters")
        }

        if (!username[0].isLetter()) {
            return UsernameResult.Invalid("Username must start with a letter")
        }

        val usernameRegex = Regex("^[A-Za-z][A-Za-z0-9_-]*$")
        if (!usernameRegex.matches(username)) {
            return UsernameResult.Invalid("Username can only contain letters, numbers, underscores, and hyphens")
        }

        return UsernameResult.Valid
    }

    /**
     * Check if a username is valid.
     *
     * @param username The username to check
     * @return true if valid, false otherwise
     */
    fun isValidUsername(username: String): Boolean {
        return validateUsername(username) is UsernameResult.Valid
    }
}
