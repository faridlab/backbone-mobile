package id.startapp.infrastructure.repository

import id.startapp.domain.auth.entity.AuthToken
import id.startapp.domain.auth.entity.RegisterResult
import id.startapp.domain.auth.entity.User
import id.startapp.domain.auth.entity.VerifyEmailResult
import id.startapp.domain.types.NetworkError
import id.startapp.domain.types.Result
import id.startapp.infrastructure.network.api.AuthApi
import id.startapp.infrastructure.network.api.UserProviderResponse
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [AuthRepositoryImpl].
 *
 * Uses [FakeAuthApiClientForRepo] (configurable fake) and [FakeTokenStorage]
 * to test repository logic: token persistence, cache integration, error handling.
 */
class AuthRepositoryImplTest {

    private val testToken = AuthToken(
        accessToken = "access-123",
        refreshToken = "refresh-456",
        expiresIn = 3600
    )

    private val testUser = User(
        id = "user-123",
        username = "testuser",
        email = "test@example.com"
    )

    private fun createRepo(
        apiClient: FakeAuthApiClientForRepo = FakeAuthApiClientForRepo(),
        tokenStorage: FakeTokenStorage = FakeTokenStorage(),
        profileCache: UserProfileCache? = null
    ): Triple<AuthRepositoryImpl, FakeAuthApiClientForRepo, FakeTokenStorage> {
        val repo = AuthRepositoryImpl(
            authApiClient = apiClient,
            tokenStorage = tokenStorage,
            profileCache = profileCache
        )
        return Triple(repo, apiClient, tokenStorage)
    }

    // ========================
    // login
    // ========================

    @Test
    fun `login stores token on success`() = runTest {
        val api = FakeAuthApiClientForRepo(loginResult = Result.Success(testToken))
        val (repo, _, tokenStorage) = createRepo(apiClient = api)

        val result = repo.login("test@example.com", "password")
        assertIs<Result.Success<AuthToken>>(result)
        assertEquals("access-123", result.data.accessToken)
        assertEquals(1, tokenStorage.storeCallCount)
        assertNotNull(tokenStorage.storedToken)
    }

    @Test
    fun `login does not store token on failure`() = runTest {
        val api = FakeAuthApiClientForRepo(
            loginResult = Result.Error(NetworkError.Unauthorized())
        )
        val (repo, _, tokenStorage) = createRepo(apiClient = api)

        val result = repo.login("test@example.com", "wrong")
        assertIs<Result.Error>(result)
        assertEquals(0, tokenStorage.storeCallCount)
        assertNull(tokenStorage.storedToken)
    }

    @Test
    fun `login returns server error`() = runTest {
        val api = FakeAuthApiClientForRepo(
            loginResult = Result.Error(NetworkError.ServerError(500))
        )
        val (repo, _, _) = createRepo(apiClient = api)

        val result = repo.login("test@example.com", "password")
        assertIs<Result.Error>(result)
        assertIs<NetworkError.ServerError>(result.error)
    }

    // ========================
    // logout
    // ========================

    @Test
    fun `logout clears token storage`() = runTest {
        val tokenStorage = FakeTokenStorage().apply { storedToken = testToken }
        val (repo, _, _) = createRepo(tokenStorage = tokenStorage)

        val result = repo.logout()
        assertIs<Result.Success<Unit>>(result)
        assertEquals(1, tokenStorage.clearCallCount)
        assertNull(tokenStorage.storedToken)
    }

    @Test
    fun `logout clears profile cache`() = runTest {
        val storage = FakeKeyValueStorage()
        val cache = UserProfileCache(storage = storage)
        cache.put(testUser)
        assertNotNull(cache.get()) // cached

        val (repo, _, _) = createRepo(profileCache = cache)
        repo.logout()

        assertNull(cache.get())
        assertNull(cache.getStale())
    }

    @Test
    fun `logout clears token even if API call throws`() = runTest {
        val api = FakeAuthApiClientForRepo(logoutThrows = true)
        val tokenStorage = FakeTokenStorage().apply { storedToken = testToken }
        val (repo, _, _) = createRepo(apiClient = api, tokenStorage = tokenStorage)

        val result = repo.logout()
        assertIs<Result.Success<Unit>>(result) // Always succeeds
        assertNull(tokenStorage.storedToken)
    }

