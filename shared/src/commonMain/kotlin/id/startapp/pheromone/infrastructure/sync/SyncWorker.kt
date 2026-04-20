package id.startapp.pheromone.infrastructure.sync

import id.startapp.pheromone.infrastructure.database.dao.CacheDao
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.Clock

/**
 * Platform-agnostic sync orchestrator.
 *
 * Delegates to [SyncEngine] for full push + pull + resolve sync,
 * or falls back to basic cache cleanup if the engine is not available.
 *
 * Called by platform-specific background task implementations
 * (WorkManager on Android, BGTaskScheduler on iOS).
 */
class SyncWorker(
    private val cacheDao: CacheDao,
    private val syncEngine: SyncEngine? = null,
) {

    /**
     * Perform a single sync cycle.
     *
     * If [SyncEngine] is available, delegates to it for full
     * push + pull + conflict resolution. Otherwise, performs
     * basic cache cleanup.
     */
    suspend fun performSync(triggerType: String = "auto"): SyncResult {
        // Delegate to SyncEngine if available
        if (syncEngine != null) {
            return syncEngine.performFullSync(triggerType)
        }

        // Fallback: basic cache cleanup
        val timestamp = Clock.System.now().toEpochMilliseconds()
        try {
            cacheDao.cleanup()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) { }

        return SyncResult(
            updatedCount = 0,
            failedCount = 0,
            timestamp = timestamp,
        )
    }

    companion object {
        /** Cache entries older than this are considered stale. */
        const val CACHE_TTL_MS = 15 * 60 * 1000L // 15 minutes
    }
}

/**
 * Result of a sync cycle.
 *
 * @property updatedCount Number of cache entries successfully refreshed
 * @property failedCount Number of entries that failed to refresh
 * @property timestamp Epoch milliseconds when the sync completed
 */
data class SyncResult(
    val updatedCount: Int,
    val failedCount: Int,
    val timestamp: Long,
)
