package id.startapp.pheromone.infrastructure.database.dao

import id.startapp.pheromone.infrastructure.database.DatabaseManager
import kotlinx.datetime.Clock

/**
 * Data Access Object for the entity_cache table.
 *
 * Provides a generic JSON blob cache with TTL support
 * for offline-first data access.
 */
class CacheDao {

    private val queries get() = DatabaseManager.getDatabase().cacheQueriesQueries

    /**
     * Get cached JSON if not expired. Returns null on cache miss.
     */
    fun getIfValid(key: String): String? {
        val now = Clock.System.now().toEpochMilliseconds()
        return queries.selectIfValid(key, now).executeAsOneOrNull()
    }

    /**
     * Get cached JSON regardless of expiry (offline fallback).
     * Returns null if the key has never been cached.
     */
    fun getAny(key: String): CacheEntry? {
        val row = queries.selectAny(key).executeAsOneOrNull() ?: return null
        return CacheEntry(
            jsonData = row.json_data,
            cachedAt = row.cached_at,
            expiresAt = row.expires_at
        )
    }

    /**
     * Store or update a cache entry.
     *
     * @param key Unique cache key
     * @param jsonData Serialized JSON string
     * @param ttlMs Time-to-live in milliseconds
     */
    fun put(key: String, jsonData: String, ttlMs: Long) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.upsertCache(
            cache_key = key,
            json_data = jsonData,
            cached_at = now,
            expires_at = now + ttlMs
        )
    }

    /**
     * Remove a specific cache entry.
     */
    fun remove(key: String) {
        queries.deleteByKey(key)
    }

    /**
     * Remove all expired entries.
     */
    fun cleanup() {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.deleteExpired(now)
    }

    /**
     * Clear all cache entries (e.g., on logout).
     */
    fun clearAll() {
        queries.deleteAll()
    }
}

/**
 * Cache entry with metadata for staleness checks.
 */
data class CacheEntry(
    val jsonData: String,
    val cachedAt: Long,
    val expiresAt: Long
) {
    val isExpired: Boolean
        get() = Clock.System.now().toEpochMilliseconds() > expiresAt
}
