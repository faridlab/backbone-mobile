package id.startapp.pheromone.infrastructure.network

import id.startapp.pheromone.domain.auth.entity.AuthToken
import id.startapp.pheromone.domain.types.NetworkError
import id.startapp.pheromone.domain.types.Result
import id.startapp.pheromone.infrastructure.network.api.AuthApiClient
import id.startapp.pheromone.infrastructure.storage.TokenStorage
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Default token refresh implementation (Phase 9D — token rotation dedup).
 *
 * Uses a dedicated unauthenticated [AuthApiClient] to avoid recursion
 * (the authenticated client calls this provider on 401, so the refresh
 * call must NOT go through the authenticated client).
 *
 * ## Concurrency model
 *
 * A [Mutex] serialises all concurrent refresh attempts so that N simultaneous
 * 401 responses trigger exactly ONE network call to the refresh endpoint:
 *
 * 1. Coroutine A acquires the mutex — token is expired — calls the refresh API.
 * 2. Coroutines B, C, … block on `withLock`.
 * 3. A stores the new token and releases the mutex.
 * 4. B acquires the mutex — double-check: token is now valid — returns early.
 * 5. C acquires the mutex — same — returns the already-refreshed token.
 *
 * The double-check on entry (`current.isValid`) is the critical dedup step.
 * Without it, each waiting coroutine would fire its own refresh call.
 */
class DefaultTokenRefreshProvider(
    private val refreshApiClient: AuthApiClient,
    private val tokenStorage: TokenStorage,
) : TokenRefreshProvider {

    private val refreshMutex = Mutex()

    override suspend fun refreshToken(): Result<AuthToken> = refreshMutex.withLock {
        // Double-check: another coroutine may have already refreshed while we waited
        // for the mutex. If the token is now valid, skip the network call entirely.
        val current = tokenStorage.getToken()
        if (current != null && current.isValid) {
            return@withLock Result.Success(current)
        }

        val refreshToken = tokenStorage.getRefreshToken()
            ?: return@withLock Result.Error(NetworkError.Unauthorized())

        when (val result = refreshApiClient.refreshToken(refreshToken)) {
            is Result.Success -> {
                tokenStorage.storeToken(result.data)
                result
            }
            is Result.Error -> {
                try { tokenStorage.clearToken() } catch (_: Exception) {}
                result
            }
        }
    }
}
