package id.startapp.pheromone.infrastructure.repository

import id.startapp.pheromone.domain.auth.entity.AuthToken
import id.startapp.pheromone.infrastructure.storage.TokenStorage

/**
 * In-memory implementation of TokenStorage for testing.
 */
class FakeTokenStorage : TokenStorage {
    var storedToken: AuthToken? = null
    var storeCallCount = 0
    var clearCallCount = 0

    override suspend fun storeToken(token: AuthToken) {
        storeCallCount++
        storedToken = token
    }

    override suspend fun getToken(): AuthToken? = storedToken

    override suspend fun getAccessToken(): String? = storedToken?.accessToken

    override suspend fun getRefreshToken(): String? = storedToken?.refreshToken

    override suspend fun hasToken(): Boolean = storedToken != null

    override suspend fun isTokenExpired(): Boolean {
        val token = storedToken ?: return true
        return token.isExpired()
    }

    override suspend fun isTokenExpiringSoon(withinSeconds: Long): Boolean {
        val token = storedToken ?: return true
        return token.isExpiringSoon(withinSeconds)
    }

    override suspend fun clearToken() {
        clearCallCount++
        storedToken = null
    }

    override suspend fun getTokenIssuedAt(): Long? = storedToken?.issuedAt

    override suspend fun getTokenExpiresAt(): Long? {
        val token = storedToken ?: return null
        return token.issuedAt + (token.expiresIn * 1000)
    }
}
