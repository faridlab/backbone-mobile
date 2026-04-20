package id.startapp.pheromone.infrastructure.storage

/**
 * Secure storage interface for sensitive data like auth tokens.
 *
 * Platform implementations:
 * - Android: EncryptedSharedPreferences (AES256-GCM/SIV)
 * - iOS: Keychain Services
 */
interface SecureStorage {

    companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_OAUTH_PREFIX = "oauth_"
    }

    /**
     * Store the JWT access token.
     */
    suspend fun storeAccessToken(token: String)

    /**
     * Get the stored JWT access token.
     */
    suspend fun getAccessToken(): String?

    /**
     * Clear the stored access token.
     */
    suspend fun clearAccessToken()

    /**
     * Store the JWT refresh token.
     */
    suspend fun storeRefreshToken(token: String)

    /**
     * Get the stored JWT refresh token.
     */
    suspend fun getRefreshToken(): String?

    /**
     * Clear the stored refresh token.
     */
    suspend fun clearRefreshToken()

    /**
     * Store an OAuth token for a specific provider (e.g., "google", "apple").
     */
    suspend fun storeOAuthToken(provider: String, token: String)

    /**
     * Get an OAuth token for a specific provider.
     */
    suspend fun getOAuthToken(provider: String): String?

    /**
     * Clear all OAuth tokens.
     */
    suspend fun clearOAuthTokens()

    /**
     * Clear all stored data.
     */
    suspend fun clearAll()
}
