package id.startapp.pheromone.infrastructure.repository

import id.startapp.pheromone.domain.auth.entity.AuthToken
import id.startapp.pheromone.domain.auth.entity.RegisterResult
import id.startapp.pheromone.domain.auth.entity.User
import id.startapp.pheromone.domain.auth.entity.VerifyEmailResult
import id.startapp.pheromone.domain.auth.repository.AuthRepository
import id.startapp.pheromone.domain.types.Result
import id.startapp.pheromone.domain.types.NetworkError
import id.startapp.pheromone.infrastructure.network.api.AuthApi
import id.startapp.pheromone.infrastructure.storage.TokenStorage

/**
 * Implementation of AuthRepository.
 *
 * Handles authentication operations by combining API calls with local token storage.
 */
class AuthRepositoryImpl(
    private val authApiClient: AuthApi,
    private val tokenStorage: TokenStorage,
    private val profileCache: UserProfileCache? = null,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthToken> {
        return when (val result = authApiClient.login(email, password)) {
            is Result.Success -> {
                tokenStorage.storeToken(result.data)
                Result.Success(result.data)
            }
            is Result.Error -> result
        }
    }

    override suspend fun loginWithOAuth(provider: String, token: String): Result<AuthToken> {
        // OAuth is a planned feature for future implementation.
        // This would integrate with providers like Google, Apple, Facebook.
        return Result.Error(NetworkError.UnknownError(
            Exception("OAuth login is not yet implemented. Planned for future release.")
        ))
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            // Call logout API if needed
            authApiClient.logout()
            // Clear local tokens, cached profile
            tokenStorage.clearToken()
            profileCache?.clear()
            Result.Success(Unit)
        } catch (e: Exception) {
            // Even if API call fails, clear all local state
            tokenStorage.clearToken()
            profileCache?.clear()
            Result.Success(Unit)
        }
    }

    override suspend fun refreshToken(): Result<AuthToken> {
        val currentToken = tokenStorage.getRefreshToken()
        if (currentToken == null) {
            return Result.Error(NetworkError.Unauthorized("No refresh token available"))
        }

        return when (val result = authApiClient.refreshToken(currentToken)) {
            is Result.Success -> {
                tokenStorage.storeToken(result.data)
                Result.Success(result.data)
            }
            is Result.Error -> {
                // Clear tokens if refresh fails
                tokenStorage.clearToken()
                result
            }
        }
    }

    override suspend fun getCurrentUser(): Result<User> {
        // Try cache first for instant display
        val cached = profileCache?.get()
        if (cached != null) return Result.Success(cached)

        return when (val result = authApiClient.getCurrentUser()) {
            is Result.Success -> {
                profileCache?.put(result.data)
                result
            }
            is Result.Error -> {
                // Fall back to stale cache when offline
                val stale = profileCache?.getStale()
                if (stale != null) Result.Success(stale) else result
            }
        }
    }

    override suspend fun isAuthenticated(): Boolean {
        return tokenStorage.hasToken() && !tokenStorage.isTokenExpired()
    }

    override suspend fun getStoredToken(): AuthToken? {
        return if (isAuthenticated()) tokenStorage.getToken() else null
    }

    override suspend fun register(
        email: String,
        password: String,
        confirmPassword: String
    ): Result<RegisterResult> {
        return authApiClient.register(email, password, confirmPassword)
    }

    override suspend fun verifyEmail(
        email: String,
        verificationToken: String
    ): Result<VerifyEmailResult> {
        return authApiClient.verifyEmail(email, verificationToken)
    }

    override suspend fun resendVerification(email: String): Result<Unit> {
        return authApiClient.resendVerification(email)
    }

    override suspend fun requestPasswordReset(email: String): Result<Unit> {
        return authApiClient.requestPasswordReset(email)
    }

    override suspend fun resetPassword(
        token: String,
        newPassword: String,
        confirmPassword: String
    ): Result<Unit> {
        return authApiClient.resetPassword(
            token = token,
            newPassword = newPassword,
            confirmPassword = confirmPassword
        )
    }
}
