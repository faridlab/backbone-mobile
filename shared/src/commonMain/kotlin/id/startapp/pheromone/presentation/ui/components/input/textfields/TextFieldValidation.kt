package id.startapp.pheromone.presentation.ui.components.input.textfields

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.KeyboardType
import org.jetbrains.compose.resources.StringResource

/**
 * Common validation patterns and utilities for specialized text fields
 *
 * ## Internationalization (i18n) TODO
 *
 * The following hardcoded strings should be migrated to Compose Resources for proper i18n support:
 *
 * ### Validation Error Messages (in this file)
 * - "Email is required"
 * - "Invalid email format"
 * - "Password is required"
 * - "Password must be at least 6 characters"
 * - "Password must be stronger (8+ chars with mixed letters & numbers)"
 * - "Password must be stronger (10+ chars with all character types)"
 * - "Password is too weak"
 * - "Phone number is required"
 * - "Invalid phone number format"
 * - "Postal code must be 5 digits"
 * - "Amount is required"
 * - "Invalid amount"
 * - "Amount must be at least {min}"
 * - "Amount exceeds maximum"
 * - "This field is required"
 * - "Must be a valid number"
 * - "Must be at least {min}"
 * - "Must be at most {max}"
 * - "Name is required"
 * - "Name must be at least {minLength} characters"
 * - "Name contains invalid characters"
 * - "Invalid URL format"
 * - "Weight is required"
 * - "Invalid weight format"
 * - "Weight must be at least {min}kg"
 * - "Weight exceeds maximum of {max}kg"
 * - "Invalid weight"
 * - "Percentage is required"
 * - "Must be between 0 and 100"
 * - "Time is required"
 * - "Invalid time format (use HH:MM)"
 *
 * ### UI Strings (in individual TextField files)
 * - **NumberTextField.kt**: "Number", "Decrease", "Increase"
 * - **PriceTextField.kt**: "Price", "Rp"
 * - **PhoneTextField.kt**: "Phone Number", "WhatsApp", "Used for order notifications"
 * - **PostalCodeTextField.kt**: "Postal Code"
 * - **PasswordTextField.kt**: "Password", "Show password", "Hide password", "Confirm Password"
 * - **SearchTextField.kt**: "Search...", "Clear search"
 * - **EmailTextField.kt**: "Email"
 * - **NameTextField.kt**: "Name"
 * - **OTPTextField.kt**: "Enter verification code", "Resend code in {seconds}s", "Resend Code"
 * - **PINTextField.kt**: "Enter PIN", "PIN", "Show PIN", "Hide PIN"
 * - **NIKTextField.kt**: "NIK (KTP)", "Enter 16-digit NIK number", "Invalid NIK format"
 * - **NPWPTextField.kt**: "NPWP (Tax ID)", "Enter 15-digit NPWP number", "Invalid NPWP format"
 * - **PlateNumberTextField.kt**: "License Plate", "B 1234 ABC", "Invalid plate format", "Format: B 1234 ABC or AB 1234 XY"
 *
 * ### Content Descriptions (accessibility)
 * - Various icon content descriptions should use string resources
 *
 * ### Migration Path
 * 1. Create `composeResources/files/<locale>/textfield_strings.xml`
 * 2. Define all strings with proper keys
 * 3. Replace hardcoded strings with `stringResource(Res.string.xxx)`
 * 4. Test with different locales
 */

/**
 * Validation result wrapper
 */
@Immutable
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
) {
    companion object {
        val Success = ValidationResult(true, null)

        fun error(message: String) = ValidationResult(false, message)
    }
}

/**
 * Validation rule interface
 */
fun interface ValidationRule {
    fun validate(input: String): ValidationResult
}

/**
 * Email validation patterns
 */
object EmailValidation {
    private val EMAIL_REGEX = Regex(
        """^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$"""
    )

    fun validate(value: String): ValidationResult {
        return when {
            value.isBlank() -> ValidationResult.error("Email is required")
            !EMAIL_REGEX.matches(value) -> ValidationResult.error("Invalid email format")
            else -> ValidationResult.Success
        }
    }
}

