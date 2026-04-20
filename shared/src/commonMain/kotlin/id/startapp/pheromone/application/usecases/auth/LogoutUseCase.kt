package id.startapp.pheromone.application.usecases.auth

import id.startapp.pheromone.domain.auth.repository.AuthRepository
import id.startapp.pheromone.domain.types.Result

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
