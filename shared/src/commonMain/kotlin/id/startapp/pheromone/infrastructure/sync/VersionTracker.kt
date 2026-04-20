package id.startapp.pheromone.infrastructure.sync

import id.startapp.pheromone.infrastructure.database.dao.CacheDao

/**
 * Tracks entity versions for optimistic locking.
 *
 * When a read returns data, the version is stored alongside the cache.
 * When a write is enqueued, the stored version is attached to the
 * outbox entry so the server can validate with 409 Conflict.
 *
 * Version storage uses the entity_cache table with a special key prefix.
 */
class VersionTracker(
    private val cacheDao: CacheDao,
) {
    companion object {
        private const val VERSION_KEY_PREFIX = "_version:"
    }

    /**
     * Store the version for an entity.
     */
    fun setVersion(entityType: String, entityId: String, version: Long) {
        val key = "$VERSION_KEY_PREFIX$entityType:$entityId"
        cacheDao.put(key, version.toString(), Long.MAX_VALUE) // Never expires
    }

    /**
     * Get the stored version for an entity.
     * Returns 0 if no version is tracked.
     */
    fun getVersion(entityType: String, entityId: String): Long {
        val key = "$VERSION_KEY_PREFIX$entityType:$entityId"
        return cacheDao.getIfValid(key)?.toLongOrNull() ?: 0L
    }

    /**
     * Remove version tracking for an entity (e.g., after deletion).
     */
    fun removeVersion(entityType: String, entityId: String) {
        val key = "$VERSION_KEY_PREFIX$entityType:$entityId"
        cacheDao.remove(key)
    }
}
