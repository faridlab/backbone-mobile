package id.startapp.pheromone.domain.types

/**
 * Common Result wrapper type for operations that can fail
 * Similar to Rust's Result<T, E> or TypeScript's Result type
 *
 * @param T Success data type
 */
sealed class Result<out T> {
    /**
     * Successful operation with data
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Failed operation with error
     */
    data class Error(val error: NetworkError) : Result<Nothing>()

    /**
     * Check if result is success
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * Check if result is error
     */
    val isError: Boolean
        get() = this is Error

    /**
     * Get data if success, null otherwise
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    /**
     * Get data if success, throw exception otherwise
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw error
    }

    /**
     * Map success data to another type
     */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    /**
     * Fold result into a single value
     */
    inline fun <R> fold(
        onSuccess: (T) -> R,
        onError: (NetworkError) -> R
    ): R = when (this) {
        is Success -> onSuccess(data)
        is Error -> onError(error)
    }
}

/**
 * Network error types
 */
sealed class NetworkError(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {
    /**
     * Network connectivity error
     */
    data class ConnectivityError(
        override val cause: Throwable? = null
    ) : NetworkError("Network connectivity error", cause)

    /**
     * Server error with HTTP status code
     */
    data class ServerError(
        val statusCode: Int,
        val errorBody: String? = null
    ) : NetworkError("Server error: $statusCode")

    /**
     * Timeout error
     */
    data class TimeoutError(
        override val cause: Throwable? = null
    ) : NetworkError("Request timeout", cause)

    /**
     * Unauthorized error (401)
     */
    data class Unauthorized(
        val errorBody: String? = null
    ) : NetworkError("Unauthorized access")

    /**
     * Forbidden error (403)
     */
    data class Forbidden(
        val errorBody: String? = null
    ) : NetworkError("Forbidden access")

    /**
     * Not found error (404)
     */
    data class NotFound(
        val resource: String
    ) : NetworkError("Resource not found: $resource")

    /**
     * Database error
     */
    data class DatabaseError(
        val details: String? = null
    ) : NetworkError(details ?: "Database error occurred")

    /**
     * Validation error with field-specific errors
     */
    data class ValidationError(
        val errors: Map<String, String>
    ) : NetworkError("Validation failed") {
        val firstFieldError: String?
            get() = errors.values.firstOrNull()

        fun hasError(field: String): Boolean = errors.containsKey(field)
    }

    /**
     * Conflict error (409)
     */
    data class ConflictError(
        val errorBody: String? = null
    ) : NetworkError("Conflict: resource already exists")

    /**
     * Unknown/unexpected error
     */
    data class UnknownError(
        override val cause: Throwable? = null
    ) : NetworkError("Unknown error occurred", cause)

    /**
     * Too many requests error (429)
     *
     * Returned when rate limit is exceeded.
     */
    data class TooManyRequests(
        val retryAfterSeconds: Int,
        override val message: String = "Too many requests. Please try again later."
    ) : NetworkError(message)
}

/**
 * Pagination result wrapper
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
 * Action result for mutations
 */
sealed class ActionResult<out T> {
    data class Success<T>(val data: T) : ActionResult<T>()
    data class Error(val error: NetworkError) : ActionResult<Nothing>()
    object Loading : ActionResult<Nothing>()
}

/**
 * Extension function to wrap suspending operations in Result
 */
suspend fun <T> resultOf(block: suspend () -> T): Result<T> = try {
    Result.Success(block())
} catch (e: Exception) {
    Result.Error(e.toNetworkError())
}

/**
 * Convert exception to NetworkError
 */
fun Exception.toNetworkError(): NetworkError = when (this) {
    is NetworkError -> this
    is ConnectivityException -> NetworkError.ConnectivityError(this)
    is TimeoutException -> NetworkError.TimeoutError(this)
    else -> NetworkError.UnknownError(this)
}

/**
 * Platform-specific connectivity exception marker.
 * This common class can be used to identify connectivity-related exceptions.
 */
class ConnectivityException : Exception("Network connectivity error")

/**
 * Platform-specific timeout exception marker.
 * This common class can be used to identify timeout-related exceptions.
 */
class TimeoutException : Exception("Request timeout")
