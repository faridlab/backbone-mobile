package id.startapp.pheromone.domain.auth.repository

import id.startapp.pheromone.domain.auth.entity.AuthToken
import id.startapp.pheromone.domain.auth.entity.RegisterResult
import id.startapp.pheromone.domain.auth.entity.User
import id.startapp.pheromone.domain.auth.entity.VerifyEmailResult
import id.startapp.pheromone.domain.types.Result

/**
 * Repository interface for authentication operations.
 *
 * This interface defines the contract for authentication-related operations
 * including login, logout, token refresh, and user management.
 */
interface AuthRepository {

    /**
     * Authenticate a user with email and password.
     *
     * @param email User's email address
     * @param password User's password
     * @return Result containing AuthToken on success
     */
    suspend fun login(email: String, password: String): Result<AuthToken>

    /**
     * Authenticate a user with OAuth provider.
     *
     * @param provider OAuth provider (e.g., "google", "apple")
     * @param token OAuth token from the provider
     * @return Result containing AuthToken on success
     */
    suspend fun loginWithOAuth(provider: String, token: String): Result<AuthToken>

    /**
     * Logout the current user.
     *
     * Clears stored tokens and optionally invalidates them on the server.
     *
     * @return Result indicating success or failure
     */
    suspend fun logout(): Result<Unit>

    /**
     * Refresh the access token using the refresh token.
     *
     * @return Result containing new AuthToken on success
     */
    suspend fun refreshToken(): Result<AuthToken>

    /**
     * Get the currently authenticated user.
     *
     * @return Result containing User on success
     */
    suspend fun getCurrentUser(): Result<User>

    /**
     * Check if a user is currently authenticated.
     *
     * @return true if user has a valid token
     */
    suspend fun isAuthenticated(): Boolean

    /**
     * Get the stored auth token without making a network request.
     *
     * @return AuthToken if available, null otherwise
     */
    suspend fun getStoredToken(): AuthToken?

    /**
     * Register a new user.
     *
     * Backend automatically sends verification email on success.
     * Does not return auth tokens — user must verify email first.
     * Username is auto-derived from email.
     *
     * @param email User's email address (also used as username)
     * @param password User's password
     * @param confirmPassword Password confirmation
     * @return Result containing RegisterResult on success
     */
    suspend fun register(
        email: String,
        password: String,
        confirmPassword: String
    ): Result<RegisterResult>

    /**
     * Verify email with OTP token.
     *
     * @param email User's email address
     * @param verificationToken The 6-digit OTP code
     * @return Result containing VerifyEmailResult on success
     */
    suspend fun verifyEmail(
        email: String,
        verificationToken: String
    ): Result<VerifyEmailResult>

    /**
     * Resend verification email.
     *
     * @param email User's email address
     * @return Result indicating success or failure
     */
    suspend fun resendVerification(email: String): Result<Unit>

    /**
     * Request a password reset email.
     *
     * @param email User's email address
     * @return Result indicating success or failure
     */
    suspend fun requestPasswordReset(email: String): Result<Unit>

    /**
     * Reset password with a token.
     *
     * @param token Password reset token
     * @param newPassword New password
     * @param confirmPassword Password confirmation (must match newPassword)
     * @return Result indicating success or failure
     */
    suspend fun resetPassword(
        token: String,
        newPassword: String,
        confirmPassword: String
    ): Result<Unit>
}