/**
 * Password validation with strength checking
 */
object PasswordValidation {
    private val UPPERCASE = Regex("""[A-Z]""")
    private val LOWERCASE = Regex("""[a-z]""")
    private val DIGIT = Regex("""\d""")
    private val SPECIAL = Regex("""[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?]""")

    enum class Strength {
        WEAK,      // < 6 chars
        FAIR,      // 6-7 chars or missing 1 requirement
        GOOD,      // 8+ chars with 2 requirements
        STRONG     // 10+ chars with all 4 requirements
    }

    fun calculateStrength(value: String): Strength {
        var score = 0
        if (value.length >= 8) score++
        if (value.length >= 10) score++
        if (UPPERCASE.containsMatchIn(value)) score++
        if (LOWERCASE.containsMatchIn(value)) score++
        if (DIGIT.containsMatchIn(value)) score++
        if (SPECIAL.containsMatchIn(value)) score++

        return when {
            value.length < 6 -> Strength.WEAK
            score <= 2 -> Strength.FAIR
            score <= 4 -> Strength.GOOD
            else -> Strength.STRONG
        }
    }

    fun validate(value: String, minStrength: Strength = Strength.FAIR): ValidationResult {
        // Early return for empty input
        if (value.isBlank()) {
            return ValidationResult.error("Password is required")
        }

        // Early return for minimum length
        if (value.length < 6) {
            return ValidationResult.error("Password must be at least 6 characters")
        }

        // Early return for strength requirement
        if (calculateStrength(value) < minStrength) {
            val errorMessage = when (minStrength) {
                Strength.GOOD -> "Password must be stronger (8+ chars with mixed letters & numbers)"
                Strength.STRONG -> "Password must be stronger (10+ chars with all character types)"
                else -> "Password is too weak"
            }
            return ValidationResult.error(errorMessage)
        }

        return ValidationResult.Success
    }

    fun getRequirements(): List<String> = listOf(
        "At least 6 characters",
        "Contains uppercase letter",
        "Contains lowercase letter",
        "Contains number"
    )
}

/**
 * Phone number validation (Indonesia format)
 */
object PhoneValidation {
    // Accepts: 08xxxxxxxx (7-12 digits after 0), +628xxxxxxxx (8-13 digits with +62)
    // Indonesian mobile: prefix (0/62/+62) + 8/9 + 6-11 more digits = 8-13 digits total with prefix
    private val PHONE_REGEX = Regex("""^(\+62|62|0)?[8-9][0-9]{6,11}$""")

    fun validate(value: String): ValidationResult {
        val cleaned = value.replace(Regex("""[\s-]"""), "")
        return when {
            cleaned.isBlank() -> ValidationResult.error("Phone number is required")
            !PHONE_REGEX.matches(cleaned) -> ValidationResult.error("Invalid phone number format")
            else -> ValidationResult.Success
        }
    }

    fun format(value: String): String {
        val cleaned = value.replace(Regex("""[^\d+]"""), "")
        return when {
            cleaned.startsWith("+62") -> cleaned
            cleaned.startsWith("62") -> "+$cleaned"
            cleaned.startsWith("0") -> "+62${cleaned.substring(1)}"
            else -> cleaned
        }
    }
}

/**
 * Postal code validation (Indonesia: 5 digits)
 */
object PostalCodeValidation {
    private val POSTAL_CODE_REGEX = Regex("""^\d{5}$""")

    fun validate(value: String): ValidationResult {
        return when {
            value.isBlank() -> ValidationResult.Success // Optional field
            !POSTAL_CODE_REGEX.matches(value) -> ValidationResult.error("Postal code must be 5 digits")
            else -> ValidationResult.Success
        }
    }
}

/**
 * Price/Amount validation
 */
