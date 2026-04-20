package id.startapp.pheromone.infrastructure.database.dao

import id.startapp.pheromone.infrastructure.database.DatabaseManager
import kotlinx.datetime.Clock

/**
 * Data Access Object for the media_cache table.
 *
 * Tracks cached media files (images, attachments) for offline access.
 * Supports LRU eviction and pending upload tracking.
 */
class MediaCacheDao {

    private val queries get() = DatabaseManager.getDatabase().mediaCacheQueriesQueries

    /**
     * Get all media for a specific entity.
     */
    fun getByEntity(entityType: String, entityId: String): List<MediaCacheEntry> {
        return queries.selectByEntity(entityType, entityId).executeAsList().map { it.toMediaCacheEntry() }
    }

    /**
     * Get a media entry by URL.
     */
    fun getByUrl(url: String): MediaCacheEntry? {
        return queries.selectByUrl(url).executeAsOneOrNull()?.toMediaCacheEntry()
    }

    /**
     * Get all pending uploads (photos taken offline).
     */
    fun getPendingUploads(): List<MediaCacheEntry> {
        return queries.selectPendingUploads().executeAsList().map { it.toMediaCacheEntry() }
    }

    /**
     * Get total cache size in bytes.
     */
    fun getTotalSize(): Long {
        return queries.selectTotalSize().executeAsOne()
    }

    /**
     * Get oldest cached entries for LRU eviction (excludes pending uploads).
     */
    fun getOldestForEviction(limit: Long): List<MediaCacheEntry> {
        return queries.selectOldestForEviction(limit).executeAsList().map { it.toMediaCacheEntry() }
    }

    /**
     * Insert or update a media cache entry.
     */
    fun upsert(
        url: String,
        localPath: String,
        sizeBytes: Long,
        entityType: String,
        entityId: String,
        mediaType: String,
        isUploaded: Boolean = true,
    ) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.upsert(
            url = url,
            local_path = localPath,
            size_bytes = sizeBytes,
            cached_at = now,
            entity_type = entityType,
            entity_id = entityId,
            media_type = mediaType,
            is_uploaded = if (isUploaded) 1L else 0L,
        )
    }

    /**
     * Mark a media entry as uploaded (photo synced to server).
     */
    fun markUploaded(localPath: String, serverUrl: String) {
        queries.markUploaded(url = serverUrl, local_path = localPath)
    }

    /**
     * Delete a media entry.
     */
    fun deleteByUrl(url: String) {
        queries.deleteByUrl(url)
    }

    /**
     * Delete all media for an entity.
     */
    fun deleteByEntity(entityType: String, entityId: String) {
        queries.deleteByEntity(entityType, entityId)
    }

    /**
     * Clear all cached media (excluding pending uploads).
     */
    fun clearAll() {
        queries.deleteAll()
    }

    /**
     * Count pending uploads.
     */
    fun countPendingUploads(): Long {
        return queries.countPendingUploads().executeAsOne()
    }
}

/**
 * Media cache entry data class.
 */
data class MediaCacheEntry(
    val url: String,
    val localPath: String,
    val sizeBytes: Long,
    val cachedAt: Long,
    val entityType: String,
    val entityId: String,
    val mediaType: String,
    val isUploaded: Boolean,
)

private fun id.startapp.pheromone.Media_cache.toMediaCacheEntry() = MediaCacheEntry(
    url = url,
    localPath = local_path,
    sizeBytes = size_bytes,
    cachedAt = cached_at,
    entityType = entity_type,
    entityId = entity_id,
    mediaType = media_type,
    isUploaded = is_uploaded == 1L,
)
