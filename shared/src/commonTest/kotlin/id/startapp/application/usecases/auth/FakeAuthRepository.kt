package id.startapp.application.usecases.auth

import id.startapp.domain.auth.entity.AuthToken
import id.startapp.domain.auth.entity.RegisterResult
import id.startapp.domain.auth.entity.User
import id.startapp.domain.auth.entity.VerifyEmailResult
import id.startapp.domain.auth.repository.AuthRepository
import id.startapp.domain.types.NetworkError
import id.startapp.domain.types.Result

/**
 * Fake AuthRepository for unit testing use cases.
 *
 * Configurable via lambda properties to control success/failure behavior.
 */
class FakeAuthRepository : AuthRepository {

    var loginResult: Result<AuthToken> = Result.Success(
        AuthToken(
            accessToken = "test-access-token",
            refreshToken = "test-refresh-token",
            expiresIn = 3600
        )
    )

    var registerResult: Result<RegisterResult> = Result.Success(
        RegisterResult(
            userId = "user-123",
            email = "test@example.com",
            verificationRequired = true,
            verificationEmailSent = true
        )
    )

    var verifyEmailResult: Result<VerifyEmailResult> = Result.Success(
        VerifyEmailResult(userId = "user-123", emailVerified = true)
    )

    var logoutResult: Result<Unit> = Result.Success(Unit)
    var refreshTokenResult: Result<AuthToken> = Result.Success(
        AuthToken(accessToken = "new-token", refreshToken = "new-refresh", expiresIn = 3600)
    )
    var getCurrentUserResult: Result<User> = Result.Error(NetworkError.Unauthorized())
    var resendVerificationResult: Result<Unit> = Result.Success(Unit)
    var requestPasswordResetResult: Result<Unit> = Result.Success(Unit)
    var resetPasswordResult: Result<Unit> = Result.Success(Unit)

    var loginCallCount = 0
    var registerCallCount = 0
    var verifyEmailCallCount = 0
    var lastLoginEmail: String? = null
    var lastRegisterEmail: String? = null

    override suspend fun login(email: String, password: String): Result<AuthToken> {
        loginCallCount++
        lastLoginEmail = email
        return loginResult
    }

    override suspend fun loginWithOAuth(provider: String, token: String): Result<AuthToken> {
        return loginResult
    }

    override suspend fun logout(): Result<Unit> = logoutResult

    override suspend fun refreshToken(): Result<AuthToken> = refreshTokenResult

    override suspend fun getCurrentUser(): Result<User> = getCurrentUserResult

    override suspend fun isAuthenticated(): Boolean = true

    override suspend fun getStoredToken(): AuthToken? = null

    override suspend fun register(
        email: String,
        password: String,
        confirmPassword: String
    ): Result<RegisterResult> {
        registerCallCount++
        lastRegisterEmail = email
        return registerResult
    }

    override suspend fun verifyEmail(
        email: String,
        verificationToken: String
    ): Result<VerifyEmailResult> {
        verifyEmailCallCount++
        return verifyEmailResult
    }

    override suspend fun resendVerification(email: String): Result<Unit> = resendVerificationResult

    override suspend fun requestPasswordReset(email: String): Result<Unit> = requestPasswordResetResult

    override suspend fun resetPassword(
        token: String,
        newPassword: String,
        confirmPassword: String
    ): Result<Unit> = resetPasswordResult
}
