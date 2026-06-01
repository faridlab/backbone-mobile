package id.startapp.infrastructure.network

import id.startapp.domain.types.NetworkError
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.test.assertFailsWith

/**
 * Unit tests for retry mechanism.
 */
class RetryHandlerTest {

    @Test
    fun `retryWithBackoff returns success on first attempt`() = runTest {
        var attempts = 0
        val result = retryWithBackoff {
            attempts++
            "success"
        }

        assertTrue(result is RetryResult.Success)
        assertEquals("success", (result as RetryResult.Success).value)
        assertEquals(1, result.attempts)
        assertEquals(1, attempts)
    }

    @Test
    fun `retryWithBackoff retries on retryable error`() = runTest {
        var attempts = 0
        val result = retryWithBackoff(
            config = RetryConfig(maxAttempts = 3, initialDelay = 10.milliseconds)
        ) {
            attempts++
            if (attempts < 2) {
                throw IOException("Temporary failure")
            }
            "success"
        }

        assertTrue(result is RetryResult.Success)
        assertEquals(2, (result as RetryResult.Success).attempts)
        assertEquals(2, attempts)
    }

    @Test
    fun `retryWithBackoff fails after max attempts`() = runTest {
        var attempts = 0
        val result = retryWithBackoff(
            config = RetryConfig(maxAttempts = 3, initialDelay = 10.milliseconds)
        ) {
            attempts++
            throw IOException("Persistent failure")
        }

        assertTrue(result is RetryResult.Failure)
        assertEquals(3, (result as RetryResult.Failure).attempts)
        assertEquals(3, attempts)
    }

    @Test
    fun `retryWithBackoff does not retry non-retryable error`() = runTest {
        var attempts = 0
        val result = retryWithBackoff(
            config = RetryConfig(maxAttempts = 3)
        ) {
            attempts++
            throw kotlinx.coroutines.CancellationException("Cancelled")
        }

        // CancellationException should propagate, not be caught
        assertEquals(1, attempts)
    }

    @Test
    fun `calculateBackoff returns zero for first attempt`() {
        val config = RetryConfig.default()
        val delay = calculateBackoff(0, config)
        assertTrue(delay == Duration.ZERO)
    }

    @Test
    fun `calculateBackoff increases exponentially`() {
        val config = RetryConfig(
            maxAttempts = 5,
            initialDelay = 100.milliseconds,
            backoffMultiplier = 2.0
        )

        val delay1 = calculateBackoff(1, config)
        val delay2 = calculateBackoff(2, config)
        val delay3 = calculateBackoff(3, config)

        // Each delay should be approximately 2x the previous
        assertTrue(delay2 >= delay1 * 1.5) // Allow for jitter
        assertTrue(delay3 >= delay2 * 1.5)
    }

    @Test
    fun `calculateBackoff respects max delay`() {
        val config = RetryConfig(
            maxAttempts = 10,
            initialDelay = 1.seconds,
            maxDelay = 2.seconds,
            backoffMultiplier = 10.0 // Very aggressive multiplier
        )

        val delay10 = calculateBackoff(5, config)
        assertTrue(delay10 <= config.maxDelay)
    }

    @Test
    fun `IOException is retryable`() {
        val error = IOException("Connection failed")
        assertTrue(error.isRetryable())
    }

    @Test
    fun `UnknownHostException is not retryable`() {
        val error = java.net.UnknownHostException("Unknown host")
        assertFalse(error.isRetryable())
    }

    @Test
    fun `NetworkError NetworkError is retryable`() {
        val error = NetworkError.NetworkError()
        assertTrue(error.isRetryable())
    }

    @Test
    fun `NetworkError ServerError 500 is retryable`() {
        val error = NetworkError.ServerError(500)
        assertTrue(error.isRetryable())
    }

    @Test
    fun `NetworkError ServerError 400 is not retryable`() {
        val error = NetworkError.ServerError(400)
        assertFalse(error.isRetryable())
    }

    @Test
    fun `NetworkError ValidationError is not retryable`() {
        val error = NetworkError.ValidationError(mapOf("field" to "error"))
        assertFalse(error.isRetryable())
    }

    @Test
    fun `retryWithBackoffResult returns Result Success`() = runTest {
        val result = retryWithBackoffResult { "success" }
        assertTrue(result is id.startapp.domain.types.Result.Success)
    }

    @Test
    fun `retryWithBackoffResult returns Result Error after exhaustion`() = runTest {
        val result = retryWithBackoffResult(
            config = RetryConfig(maxAttempts = 2, initialDelay = 10.milliseconds)
        ) {
            throw IOException("Failed")
        }
        assertTrue(result is id.startapp.domain.types.Result.Error)
    }
}
