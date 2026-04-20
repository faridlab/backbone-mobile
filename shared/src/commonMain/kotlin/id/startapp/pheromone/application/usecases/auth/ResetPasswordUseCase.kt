package id.startapp.pheromone.application.usecases.auth

import id.startapp.pheromone.application.validators.PasswordValidator
import id.startapp.pheromone.domain.auth.repository.AuthRepository
import id.startapp.pheromone.domain.types.NetworkError
import id.startapp.pheromone.domain.types.Result

/**
 * Use case for resetting a password with an OTP token.
 *
 * Validates the OTP token format and password strength before delegating to the repository.
 */
class ResetPasswordUseCase(
    private val authRepository: AuthRepository
) {

    /**
     * Execute the reset password use case.
     *
     * @param token The 6-digit OTP code from email
     * @param newPassword The new password
     * @param confirmPassword Password confirmation (must match newPassword)
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(
        token: String,
        newPassword: String,
        confirmPassword: String
    ): Result<Unit> {
        if (token.length != 6 || !token.all { it.isDigit() }) {
            return Result.Error(
                NetworkError.ValidationError(mapOf("token" to "Please enter a valid 6-digit code"))
            )
        }

        // Use PasswordValidator for consistent password requirements
        val passwordResult = PasswordValidator.validate(newPassword)
        if (passwordResult is PasswordValidator.ValidationResult.Invalid) {
            return Result.Error(
                NetworkError.ValidationError(
                    mapOf("password" to PasswordValidator.getErrorMessage(passwordResult.errors))
                )
            )
        }

        if (newPassword != confirmPassword) {
            return Result.Error(
                NetworkError.ValidationError(mapOf("confirmPassword" to "Passwords do not match"))
            )
        }
        return authRepository.resetPassword(token, newPassword, confirmPassword)
    }
}