    // ========================
    // refreshToken
    // ========================

    @Test
    fun `refreshToken returns error when no refresh token`() = runTest {
        val (repo, _, _) = createRepo() // tokenStorage has no token

        val result = repo.refreshToken()
        assertIs<Result.Error>(result)
        assertIs<NetworkError.Unauthorized>(result.error)
    }

    @Test
    fun `refreshToken stores new token on success`() = runTest {
        val newToken = AuthToken("new-access", "new-refresh", 7200)
        val api = FakeAuthApiClientForRepo(refreshResult = Result.Success(newToken))
        val tokenStorage = FakeTokenStorage().apply { storedToken = testToken }
        val (repo, _, _) = createRepo(apiClient = api, tokenStorage = tokenStorage)

        val result = repo.refreshToken()
        assertIs<Result.Success<AuthToken>>(result)
        assertEquals("new-access", result.data.accessToken)
        assertEquals("new-access", tokenStorage.storedToken?.accessToken)
    }

    @Test
    fun `refreshToken clears token on failure`() = runTest {
        val api = FakeAuthApiClientForRepo(
            refreshResult = Result.Error(NetworkError.Unauthorized())
        )
        val tokenStorage = FakeTokenStorage().apply { storedToken = testToken }
        val (repo, _, _) = createRepo(apiClient = api, tokenStorage = tokenStorage)

        val result = repo.refreshToken()
        assertIs<Result.Error>(result)
        assertNull(tokenStorage.storedToken) // Cleared
    }

    // ========================
    // getCurrentUser
    // ========================

    @Test
    fun `getCurrentUser returns cached profile when available`() = runTest {
        val storage = FakeKeyValueStorage()
        val cache = UserProfileCache(storage = storage)
        cache.put(testUser)

        val api = FakeAuthApiClientForRepo() // API should NOT be called
        val (repo, _, _) = createRepo(apiClient = api, profileCache = cache)

        val result = repo.getCurrentUser()
        assertIs<Result.Success<User>>(result)
        assertEquals("user-123", result.data.id)
        assertEquals(0, api.getCurrentUserCallCount) // Not called
    }

    @Test
    fun `getCurrentUser fetches from API on cache miss and caches result`() = runTest {
        val storage = FakeKeyValueStorage()
        val cache = UserProfileCache(storage = storage)
        // No cached user

        val api = FakeAuthApiClientForRepo(getCurrentUserResult = Result.Success(testUser))
        val (repo, _, _) = createRepo(apiClient = api, profileCache = cache)

        val result = repo.getCurrentUser()
        assertIs<Result.Success<User>>(result)
        assertEquals("user-123", result.data.id)
        assertEquals(1, api.getCurrentUserCallCount)

        // Verify it was cached
        val cached = cache.get()
        assertNotNull(cached)
        assertEquals("user-123", cached.id)
    }

    @Test
    fun `getCurrentUser falls back to stale cache on network error`() = runTest {
        val storage = FakeKeyValueStorage()
        val cache = UserProfileCache(storage = storage)
        cache.put(testUser)

        // Expire the cache by setting timestamp to 20 minutes ago
        val twentyMinAgo = kotlinx.datetime.Clock.System.now().toEpochMilliseconds() - (20 * 60 * 1000L)
        storage.putLong("cached_user_profile_at", twentyMinAgo)

        // Verify get() returns null (expired)
        assertNull(cache.get())

        // API returns error
        val api = FakeAuthApiClientForRepo(
            getCurrentUserResult = Result.Error(NetworkError.ConnectivityError())
        )
        val (repo, _, _) = createRepo(apiClient = api, profileCache = cache)

        // Should fall back to stale cache
        val result = repo.getCurrentUser()
        assertIs<Result.Success<User>>(result)
        assertEquals("user-123", result.data.id)
    }

    @Test
    fun `getCurrentUser returns error when no cache and API fails`() = runTest {
        val api = FakeAuthApiClientForRepo(
            getCurrentUserResult = Result.Error(NetworkError.ConnectivityError())
        )
        val storage = FakeKeyValueStorage()
        val cache = UserProfileCache(storage = storage)
        val (repo, _, _) = createRepo(apiClient = api, profileCache = cache)

        val result = repo.getCurrentUser()
        assertIs<Result.Error>(result)
        assertIs<NetworkError.ConnectivityError>(result.error)
    }

