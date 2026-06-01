package id.startapp.infrastructure.network.api

import id.startapp.domain.auth.entity.AuthToken
import id.startapp.domain.auth.entity.RegisterResult
import id.startapp.domain.auth.entity.User
import id.startapp.domain.auth.entity.VerifyEmailResult
import id.startapp.domain.types.Result

/**
 * Contract for authentication API operations.
 *
 * Extracted as an interface to follow Dependency Inversion Principle,
 * allowing [AuthRepositoryImpl][id.startapp.infrastructure.repository.AuthRepositoryImpl]
 * to be tested with fakes in commonTest (no Ktor dependency needed).
 *
 * @see AuthApiClient for the Ktor-based production implementation
 */
interface AuthApi {

    suspend fun login(email: String, password: String): Result<AuthToken>

    suspend fun register(
        email: String,
        password: String,
        confirmPassword: String
    ): Result<RegisterResult>

    suspend fun verifyEmail(
        email: String,
        verificationToken: String
    ): Result<VerifyEmailResult>

    suspend fun resendVerification(email: String): Result<Unit>

    suspend fun refreshToken(refreshToken: String): Result<AuthToken>

    suspend fun getCurrentUser(): Result<User>

    suspend fun getUserProvider(): Result<UserProviderResponse?>

    suspend fun logout(): Result<Unit>

    suspend fun requestPasswordReset(email: String): Result<Unit>

    suspend fun resetPassword(
        token: String,
        newPassword: String,
        confirmPassword: String
    ): Result<Unit>
}
