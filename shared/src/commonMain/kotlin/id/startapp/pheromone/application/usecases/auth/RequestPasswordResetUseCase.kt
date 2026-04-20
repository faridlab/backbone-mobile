package id.startapp.pheromone.application.usecases.auth

import id.startapp.pheromone.domain.auth.repository.AuthRepository
import id.startapp.pheromone.domain.types.NetworkError
import id.startapp.pheromone.domain.types.Result

/**
 * Use case for requesting a password reset email.
 *
 * Validates the email format before delegating to the repository.
 */
class RequestPasswordResetUseCase(
    private val authRepository: AuthRepository
) {

    companion object {
        private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }

    /**
     * Execute the request password reset use case.
     *
     * @param email User's email address
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(email: String): Result<Unit> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || !emailRegex.matches(trimmedEmail)) {
            return Result.Error(
                NetworkError.ValidationError(mapOf("email" to "Please enter a valid email"))
            )
        }
        return authRepository.requestPasswordReset(trimmedEmail)
    }
}
