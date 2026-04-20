package id.startapp.pheromone.infrastructure.network

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Request signer for HMAC-based API authentication.
 *
 * Provides request signing using HMAC-SHA256 for additional security beyond
 * simple bearer tokens. Each request is signed with a timestamp and nonce to prevent
 * replay attacks.
 *
 * @param apiKey The API key identifier (must not be blank)
 * @param secretKey The secret key for HMAC signing (must not be blank)
 * @throws IllegalArgumentException if apiKey or secretKey is blank
 */
class RequestSigner(
    private val apiKey: String,
    private val secretKey: String
) {
    init {
        require(apiKey.isNotBlank()) { "API key cannot be blank" }
        require(secretKey.isNotBlank()) { "Secret key cannot be blank" }
    }

    /**
     * Signature configuration.
     */
    companion object {
        /**
         * Header name for the API key.
         */
        const val HEADER_API_KEY = "X-API-Key"

        /**
         * Header name for the timestamp.
         */
        const val HEADER_TIMESTAMP = "X-Timestamp"

        /**
         * Header name for the nonce.
         */
        const val HEADER_NONCE = "X-Nonce"

        /**
         * Header name for the signature.
         */
        const val HEADER_SIGNATURE = "X-Signature"

        /**
         * Algorithm used for signing.
         */
        const val SIGNATURE_ALGORITHM = "HMAC-SHA256"

        /**
         * Nonce length in bytes.
         */
        const val NONCE_LENGTH = 16
    }

    /**
     * Signed request data containing headers and signature.
     */
    data class SignedRequest(
        val apiKey: String,
        val timestamp: String,
        val nonce: String,
        val signature: String
    ) {
        /**
         * Get all headers as a map.
         */
        fun toHeaders(): Map<String, String> = mapOf(
            HEADER_API_KEY to apiKey,
            HEADER_TIMESTAMP to timestamp,
            HEADER_NONCE to nonce,
            HEADER_SIGNATURE to signature
        )
    }

    /**
     * Sign an HTTP request.
     *
     * @param method HTTP method (GET, POST, etc.)
     * @param path Request path (e.g., "/api/v1/users")
     * @param queryString Query string (without "?")
     * @param body Request body (for POST/PUT)
     * @return SignedRequest containing all signature headers
     */
    fun signRequest(
        method: String,
        path: String,
        queryString: String = "",
        body: String = ""
    ): SignedRequest {
        val timestamp = Clock.System.now().toEpochMilliseconds().toString()
        val nonce = generateNonce()

        // Build the string to sign
        val stringToSign = buildStringToSign(method, path, queryString, body, timestamp, nonce)

        // Generate HMAC-SHA256 signature
        val signature = hmacSha256(secretKey, stringToSign)

        return SignedRequest(
            apiKey = apiKey,
            timestamp = timestamp,
            nonce = nonce,
            signature = signature
        )
    }

    /**
     * Verify a request signature.
     *
     * @param method HTTP method
     * @param path Request path
     * @param queryString Query string
     * @param body Request body
     * @param signature Signature to verify
     * @param timestamp Request timestamp
     * @param nonce Request nonce
     * @return true if signature is valid
     */
    fun verifySignature(
        method: String,
        path: String,
        queryString: String,
        body: String,
        signature: String,
        timestamp: String,
        nonce: String
    ): Boolean {
        val stringToSign = buildStringToSign(method, path, queryString, body, timestamp, nonce)
        val expectedSignature = hmacSha256(secretKey, stringToSign)
        return constantTimeEquals(signature, expectedSignature)
    }

    /**
     * Build the canonical string to sign.
     *
     * Format: {method}\n{path}\n{query}\n{body}\n{timestamp}\n{nonce}
     */
    private fun buildStringToSign(
        method: String,
        path: String,
        queryString: String,
        body: String,
        timestamp: String,
        nonce: String
    ): String {
        return buildString {
            append(method.uppercase())
            append("\n")
            append(path)
            append("\n")
            append(queryString)
            append("\n")
            append(body)
            append("\n")
            append(timestamp)
            append("\n")
            append(nonce)
        }
    }

    /**
     * Generate a random nonce for request signing.
     */
    private fun generateNonce(): String {
        val bytes = ByteArray(NONCE_LENGTH)
        kotlin.random.Random.nextBytes(bytes)
        return bytes.joinToString("") { it.toInt().and(0xFF).toString(16).padStart(2, '0') }
    }

    /**
     * Generate HMAC-SHA256 signature.
     *
     * Note: This is a simplified implementation. In production, use platform-specific
     * crypto APIs (javax.crypto for Android JVM, CommonCrypto for iOS).
     */
    private fun hmacSha256(key: String, data: String): String {
        // Platform-specific implementation via expect/actual
        return hmacSha256Platform(key, data)
    }

    /**
     * Constant-time string comparison to prevent timing attacks.
     */
    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false

        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }
}

/**
 * Platform-specific HMAC-SHA256 implementation.
 */
expect fun hmacSha256Platform(key: String, data: String): String

/**
 * Create a request signer with configuration from BuildConfig.
 *
 * This factory method allows the signer to be configured per environment.
 *
 * @param apiKey The API key identifier
 * @param secretKey The secret key for signing
 * @return Configured RequestSigner
 */
fun createRequestSigner(
    apiKey: String,
    secretKey: String
): RequestSigner = RequestSigner(apiKey, secretKey)

/**
 * Extension function to add signature headers to an HTTP request.
 *
 * @param signer The request signer
 * @param method HTTP method
 * @param path Request path
 * @param queryString Query string
 * @param body Request body
 */
fun io.ktor.client.request.HttpRequestBuilder.signRequest(
    signer: RequestSigner,
    method: String,
    path: String,
    queryString: String = "",
    body: String = ""
) {
    val signed = signer.signRequest(method, path, queryString, body)
    signed.toHeaders().forEach { (key, value) ->
        headers.append(key, value)
    }
}
