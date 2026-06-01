package id.startapp.infrastructure.database.dao

import id.startapp.infrastructure.database.DatabaseManager
import kotlinx.datetime.Clock

/**
 * Data Access Object for the sync_history table.
 *
 * Logs sync operation history for the sync history screen.
 * Each sync cycle (push + pull) is recorded with counts and details.
 */
class SyncHistoryDao {

    private val queries get() = DatabaseManager.getDatabase().syncHistoryQueriesQueries

    /**
     * Start a new sync history entry.
     *
     * @param id Unique sync cycle ID
     * @param triggerType One of: "auto", "manual", "on_connect", "pull_to_refresh"
     */
    fun startSync(id: String, triggerType: String) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.insert(
            id = id,
            started_at = now,
            trigger_type = triggerType,
        )
    }

    /**
     * Complete a sync history entry with results.
     *
     * @param id Sync cycle ID
     * @param totalPushed Number of entries successfully pushed to server
     * @param totalPulled Number of entries pulled from server
     * @param totalFailed Number of failed operations
     * @param totalConflicts Number of conflicts detected
     * @param details JSON string with per-entity breakdown
     * @param status "completed" or "failed"
     */
    fun completeSync(
        id: String,
        totalPushed: Long,
        totalPulled: Long,
        totalFailed: Long,
        totalConflicts: Long,
        details: String?,
        status: String,
    ) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.complete(
            completed_at = now,
            total_pushed = totalPushed,
            total_pulled = totalPulled,
            total_failed = totalFailed,
            total_conflicts = totalConflicts,
            details = details,
            status = status,
            id = id,
        )
    }

    /**
     * Get recent sync history entries.
     */
    fun getRecent(limit: Long = 50): List<SyncHistoryEntry> {
        return queries.selectRecent(limit).executeAsList().map { it.toSyncHistoryEntry() }
    }

    /**
     * Get sync history filtered by trigger type.
     */
    fun getByTrigger(triggerType: String, limit: Long = 50): List<SyncHistoryEntry> {
        return queries.selectByTrigger(triggerType, limit).executeAsList().map { it.toSyncHistoryEntry() }
    }

    /**
     * Get a single sync history entry by ID.
     */
    fun getById(id: String): SyncHistoryEntry? {
        return queries.selectById(id).executeAsOneOrNull()?.toSyncHistoryEntry()
    }

    /**
     * Get the timestamp of the most recent completed sync.
     */
    fun getLastCompletedAt(): Long? {
        return queries.selectLastCompletedAt().executeAsOneOrNull()?.completed_at
    }

    /**
     * Delete old sync history entries (retention cleanup).
     */
    fun deleteOlderThan(olderThanMs: Long) {
        queries.deleteOlderThan(olderThanMs)
    }
}

/**
 * Sync history entry data class for use in the application layer.
 */
data class SyncHistoryEntry(
    val id: String,
    val startedAt: Long,
    val completedAt: Long?,
    val triggerType: String,
    val totalPushed: Long,
    val totalPulled: Long,
    val totalFailed: Long,
    val totalConflicts: Long,
    val details: String?,
    val status: String,
)

/**
 * Extension to convert SQLDelight generated row to SyncHistoryEntry.
 */
private fun id.startapp.Sync_history.toSyncHistoryEntry() = SyncHistoryEntry(
    id = id,
    startedAt = started_at,
    completedAt = completed_at,
    triggerType = trigger_type,
    totalPushed = total_pushed,
    totalPulled = total_pulled,
    totalFailed = total_failed,
    totalConflicts = total_conflicts,
    details = details,
    status = status,
)
