package id.startapp.application.validators

import id.startapp.application.utils.formatDuration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for [PasswordValidator].
 *
 * Tests password strength validation requirements.
 */
class PasswordValidatorTest {

    // Valid Password Tests

    @Test
    fun `validate accepts strong password with all requirements`() {
        val result = PasswordValidator.validate("StrongPass123@")
        assertTrue(result is PasswordValidator.ValidationResult.Valid)
    }

    @Test
    fun `validate accepts password with underscore special char`() {
        val result = PasswordValidator.validate("TestPass123_")
        assertTrue(result is PasswordValidator.ValidationResult.Valid)
    }

    @Test
    fun `validate accepts password at maximum length`() {
        val result = PasswordValidator.validate("A".repeat(64) + "a1@")
        assertTrue(result is PasswordValidator.ValidationResult.Valid)
    }

    @Test
    fun `isValid returns true for valid password`() {
        assertTrue(PasswordValidator.isValid("ValidPass123@"))
    }

    // Length Validation Tests

    @Test
    fun `validate rejects password shorter than 8 characters`() {
        val result = PasswordValidator.validate("Short1@")
        assertTrue(result is PasswordValidator.ValidationResult.Invalid)
        val errors = (result as PasswordValidator.ValidationResult.Invalid).errors
        assertTrue(errors.contains(PasswordValidator.PasswordError.TOO_SHORT))
    }

    @Test
    fun `validate rejects password longer than 128 characters`() {
        val result = PasswordValidator.validate("A".repeat(65) + "a".repeat(65))
        assertTrue(result is PasswordValidator.ValidationResult.Invalid)
        val errors = (result as PasswordValidator.ValidationResult.Invalid).errors
        assertTrue(errors.contains(PasswordValidator.PasswordError.TOO_LONG))
    }

    @Test
    fun `validate rejects empty password`() {
        val result = PasswordValidator.validate("")
        assertTrue(result is PasswordValidator.ValidationResult.Invalid)
        val errors = (result as PasswordValidator.ValidationResult.Invalid).errors
        assertTrue(errors.contains(PasswordValidator.PasswordError.TOO_SHORT))
    }

    // Uppercase Validation Tests

    @Test
    fun `validate rejects password without uppercase letter`() {
        val result = PasswordValidator.validate("lowercase123@")
        assertTrue(result is PasswordValidator.ValidationResult.Invalid)
        val errors = (result as PasswordValidator.ValidationResult.Invalid).errors
        assertTrue(errors.contains(PasswordValidator.PasswordError.NO_UPPERCASE))
    }

    // Lowercase Validation Tests

    @Test
    fun `validate rejects password without lowercase letter`() {
        val result = PasswordValidator.validate("UPPERCASE123@")
        assertTrue(result is PasswordValidator.ValidationResult.Invalid)
        val errors = (result as PasswordValidator.ValidationResult.Invalid).errors
        assertTrue(errors.contains(PasswordValidator.PasswordError.NO_LOWERCASE))
    }

    // Digit Validation Tests

    @Test
    fun `validate rejects password without digit`() {
        val result = PasswordValidator.validate("NoDigitsHere@")
        assertTrue(result is PasswordValidator.ValidationResult.Invalid)
        val errors = (result as PasswordValidator.ValidationResult.Invalid).errors
        assertTrue(errors.contains(PasswordValidator.PasswordError.NO_DIGIT))
    }

    // Special Character Validation Tests

    @Test
    fun `validate rejects password without special character`() {
        val result = PasswordValidator.validate("NoSpecial123")
        assertTrue(result is PasswordValidator.ValidationResult.Invalid)
        val errors = (result as PasswordValidator.ValidationResult.Invalid).errors
        assertTrue(errors.contains(PasswordValidator.PasswordError.NO_SPECIAL_CHAR))
    }

