package id.startapp.pheromone.infrastructure.network

import kotlinx.io.IOException

import id.startapp.pheromone.domain.types.ConnectivityException
import id.startapp.pheromone.domain.types.TimeoutException
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlin.math.max
import kotlin.math.pow
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Retry configuration for HTTP requests.
 *
 * @param maxAttempts Maximum number of retry attempts
 * @param initialDelay Initial delay before first retry
 * @param maxDelay Maximum delay between retries
 * @param backoffMultiplier Multiplier for exponential backoff
 */
data class RetryConfig(
    val maxAttempts: Int = 3,
    val initialDelay: Duration = 1.seconds,
    val maxDelay: Duration = 30.seconds,
    val backoffMultiplier: Double = 2.0
) {
    companion object {
        /**
         * Default retry configuration for API requests.
         * 3 attempts with exponential backoff starting at 1 second.
         */
        fun default(): RetryConfig = RetryConfig()

        /**
         * Conservative retry configuration for critical operations.
         * 5 attempts with longer delays.
         */
        fun conservative(): RetryConfig = RetryConfig(
            maxAttempts = 5,
            initialDelay = 2.seconds,
            maxDelay = 60.seconds,
            backoffMultiplier = 2.0
        )

        /**
         * Aggressive retry configuration for non-critical operations.
         * 2 attempts with short delays.
         */
        fun aggressive(): RetryConfig = RetryConfig(
            maxAttempts = 2,
            initialDelay = 500.toDuration(DurationUnit.MILLISECONDS),
            maxDelay = 5.toDuration(DurationUnit.SECONDS),
            backoffMultiplier = 1.5
        )

        /**
         * No retry - single attempt only.
         */
        fun noRetry(): RetryConfig = RetryConfig(maxAttempts = 1)
    }
}

/**
 * Retry status after an operation.
 */
sealed class RetryResult<out T> {
    data class Success<T>(val value: T, val attempts: Int) : RetryResult<T>()
    data class Failure<T>(val error: Throwable, val attempts: Int) : RetryResult<T>()
}

/**
 * Extension function to check if an error is retryable.
 */
fun Throwable.isRetryable(): Boolean {
    return when (this) {
        // Network errors are typically retryable
        is ConnectivityException -> false // DNS failure - not retryable
        is TimeoutException -> true // Timeout - retryable
        is IOException -> true // General IO error - retryable

        // Custom network errors
        is id.startapp.pheromone.domain.types.NetworkError -> when (this) {
            is id.startapp.pheromone.domain.types.NetworkError.ConnectivityError -> true
            is id.startapp.pheromone.domain.types.NetworkError.TimeoutError -> true
            is id.startapp.pheromone.domain.types.NetworkError.ServerError -> {
                // Retry on 5xx server errors, but not on 4xx client errors
                statusCode in 500..599
            }
            else -> false
        }

        // Cancellation should not be retried
        is kotlinx.coroutines.CancellationException -> false

        else -> false
    }
}

/**
 * Calculate delay with exponential backoff and jitter.
 *
 * Uses exponential backoff with added jitter (±25%) to prevent
 * thundering herd problems when multiple clients retry simultaneously.
 *
 * @param attempt The attempt number (0-based)
 * @param config Retry configuration
 * @return Delay duration
 */
fun calculateBackoff(attempt: Int, config: RetryConfig): Duration {
    if (attempt == 0) return Duration.ZERO

    // Step 1: Calculate exponential backoff
    val baseDelay = calculateExponentialDelay(attempt, config)

    // Step 2: Apply maximum delay cap
    val cappedDelay = minOf(baseDelay, config.maxDelay)

    // Step 3: Add jitter to prevent thundering herd
    return applyJitter(cappedDelay)
}

/**
 * Calculate exponential backoff delay.
 *
 * Formula: initialDelay * (backoffMultiplier ^ (attempt - 1))
 */
