package id.startapp.application.usecases.auth

import id.startapp.application.security.RateLimiter
import id.startapp.application.utils.formatDuration
import id.startapp.application.validators.FieldValidators
import id.startapp.domain.auth.entity.AuthToken
import id.startapp.domain.auth.repository.AuthRepository
import id.startapp.domain.types.Result
import id.startapp.domain.types.NetworkError

/**
 * Use case for user login.
 *
 * Validates email format and authenticates the user.
 * Note: Password strength is NOT validated here - the user already has a password.
 * Includes rate limiting to prevent brute force attacks.
 */
class LoginUseCase(
    private val authRepository: AuthRepository,
    private val rateLimiter: RateLimiter = RateLimiter.forLogin()
) {

    /**
     * Execute the login use case.
     *
     * @param email User's email address
     * @param password User's password
     * @return Result containing AuthToken on success
     */
    suspend operator fun invoke(
        email: String,
        password: String
    ): Result<AuthToken> {
        // Check rate limit first
        if (!rateLimiter.allowRequest()) {
            val retryAfterMs = rateLimiter.getRetryAfterMs()
            val retryAfterSeconds = (retryAfterMs / 1000).coerceAtLeast(1)
            return Result.Error(
                NetworkError.TooManyRequests(
                    retryAfterSeconds = retryAfterSeconds.toInt(),
                    message = "Too many login attempts. Please try again in ${formatDuration(retryAfterSeconds)}."
                )
            )
        }

        // Validate email format - early return on invalid
        val emailResult = FieldValidators.validateEmail(email)
        if (emailResult is FieldValidators.EmailResult.Invalid) {
            return Result.Error(
                NetworkError.ValidationError(mapOf("email" to emailResult.reason))
            )
        }

        // Check for empty password - don't send unnecessary requests
        if (password.isEmpty()) {
            return Result.Error(
                NetworkError.ValidationError(mapOf("password" to "Password is required"))
            )
        }

        // Attempt login (server will validate the password)
        return authRepository.login(email, password)
    }

    /**
     * Get the number of remaining login attempts in the current window.
     */
    suspend fun getRemainingAttempts(): Int {
        return rateLimiter.getRemainingRequests()
    }

    /**
     * Get the time until the next login attempt is allowed (in milliseconds).
     */
    suspend fun getRetryAfterMs(): Long {
        return rateLimiter.getRetryAfterMs()
    }
}
