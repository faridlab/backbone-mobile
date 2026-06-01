package id.startapp.application.usecases.auth

import id.startapp.domain.auth.entity.AuthToken
import id.startapp.domain.auth.repository.AuthRepository
import id.startapp.domain.types.Result

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