private fun calculateExponentialDelay(attempt: Int, config: RetryConfig): Duration {
    val multiplier = config.backoffMultiplier.pow(attempt - 1)
    return config.initialDelay.times(multiplier)
}

/**
 * Apply jitter to prevent synchronized retry storms.
 *
 * Adds ±25% random jitter to the delay duration.
 */
private fun applyJitter(delay: Duration): Duration {
    val jitterRangeMs = (delay * JITTER_PERCENT).inWholeMilliseconds
    val jitter = ((Random.nextDouble() - 0.5) * 2 * jitterRangeMs).toLong()

    val totalMs = delay.inWholeMilliseconds + jitter
    val finalMs = maxOf(0L, totalMs) // Ensure non-negative
    return finalMs.toDuration(DurationUnit.MILLISECONDS)
}

/**
 * Jitter percentage for retry backoff (±25%).
 */
private const val JITTER_PERCENT = 0.25

/**
 * Execute a suspending operation with retry logic.
 *
 * Retries the operation on transient failures using exponential backoff with jitter.
 *
 * @param config Retry configuration
 * @param isRetryable Optional function to determine if an error is retryable
 * @param block The operation to execute
 * @return RetryResult containing the result or final error
 */
suspend fun <T> retryWithBackoff(
    config: RetryConfig = RetryConfig.default(),
    isRetryable: (Throwable) -> Boolean = Throwable::isRetryable,
    block: suspend () -> T
): RetryResult<T> {
    var lastError: Throwable? = null

    for (attempt in 0 until config.maxAttempts) {
        try {
            val result = block()
            return RetryResult.Success(result, attempts = attempt + 1)
        } catch (e: kotlinx.coroutines.CancellationException) {
            // Don't retry on cancellation - propagate immediately
            throw e
        } catch (e: Exception) {
            lastError = e

            // Check if we should retry
            if (attempt < config.maxAttempts - 1 && isRetryable(e)) {
                val delay = calculateBackoff(attempt + 1, config)
                delay(delay)
            } else {
                // Not retryable or no more attempts
                break
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    return RetryResult.Failure<T>(lastError!!, attempts = config.maxAttempts)
}

/**
 * Execute a suspending operation with retry logic, returning a Result type.
 *
 * @param config Retry configuration
 * @param isRetryable Optional function to determine if an error is retryable
 * @param block The operation to execute
 * @return Result containing the data or error
 */
suspend fun <T> retryWithBackoffResult(
    config: RetryConfig = RetryConfig.default(),
    isRetryable: (Throwable) -> Boolean = Throwable::isRetryable,
    block: suspend () -> T
): id.startapp.pheromone.domain.types.Result<T> {
    val result = retryWithBackoff(config, isRetryable, block)

    return when (result) {
        is RetryResult.Success -> {
            id.startapp.pheromone.domain.types.Result.Success(result.value)
        }
        is RetryResult.Failure -> {
            val error = result.error
            when (error) {
                is id.startapp.pheromone.domain.types.NetworkError -> {
                    id.startapp.pheromone.domain.types.Result.Error(error)
                }
                is ConnectivityException -> {
                    id.startapp.pheromone.domain.types.Result.Error(
                        id.startapp.pheromone.domain.types.NetworkError.ConnectivityError(error)
                    )
                }
                is TimeoutException -> {
                    id.startapp.pheromone.domain.types.Result.Error(
                        id.startapp.pheromone.domain.types.NetworkError.TimeoutError(error)
                    )
                }
                is IOException -> {
                    id.startapp.pheromone.domain.types.Result.Error(
                        id.startapp.pheromone.domain.types.NetworkError.ConnectivityError(error)
                    )
                }
                else -> {
                    id.startapp.pheromone.domain.types.Result.Error(
                        id.startapp.pheromone.domain.types.NetworkError.UnknownError(error)
                    )
                }
            }
        }
    }
}
