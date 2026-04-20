package id.startapp.pheromone.infrastructure.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for FilterValidator.
 *
 * Tests validation logic for API filter parameters to ensure
 * invalid data doesn't reach the backend.
 */
class FilterValidatorTest {

    // ========== validateId Tests ==========

    @Test
    fun `validateId returns null for null input`() {
        val result = FilterValidator.validateId(null)
        assertNull(result)
    }

    @Test
    fun `validateId returns null for empty string`() {
        val result = FilterValidator.validateId("")
        assertNull(result)
    }

    @Test
    fun `validateId returns null for blank string`() {
        val result = FilterValidator.validateId("   ")
        assertNull(result)
    }

    @Test
    fun `validateId returns trimmed value for valid ID`() {
        val result = FilterValidator.validateId("  uuid-123  ")
        assertEquals("uuid-123", result)
    }

    @Test
    fun `validateId returns same value for already trimmed ID`() {
        val result = FilterValidator.validateId("uuid-456")
        assertEquals("uuid-456", result)
    }

    @Test
    fun `validateId handles UUID format correctly`() {
        val uuid = "550e8400-e29b-41d4-a716-446655440000"
        val result = FilterValidator.validateId(uuid)
        assertEquals(uuid, result)
    }

    // ========== validateLimit Tests ==========

    @Test
    fun `validateLimit clamps zero to minimum 1`() {
        val result = FilterValidator.validateLimit(0)
        assertEquals(1, result)
    }

    @Test
    fun `validateLimit clamps negative to minimum 1`() {
        val result = FilterValidator.validateLimit(-10)
        assertEquals(1, result)
    }

    @Test
    fun `validateLimit returns value within range`() {
        val result = FilterValidator.validateLimit(50)
        assertEquals(50, result)
    }

    @Test
    fun `validateLimit clamps to default max of 100`() {
        val result = FilterValidator.validateLimit(200)
        assertEquals(100, result)
    }

    @Test
    fun `validateLimit clamps to custom max limit`() {
        val result = FilterValidator.validateLimit(500, maxLimit = 250)
        assertEquals(250, result)
    }

    @Test
    fun `validateLimit returns boundary value of 1`() {
        val result = FilterValidator.validateLimit(1)
        assertEquals(1, result)
    }

    @Test
    fun `validateLimit returns boundary value of maxLimit`() {
        val result = FilterValidator.validateLimit(100)
        assertEquals(100, result)
    }

    // ========== validatePage Tests ==========

    @Test
    fun `validatePage clamps zero to minimum 1`() {
        val result = FilterValidator.validatePage(0)
        assertEquals(1, result)
    }

    @Test
    fun `validatePage clamps negative to minimum 1`() {
        val result = FilterValidator.validatePage(-5)
        assertEquals(1, result)
    }

    @Test
    fun `validatePage returns value for valid page`() {
        val result = FilterValidator.validatePage(5)
        assertEquals(5, result)
    }

    @Test
    fun `validatePage returns 1 for page 1`() {
        val result = FilterValidator.validatePage(1)
        assertEquals(1, result)
    }

    @Test
    fun `validatePage returns large page number unchanged`() {
        val result = FilterValidator.validatePage(10000)
        assertEquals(10000, result)
    }

    // ========== validateSortField Tests ==========

    @Test
    fun `validateSortField returns default for empty string`() {
        val result = FilterValidator.validateSortField("")
        assertEquals("created_at", result)
    }

    @Test
    fun `validateSortField returns valid field name`() {
        val result = FilterValidator.validateSortField("name")
        assertEquals("name", result)
    }

    @Test
    fun `validateSortField returns valid field with underscores`() {
        val result = FilterValidator.validateSortField("created_at")
        assertEquals("created_at", result)
    }

    @Test
    fun `validateSortField returns valid field with numbers`() {
        val result = FilterValidator.validateSortField("field_123")
        assertEquals("field_123", result)
    }

    @Test
    fun `validateSortField rejects SQL injection attempt - semicolon`() {
        val result = FilterValidator.validateSortField("name; DROP TABLE users--")
        assertEquals("created_at", result)
    }

    @Test
    fun `validateSortField rejects SQL injection attempt - single quote`() {
        val result = FilterValidator.validateSortField("name' OR '1'='1")
        assertEquals("created_at", result)
    }

    @Test
    fun `validateSortField rejects field with spaces`() {
        val result = FilterValidator.validateSortField("name asc")
        assertEquals("created_at", result)
    }

    @Test
    fun `validateSortField rejects field with special characters`() {
        val result = FilterValidator.validateSortField("name@#$!")
        assertEquals("created_at", result)
    }

    @Test
    fun `validateSortField rejects field with hyphen`() {
        val result = FilterValidator.validateSortField("created-at")
        assertEquals("created_at", result)
    }

    @Test
    fun `validateSortField accepts camelCase field name`() {
        val result = FilterValidator.validateSortField("fieldName")
        assertEquals("fieldName", result)
    }

    @Test
    fun `validateSortField accepts PascalCase field name`() {
        val result = FilterValidator.validateSortField("FieldName")
        assertEquals("FieldName", result)
    }

    @Test
    fun `validateSortField uses custom allowed fields when provided`() {
        val customFields = setOf("custom_field", "another_field")
        val result = FilterValidator.validateSortField("custom_field", customFields)
        assertEquals("custom_field", result)
    }

    @Test
    fun `validateSortField returns default when field not in custom allowed list`() {
        val customFields = setOf("allowed_field")
        val result = FilterValidator.validateSortField("not_allowed", customFields)
        assertEquals("created_at", result)
    }
}
