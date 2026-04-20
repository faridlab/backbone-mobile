package id.startapp.pheromone.infrastructure.repository

import id.startapp.pheromone.domain.auth.entity.User
import id.startapp.pheromone.infrastructure.storage.KeyValueStorage
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Caches the current user's profile in KeyValueStorage (SharedPreferences / NSUserDefaults).
 *
 * Used for:
 * - Showing user info immediately on app launch (before network call completes)
 * - Providing offline access to user profile
 * - Avoiding redundant getCurrentUser API calls
 */
class UserProfileCache(
    private val storage: KeyValueStorage,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    companion object {
        private const val KEY_USER_PROFILE = "cached_user_profile"
        private const val KEY_CACHED_AT = "cached_user_profile_at"
        private const val TTL_MS = 15 * 60 * 1000L // 15 minutes
    }

    /**
     * Get cached user profile if available and not expired.
     */
    suspend fun get(): User? {
        val jsonStr = storage.getString(KEY_USER_PROFILE) ?: return null
        val cachedAt = storage.getLong(KEY_CACHED_AT) ?: return null
        val now = Clock.System.now().toEpochMilliseconds()

        if (now - cachedAt > TTL_MS) return null

        return try {
            json.decodeFromString<User>(jsonStr)
        } catch (_: Exception) {
            clear()
            null
        }
    }

    /**
     * Get cached user profile even if expired (for offline fallback).
     */
    suspend fun getStale(): User? {
        val jsonStr = storage.getString(KEY_USER_PROFILE) ?: return null
        return try {
            json.decodeFromString<User>(jsonStr)
        } catch (_: Exception) {
            clear()
            null
        }
    }

    /**
     * Cache the user profile.
     */
    suspend fun put(user: User) {
        try {
            val jsonStr = json.encodeToString(user)
            storage.putString(KEY_USER_PROFILE, jsonStr)
            storage.putLong(KEY_CACHED_AT, Clock.System.now().toEpochMilliseconds())
        } catch (_: Exception) {
            // Cache write failure is non-fatal
        }
    }

    /**
     * Clear cached profile (e.g., on logout).
     */
    suspend fun clear() {
        storage.remove(KEY_USER_PROFILE)
        storage.remove(KEY_CACHED_AT)
    }
}
