package id.startapp.application.usecases.auth

import id.startapp.application.security.RateLimiter
import id.startapp.domain.auth.entity.RegisterResult
import id.startapp.domain.types.NetworkError
import id.startapp.domain.types.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RegisterUseCaseTest {

    private val fakeRepo = FakeAuthRepository()
    private val rateLimiter = RateLimiter(maxRequests = 3, windowMs = 300_000)
    private val useCase = RegisterUseCase(fakeRepo, rateLimiter)

    private val validPassword = "StrongPass1@"

    @Test
    fun successfulRegistration() = runTest {
        val result = useCase("new@example.com", validPassword, validPassword)

        assertIs<Result.Success<RegisterResult>>(result)
        assertEquals("user-123", result.data.userId)
        assertTrue(result.data.verificationRequired)
        assertEquals(1, fakeRepo.registerCallCount)
        assertEquals("new@example.com", fakeRepo.lastRegisterEmail)
    }

    @Test
    fun invalidEmailFormat() = runTest {
        val result = useCase("bad-email", validPassword, validPassword)

        assertIs<Result.Error>(result)
        assertIs<NetworkError.ValidationError>(result.error)
        val error = result.error as NetworkError.ValidationError
        assertTrue(error.hasError("email"))
        assertEquals(0, fakeRepo.registerCallCount)
    }

    @Test
    fun blankEmail() = runTest {
        val result = useCase("", validPassword, validPassword)

        assertIs<Result.Error>(result)
        assertIs<NetworkError.ValidationError>(result.error)
        val error = result.error as NetworkError.ValidationError
        assertTrue(error.hasError("email"))
    }

    @Test
    fun weakPasswordTooShort() = runTest {
        val result = useCase("user@example.com", "Ab1@", "Ab1@")

        assertIs<Result.Error>(result)
        assertIs<NetworkError.ValidationError>(result.error)
        val error = result.error as NetworkError.ValidationError
        assertTrue(error.hasError("password"))
    }

    @Test
    fun passwordMissingUppercase() = runTest {
        val result = useCase("user@example.com", "lowercase1@", "lowercase1@")

        assertIs<Result.Error>(result)
        assertIs<NetworkError.ValidationError>(result.error)
        val error = result.error as NetworkError.ValidationError
        assertTrue(error.hasError("password"))
    }

    @Test
    fun passwordMissingSpecialChar() = runTest {
        val result = useCase("user@example.com", "StrongPass1", "StrongPass1")

        assertIs<Result.Error>(result)
        assertIs<NetworkError.ValidationError>(result.error)
        val error = result.error as NetworkError.ValidationError
        assertTrue(error.hasError("password"))
    }

    @Test
    fun passwordsDoNotMatch() = runTest {
        val result = useCase("user@example.com", validPassword, "DifferentPass1@")

        assertIs<Result.Error>(result)
        assertIs<NetworkError.ValidationError>(result.error)
        val error = result.error as NetworkError.ValidationError
        assertTrue(error.hasError("confirmPassword"))
        assertEquals("Passwords do not match", error.errors["confirmPassword"])
    }

    @Test
    fun rateLimitExceeded() = runTest {
        val strictLimiter = RateLimiter(maxRequests = 1, windowMs = 300_000)
        val limitedUseCase = RegisterUseCase(fakeRepo, strictLimiter)

        // First should succeed
        val r1 = limitedUseCase("user@example.com", validPassword, validPassword)
        assertIs<Result.Success<RegisterResult>>(r1)

        // Second should be rate limited
        val r2 = limitedUseCase("user2@example.com", validPassword, validPassword)
        assertIs<Result.Error>(r2)
        assertIs<NetworkError.TooManyRequests>(r2.error)
    }

    @Test
    fun repositoryError() = runTest {
        fakeRepo.registerResult = Result.Error(
            NetworkError.ValidationError(mapOf("email" to "Email already registered"))
        )

        val result = useCase("existing@example.com", validPassword, validPassword)

        assertIs<Result.Error>(result)
        assertIs<NetworkError.ValidationError>(result.error)
    }
}
