package id.startapp.infrastructure.repository

import id.startapp.domain.types.Result
import id.startapp.infrastructure.cache.inWholeMillisecondsLong
import id.startapp.infrastructure.database.dao.CacheDao
import id.startapp.infrastructure.network.ConnectivityMonitor
import id.startapp.infrastructure.network.ConnectivityStatus
import id.startapp.infrastructure.pagination.PaginatedApiResponse
import kotlinx.serialization.json.Json
import kotlin.time.Duration

/**
 * Abstract cache-first repository for offline-first data access.
 *
 * Generalizes the pattern from [CachedServiceCategoryRepository]:
 * 1. Return valid cache immediately if available
 * 2. Fetch from network on cache miss or expiry
 * 3. Fall back to stale cache if network fails (offline support)
 * 4. Cache network responses for future use
 *
 * Subclasses provide:
 * - [entityType] for cache key namespacing
 * - [ttl] for cache expiry duration
 * - [fetchOneFromApi] / [fetchListFromApi] for network calls
 * - [serializeOne] / [deserializeOne] / [serializeList] / [deserializeList] for JSON
 */
abstract class OfflineFirstRepository<T>(
    protected val entityType: String,
    protected val ttl: Duration,
    protected val cacheDao: CacheDao,
    protected val connectivityMonitor: ConnectivityMonitor,
    protected val json: Json = Json { ignoreUnknownKeys = true },
) {
    // -- Template methods for subclasses --

    protected abstract suspend fun fetchOneFromApi(id: String): Result<T>

    protected abstract suspend fun fetchListFromApi(
        page: Int,
        limit: Int,
        sortBy: String,
        sortDesc: Boolean,
    ): Result<PaginatedApiResponse<T>>

    protected abstract fun serializeOne(item: T): String
    protected abstract fun deserializeOne(jsonStr: String): T
    protected abstract fun serializeList(response: PaginatedApiResponse<T>): String
    protected abstract fun deserializeList(jsonStr: String): PaginatedApiResponse<T>

    // -- Cache-first read operations --

    /**
     * Get a single entity by ID with cache-first strategy.
     */
    suspend fun getById(id: String): Result<T> {
        val cacheKey = itemCacheKey(id)

        // 1. Check valid cache
        val cached = cacheDao.getIfValid(cacheKey)
        if (cached != null) {
            return try {
                Result.Success(deserializeOne(cached))
            } catch (_: Exception) {
                cacheDao.remove(cacheKey)
                fetchByIdAndCache(cacheKey, id)
            }
        }

        // 2. Fetch from network (or fall back to stale cache)
        return fetchByIdAndCache(cacheKey, id)
    }

    /**
     * Get paginated list with cache-first strategy.
     */
    suspend fun getAll(
        page: Int = 1,
        limit: Int = 20,
        sortBy: String = "created_at",
        sortDesc: Boolean = true,
    ): Result<PaginatedApiResponse<T>> {
        val cacheKey = listCacheKey(page, limit, sortBy, sortDesc)

        val cached = cacheDao.getIfValid(cacheKey)
        if (cached != null) {
            return try {
                Result.Success(deserializeList(cached))
            } catch (_: Exception) {
                cacheDao.remove(cacheKey)
                fetchListAndCache(cacheKey, page, limit, sortBy, sortDesc)
            }
        }

        return fetchListAndCache(cacheKey, page, limit, sortBy, sortDesc)
    }

    /**
     * Force refresh from network, bypassing cache.
     */
    suspend fun refresh(
        page: Int = 1,
        limit: Int = 20,
        sortBy: String = "created_at",
        sortDesc: Boolean = true,
    ): Result<PaginatedApiResponse<T>> {
        val cacheKey = listCacheKey(page, limit, sortBy, sortDesc)
        cacheDao.remove(cacheKey)
        return fetchListAndCache(cacheKey, page, limit, sortBy, sortDesc)
    }

    /**
     * Force refresh a single entity by ID.
     */
    suspend fun refreshById(id: String): Result<T> {
        val cacheKey = itemCacheKey(id)
        cacheDao.remove(cacheKey)
        return fetchByIdAndCache(cacheKey, id)
    }

    /**
     * Invalidate a single cached entity by ID.
     */
    fun invalidateOne(id: String) {
        try { cacheDao.remove(itemCacheKey(id)) } catch (_: Exception) { }
    }

    /**
     * Invalidate all cached data for this entity type.
     */
    fun invalidateAll() {
        cacheDao.cleanup()
    }

    /**
     * Check if the current connectivity is offline.
     */
    fun isOffline(): Boolean {
        return connectivityMonitor.status.value == ConnectivityStatus.OFFLINE
    }

    /**
     * Cache a single entity directly (used by InitialSyncManager).
     */
    fun cacheOne(id: String, item: T) {
        try {
            cacheDao.put(itemCacheKey(id), serializeOne(item), ttl.inWholeMillisecondsLong)
        } catch (_: Exception) { }
    }

    /**
     * Cache a paginated list directly (used by InitialSyncManager).
     */
    fun cacheList(page: Int, limit: Int, sortBy: String, sortDesc: Boolean, response: PaginatedApiResponse<T>) {
        try {
            cacheDao.put(listCacheKey(page, limit, sortBy, sortDesc), serializeList(response), ttl.inWholeMillisecondsLong)
        } catch (_: Exception) { }
    }

    // -- Internal helpers --

    protected fun itemCacheKey(id: String): String = "$entityType:item:$id"

    protected fun listCacheKey(page: Int, limit: Int, sortBy: String, sortDesc: Boolean): String =
        "$entityType:list:$page:$limit:$sortBy:$sortDesc"

    private suspend fun fetchByIdAndCache(cacheKey: String, id: String): Result<T> {
        return when (val result = fetchOneFromApi(id)) {
            is Result.Success -> {
                try {
                    cacheDao.put(cacheKey, serializeOne(result.data), ttl.inWholeMillisecondsLong)
                } catch (_: Exception) { }
                result
            }
            is Result.Error -> {
                // Fall back to stale cache for offline support
                val stale = cacheDao.getAny(cacheKey)
                if (stale != null) {
                    try {
                        Result.Success(deserializeOne(stale.jsonData))
                    } catch (_: Exception) { result }
                } else {
                    result
                }
            }
        }
    }

    private suspend fun fetchListAndCache(
        cacheKey: String,
        page: Int,
        limit: Int,
        sortBy: String,
        sortDesc: Boolean,
    ): Result<PaginatedApiResponse<T>> {
        return when (val result = fetchListFromApi(page, limit, sortBy, sortDesc)) {
            is Result.Success -> {
                try {
                    cacheDao.put(cacheKey, serializeList(result.data), ttl.inWholeMillisecondsLong)
                } catch (_: Exception) { }
                result
            }
            is Result.Error -> {
                val stale = cacheDao.getAny(cacheKey)
                if (stale != null) {
                    try {
                        Result.Success(deserializeList(stale.jsonData))
                    } catch (_: Exception) { result }
                } else {
                    result
                }
            }
        }
    }
}
