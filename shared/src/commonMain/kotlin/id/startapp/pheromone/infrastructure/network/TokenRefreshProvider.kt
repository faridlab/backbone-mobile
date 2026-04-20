package id.startapp.pheromone.infrastructure.network

import id.startapp.pheromone.domain.auth.entity.AuthToken
import id.startapp.pheromone.domain.types.Result

/**
 * Abstraction for token refresh to break circular dependency:
 * HttpClient → AuthRepository → AuthApiClient → HttpClient.
 *
 * The authenticated HttpClient can use this to transparently refresh
 * tokens on 401 responses without knowing about AuthRepository.
 */
interface TokenRefreshProvider {
    suspend fun refreshToken(): Result<AuthToken>
}
