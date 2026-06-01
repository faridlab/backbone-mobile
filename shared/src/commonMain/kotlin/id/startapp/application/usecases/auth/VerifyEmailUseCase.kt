package id.startapp.application.usecases.auth

import id.startapp.domain.auth.entity.VerifyEmailResult
import id.startapp.domain.auth.repository.AuthRepository
import id.startapp.domain.types.NetworkError
import id.startapp.domain.types.Result

/**
 * Use case for email verification.
 *
 * Validates the OTP token and verifies the user's email address.
 */
class VerifyEmailUseCase(
    private val authRepository: AuthRepository
) {

    /**
     * Execute the email verification use case.
     *
     * @param email User's email address
     * @param verificationToken The 6-digit OTP code
     * @return Result containing VerifyEmailResult on success
     */
    suspend operator fun invoke(
        email: String,
        verificationToken: String
    ): Result<VerifyEmailResult> {
        if (verificationToken.length != 6 || !verificationToken.all { it.isDigit() }) {
            return Result.Error(
                NetworkError.ValidationError(mapOf("otp" to "Please enter a valid 6-digit code"))
            )
        }

        return authRepository.verifyEmail(email, verificationToken)
    }
}
