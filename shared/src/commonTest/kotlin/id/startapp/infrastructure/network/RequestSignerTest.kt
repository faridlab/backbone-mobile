package id.startapp.infrastructure.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [RequestSigner].
 *
 * Security Note: Test values are hardcoded for unit testing only.
 * These are NOT production credentials and are only used in local tests.
 */
class RequestSignerTest {

    // Test-only credentials - never use these in production
    private val apiKey = "test-api-key"
    private val secretKey = "test-secret-key-for-signing"

    @Test
    fun `signRequest generates consistent signature`() {
        val signer = RequestSigner(apiKey, secretKey)

        val result1 = signer.signRequest("GET", "/api/v1/users", "", "")
        val result2 = signer.signRequest("GET", "/api/v1/users", "", "")

        assertEquals(result1.signature, result2.signature)
    }

    @Test
    fun `signRequest includes all headers`() {
        val signer = RequestSigner(apiKey, secretKey)

        val result = signer.signRequest("POST", "/api/v1/auth/login", "", "{\"email\":\"test\"}")

        assertEquals(apiKey, result.apiKey)
        assertTrue(result.timestamp.isNotEmpty())
        assertTrue(result.nonce.isNotEmpty())
        assertTrue(result.signature.isNotEmpty())
    }

    @Test
    fun `signRequest generates different signatures for different paths`() {
        val signer = RequestSigner(apiKey, secretKey)

        val result1 = signer.signRequest("GET", "/api/v1/users", "", "")
        val result2 = signer.signRequest("GET", "/api/v1/posts", "", "")

        assertNotEquals(result1.signature, result2.signature)
    }

    @Test
    fun `signRequest generates different signatures for different bodies`() {
        val signer = RequestSigner(apiKey, secretKey)

        val result1 = signer.signRequest("POST", "/api/v1/data", "", "{\"key\":\"value1\"}")
        val result2 = signer.signRequest("POST", "/api/v1/data", "", "{\"key\":\"value2\"}")

        assertNotEquals(result1.signature, result2.signature)
    }

    @Test
    fun `signRequest generates different signatures for different methods`() {
        val signer = RequestSigner(apiKey, secretKey)

        val result1 = signer.signRequest("GET", "/api/v1/data", "", "")
        val result2 = signer.signRequest("POST", "/api/v1/data", "", "")

        assertNotEquals(result1.signature, result2.signature)
    }

    @Test
    fun `signRequest toHeaders returns all required headers`() {
        val signer = RequestSigner(apiKey, secretKey)

        val result = signer.signRequest("GET", "/api/v1/users", "", "")
        val headers = result.toHeaders()

        assertEquals(4, headers.size)
        assertEquals(apiKey, headers[RequestSigner.HEADER_API_KEY])
        assertEquals(result.timestamp, headers[RequestSigner.HEADER_TIMESTAMP])
        assertEquals(result.nonce, headers[RequestSigner.HEADER_NONCE])
        assertEquals(result.signature, headers[RequestSigner.HEADER_SIGNATURE])
    }

    @Test
    fun `verifySignature returns true for valid signature`() {
        val signer = RequestSigner(apiKey, secretKey)

        val signed = signer.signRequest("GET", "/api/v1/users", "", "")

        val isValid = signer.verifySignature(
            method = "GET",
            path = "/api/v1/users",
            queryString = "",
            body = "",
            signature = signed.signature,
            timestamp = signed.timestamp,
            nonce = signed.nonce
        )

        assertTrue(isValid)
    }

    @Test
    fun `verifySignature returns false for invalid signature`() {
        val signer = RequestSigner(apiKey, secretKey)

        val signed = signer.signRequest("GET", "/api/v1/users", "", "")

        val isValid = signer.verifySignature(
            method = "GET",
            path = "/api/v1/users",
            queryString = "",
            body = "",
            signature = "invalid-signature",
            timestamp = signed.timestamp,
            nonce = signed.nonce
        )

        assertFalse(isValid)
    }

    @Test
    fun `verifySignature returns false for tampered body`() {
        val signer = RequestSigner(apiKey, secretKey)

        val signed = signer.signRequest("POST", "/api/v1/data", "", "{\"original\":\"body\"}")

        val isValid = signer.verifySignature(
            method = "POST",
            path = "/api/v1/data",
            queryString = "",
            body = "{\"tampered\":\"body\"}",
            signature = signed.signature,
            timestamp = signed.timestamp,
            nonce = signed.nonce
        )

        assertFalse(isValid)
    }

    @Test
    fun `generateNonce produces unique values`() {
        val signer = RequestSigner(apiKey, secretKey)

        val result = signer.signRequest("GET", "/api/v1/users", "", "")
        val result2 = signer.signRequest("GET", "/api/v1/users", "", "")

        // Nonces should be different (very high probability)
        assertNotEquals(result.nonce, result2.nonce)
    }

    @Test
    fun `generateNonce produces fixed length string`() {
        val signer = RequestSigner(apiKey, secretKey)

        val result = signer.signRequest("GET", "/api/v1/users", "", "")

        // 16 bytes = 32 hex characters
        assertEquals(32, result.nonce.length)
    }

    @Test
    fun `signRequest handles query string`() {
        val signer = RequestSigner(apiKey, secretKey)

        val result = signer.signRequest("GET", "/api/v1/users", "page=1&limit=10", "")

        val resultWithoutQuery = signer.signRequest("GET", "/api/v1/users", "", "")

        // Signatures should be different with and without query string
        assertNotEquals(result.signature, resultWithoutQuery.signature)
    }

    @Test
    fun `signRequest handles empty body`() {
        val signer = RequestSigner(apiKey, secretKey)

        val result = signer.signRequest("POST", "/api/v1/data", "", "")

        assertEquals(apiKey, result.apiKey)
        assertTrue(result.signature.isNotEmpty())
    }
}
