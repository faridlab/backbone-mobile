package id.startapp.application.usecases.auth

import id.startapp.domain.auth.entity.User
import id.startapp.domain.auth.repository.AuthRepository
import id.startapp.domain.types.Result

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
