package id.startapp.application.usecases.auth

import id.startapp.domain.auth.entity.VerifyEmailResult
import id.startapp.domain.types.NetworkError
import id.startapp.domain.types.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class VerifyEmailUseCaseTest {

    private val fakeRepo = FakeAuthRepository()
    private val useCase = VerifyEmailUseCase(fakeRepo)

    @Test
    fun successfulVerification() = runTest {
        val result = useCase("user@example.com", "123456")

        assertIs<Result.Success<VerifyEmailResult>>(result)
        assertTrue(result.data.emailVerified)
        assertEquals("user-123", result.data.userId)
        assertEquals(1, fakeRepo.verifyEmailCallCount)
    }

    @Test
    fun otpTooShort() = runTest {
        val result = useCase("user@example.com", "12345")

        assertIs<Result.Error>(result)
        assertIs<NetworkError.ValidationError>(result.error)
        val error = result.error as NetworkError.ValidationError
        assertTrue(error.hasError("otp"))
        assertEquals(0, fakeRepo.verifyEmailCallCount)
    }

    @Test
    fun otpTooLong() = runTest {
        val result = useCase("user@example.com", "1234567")

        assertIs<Result.Error>(result)
        assertIs<NetworkError.ValidationError>(result.error)
        val error = result.error as NetworkError.ValidationError
        assertTrue(error.hasError("otp"))
        assertEquals(0, fakeRepo.verifyEmailCallCount)
    }

    @Test
    fun otpContainsNonDigits() = runTest {
        val result = useCase("user@example.com", "12345a")

        assertIs<Result.Error>(result)
        assertIs<NetworkError.ValidationError>(result.error)
        val error = result.error as NetworkError.ValidationError
        assertTrue(error.hasError("otp"))
        assertEquals(0, fakeRepo.verifyEmailCallCount)
    }

    @Test
    fun emptyOtp() = runTest {
        val result = useCase("user@example.com", "")

        assertIs<Result.Error>(result)
        assertIs<NetworkError.ValidationError>(result.error)
    }

    @Test
    fun repositoryError() = runTest {
        fakeRepo.verifyEmailResult = Result.Error(
            NetworkError.ValidationError(mapOf("otp" to "Invalid or expired code"))
        )

        val result = useCase("user@example.com", "123456")

        assertIs<Result.Error>(result)
        assertIs<NetworkError.ValidationError>(result.error)
    }

    @Test
    fun serverError() = runTest {
        fakeRepo.verifyEmailResult = Result.Error(NetworkError.ServerError(500))

        val result = useCase("user@example.com", "123456")

        assertIs<Result.Error>(result)
        assertIs<NetworkError.ServerError>(result.error)
    }
}