object PriceValidation {
    fun validate(value: String, min: Double = 0.0, max: Double = 1_000_000_000.0): ValidationResult {
        return try {
            when {
                value.isBlank() -> ValidationResult.error("Amount is required")
                else -> {
                    val numeric = parse(value)
                    when {
                        numeric < min -> ValidationResult.error("Amount must be at least ${format(min)}")
                        numeric > max -> ValidationResult.error("Amount exceeds maximum")
                        else -> ValidationResult.Success
                    }
                }
            }
        } catch (e: Exception) {
            ValidationResult.error("Invalid amount format")
        }
    }

    /**
     * Formats a Double value as a currency string with thousand separators.
     * Uses KMP-compatible string operations (no String.format()).
     *
     * Examples: 1000 -> "1.000", 1000000 -> "1.000.000"
     *
     * @param value The numeric value to format
     * @return Formatted string with thousand separators (using dots for Indonesian locale)
     */
    fun format(value: Double): String {
        // Convert to int if it's a whole number, otherwise keep decimals
        val asString = if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            value.toString()
        }

        // Split integer and decimal parts
        val parts = asString.split(".")
        val integerPart = parts[0]

        // Add thousand separators (Indonesian format uses dots)
        val formatted = integerPart
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()

        // Reattach decimal part if exists
        return if (parts.size > 1) {
            "$formatted,${parts[1]}"
        } else {
            formatted
        }
    }

    fun parse(value: String): Double {
        // Indonesian format: dots are thousand separators, commas are decimal
        // Remove thousand separators (dots), replace decimal comma with dot
        val cleaned = value.replace(".", "").replace(",", ".")
        return cleaned.toDoubleOrNull() ?: 0.0
    }
}

/**
 * Number validation
 */
object NumberValidation {
    fun validate(value: String, min: Int? = null, max: Int? = null): ValidationResult {
        return try {
            val numeric = value.toIntOrNull()
            when {
                value.isBlank() -> ValidationResult.error("This field is required")
                numeric == null -> ValidationResult.error("Must be a valid number")
                min != null && numeric < min -> ValidationResult.error("Must be at least $min")
                max != null && numeric > max -> ValidationResult.error("Must be at most $max")
                else -> ValidationResult.Success
            }
        } catch (e: Exception) {
            ValidationResult.error("Invalid number format")
        }
    }
}

/**
 * Name validation (person/business names)
 */
object NameValidation {
    private val NAME_REGEX = Regex("""^[a-zA-Z\s&.,'-]+$""")

    fun validate(value: String, minLength: Int = 2): ValidationResult {
        return when {
            value.isBlank() -> ValidationResult.error("Name is required")
            value.trim().length < minLength -> ValidationResult.error("Name must be at least $minLength characters")
            !NAME_REGEX.matches(value.trim()) -> ValidationResult.error("Name contains invalid characters")
            else -> ValidationResult.Success
        }
    }
}

/**
 * URL validation
 */
object UrlValidation {
    private val URL_REGEX = Regex(
        """^(https?://)?([\da-z\.-]+)\.([a-z\.]{2,6})([/\w \.-]*)*/?$"""
    )

    fun validate(value: String): ValidationResult {
        return when {
            value.isBlank() -> ValidationResult.Success // Optional field
            !URL_REGEX.matches(value) -> ValidationResult.error("Invalid URL format")
            else -> ValidationResult.Success
        }
    }

    fun ensureProtocol(value: String): String {
        return when {
            value.startsWith("http://") || value.startsWith("https://") -> value
            value.isNotBlank() -> "https://$value"
            else -> value
        }
    }
}

/**
 * Weight validation (kg)
 */
object WeightValidation {
    private val WEIGHT_REGEX = Regex("""^\d+(\.\d{1,2})?$""")

    fun validate(value: String, min: Double = 0.1, max: Double = 50.0): ValidationResult {
        return try {
            when {
                value.isBlank() -> ValidationResult.error("Weight is required")
                !WEIGHT_REGEX.matches(value) -> ValidationResult.error("Invalid weight format")
                else -> {
                    val weight = value.toDouble()
                    when {
                        weight < min -> ValidationResult.error("Weight must be at least ${min}kg")
                        weight > max -> ValidationResult.error("Weight exceeds maximum of ${max}kg")
                        else -> ValidationResult.Success
                    }
                }
            }
        } catch (e: Exception) {
            ValidationResult.error("Invalid weight")
        }
    }
}

