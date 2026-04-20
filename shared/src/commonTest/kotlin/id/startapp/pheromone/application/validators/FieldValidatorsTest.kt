package id.startapp.pheromone.application.validators

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [FieldValidators].
 *
 * Tests email and username validation.
 */
class FieldValidatorsTest {

    // Email Validation Tests

    @Test
    fun `validateEmail accepts valid email`() {
        val result = FieldValidators.validateEmail("user@example.com")
        assertTrue(result is FieldValidators.EmailResult.Valid)
    }

    @Test
    fun `validateEmail accepts email with subdomain`() {
        val result = FieldValidators.validateEmail("user@mail.example.com")
        assertTrue(result is FieldValidators.EmailResult.Valid)
    }

    @Test
    fun `validateEmail accepts email with plus tag`() {
        val result = FieldValidators.validateEmail("user+tag@example.com")
        assertTrue(result is FieldValidators.EmailResult.Valid)
    }

    @Test
    fun `validateEmail rejects empty email`() {
        val result = FieldValidators.validateEmail("")
        assertTrue(result is FieldValidators.EmailResult.Invalid)
        assertEquals("Email is required", (result as FieldValidators.EmailResult.Invalid).reason)
    }

    @Test
    fun `validateEmail rejects blank email`() {
        val result = FieldValidators.validateEmail("   ")
        assertTrue(result is FieldValidators.EmailResult.Invalid)
    }

    @Test
    fun `validateEmail rejects email without at sign`() {
        val result = FieldValidators.validateEmail("userexample.com")
        assertTrue(result is FieldValidators.EmailResult.Invalid)
        assertEquals("Invalid email format", result.reason)
    }

    @Test
    fun `validateEmail rejects email without domain`() {
        val result = FieldValidators.validateEmail("user@")
        assertTrue(result is FieldValidators.EmailResult.Invalid)
    }

    @Test
    fun `validateEmail rejects email with double dots in domain`() {
        val result = FieldValidators.validateEmail("user@example..com")
        assertTrue(result is FieldValidators.EmailResult.Invalid)
    }

    @Test
    fun `isValidEmail returns true for valid email`() {
        assertTrue(FieldValidators.isValidEmail("user@example.com"))
    }

    @Test
    fun `isValidEmail returns false for invalid email`() {
        assertFalse(FieldValidators.isValidEmail("invalid-email"))
    }

    // Username Validation Tests

    @Test
    fun `validateUsername accepts valid username`() {
        val result = FieldValidators.validateUsername("john_doe")
        assertTrue(result is FieldValidators.UsernameResult.Valid)
    }

    @Test
    fun `validateUsername accepts username with hyphen`() {
        val result = FieldValidators.validateUsername("john-doe")
        assertTrue(result is FieldValidators.UsernameResult.Valid)
    }

    @Test
    fun `validateUsername accepts username with numbers`() {
        val result = FieldValidators.validateUsername("user123")
        assertTrue(result is FieldValidators.UsernameResult.Valid)
    }

    @Test
    fun `validateUsername rejects empty username`() {
        val result = FieldValidators.validateUsername("")
        assertTrue(result is FieldValidators.UsernameResult.Invalid)
        assertEquals("Username is required", result.reason)
    }

    @Test
    fun `validateUsername rejects blank username`() {
        val result = FieldValidators.validateUsername("   ")
        assertTrue(result is FieldValidators.UsernameResult.Invalid)
    }

    @Test
    fun `validateUsername rejects username shorter than 3 characters`() {
        val result = FieldValidators.validateUsername("ab")
        assertTrue(result is FieldValidators.UsernameResult.Invalid)
        assertEquals("Username must be at least 3 characters", result.reason)
    }

    @Test
    fun `validateUsername rejects username longer than 30 characters`() {
        val result = FieldValidators.validateUsername("a".repeat(31))
        assertTrue(result is FieldValidators.UsernameResult.Invalid)
        assertEquals("Username must be less than 30 characters", result.reason)
    }

    @Test
    fun `validateUsername rejects username starting with number`() {
        val result = FieldValidators.validateUsername("123user")
        assertTrue(result is FieldValidators.UsernameResult.Invalid)
        assertEquals("Username must start with a letter", result.reason)
    }

    @Test
    fun `validateUsername rejects username with special characters`() {
        val result = FieldValidators.validateUsername("user@name")
        assertTrue(result is FieldValidators.UsernameResult.Invalid)
    }

    @Test
    fun `isValidUsername returns true for valid username`() {
        assertTrue(FieldValidators.isValidUsername("john_doe"))
    }

    @Test
    fun `isValidUsername returns false for invalid username`() {
        assertFalse(FieldValidators.isValidUsername("ab"))
    }
}
