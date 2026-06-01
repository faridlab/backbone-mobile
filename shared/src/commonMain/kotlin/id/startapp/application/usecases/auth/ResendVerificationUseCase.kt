package id.startapp.application.usecases.auth

import id.startapp.domain.auth.repository.AuthRepository
import id.startapp.domain.types.Result

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