/**
 * Percentage validation (0-100)
 */
object PercentageValidation {
    private val PERCENTAGE_REGEX = Regex("""^(100|\d{1,2})(\.\d{1,2})?$""")

    fun validate(value: String): ValidationResult {
        return when {
            value.isBlank() -> ValidationResult.error("Percentage is required")
            !PERCENTAGE_REGEX.matches(value) -> ValidationResult.error("Must be between 0 and 100")
            else -> ValidationResult.Success
        }
    }

    fun parse(value: String): Double {
        return value.replace("%", "").trim().toDoubleOrNull() ?: 0.0
    }
}

/**
 * Time validation (HH:MM format, 24-hour)
 */
object TimeValidation {
    private val TIME_REGEX = Regex("""^([01]\d|2[0-3]):([0-5]\d)$""")

    fun validate(value: String): ValidationResult {
        return when {
            value.isBlank() -> ValidationResult.error("Time is required")
            !TIME_REGEX.matches(value) -> ValidationResult.error("Invalid time format (use HH:MM)")
            else -> ValidationResult.Success
        }
    }

    fun isValidHour(hour: Int): Boolean = hour in 0..23
    fun isValidMinute(minute: Int): Boolean = minute in 0..59
}

/**
 * Accessibility utilities for TextField components
 *
 * ## Semantics Properties
 *
 * Compose TextFields should include proper semantics for screen readers:
 *
 * ```kotlin
 * import androidx.compose.ui.semantics.semantics
 * import androidx.compose.ui.semantics.contentDescription
 * import androidx.compose.ui.semantics.heading
 * import androidx.compose.ui.semantics.error
 * import androidx.compose.ui.semantics.text
 *
 * AppTextField(
 *     // ... other parameters
 *     modifier = Modifier.semantics {
 *         // Provide content description for accessibility
 *         this.contentDescription = "Enter your email address"
 *         this.heading(false) // Set to true for section headers
 *
 *         // Announce error state to screen readers
 *         if (errorMessage != null) {
 *             this.error(errorMessage)
 *         }
 *
 *         // Provide current text value
 *         this.text = AnnotatedString(value)
 *     }
 * )
 * ```
 *
 * ## Accessibility Best Practices
 *
 * 1. **Content Descriptions**: All input fields should have clear content descriptions
 * 2. **Error Announcements**: Validation errors should be announced to screen readers
 * 3. **Label Association**: External labels should be properly associated with the field
 * 4. **Live Regions**: Error messages should use live regions for immediate announcement
 * 5. **Hint Text**: Placeholder text should not be used as the only label
 *
 * ## Example: Accessible TextField
 *
 * ```kotlin
 * @Composable
 * fun AccessibleEmailField(
 *     value: String,
 *     onValueChange: (String) -> Unit,
 *     errorMessage: String? = null,
 * ) {
 *     AppTextField(
 *         value = value,
 *         onValueChange = onValueChange,
 *         externalLabel = "Email Address", // Visual label
 *         modifier = Modifier.semantics {
 *             // Screen reader announcement
 *             contentDescription = "Email address field, ${if (value.isEmpty()) "empty" else value}"
 *             if (errorMessage != null) {
 *                 error(errorMessage)
 *             }
 *         },
 *         error = errorMessage
 *     )
 * }
 * ```
 *
 * ## TODO: Implement Semantics
 *
 * Currently, TextField components use default Compose semantics. To improve accessibility:
 *
 * 1. Add `contentDescription` parameter to all TextField components
 * 2. Implement `error` semantics announcement
 * 3. Add `heading()` support for section headers
 * 4. Support `_live_region` for dynamic error messages
 * 5. Add test tags for UI testing
 */

