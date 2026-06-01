package id.startapp.infrastructure.media

import id.startapp.infrastructure.database.dao.MediaCacheDao
import id.startapp.infrastructure.database.dao.MediaCacheEntry
import id.startapp.infrastructure.storage.KeyValueStorage

/**
 * Manages media file caching with configurable levels and LRU eviction.
 *
 * Features:
 * - Configurable cache levels (none / thumbnails / full)
 * - LRU eviction when storage quota exceeded
 * - Pending upload tracking for offline photos
 * - Cache size reporting
 */
class MediaCacheManager(
    private val mediaCacheDao: MediaCacheDao,
    private val keyValueStorage: KeyValueStorage,
) {
    companion object {
        const val KEY_CACHE_LEVEL = "media_cache_level"
        const val DEFAULT_MAX_CACHE_BYTES = 500L * 1024 * 1024 // 500 MB
        const val EVICTION_BATCH_SIZE = 20L
    }

    /**
     * Get the current cache level setting.
     */
    suspend fun getCacheLevel(): MediaCacheLevel {
        val value = keyValueStorage.getString(KEY_CACHE_LEVEL) ?: "thumbnails"
        return MediaCacheLevel.fromString(value)
    }

    /**
     * Set the cache level.
     */
    suspend fun setCacheLevel(level: MediaCacheLevel) {
        keyValueStorage.putString(KEY_CACHE_LEVEL, level.value)
    }

    /**
     * Record a cached media file.
     */
    fun cacheMedia(
        url: String,
        localPath: String,
        sizeBytes: Long,
        entityType: String,
        entityId: String,
        mediaType: String,
    ) {
        mediaCacheDao.upsert(
            url = url,
            localPath = localPath,
            sizeBytes = sizeBytes,
            entityType = entityType,
            entityId = entityId,
            mediaType = mediaType,
            isUploaded = true,
        )
        evictIfOverQuota()
    }

    /**
     * Record an offline photo capture (pending upload).
     */
    fun recordOfflinePhoto(
        localPath: String,
        sizeBytes: Long,
        entityType: String,
        entityId: String,
    ) {
        mediaCacheDao.upsert(
            url = "local://$localPath",
            localPath = localPath,
            sizeBytes = sizeBytes,
            entityType = entityType,
            entityId = entityId,
            mediaType = "photo",
            isUploaded = false,
        )
    }

    /**
     * Mark a photo as uploaded and update its server URL.
     */
    fun markPhotoUploaded(localPath: String, serverUrl: String) {
        mediaCacheDao.markUploaded(localPath, serverUrl)
    }

    /**
     * Get all pending photo uploads.
     */
    fun getPendingUploads(): List<MediaCacheEntry> {
        return mediaCacheDao.getPendingUploads()
    }

    /**
     * Get media for an entity.
     */
    fun getMediaForEntity(entityType: String, entityId: String): List<MediaCacheEntry> {
        return mediaCacheDao.getByEntity(entityType, entityId)
    }

    /**
     * Get total cache size in bytes.
     */
    fun getTotalCacheSize(): Long {
        return mediaCacheDao.getTotalSize()
    }

    /**
     * Get formatted cache size string.
     */
    fun getFormattedCacheSize(): String {
        val bytes = getTotalCacheSize()
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }

    /**
     * Count pending uploads.
     */
    fun countPendingUploads(): Long {
        return mediaCacheDao.countPendingUploads()
    }

    /**
     * Clear all cached media (preserves pending uploads).
     */
    fun clearCache() {
        mediaCacheDao.clearAll()
    }

    /**
     * Evict oldest entries if cache exceeds quota.
     * Pending uploads are never evicted.
     */
    private fun evictIfOverQuota() {
        var currentSize = mediaCacheDao.getTotalSize()
        if (currentSize <= DEFAULT_MAX_CACHE_BYTES) return

        // Evict in batches until under quota or no more evictable entries
        var rounds = 0
        val maxRounds = 10
        while (currentSize > DEFAULT_MAX_CACHE_BYTES && rounds < maxRounds) {
            val toEvict = mediaCacheDao.getOldestForEviction(EVICTION_BATCH_SIZE)
            if (toEvict.isEmpty()) break

            for (entry in toEvict) {
                mediaCacheDao.deleteByUrl(entry.url)
            }
            currentSize = mediaCacheDao.getTotalSize()
            rounds++
        }
    }
}

/**
 * Media cache level configuration.
 */
enum class MediaCacheLevel(val value: String) {
    /** No media downloaded; show placeholders offline */
    NONE("none"),
    /** Download thumbnails only (~20 KB each) */
    THUMBNAILS("thumbnails"),
    /** Download original photos (~200 KB - 2 MB each) */
    FULL("full");

    companion object {
        fun fromString(value: String): MediaCacheLevel = when (value) {
            "none" -> NONE
            "full" -> FULL
            else -> THUMBNAILS
        }
    }
}
