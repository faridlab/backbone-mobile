package id.startapp.pheromone.core.error

import id.startapp.pheromone.domain.types.NetworkError
import id.startapp.pheromone.infrastructure.network.isRetryable

/**
 * User-facing error hierarchy (Phase 6C).
 *
 * Wraps [NetworkError] with display-ready strings and a retry flag.
 * UI layers consume [AppError] instead of raw [NetworkError] so that
 * error messages are consistent and localisation-friendly.
 *
 * ## Usage
 * ```kotlin
 * when (val result = useCases.getAll()) {
 *     is Result.Error -> {
 *         val appError = result.error.toAppError()
 *         showDialog(title = appError.displayTitle, message = appError.displayMessage)
 *         if (appError.isRetryable) showRetryButton()
 *     }
 *     is Result.Success -> { ... }
 * }
 * ```
 */
sealed class AppError(
    /** Short, user-facing title (e.g. "No internet connection"). */
    val displayTitle: String,
    /** Full, user-facing message. */
    val displayMessage: String,
    /** Whether the operation should be retried automatically or offered to the user. */
    val isRetryable: Boolean,
    /** Optional recovery suggestion shown below the error message. */
    val recoverySuggestion: String? = null,
    /** The underlying domain error, preserved for logging. */
    val cause: NetworkError,
) {

    /** Device has no network access. */
    class Connectivity(cause: NetworkError.ConnectivityError) : AppError(
        displayTitle = "No Internet Connection",
        displayMessage = "Please check your connection and try again.",
        isRetryable = true,
        recoverySuggestion = "Turn on Wi-Fi or mobile data.",
        cause = cause,
    )

    /** Request took too long. */
    class Timeout(cause: NetworkError.TimeoutError) : AppError(
        displayTitle = "Request Timed Out",
        displayMessage = "The server took too long to respond.",
        isRetryable = true,
        recoverySuggestion = "Try again in a moment.",
        cause = cause,
    )

    /** Session expired — user must re-authenticate. */
    class Unauthorized(cause: NetworkError.Unauthorized) : AppError(
        displayTitle = "Session Expired",
        displayMessage = "Please log in again to continue.",
        isRetryable = false,
        cause = cause,
    )

    /** User is not allowed to perform this action. */
    class Forbidden(cause: NetworkError.Forbidden) : AppError(
        displayTitle = "Access Denied",
        displayMessage = "You don't have permission to perform this action.",
        isRetryable = false,
        cause = cause,
    )

    /** Requested resource does not exist. */
    class NotFound(cause: NetworkError.NotFound) : AppError(
        displayTitle = "Not Found",
        displayMessage = "The requested item could not be found.",
        isRetryable = false,
        cause = cause,
    )

    /** Input failed server-side validation. */
    class Validation(
        cause: NetworkError.ValidationError,
        /** Field-level errors, keyed by field name. */
        val fieldErrors: Map<String, String> = cause.errors,
    ) : AppError(
        displayTitle = "Invalid Input",
        displayMessage = cause.firstFieldError ?: "Please check the highlighted fields.",
        isRetryable = false,
        cause = cause,
    )

    /** Transient server error (5xx). */
    class Server(cause: NetworkError.ServerError) : AppError(
        displayTitle = "Server Error",
        displayMessage = "Something went wrong on our end (${cause.statusCode}).",
        isRetryable = cause.statusCode in 500..599,
        recoverySuggestion = "Try again in a moment.",
        cause = cause,
    )

    /** Catch-all for errors not mapped above. */
    class Unknown(cause: NetworkError) : AppError(
        displayTitle = "Unexpected Error",
        displayMessage = "An unexpected error occurred. Please try again.",
        isRetryable = false,
        cause = cause,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Conversion helpers
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Convert a [NetworkError] to its user-facing [AppError] representation.
 */
fun NetworkError.toAppError(): AppError = when (this) {
    is NetworkError.ConnectivityError -> AppError.Connectivity(this)
    is NetworkError.TimeoutError      -> AppError.Timeout(this)
    is NetworkError.Unauthorized      -> AppError.Unauthorized(this)
    is NetworkError.Forbidden         -> AppError.Forbidden(this)
    is NetworkError.NotFound          -> AppError.NotFound(this)
    is NetworkError.ValidationError   -> AppError.Validation(this)
    is NetworkError.ServerError       -> AppError.Server(this)
    else                              -> AppError.Unknown(this)
}

/**
 * True if the underlying [NetworkError] is worth retrying automatically.
 *
 * Delegates to [NetworkError.isRetryable] from RetryHandler for consistency.
 */
val AppError.shouldAutoRetry: Boolean
    get() = isRetryable && cause.isRetryable()