    @Test
    fun `getCurrentUser works without profile cache`() = runTest {
        val api = FakeAuthApiClientForRepo(getCurrentUserResult = Result.Success(testUser))
        val (repo, _, _) = createRepo(apiClient = api, profileCache = null)

        val result = repo.getCurrentUser()
        assertIs<Result.Success<User>>(result)
        assertEquals("user-123", result.data.id)
    }

    // ========================
    // isAuthenticated
    // ========================

    @Test
    fun `isAuthenticated returns false when no token`() = runTest {
        val (repo, _, _) = createRepo()
        assertFalse(repo.isAuthenticated())
    }

    @Test
    fun `isAuthenticated returns true when token is valid`() = runTest {
        val tokenStorage = FakeTokenStorage().apply { storedToken = testToken }
        val (repo, _, _) = createRepo(tokenStorage = tokenStorage)
        assertTrue(repo.isAuthenticated())
    }

    // ========================
    // register / verifyEmail (delegation tests)
    // ========================

    @Test
    fun `register delegates to API client`() = runTest {
        val registerResult = RegisterResult(
            userId = "new-user",
            email = "new@example.com",
            verificationRequired = true,
            verificationEmailSent = true
        )
        val api = FakeAuthApiClientForRepo(registerResult = Result.Success(registerResult))
        val (repo, _, _) = createRepo(apiClient = api)

        val result = repo.register("new@example.com", "Pass123@", "Pass123@")
        assertIs<Result.Success<RegisterResult>>(result)
        assertEquals("new-user", result.data.userId)
    }

    @Test
    fun `verifyEmail delegates to API client`() = runTest {
        val verifyResult = VerifyEmailResult(userId = "user-123", emailVerified = true)
        val api = FakeAuthApiClientForRepo(verifyEmailResult = Result.Success(verifyResult))
        val (repo, _, _) = createRepo(apiClient = api)

        val result = repo.verifyEmail("test@example.com", "123456")
        assertIs<Result.Success<VerifyEmailResult>>(result)
        assertTrue(result.data.emailVerified)
    }
}

/**
 * Fake [AuthApi] for repository testing.
 *
 * Implements the [AuthApi] interface with configurable results,
 * allowing [AuthRepositoryImpl] to be tested without Ktor/HTTP.
 */
class FakeAuthApiClientForRepo(
    var loginResult: Result<AuthToken> = Result.Success(
        AuthToken("test-access", "test-refresh", 3600)
    ),
    var registerResult: Result<RegisterResult> = Result.Success(
        RegisterResult("user-1", "test@example.com", true, true)
    ),
    var verifyEmailResult: Result<VerifyEmailResult> = Result.Success(
        VerifyEmailResult("user-1", true)
    ),
    var refreshResult: Result<AuthToken> = Result.Success(
        AuthToken("refreshed-access", "refreshed-refresh", 3600)
    ),
    var getCurrentUserResult: Result<User> = Result.Error(NetworkError.Unauthorized()),
    var logoutThrows: Boolean = false
) : AuthApi {
    var getCurrentUserCallCount = 0

    override suspend fun login(email: String, password: String): Result<AuthToken> = loginResult

    override suspend fun register(email: String, password: String, confirmPassword: String): Result<RegisterResult> = registerResult

    override suspend fun verifyEmail(email: String, verificationToken: String): Result<VerifyEmailResult> = verifyEmailResult

    override suspend fun resendVerification(email: String): Result<Unit> = Result.Success(Unit)

    override suspend fun refreshToken(refreshToken: String): Result<AuthToken> = refreshResult

    override suspend fun getCurrentUser(): Result<User> {
        getCurrentUserCallCount++
        return getCurrentUserResult
    }

    override suspend fun getUserProvider(): Result<UserProviderResponse?> = Result.Success(null)

    override suspend fun logout(): Result<Unit> {
        if (logoutThrows) throw RuntimeException("Network error")
        return Result.Success(Unit)
    }

    override suspend fun requestPasswordReset(email: String): Result<Unit> = Result.Success(Unit)

    override suspend fun resetPassword(token: String, newPassword: String, confirmPassword: String): Result<Unit> = Result.Success(Unit)
}
