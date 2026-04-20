package id.startapp.pheromone.application.usecases.auth

import id.startapp.pheromone.domain.auth.entity.User
import id.startapp.pheromone.domain.auth.repository.AuthRepository
import id.startapp.pheromone.domain.types.Result

/**
 * Use case for getting the current authenticated user.
 */
class GetCurrentUserUseCase(
    private val authRepository: AuthRepository
) {

    /**
     * Execute the use case.
     *
     * @return Result containing User on success
     */
    suspend operator fun invoke(): Result<User> {
        return authRepository.getCurrentUser()
    }
}
