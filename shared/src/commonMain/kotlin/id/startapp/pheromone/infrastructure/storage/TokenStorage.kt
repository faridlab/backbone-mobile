package id.startapp.pheromone.infrastructure.storage

import id.startapp.pheromone.domain.auth.entity.AuthToken

/**
 * Storage interface for authentication tokens.
 *
 * Provides a higher-level abstraction over SecureStorage for AuthToken entities.
 */
interface TokenStorage {

    /**
     * Store a complete AuthToken (access + refresh).
     */
    suspend fun storeToken(token: AuthToken)

    /**
     * Get the complete AuthToken if both access and refresh tokens exist.
     */
    suspend fun getToken(): AuthToken?

    /**
     * Get only the access token.
     */
    suspend fun getAccessToken(): String?

    /**
     * Get only the refresh token.
     */
    suspend fun getRefreshToken(): String?

    /**
     * Check if a token is currently stored.
     */
    suspend fun hasToken(): Boolean

    /**
     * Check if the stored token is expired.
     */
    suspend fun isTokenExpired(): Boolean

    /**
     * Check if the stored token will expire within the given seconds.
     */
    suspend fun isTokenExpiringSoon(withinSeconds: Long): Boolean

    /**
     * Clear the stored token.
     */
    suspend fun clearToken()

    /**
     * Get the timestamp when the token was issued.
     */
    suspend fun getTokenIssuedAt(): Long?

    /**
     * Get the timestamp when the token expires.
     */
    suspend fun getTokenExpiresAt(): Long?
}
