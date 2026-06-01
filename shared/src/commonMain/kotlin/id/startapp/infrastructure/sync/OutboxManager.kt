package id.startapp.infrastructure.sync

import id.startapp.infrastructure.database.dao.OutboxDao
import id.startapp.infrastructure.database.dao.OutboxEntry
import id.startapp.infrastructure.events.AppEvent
import id.startapp.infrastructure.events.AppEventBus
import id.startapp.infrastructure.network.ConnectivityMonitor
import id.startapp.infrastructure.network.ConnectivityStatus
import id.startapp.infrastructure.storage.KeyValueStorage
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Manages the offline write queue (outbox pattern).
 *
 * Enqueues local mutations for deferred sync to the server.
 * When online, triggers immediate processing via [SyncEngine].
 */
class OutboxManager(
    private val outboxDao: OutboxDao,
    private val connectivityMonitor: ConnectivityMonitor,
    private val keyValueStorage: KeyValueStorage,
    private val appEventBus: AppEventBus,
    private val syncStateHolder: SyncStateHolder,
) {
    companion object {
        const val KEY_DEVICE_ID = "device_id"

        // Sync priorities (lower = higher priority)
        const val PRIORITY_ORDER_STATUS = 0
        const val PRIORITY_PAYMENT = 1
        const val PRIORITY_ORDER = 2
        const val PRIORITY_CUSTOMER = 3
        const val PRIORITY_ITEM = 4
        const val PRIORITY_MEDIA = 5
        const val PRIORITY_REFERENCE = 9
    }

    /**
     * Enqueue a mutation for deferred sync.
     *
     * @param entityType Entity type (e.g., "orders", "customers")
     * @param entityId Entity ID (local UUID for creates, server ID for updates)
     * @param operation Operation type: "CREATE", "UPDATE", "DELETE", "TRANSITION"
     * @param payload Serialized JSON of the mutation request
     * @param priority Sync priority (0 = highest)
     */
    @OptIn(ExperimentalUuidApi::class)
    suspend fun enqueue(
        entityType: String,
        entityId: String,
        operation: String,
        payload: String,
        priority: Int = PRIORITY_REFERENCE,
    ) {
        val id = Uuid.random().toString()
        val deviceId = getOrCreateDeviceId()

        outboxDao.addEntry(
            id = id,
            entityType = entityType,
            entityId = entityId,
            operation = operation,
            payload = payload,
            priority = priority,
            deviceId = deviceId,
        )

        val pendingCount = outboxDao.countPending()
        syncStateHolder.refreshCounts()
        appEventBus.tryEmit(AppEvent.OutboxUpdated(pendingCount))
    }

    /**
     * Get all pending entries ordered by priority.
     */
    fun getPending(): List<OutboxEntry> = outboxDao.getPending()

    /**
     * Get entries by status.
     */
    fun getByStatus(status: String): List<OutboxEntry> = outboxDao.getByStatus(status)

    /**
     * Mark an entry as syncing (in progress).
     */
    fun markSyncing(id: String) = outboxDao.markSyncing(id)

    /**
     * Mark an entry as successfully synced.
     */
    fun markSynced(id: String) {
        outboxDao.markSynced(id)
        syncStateHolder.refreshCounts()
    }

    /**
     * Mark an entry as failed with error message.
     */
    fun markFailed(id: String, errorMessage: String) {
        outboxDao.updateStatus(id, "failed", errorMessage)
        syncStateHolder.refreshCounts()
    }

    /**
     * Mark an entry as conflict (moved to sync_conflicts table).
     */
    fun markConflict(id: String) {
        outboxDao.updateStatus(id, "conflict", "Version conflict detected")
        syncStateHolder.refreshCounts()
    }

    /**
     * Increment retry count and reset to pending for next attempt.
     */
    fun incrementRetry(id: String) = outboxDao.incrementRetry(id)

    /**
     * Discard a specific entry (user action).
     */
    fun discard(id: String) {
        outboxDao.deleteById(id)
        syncStateHolder.refreshCounts()
    }

    /**
     * Discard all failed entries.
     */
    fun discardAllFailed() {
        val failed = outboxDao.getByStatus("failed")
        for (entry in failed) {
            outboxDao.deleteById(entry.id)
        }
        syncStateHolder.refreshCounts()
    }

    /**
     * Retry all failed entries by resetting them to pending.
     */
    fun retryAllFailed() {
        val failed = outboxDao.getByStatus("failed")
        for (entry in failed) {
            outboxDao.incrementRetry(entry.id)
        }
        syncStateHolder.refreshCounts()
    }

    /**
     * Clean up all synced entries.
     */
    fun cleanupSynced() {
        outboxDao.deleteSynced()
    }

    /**
     * Replace local entity ID with server-assigned ID after CREATE sync.
     */
    fun replaceEntityId(oldId: String, newId: String, entityType: String) {
        outboxDao.updateEntityId(oldId, newId, entityType)
    }

    /**
     * Check if currently online and sync should be attempted.
     */
    fun isOnline(): Boolean =
        connectivityMonitor.status.value != ConnectivityStatus.OFFLINE

    /**
     * Get the persistent device ID (creates one if needed).
     * Public for use by app initialization (device identity headers).
     */
    suspend fun getDeviceId(): String = getOrCreateDeviceId()

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun getOrCreateDeviceId(): String {
        val existing = keyValueStorage.getString(KEY_DEVICE_ID)
        if (existing != null) return existing

        val newId = Uuid.random().toString()
        keyValueStorage.putString(KEY_DEVICE_ID, newId)
        return newId
    }
}
