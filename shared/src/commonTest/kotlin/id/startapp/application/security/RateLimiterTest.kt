package id.startapp.application.security

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [RateLimiter].
 *
 * Tests rate limiting functionality using token bucket algorithm.
 */
class RateLimiterTest {

    @Test
    fun `allowRequest returns true for first request`() {
        val rateLimiter = RateLimiter(maxRequests = 5, windowMs = 1000)
        assertTrue(rateLimiter.allowRequest())
    }

    @Test
    fun `allowRequest returns true when under limit`() {
        val rateLimiter = RateLimiter(maxRequests = 5, windowMs = 1000)
        repeat(4) {
            assertTrue(rateLimiter.allowRequest())
        }
    }

    @Test
    fun `allowRequest returns false when limit exceeded`() {
        val rateLimiter = RateLimiter(maxRequests = 3, windowMs = 1000)
        repeat(3) {
            assertTrue(rateLimiter.allowRequest())
        }
        assertFalse(rateLimiter.allowRequest())
    }

    @Test
    fun `allowRequest returns true after window expires`() {
        val rateLimiter = RateLimiter(maxRequests = 2, windowMs = 100)
        repeat(2) {
            assertTrue(rateLimiter.allowRequest())
        }
        assertFalse(rateLimiter.allowRequest())

        // Wait for window to expire
        Thread.sleep(150)

        assertTrue(rateLimiter.allowRequest())
    }

    @Test
    fun `getRemainingRequests returns correct count`() {
        val rateLimiter = RateLimiter(maxRequests = 5, windowMs = 1000)
        assertEquals(5, rateLimiter.getRemainingRequests())

        rateLimiter.allowRequest()
        assertEquals(4, rateLimiter.getRemainingRequests())

        repeat(3) { rateLimiter.allowRequest() }
        assertEquals(1, rateLimiter.getRemainingRequests())
    }

    @Test
    fun `getRemainingRequests returns zero when limit exceeded`() {
        val rateLimiter = RateLimiter(maxRequests = 2, windowMs = 1000)
        repeat(3) { rateLimiter.allowRequest() }
        assertEquals(0, rateLimiter.getRemainingRequests())
    }

    @Test
    fun `getRetryAfterMs returns zero when under limit`() {
        val rateLimiter = RateLimiter(maxRequests = 5, windowMs = 1000)
        assertEquals(0, rateLimiter.getRetryAfterMs())
    }

    @Test
    fun `getRetryAfterMs returns positive value when limited`() {
        val rateLimiter = RateLimiter(maxRequests = 2, windowMs = 1000)
        repeat(2) { rateLimiter.allowRequest() }
        val retryAfter = rateLimiter.getRetryAfterMs()
        assertTrue(retryAfter > 0)
        assertTrue(retryAfter <= 1000)
    }

    @Test
    fun `reset clears all request timestamps`() {
        val rateLimiter = RateLimiter(maxRequests = 2, windowMs = 1000)
        repeat(2) { rateLimiter.allowRequest() }
        assertFalse(rateLimiter.allowRequest())

        rateLimiter.reset()
        assertTrue(rateLimiter.allowRequest())
        assertEquals(1, rateLimiter.getRemainingRequests())
    }

    // Factory Method Tests

    @Test
    fun `forLogin creates rate limiter with 5 requests per minute`() {
        val rateLimiter = RateLimiter.forLogin()
        repeat(5) { assertTrue(rateLimiter.allowRequest()) }
        assertFalse(rateLimiter.allowRequest())
    }

    @Test
    fun `forApi creates rate limiter with 100 requests per minute`() {
        val rateLimiter = RateLimiter.forApi()
        repeat(100) { assertTrue(rateLimiter.allowRequest()) }
        assertFalse(rateLimiter.allowRequest())
    }

    @Test
    fun `forPasswordReset creates rate limiter with 3 requests per hour`() {
        val rateLimiter = RateLimiter.forPasswordReset()
        repeat(3) { assertTrue(rateLimiter.allowRequest()) }
        assertFalse(rateLimiter.allowRequest())
    }
}
