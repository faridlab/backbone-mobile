package id.startapp.pheromone.infrastructure.database.dao

import id.startapp.pheromone.infrastructure.database.DatabaseManager
import kotlinx.datetime.Clock

/**
 * Data Access Object for the sync_outbox table.
 *
 * Manages the offline write queue (outbox pattern).
 * Entries represent local mutations that need to be synced to the server.
 */
class OutboxDao {

    private val queries get() = DatabaseManager.getDatabase().syncOutboxQueriesQueries

    /**
     * Add a new entry to the outbox queue.
     */
    fun addEntry(
        id: String,
        entityType: String,
        entityId: String,
        operation: String,
        payload: String,
        priority: Int,
        deviceId: String,
        version: Long = 1L,
    ) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.insert(
            id = id,
            entity_type = entityType,
            entity_id = entityId,
            operation = operation,
            payload = payload,
            created_at = now,
            priority = priority.toLong(),
            device_id = deviceId,
            version = version,
        )
    }

    /**
     * Get all pending entries ordered by priority (highest first), then creation time.
     */
    fun getPending(): List<OutboxEntry> {
        return queries.selectPending().executeAsList().map { it.toOutboxEntry() }
    }

    /**
     * Get pending entries for a specific entity (causal ordering).
     */
    fun getPendingForEntity(entityType: String, entityId: String): List<OutboxEntry> {
        return queries.selectPendingForEntity(entityType, entityId)
            .executeAsList()
            .map { it.toOutboxEntry() }
    }

    /**
     * Get entries by status.
     */
    fun getByStatus(status: String): List<OutboxEntry> {
        return queries.selectByStatus(status).executeAsList().map { it.toOutboxEntry() }
    }

    /**
     * Update the status of an entry.
     */
    fun updateStatus(id: String, status: String, errorMessage: String? = null) {
        queries.updateStatus(
            status = status,
            error_message = errorMessage,
            id = id,
        )
    }

    /**
     * Mark an entry as synced (successfully pushed to server).
     */
    fun markSynced(id: String) {
        queries.markSynced(id)
    }

    /**
     * Mark an entry as syncing (in progress).
     */
    fun markSyncing(id: String) {
        queries.markSyncing(id)
    }

    /**
     * Increment retry count and reset to pending.
     */
    fun incrementRetry(id: String) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.incrementRetry(last_retry_at = now, id = id)
    }

    /**
     * Count all non-synced entries (pending + syncing + failed) for badge display.
     */
    fun countPending(): Long {
        return queries.countPending().executeAsOne()
    }

    /**
     * Count entries by specific status.
     */
    fun countByStatus(status: String): Long {
        return queries.countByStatus(status).executeAsOne()
    }

    /**
     * Check if an entity has pending outbox entries.
     */
    fun hasPendingForEntity(entityType: String, entityId: String): Boolean {
        return queries.hasPendingForEntity(entityType, entityId).executeAsOne()
    }

    /**
     * Delete all synced entries (cleanup after successful sync).
     */
    fun deleteSynced() {
        queries.deleteSynced()
    }

    /**
     * Delete a specific entry (discard by user).
     */
    fun deleteById(id: String) {
        queries.deleteById(id)
    }

    /**
     * Update entity ID (for local ID replacement after server sync).
     */
    fun updateEntityId(oldEntityId: String, newEntityId: String, entityType: String) {
        queries.updateEntityId(
            entity_id = newEntityId,
            entity_id_ = oldEntityId,
            entity_type = entityType,
        )
    }
}

/**
 * Outbox entry data class for use in the application layer.
 */
data class OutboxEntry(
    val id: String,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val payload: String,
    val createdAt: Long,
    val retryCount: Long,
    val lastRetryAt: Long?,
    val status: String,
    val errorMessage: String?,
    val priority: Long,
    val deviceId: String,
    val version: Long,
)

/**
 * Extension to convert SQLDelight generated row to OutboxEntry.
 */
private fun id.startapp.pheromone.Sync_outbox.toOutboxEntry() = OutboxEntry(
    id = id,
    entityType = entity_type,
    entityId = entity_id,
    operation = operation,
    payload = payload,
    createdAt = created_at,
    retryCount = retry_count,
    lastRetryAt = last_retry_at,
    status = status,
    errorMessage = error_message,
    priority = priority,
    deviceId = device_id,
    version = version,
)
