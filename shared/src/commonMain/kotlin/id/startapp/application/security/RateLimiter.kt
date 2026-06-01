package id.startapp.application.security

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Thread-safe rate limiter using token bucket algorithm.
 *
 * Limits the rate of operations (e.g., login attempts) to prevent abuse.
 *
 * @param maxRequests Maximum number of requests allowed
 * @param windowMs Time window in milliseconds
 */
class RateLimiter(
    private val maxRequests: Int,
    private val windowMs: Long
) {
    private val mutex = Mutex()
    private val requestTimestamps = mutableListOf<Instant>()

    /**
     * Check if a request is allowed under the rate limit.
     *
     * @return true if request is allowed, false if rate limited
     */
    suspend fun allowRequest(): Boolean = mutex.withLock {
        val now = Clock.System.now()
        val windowDuration: Duration = windowMs.toDuration(DurationUnit.MILLISECONDS)
        val windowStart = now.minus(windowDuration)
        requestTimestamps.removeAll { it < windowStart }

        // Check if we've exceeded the limit
        if (requestTimestamps.size >= maxRequests) {
            return false
        }

        // Record this request
        requestTimestamps.add(now)
        return true
    }

    /**
     * Get the number of requests remaining in the current window.
     */
    suspend fun getRemainingRequests(): Int = mutex.withLock {
        val now = Clock.System.now()
        val windowDuration: Duration = windowMs.toDuration(DurationUnit.MILLISECONDS)
        val windowStart = now.minus(windowDuration)
        requestTimestamps.removeAll { it < windowStart }
        return maxOf(maxRequests - requestTimestamps.size, 0)
    }

    /**
     * Get the time until the next request is allowed (in milliseconds).
     * Returns 0 if a request is allowed immediately.
     */
    suspend fun getRetryAfterMs(): Long = mutex.withLock {
        val now = Clock.System.now()
        val windowDuration: Duration = windowMs.toDuration(DurationUnit.MILLISECONDS)
        val windowStart = now.minus(windowDuration)
        requestTimestamps.removeAll { it < windowStart }

        if (requestTimestamps.size < maxRequests) {
            return 0L
        }

        val oldestRequest = requestTimestamps.firstOrNull()
        return if (oldestRequest != null) {
            val resetTime = oldestRequest.plus(windowDuration)
            val diff = resetTime.minus(now)
            maxOf(diff.inWholeMilliseconds, 0L)
        } else {
            0L
        }
    }

    /**
     * Reset the rate limiter (for testing or admin use).
     */
    suspend fun reset() = mutex.withLock {
        requestTimestamps.clear()
    }

    companion object {
        /**
         * Create a rate limiter for login attempts.
         * Allows 5 attempts per minute.
         */
        fun forLogin(): RateLimiter {
            return RateLimiter(
                maxRequests = 5,
                windowMs = 60_000 // 1 minute
            )
        }

        /**
         * Create a rate limiter for API requests.
         * Allows 100 requests per minute.
         */
        fun forApi(): RateLimiter {
            return RateLimiter(
                maxRequests = 100,
                windowMs = 60_000 // 1 minute
            )
        }

        /**
         * Create a rate limiter for registration attempts.
         * Allows 3 attempts per 5 minutes.
         */
        fun forRegistration(): RateLimiter {
            return RateLimiter(
                maxRequests = 3,
                windowMs = 300_000 // 5 minutes
            )
        }

        /**
         * Create a rate limiter for password reset.
         * Allows 3 requests per hour.
         */
        fun forPasswordReset(): RateLimiter {
            return RateLimiter(
                maxRequests = 3,
                windowMs = 3_600_000 // 1 hour
            )
        }
    }
}
