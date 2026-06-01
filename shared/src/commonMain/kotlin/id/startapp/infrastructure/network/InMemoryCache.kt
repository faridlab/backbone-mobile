package id.startapp.infrastructure.network

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

/**
 * Lightweight in-memory cache with TTL for API responses.
 *
 * Thread-safe via [Mutex]. Entries expire after [defaultTtlMs] milliseconds.
 * Not persistent — cleared on process death, which is acceptable for
 * reducing redundant API calls within a single session.
 *
 * Usage:
 * ```
 * val data = cache.getOrFetch("service-templates") {
 *     api.getAll()  // only called if cache miss or expired
 * }
 * ```
 */
class InMemoryCache(private val defaultTtlMs: Long = 5 * 60 * 1000L) {

    private data class CacheEntry(
        val data: Any?,
        val expiresAt: Long,
    )

    private val store = mutableMapOf<String, CacheEntry>()
    private val mutex = Mutex()

    /**
     * Return cached value if valid, otherwise call [fetch] and cache the result.
     *
     * @param key Unique cache key
     * @param ttlMs Time-to-live override for this entry
     * @param fetch Suspend function to produce the value on cache miss
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun <T> getOrFetch(
        key: String,
        ttlMs: Long = defaultTtlMs,
        fetch: suspend () -> T,
    ): T = mutex.withLock {
        val now = Clock.System.now().toEpochMilliseconds()
        val existing = store[key]
        if (existing != null && existing.expiresAt > now) {
            return@withLock existing.data as T
        }

        val fresh = fetch()
        store[key] = CacheEntry(data = fresh, expiresAt = now + ttlMs)
        fresh
    }

    /**
     * Invalidate a specific cache entry.
     */
    suspend fun invalidate(key: String): Unit = mutex.withLock {
        store.remove(key)
        Unit
    }

    /**
     * Clear all cached entries.
     */
    suspend fun clear(): Unit = mutex.withLock {
        store.clear()
    }
}