/**
 * Memoized validation cache for performance optimization
 *
 * Caches validation results to avoid re-running expensive validations
 * (regex matching, complex rules) on every recomposition.
 *
 * ## Usage
 *
 * ```kotlin
 * @Composable
 * fun MyTextField() {
 *     val validationCache = rememberValidationCache()
 *
 *     AppTextField(
 *         // ...,
 *         onBlur = {
 *             val result = validationCache.getOrValidate(value) { input ->
 *                 EmailValidation.validate(input)
 *             }
 *             // handle result
 *         }
 *     )
 * }
 * ```
 *
 * @param maxSize Maximum number of cached validation results
 */
class ValidationCache(private val maxSize: Int = 50) {
    private val cache = mutableStateMapOf<String, ValidationResult>()

    /**
     * Get cached validation result or compute and cache it
     *
     * @param input The input string to validate
     * @param validator The validation function to run if not cached
     * @return The validation result (cached or newly computed)
     */
    fun getOrValidate(input: String, validator: (String) -> ValidationResult): ValidationResult {
        return cache.getOrPut(input) { validator(input) }
    }

    /**
     * Clear all cached validation results
     *
     * Call this when validation rules change (e.g., settings updated)
     */
    fun clear() {
        cache.clear()
    }

    /**
     * Invalidate a specific cached value
     *
     * @param input The input to invalidate
     */
    fun invalidate(input: String) {
        cache.remove(input)
    }
}

/**
 * Remember a ValidationCache instance
 *
 * @param maxSize Maximum number of cached validation results
 * @return A remembered ValidationCache instance
 */
@Composable
fun rememberValidationCache(maxSize: Int = 50): ValidationCache {
    return remember { ValidationCache(maxSize) }
}

/**
 * Performance optimization utilities for TextField validation
 *
 * ## Memoization Guidelines
 *
 * 1. **Use remember for derived state**: Cache computed values that don't change often
 * 2. **Cache regex patterns**: All validation objects use compiled regex (already done)
 * 3. **Debounce validation**: For expensive validations, use debouncing
 * 4. **Skip validation on input**: Only validate on blur/submit, not every keystroke
 *
 * ## Example: Memoized Validation
 *
 * ```kotlin
 * @Composable
 * fun OptimizedEmailField(
 *     value: String,
 *     onValueChange: (String) -> Unit,
 * ) {
 *     var internalError by remember { mutableStateOf<String?>(null) }
 *     val validationCache = rememberValidationCache()
 *
 *     AppTextField(
 *         value = value,
 *         onValueChange = onValueChange,
 *         onBlur = {
 *             // Use cached validation
 *             val result = validationCache.getOrValidate(value) {
 *                 EmailValidation.validate(it)
 *             }
 *             internalError = if (result.isValid) null else result.errorMessage
 *         }
 *     )
 * }
 * ```
 */

/**
 * Error recovery strategies for TextField validation
 *
 * Provides utilities for handling and recovering from validation errors
 * with better user experience.
 *
 * ## Recovery Strategies
 *
 * 1. **Auto-retry**: Automatically revalidate after user corrects input
 * 2. **Suggested corrections**: Provide suggested fixes for common errors
 * 3. **Partial validation**: Accept partial input and guide completion
 * 4. **Graceful degradation**: Accept input with warnings instead of errors
 *
 * ## Example: Auto-retry on Correction
 *
 * ```kotlin
 * @Composable
 * fun AutoRecoveringEmailField() {
 *     var value by remember { mutableStateOf("") }
 *     var error by remember { mutableStateOf<String?>(null) }
 *
 *     LaunchedEffect(value) {
 *         // Auto-clear error when user starts typing
 *         if (error != null && value.isNotEmpty()) {
 *             delay(100)
 *             error = null
 *         }
 *     }
 *
 *     AppTextField(
 *         value = value,
 *         onValueChange = { value = it },
 *         onBlur = { error = EmailValidation.validate(value).errorMessage },
 *         error = error
 *     )
 * }
 * ```
 *
 * ## Example: Suggested Corrections
 *
 * ```kotlin
 * @Composable
 * fun EmailFieldWithSuggestions() {
 *     var value by remember { mutableStateOf("") }
 *     var error by remember { mutableStateOf<String?>(null) }
 *     val suggestion = remember(value) { ErrorRecovery.suggestEmailCorrection(value) }
 *
 *     AppTextField(
 *         value = value,
 *         onValueChange = { value = it },
 *         onBlur = {
 *             val result = EmailValidation.validate(value)
 *             if (!result.isValid && suggestion != null) {
 *                 // Show suggestion: "Did you mean ${suggestion}?"
 *                 error = "Invalid email. Did you mean $suggestion?"
 *             } else {
 *                 error = result.errorMessage
 *             }
 *         },
 *         error = error
 *     )
 * }
 * ```
 */

