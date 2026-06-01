package id.startapp.application.usecases.auth

import id.startapp.application.security.RateLimiter
import id.startapp.domain.auth.entity.AuthToken
import id.startapp.domain.types.NetworkError
import id.startapp.domain.types.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LoginUseCaseTest {

    private val fakeRepo = FakeAuthRepository()
    private val rateLimiter = RateLimiter(maxRequests = 5, windowMs = 60_000)
    private val useCase = LoginUseCase(fakeRepo, rateLimiter)

    @Test
    fun successfulLogin() = runTest {
        val result = useCase("user@example.com", "password123")

        assertIs<Result.Success<AuthToken>>(result)
        assertEquals("test-access-token", result.data.accessToken)
        assertEquals(1, fakeRepo.loginCallCount)
        assertEquals("user@example.com", fakeRepo.lastLoginEmail)
    }

    @Test
    fun invalidEmailFormat() = runTest {
        val result = useCase("not-an-email", "password123")

        assertIs<Result.Error>(result)
        assertIs<NetworkError.ValidationError>(result.error)
        val error = result.error as NetworkError.ValidationError
        assertTrue(error.hasError("email"))
        assertEquals(0, fakeRepo.loginCallCount)
    }

    @Test
    fun blankEmail() = runTest {
        val result = useCase("", "password123")

        assertIs<Result.Error>(result)
        assertIs<NetworkError.ValidationError>(result.error)
        val error = result.error as NetworkError.ValidationError
        assertTrue(error.hasError("email"))
        assertEquals(0, fakeRepo.loginCallCount)
    }

    @Test
    fun emptyPassword() = runTest {
        val result = useCase("user@example.com", "")

        assertIs<Result.Error>(result)
        assertIs<NetworkError.ValidationError>(result.error)
        val error = result.error as NetworkError.ValidationError
        assertTrue(error.hasError("password"))
        assertEquals("Password is required", error.errors["password"])
        assertEquals(0, fakeRepo.loginCallCount)
    }

    @Test
    fun repositoryError() = runTest {
        fakeRepo.loginResult = Result.Error(NetworkError.Unauthorized())

        val result = useCase("user@example.com", "password123")

        assertIs<Result.Error>(result)
        assertIs<NetworkError.Unauthorized>(result.error)
    }

    @Test
    fun rateLimitExceeded() = runTest {
        val strictLimiter = RateLimiter(maxRequests = 2, windowMs = 60_000)
        val limitedUseCase = LoginUseCase(fakeRepo, strictLimiter)

        // First two should succeed
        val r1 = limitedUseCase("user@example.com", "pass1")
        assertIs<Result.Success<AuthToken>>(r1)

        val r2 = limitedUseCase("user@example.com", "pass2")
        assertIs<Result.Success<AuthToken>>(r2)

        // Third should be rate limited
        val r3 = limitedUseCase("user@example.com", "pass3")
        assertIs<Result.Error>(r3)
        assertIs<NetworkError.TooManyRequests>(r3.error)
    }

    @Test
    fun serverError() = runTest {
        fakeRepo.loginResult = Result.Error(NetworkError.ServerError(500))

        val result = useCase("user@example.com", "password123")

        assertIs<Result.Error>(result)
        assertIs<NetworkError.ServerError>(result.error)
    }
}
