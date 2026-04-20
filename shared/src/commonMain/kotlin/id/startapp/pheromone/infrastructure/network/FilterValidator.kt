package id.startapp.pheromone.infrastructure.network

/**
 * Filter validation utilities for API client extensions.
 *
 * Provides reusable validation functions for filter parameters
 * to prevent sending invalid or empty values to the backend.
 */
object FilterValidator {

    /**
     * Validates that a string ID is not blank.
     *
     * @param value The ID value to validate
     * @return The trimmed value if valid, null otherwise
     */
    fun validateId(value: String?): String? {
        return value?.takeIf { it.isNotBlank() }?.trim()
    }

    /**
     * Validates pagination limits.
     *
     * @param limit The requested limit
     * @param maxLimit Maximum allowed limit (default 100)
     * @return The validated limit, clamped to [1, maxLimit]
     */
    fun validateLimit(limit: Int, maxLimit: Int = 100): Int {
        return limit.coerceIn(1, maxLimit)
    }

    /**
     * Validates page number.
     *
     * @param page The requested page number
     * @return The validated page, clamped to minimum of 1
     */
    fun validatePage(page: Int): Int {
        return page.coerceAtLeast(1)
    }

    /**
     * Validates sort field name to prevent SQL injection.
     *
     * @param sortBy The sort field name
     * @param allowedFields List of allowed field names
     * @return The sort field if valid, default "created_at" otherwise
     */
    fun validateSortField(sortBy: String, allowedFields: Set<String> = DEFAULT_SORT_FIELDS): String {
        // Only allow alphanumeric characters and underscores
        if (sortBy.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            return sortBy
        }
        return "created_at"
    }

    /**
     * Default allowed sort fields for common entities.
     */
    private val DEFAULT_SORT_FIELDS = setOf(
        "id", "created_at", "updated_at", "name",
        "code", "status", "sort_order"
    )
}
