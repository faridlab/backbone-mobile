package id.startapp.application.usecases.auth

import id.startapp.application.security.RateLimiter
import id.startapp.application.utils.formatDuration
import id.startapp.application.validators.FieldValidators
import id.startapp.application.validators.PasswordValidator
import id.startapp.domain.auth.entity.RegisterResult
import id.startapp.domain.auth.repository.AuthRepository
import id.startapp.domain.types.NetworkError
import id.startapp.domain.types.Result

/**
 * Use case for user registration.
 *
 * Validates input fields and registers the user via the repository.
 * Backend automatically sends a verification email on success.
 */
class RegisterUseCase(
    private val authRepository: AuthRepository,
    private val rateLimiter: RateLimiter = RateLimiter.forRegistration()
) {

    /**
     * Execute the registration use case.
     *
     * @param email User's email address (also used as username)
     * @param password User's password
     * @param confirmPassword Password confirmation
     * @return Result containing RegisterResult on success
     */
    suspend operator fun invoke(
        email: String,
        password: String,
        confirmPassword: String
    ): Result<RegisterResult> {
        if (!rateLimiter.allowRequest()) {
            val retryAfterMs = rateLimiter.getRetryAfterMs()
            val retryAfterSeconds = (retryAfterMs / 1000).coerceAtLeast(1)
            return Result.Error(
                NetworkError.TooManyRequests(
                    retryAfterSeconds = retryAfterSeconds.toInt(),
                    message = "Too many registration attempts. Please try again in ${formatDuration(retryAfterSeconds)}."
                )
            )
        }

        val emailResult = FieldValidators.validateEmail(email)
        if (emailResult is FieldValidators.EmailResult.Invalid) {
            return Result.Error(
                NetworkError.ValidationError(mapOf("email" to emailResult.reason))
            )
        }

        val passwordResult = PasswordValidator.validate(password)
        if (passwordResult is PasswordValidator.ValidationResult.Invalid) {
            return Result.Error(
                NetworkError.ValidationError(
                    mapOf("password" to PasswordValidator.getErrorMessage(passwordResult.errors))
                )
            )
        }

        if (password != confirmPassword) {
            return Result.Error(
                NetworkError.ValidationError(mapOf("confirmPassword" to "Passwords do not match"))
            )
        }

        return authRepository.register(email, password, confirmPassword)
    }
}
