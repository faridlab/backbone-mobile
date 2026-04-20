package id.startapp.pheromone.infrastructure.repository

import id.startapp.pheromone.domain.auth.entity.User
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests for [UserProfileCache].
 *
 * Verifies TTL-based caching, stale fallback, put/get/clear operations.
 */
class UserProfileCacheTest {

    private val testUser = User(
        id = "user-123",
        username = "testuser",
        email = "test@example.com",
        fullName = "Test User"
    )

    private fun createCache(storage: FakeKeyValueStorage = FakeKeyValueStorage()): Pair<UserProfileCache, FakeKeyValueStorage> {
        return UserProfileCache(storage = storage) to storage
    }

    // ========================
    // put + get
    // ========================

    @Test
    fun `get returns null when cache is empty`() = runTest {
        val (cache, _) = createCache()
        assertNull(cache.get())
    }

    @Test
    fun `put then get returns cached user`() = runTest {
        val (cache, _) = createCache()
        cache.put(testUser)
        val result = cache.get()
        assertNotNull(result)
        assertEquals("user-123", result.id)
        assertEquals("testuser", result.username)
        assertEquals("test@example.com", result.email)
    }

    @Test
    fun `put overwrites previous cached user`() = runTest {
        val (cache, _) = createCache()
        cache.put(testUser)
        val updatedUser = testUser.copy(fullName = "Updated Name")
        cache.put(updatedUser)
        val result = cache.get()
        assertNotNull(result)
        assertEquals("Updated Name", result.fullName)
    }

    // ========================
    // clear
    // ========================

    @Test
    fun `clear removes cached user`() = runTest {
        val (cache, _) = createCache()
        cache.put(testUser)
        assertNotNull(cache.get())
        cache.clear()
        assertNull(cache.get())
    }

    @Test
    fun `clear is safe on empty cache`() = runTest {
        val (cache, _) = createCache()
        cache.clear() // Should not throw
        assertNull(cache.get())
    }

    // ========================
    // getStale
    // ========================

    @Test
    fun `getStale returns null when cache is empty`() = runTest {
        val (cache, _) = createCache()
        assertNull(cache.getStale())
    }

    @Test
    fun `getStale returns user even after clear of timestamp`() = runTest {
        val storage = FakeKeyValueStorage()
        val cache = UserProfileCache(storage = storage)
        cache.put(testUser)
        // Remove only the timestamp to simulate expired state
        storage.remove("cached_user_profile_at")
        // get() should return null (no timestamp)
        assertNull(cache.get())
        // getStale() should still return the user
        val stale = cache.getStale()
        assertNotNull(stale)
        assertEquals("user-123", stale.id)
    }

    @Test
    fun `getStale returns null after full clear`() = runTest {
        val (cache, _) = createCache()
        cache.put(testUser)
        cache.clear()
        assertNull(cache.getStale())
    }

    // ========================
    // TTL expiry
    // ========================

    @Test
    fun `get returns null when cache timestamp is expired`() = runTest {
        val storage = FakeKeyValueStorage()
        val cache = UserProfileCache(storage = storage)
        cache.put(testUser)

        // Manually set cached_at to 16 minutes ago (TTL is 15 min)
        val sixteenMinutesAgo = kotlinx.datetime.Clock.System.now().toEpochMilliseconds() - (16 * 60 * 1000L)
        storage.putLong("cached_user_profile_at", sixteenMinutesAgo)

        // get() should return null (expired)
        assertNull(cache.get())
    }

    @Test
    fun `getStale returns user even when TTL is expired`() = runTest {
        val storage = FakeKeyValueStorage()
        val cache = UserProfileCache(storage = storage)
        cache.put(testUser)

        // Manually set cached_at to 16 minutes ago
        val sixteenMinutesAgo = kotlinx.datetime.Clock.System.now().toEpochMilliseconds() - (16 * 60 * 1000L)
        storage.putLong("cached_user_profile_at", sixteenMinutesAgo)

        // get() returns null, but getStale() should still work
        assertNull(cache.get())
        val stale = cache.getStale()
        assertNotNull(stale)
        assertEquals("user-123", stale.id)
    }

    @Test
    fun `get returns user when cache is fresh`() = runTest {
        val storage = FakeKeyValueStorage()
        val cache = UserProfileCache(storage = storage)
        cache.put(testUser)

        // Manually set cached_at to 5 minutes ago (within 15 min TTL)
        val fiveMinutesAgo = kotlinx.datetime.Clock.System.now().toEpochMilliseconds() - (5 * 60 * 1000L)
        storage.putLong("cached_user_profile_at", fiveMinutesAgo)

        val result = cache.get()
        assertNotNull(result)
        assertEquals("user-123", result.id)
    }

    // ========================
    // Error handling
    // ========================

    @Test
    fun `get returns null for corrupted JSON and clears cache`() = runTest {
        val storage = FakeKeyValueStorage()
        val cache = UserProfileCache(storage = storage)
        // Write corrupted JSON
        storage.putString("cached_user_profile", "not valid json {{{")
        storage.putLong("cached_user_profile_at", kotlinx.datetime.Clock.System.now().toEpochMilliseconds())

        // Should return null and clear the corrupted data
        assertNull(cache.get())
        // After clearing, getStale should also be null
        assertNull(cache.getStale())
    }

    @Test
    fun `getStale returns null for corrupted JSON and clears cache`() = runTest {
        val storage = FakeKeyValueStorage()
        val cache = UserProfileCache(storage = storage)
        storage.putString("cached_user_profile", "corrupted")

        assertNull(cache.getStale())
    }
}
