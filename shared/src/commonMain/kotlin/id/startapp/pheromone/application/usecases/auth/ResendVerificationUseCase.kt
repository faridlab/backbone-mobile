package id.startapp.pheromone.application.usecases.auth

import id.startapp.pheromone.domain.auth.repository.AuthRepository
import id.startapp.pheromone.domain.types.Result

/**
 * Use case for resending verification email.
 */
class ResendVerificationUseCase(
    private val authRepository: AuthRepository
) {

    /**
     * Execute the resend verification use case.
     *
     * @param email User's email address
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(email: String): Result<Unit> {
        return authRepository.resendVerification(email)
    }
}
