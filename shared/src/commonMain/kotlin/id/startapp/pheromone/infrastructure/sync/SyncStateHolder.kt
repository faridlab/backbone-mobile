package id.startapp.pheromone.infrastructure.sync

import id.startapp.pheromone.infrastructure.database.dao.ConflictDao
import id.startapp.pheromone.infrastructure.database.dao.OutboxDao
import id.startapp.pheromone.infrastructure.network.ConnectivityMonitor
import id.startapp.pheromone.infrastructure.network.ConnectivityStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Aggregated sync state observable by ViewModels and UI components.
 *
 * Combines connectivity status, outbox pending count, conflict count,
 * and sync progress into a single [StateFlow] for reactive observation.
 */
class SyncStateHolder(
    private val connectivityMonitor: ConnectivityMonitor,
    private val outboxDao: OutboxDao,
    private val conflictDao: ConflictDao,
) {
    private val _state = MutableStateFlow(SyncState())
    val state: StateFlow<SyncState> = _state.asStateFlow()

    private var isObserving = false

    /**
     * Start observing connectivity changes and refresh counts.
     * Call once from the app root scope after DI initialization.
     * Safe to call multiple times — subsequent calls are no-ops.
     */
    fun startObserving(scope: CoroutineScope) {
        if (isObserving) return
        isObserving = true

        // Observe connectivity changes
        scope.launch {
            connectivityMonitor.status.collect { connectivity ->
                _state.update { it.copy(connectivity = connectivity) }
            }
        }

        // Initial count refresh
        refreshCounts()
    }

    /**
     * Refresh outbox and conflict counts from the database.
     * Call after sync operations or outbox changes.
     */
    fun refreshCounts() {
        try {
            val pendingCount = outboxDao.countPending()
            val conflictCount = conflictDao.countUnresolved()
            _state.update {
                it.copy(
                    pendingCount = pendingCount,
                    conflictCount = conflictCount,
                )
            }
        } catch (_: Exception) {
            // Database may not be initialized yet during startup
        }
    }

    /**
     * Update syncing state (called by SyncEngine during sync).
     */
    fun setSyncing(syncing: Boolean) {
        _state.update { it.copy(isSyncing = syncing) }
    }

    /**
     * Update last sync timestamp (called after successful sync).
     */
    fun setLastSyncAt(timestamp: Long) {
        _state.update { it.copy(lastSyncAt = timestamp) }
    }
}

/**
 * Aggregated sync state for UI consumption.
 */
data class SyncState(
    val connectivity: ConnectivityStatus = ConnectivityStatus.ONLINE,
    val pendingCount: Long = 0,
    val conflictCount: Long = 0,
    val isSyncing: Boolean = false,
    val lastSyncAt: Long? = null,
) {
    val isOffline: Boolean get() = connectivity == ConnectivityStatus.OFFLINE
    val isOnline: Boolean get() = connectivity != ConnectivityStatus.OFFLINE
    val hasPendingChanges: Boolean get() = pendingCount > 0
    val hasConflicts: Boolean get() = conflictCount > 0
}