    @Test
    fun `validate accepts password with various special characters`() {
        val specialChars = listOf('@', '#', '$', '%', '^', '&', '+', '=', '!', '?', '*', '.', '~')
        specialChars.forEach { char ->
            val result = PasswordValidator.validate("TestPass123$char")
            assertTrue("Password with '$char' should be valid", result is PasswordValidator.ValidationResult.Valid)
        }
    }

    // Multiple Error Tests

    @Test
    fun `validate returns all validation errors for weak password`() {
        val result = PasswordValidator.validate("weak")
        assertTrue(result is PasswordValidator.ValidationResult.Invalid)
        val errors = (result as PasswordValidator.ValidationResult.Invalid).errors
        assertTrue(errors.contains(PasswordValidator.PasswordError.TOO_SHORT))
        assertTrue(errors.contains(PasswordValidator.PasswordError.NO_UPPERCASE))
        assertTrue(errors.contains(PasswordValidator.PasswordError.NO_DIGIT))
        assertTrue(errors.contains(PasswordValidator.PasswordError.NO_SPECIAL_CHAR))
    }

    // Error Message Tests

    @Test
    fun `getErrorMessage returns single error message for single error`() {
        val errors = listOf(PasswordValidator.PasswordError.TOO_SHORT)
        val message = PasswordValidator.getErrorMessage(errors)
        assertEquals("Password must be at least 8 characters", message)
    }

    @Test
    fun `getErrorMessage returns combined message for multiple errors`() {
        val errors = listOf(
            PasswordValidator.PasswordError.TOO_SHORT,
            PasswordValidator.PasswordError.NO_UPPERCASE,
            PasswordValidator.PasswordError.NO_DIGIT
        )
        val message = PasswordValidator.getErrorMessage(errors)
        // Should contain "Password must contain:" and list the requirements
        assertTrue(message.contains("Password must contain"))
        assertTrue(message.contains("at least 8 characters"))
        assertTrue(message.contains("uppercase"))
        assertTrue(message.contains("number"))
    }

    @Test
    fun `getErrorMessage includes example characters for clarity`() {
        val errors = listOf(PasswordValidator.PasswordError.NO_SPECIAL_CHAR)
        val message = PasswordValidator.getErrorMessage(errors)
        assertTrue(message.contains("(@#\$%^&+=)"))
    }

    @Test
    fun `getFirstError returns first error message`() {
        val result = PasswordValidator.validate("weak")
        val firstError = PasswordValidator.getFirstError("weak")
        assertNotNull(firstError)
    }

    @Test
    fun `getFirstError returns null for valid password`() {
        val firstError = PasswordValidator.getFirstError("ValidPass123@")
        assertEquals(null, firstError)
    }

    // Edge Cases

    @Test
    fun `validate accepts password with all uppercase and special char`() {
        val result = PasswordValidator.validate("AAAAAA1@")
        assertTrue(result is PasswordValidator.ValidationResult.Valid)
    }

    @Test
    fun `validate accepts password with all lowercase and special char`() {
        val result = PasswordValidator.validate("aaaaaa1@")
        assertTrue(result is PasswordValidator.ValidationResult.Valid)
    }

    @Test
    fun `validate accepts exactly 8 character password`() {
        val result = PasswordValidator.validate("Aa1@aaaa")
        assertTrue(result is PasswordValidator.ValidationResult.Valid)
    }

    // Format Duration Tests

    @Test
    fun `formatDuration returns correct format for seconds`() {
        assertEquals("1 second", formatDuration(1L))
        assertEquals("5 seconds", formatDuration(5L))
        assertEquals("59 seconds", formatDuration(59L))
    }

    @Test
    fun `formatDuration returns correct format for minutes`() {
        assertEquals("1 minute", formatDuration(60L))
        assertEquals("30 minutes", formatDuration(1800L))
        assertEquals("59 minutes", formatDuration(3540L))
    }

    @Test
    fun `formatDuration returns correct format for hours`() {
        assertEquals("1 hour", formatDuration(3600L))
        assertEquals("3 hours", formatDuration(10800L))
    }
}
