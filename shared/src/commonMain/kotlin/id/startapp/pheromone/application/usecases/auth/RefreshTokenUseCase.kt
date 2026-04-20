package id.startapp.pheromone.application.usecases.auth

import id.startapp.pheromone.domain.auth.entity.AuthToken
import id.startapp.pheromone.domain.auth.repository.AuthRepository
import id.startapp.pheromone.domain.types.Result

/**
 * Use case for refreshing the authentication token.
 *
 * Uses the refresh token to obtain a new access token.
 */
class RefreshTokenUseCase(
    private val authRepository: AuthRepository
) {

    /**
     * Execute the refresh token use case.
     *
     * @return Result containing new AuthToken on success
     */
    suspend operator fun invoke(): Result<AuthToken> {
        return authRepository.refreshToken()
    }
}
