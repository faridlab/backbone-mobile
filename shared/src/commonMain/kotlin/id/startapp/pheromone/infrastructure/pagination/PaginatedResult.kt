package id.startapp.pheromone.infrastructure.pagination

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Paginated result wrapper
 *
 * Common pagination result type used across all repositories.
 *
 * ## Usage Pattern
 * ```
 * val result: PaginatedResult<User> = repository.getAll(page = 1, limit = 20)
 * println("Showing ${result.data.size} of ${result.total} items")
 * ```
 *
 * @property data The list of items for the current page
 * @property total Total number of items across all pages
 * @property page Current page number (1-indexed)
 * @property limit Number of items per page
 * @property totalPages Total number of pages available
 * @property hasNext Whether there is a next page
 * @property hasPrev Whether there is a previous page
 */
data class PaginatedResult<T>(
    val data: List<T>,
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrev: Boolean
)

/**
 * Paginated API response wrapper
 *
 * Used by API clients for deserializing paginated HTTP responses
 * where pagination fields are at the root level with data.
 *
 * ## Response Format
 * ```json
 * {
 *   "data": [...],
 *   "total": 100,
 *   "page": 1,
 *   "limit": 20,
 *   "total_pages": 5,
 *   "has_next": true,
 *   "has_prev": false
 * }
 * ```
 *
 * This format is NOT used by the current backend API.
 * Use [BackendPaginatedResponse] for backend API responses.
 */
@Serializable
data class PaginatedApiResponse<T>(
    val data: List<T>,
    val total: Int,
    val page: Int,
    val limit: Int,
    @SerialName("total_pages")
    val totalPages: Int,
    @SerialName("has_next")
    val hasNext: Boolean,
    @SerialName("has_prev")
    val hasPrev: Boolean
) {
    /**
     * Convert to [PaginatedResult] for repository layer compatibility
     */
    fun toPaginatedResult(): PaginatedResult<T> = PaginatedResult(
        data = data,
        total = total,
        page = page,
        limit = limit,
        totalPages = totalPages,
        hasNext = hasNext,
        hasPrev = hasPrev
    )
}

/**
 * Pagination metadata from backend API
 *
 * ## Backend Response Format
 * The Backbone backend API returns paginated responses in this format:
 *
 * ```json
 * {
 *   "success": true,
 *   "data": [...],
 *   "meta": {
 *     "total": 100,
 *     "page": 1,
 *     "limit": 20,
 *     "total_pages": 5
 *   }
 * }
 * ```
 *
 * Note: The backend may not include `has_next` and `has_prev` fields.
 * These are computed from `page` and `total_pages` when missing.
 *
 * ## Field Descriptions
 * - `success`: Boolean indicating if the request was successful
 * - `data`: Array of entity objects for the current page
 * - `meta.total`: Total number of records across all pages
 * - `meta.page`: Current page number (1-indexed)
 * - `meta.limit`: Number of records per page
 * - `meta.total_pages`: Total number of pages available
 * - `meta.has_next`: Optional boolean indicating if a next page exists
 * - `meta.has_prev`: Optional boolean indicating if a previous page exists
 *
 * @property total Total number of records across all pages
 * @property page Current page number (1-indexed)
 * @property limit Number of records per page
 * @property totalPages Total number of pages available
 * @property hasNext Whether there is a next page
 * @property hasPrev Whether there is a previous page
 */
@Serializable
data class PaginationMeta(
    val total: Int,
    val page: Int,
    val limit: Int,
    @SerialName("total_pages")
    val totalPages: Int,
    @SerialName("has_next")
    val hasNext: Boolean = false,
    @SerialName("has_prev")
    val hasPrev: Boolean = false
) {
    /**
     * Get effective hasNext, computed from page/totalPages when not provided by backend
     */
    val effectiveHasNext: Boolean get() = hasNext || page < totalPages

    /**
     * Get effective hasPrev, computed from page when not provided by backend
     */
    val effectiveHasPrev: Boolean get() = hasPrev || page > 1
}

/**
 * Backend paginated API response wrapper
 *
 * Matches the Backbone backend API response format with nested metadata.
 * Used by API clients to deserialize paginated HTTP responses.
 *
 * ## Usage Pattern
 * ```kotlin
 * suspend fun getAll(page: Int = 1, limit: Int = 20): Result<PaginatedApiResponse<Item>> {
 *     return apiCall {
 *         val response: BackendPaginatedResponse<Item> = client.get("$baseUrl/api/v1/items") {
 *             parameter("page", page)
 *             parameter("limit", limit)
 *         }.body()
 *         response.toPaginatedApiResponse()
 *     }
 * }
 * ```
 *
 * @property success Boolean indicating if the request was successful
 * @property data Array of entity objects for the current page
 * @property meta Pagination metadata (total, page, limit, etc.)
 * @see PaginationMeta for detailed field descriptions
 */
@Serializable
data class BackendPaginatedResponse<T>(
    val success: Boolean,
    val data: List<T>,
    val meta: PaginationMeta
) {
    /**
     * Convert to [PaginatedResult] for repository layer compatibility
     */
    fun toPaginatedResult(): PaginatedResult<T> = PaginatedResult(
        data = data,
        total = meta.total,
        page = meta.page,
        limit = meta.limit,
        totalPages = meta.totalPages,
        hasNext = meta.effectiveHasNext,
        hasPrev = meta.effectiveHasPrev
    )

    /**
     * Convert to [PaginatedApiResponse] for consistency across API clients
     */
    fun toPaginatedApiResponse(): PaginatedApiResponse<T> = PaginatedApiResponse(
        data = data,
        total = meta.total,
        page = meta.page,
        limit = meta.limit,
        totalPages = meta.totalPages,
        hasNext = meta.effectiveHasNext,
        hasPrev = meta.effectiveHasPrev
    )
}

// <<< CUSTOM - BackendSingleResponse used by all API clients for single-entity endpoints
/**
 * Backend single-entity API response wrapper
 *
 * Matches the Backbone backend API response format for single-entity endpoints
 * (e.g., getById, create, update).
 *
 * ## Response Format
 * ```json
 * {
 *   "success": true,
 *   "data": { ... }
 * }
 * ```
 *
 * @property success Boolean indicating if the request was successful
 * @property data The entity object
 */
@Serializable
data class BackendSingleResponse<T>(
    val success: Boolean,
    val data: T
)
// END CUSTOM
