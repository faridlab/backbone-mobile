package id.startapp.application.usecases.auth

import id.startapp.domain.auth.repository.AuthRepository
import id.startapp.domain.types.Result

/**
 * Use case for user logout.
 *
 * Clears authentication tokens and optionally invalidates them on the server.
 */
class LogoutUseCase(
    private val authRepository: AuthRepository
) {

    /**
     * Execute the logout use case.
     *
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.logout()
    }
}