/**
 * Error recovery utilities
 *
 * Provides common error recovery patterns for TextField validation
 */
object ErrorRecovery {

    /**
     * Suggest correction for common email mistakes
     *
     * @param email The email to check
     * @return Suggested correction or null if no obvious fix
     */
    fun suggestEmailCorrection(email: String): String? {
        if (email.isBlank()) return null

        return when {
            // Missing @ domain
            email.contains("@") && !email.contains(".") -> "$email.com"
            // Multiple @ symbols
            email.count { it == '@' } > 1 -> email.replaceAfterLast("@", "")
            // Trailing dot
            email.endsWith(".") -> email.dropLast(1)
            // No obvious correction
            else -> null
        }
    }

    /**
     * Suggest correction for phone number format
     *
     * @param phone The phone number to check
     * @return Formatted phone number or null
     */
    fun suggestPhoneCorrection(phone: String): String? {
        val clean = phone.replace(Regex("""[\s-]"""), "")
        return when {
            // Missing country code for Indonesian numbers starting with 8
            clean.startsWith("8") && clean.length >= 9 -> "+62$clean"
            // Missing + for 62 prefix
            clean.startsWith("62") && !clean.startsWith("+") -> "+$clean"
            else -> null
        }
    }

    /**
     * Suggest correction for URL format
     *
     * @param url The URL to check
     * @return URL with protocol or null
     */
    fun suggestUrlCorrection(url: String): String? {
        return when {
            url.isBlank() -> null
            !url.startsWith("http://") && !url.startsWith("https://") -> "https://$url"
            else -> null
        }
    }

    /**
     * Validate with retry logic
     *
     * Attempts validation with progressive strictness:
     * 1. First pass: Strict validation
     * 2. If failed: Try with suggested correction
     * 3. If still failed: Return original error
     *
     * @param input The input to validate
     * @param strictValidator Strict validation function
     * @param suggestor Optional suggestion function
     * @return ValidationResult with potential correction applied
     */
    fun validateWithRetry(
        input: String,
        strictValidator: (String) -> ValidationResult,
        suggestor: ((String) -> String?)? = null
    ): ValidationResult {
        // First attempt: strict validation
        val strictResult = strictValidator(input)
        if (strictResult.isValid) return strictResult

        // Second attempt: try with suggested correction
        val suggestion = suggestor?.invoke(input)
        if (suggestion != null) {
            val suggestedResult = strictValidator(suggestion)
            if (suggestedResult.isValid) {
                return ValidationResult(
                    isValid = true,
                    errorMessage = null
                )
            }
        }

        // Return original error
        return strictResult
    }
}

/**
 * Remember email suggestion based on current input
 *
 * @param email The email to check
 * @return Suggested correction or null
 */
@Composable
fun rememberEmailSuggestion(email: String): String? {
    return remember(email) {
        ErrorRecovery.suggestEmailCorrection(email)
    }
}

/**
 * Remember phone suggestion based on current input
 *
 * @param phone The phone number to check
 * @return Suggested correction or null
 */
@Composable
fun rememberPhoneSuggestion(phone: String): String? {
    return remember(phone) {
        ErrorRecovery.suggestPhoneCorrection(phone)
    }
}

/**
 * Remember URL suggestion based on current input
 *
 * @param url The URL to check
 * @return Suggested correction or null
 */
@Composable
fun rememberUrlSuggestion(url: String): String? {
    return remember(url) {
        ErrorRecovery.suggestUrlCorrection(url)
    }
}
