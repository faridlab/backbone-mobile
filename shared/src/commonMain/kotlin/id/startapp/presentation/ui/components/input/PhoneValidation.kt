package id.startapp.presentation.ui.components.input

/**
 * Phone number validation utilities.
 */

/**
 * Validates Indonesian mobile phone numbers.
 * Format: 08xx-xxxx-xxxx (9-13 digits after leading 0)
 *
 * Valid patterns:
 * - 08xxxxxxxx (9-12 digits)
 * - Starts with 08 followed by 1-11 more digits
 */
private val INDONESIAN_MOBILE_REGEX = Regex("^8[0-9]{8,12}$")

/**
 * Validates generic international phone numbers.
 * Format: 7-15 digits, optionally starting with +
 *
 * E.164 format recommendation
 */
private val INTERNATIONAL_PHONE_REGEX = Regex("^[+]?[0-9]{7,15}$")

/**
 * Validates Singapore phone numbers.
 * Format: 8xxxxxxx or 9xxxxxxxx (8 digits)
 */
private val SINGAPORE_MOBILE_REGEX = Regex("^[89][0-9]{7}$")

/**
 * Validates Malaysia phone numbers.
 * Format: 01xxxxxxxxx (10-11 digits starting with 01)
 */
private val MALAYSIA_MOBILE_REGEX = Regex("^01[0-9]{8,9}$")

/**
 * Phone number validation result.
 */
sealed interface PhoneValidationResult {
    data object Valid : PhoneValidationResult
    data class Invalid(val reason: String) : PhoneValidationResult
}

/**
 * Validates a phone number based on country code.
 *
 * @param phone Phone number without country code
 * @param countryCode Country code enum
 * @return PhoneValidationResult indicating if the phone is valid
 */
fun validatePhoneNumber(phone: String, countryCode: CountryCode = CountryCode.Indonesia): PhoneValidationResult {
    val cleanPhone = phone.filter { it.isDigit() }

    if (cleanPhone.isEmpty()) {
        return PhoneValidationResult.Invalid("Phone number cannot be empty")
    }

    return when (countryCode) {
        CountryCode.Indonesia -> validateIndonesianPhone(cleanPhone)
        CountryCode.Singapore -> validateSingaporePhone(cleanPhone)
        CountryCode.Malaysia -> validateMalaysianPhone(cleanPhone)
        else -> validateInternationalPhone(cleanPhone)
    }
}

/**
 * Validates Indonesian mobile phone number.
 */
private fun validateIndonesianPhone(phone: String): PhoneValidationResult {
    if (!phone.startsWith("8")) {
        return PhoneValidationResult.Invalid("Indonesian mobile numbers must start with 8 (after country code)")
    }

    if (phone.length !in 9..13) {
        return PhoneValidationResult.Invalid("Phone number must be 9-13 digits")
    }

    if (!INDONESIAN_MOBILE_REGEX.matches(phone)) {
        return PhoneValidationResult.Invalid("Invalid Indonesian phone number format")
    }

    return PhoneValidationResult.Valid
}

/**
 * Validates Singapore phone number.
 */
private fun validateSingaporePhone(phone: String): PhoneValidationResult {
    if (!SINGAPORE_MOBILE_REGEX.matches(phone)) {
        return PhoneValidationResult.Invalid("Singapore mobile numbers must be 8 digits starting with 8 or 9")
    }

    return PhoneValidationResult.Valid
}

/**
 * Validates Malaysia phone number.
 */
private fun validateMalaysianPhone(phone: String): PhoneValidationResult {
    if (!MALAYSIA_MOBILE_REGEX.matches(phone)) {
        return PhoneValidationResult.Invalid("Malaysian mobile numbers must be 10-11 digits starting with 01")
    }

    return PhoneValidationResult.Valid
}

/**
 * Validates international phone number (generic).
 */
private fun validateInternationalPhone(phone: String): PhoneValidationResult {
    if (phone.length !in 7..15) {
        return PhoneValidationResult.Invalid("Phone number must be 7-15 digits")
    }

    return PhoneValidationResult.Valid
}

/**
 * Formats phone number for display.
 * Indonesian: 812 3456 7890
 * Generic: Groups of 4 digits
 */
fun formatPhoneNumber(phone: String, countryCode: CountryCode = CountryCode.Indonesia): String {
    val cleanPhone = phone.filter { it.isDigit() }

    return when (countryCode) {
        CountryCode.Indonesia -> {
            // Format: 812 3456 7890
            when {
                cleanPhone.length >= 8 -> {
                    val first = cleanPhone.take(3)
                    val middle = cleanPhone.substring(3, 7)
                    val last = cleanPhone.drop(7)
                    "$first $middle $last"
                }
                cleanPhone.length >= 4 -> {
                    val first = cleanPhone.take(3)
                    val rest = cleanPhone.drop(3)
                    "$first $rest"
                }
                else -> cleanPhone
            }
        }
        else -> {
            // Generic format: groups of 4 digits
            cleanPhone.chunked(4).joinToString(" ")
        }
    }
}
