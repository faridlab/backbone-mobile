package id.startapp.pheromone.infrastructure.sync

import id.startapp.pheromone.domain.types.NetworkError
import id.startapp.pheromone.domain.types.Result
import id.startapp.pheromone.infrastructure.database.dao.ConflictDao
import id.startapp.pheromone.infrastructure.database.dao.OutboxEntry
import id.startapp.pheromone.infrastructure.database.dao.SyncHistoryDao
import id.startapp.pheromone.infrastructure.events.AppEvent
import id.startapp.pheromone.infrastructure.events.AppEventBus
import id.startapp.pheromone.infrastructure.storage.KeyValueStorage
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Full sync orchestrator: PUSH outbox -> PULL delta -> RESOLVE conflicts.
 *
 * Called by [SyncWorker] during background sync, on-connect sync,
 * or manual "Sinkronkan Sekarang" triggers.
 */
class SyncEngine(
    private val outboxManager: OutboxManager,
    private val conflictDao: ConflictDao,
    private val syncHistoryDao: SyncHistoryDao,
    private val entityRegistry: SyncEntityRegistry,
    private val keyValueStorage: KeyValueStorage,
    private val syncStateHolder: SyncStateHolder,
    private val appEventBus: AppEventBus,
) {
    companion object {
        private const val MAX_RETRIES = 5
        private const val LAST_SYNC_PREFIX = "last_sync_"
    }

    /**
     * Perform a full sync cycle: push + pull + resolve.
     *
     * @param triggerType Trigger source for history logging
     * @return Aggregated sync result
     */
    @OptIn(ExperimentalUuidApi::class)
    suspend fun performFullSync(triggerType: String = "auto"): SyncResult {
        val historyId = Uuid.random().toString()
        syncHistoryDao.startSync(historyId, triggerType)
        syncStateHolder.setSyncing(true)

        var totalPushed = 0
        var totalPulled = 0
        var totalFailed = 0
        var totalConflicts = 0
        val idMapper = LocalIdMapper()

        try {
            // Phase 1: PUSH - Process outbox entries
            val pushResult = pushOutbox(idMapper)
            totalPushed = pushResult.pushed
            totalFailed += pushResult.failed
            totalConflicts += pushResult.conflicts

            // Phase 2: PULL - Fetch remote changes
            val pullResult = pullRemoteChanges()
            totalPulled = pullResult.pulled
            totalFailed += pullResult.failed

            // Phase 3: RESOLVE - Update conflict count
            syncStateHolder.refreshCounts()

            // Cleanup synced entries and old records
            outboxManager.cleanupSynced()
            cleanupOldRecords()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            totalFailed++
        } finally {
            syncStateHolder.setSyncing(false)
            idMapper.clear()
        }

        val now = Clock.System.now().toEpochMilliseconds()
        syncStateHolder.setLastSyncAt(now)

        syncHistoryDao.completeSync(
            id = historyId,
            totalPushed = totalPushed.toLong(),
            totalPulled = totalPulled.toLong(),
            totalFailed = totalFailed.toLong(),
            totalConflicts = totalConflicts.toLong(),
            details = null,
            status = if (totalFailed == 0) "completed" else "completed_with_errors",
        )

        val result = SyncResult(
            updatedCount = totalPushed + totalPulled,
            failedCount = totalFailed,
            timestamp = now,
        )

        appEventBus.tryEmit(
            AppEvent.SyncCompleted(
                pushed = totalPushed,
                pulled = totalPulled,
                failed = totalFailed,
                conflicts = totalConflicts,
            )
        )

        return result
    }

    /**
     * PUSH phase: Process all pending outbox entries.
     */
    @OptIn(ExperimentalUuidApi::class)
    private suspend fun pushOutbox(idMapper: LocalIdMapper): PushPhaseResult {
        val pending = outboxManager.getPending()
        var pushed = 0
        var failed = 0
        var conflicts = 0

        for (entry in pending) {
            // Skip entries that exceeded max retries
            if (entry.retryCount >= MAX_RETRIES) {
                outboxManager.markFailed(entry.id, "Max retries ($MAX_RETRIES) exceeded")
                failed++
                continue
            }

            val handler = entityRegistry.getHandler(entry.entityType)
            if (handler == null) {
                outboxManager.markFailed(entry.id, "No sync handler for ${entry.entityType}")
                failed++
                continue
            }

            outboxManager.markSyncing(entry.id)

            // Resolve entity ID in case it was a local UUID that got mapped
            val resolvedEntityId = idMapper.resolve(entry.entityId)

            when (val result = handler.push(entry.operation, resolvedEntityId, entry.payload)) {
                is Result.Success -> {
                    outboxManager.markSynced(entry.id)
                    pushed++

                    // Track local-to-server ID mapping for CREATEs
                    val serverId = result.data.serverId
                    if (entry.operation == "CREATE" && serverId != null && serverId != entry.entityId) {
                        idMapper.record(entry.entityId, serverId)
                        outboxManager.replaceEntityId(entry.entityId, serverId, entry.entityType)
                    }
                }
                is Result.Error -> {
                    handlePushError(entry, result.error, idMapper)
                    when (result.error) {
                        is NetworkError.ConflictError -> conflicts++
                        else -> failed++
                    }
                }
            }
        }

        return PushPhaseResult(pushed, failed, conflicts)
    }

    /**
     * Handle a push error based on the error type.
     */
    @OptIn(ExperimentalUuidApi::class)
    private fun handlePushError(entry: OutboxEntry, error: NetworkError, idMapper: LocalIdMapper) {
        when (error) {
            is NetworkError.ConflictError -> {
                // 409 Conflict: store in sync_conflicts table
                try {
                    conflictDao.addConflict(
                        id = Uuid.random().toString(),
                        entityType = entry.entityType,
                        entityId = idMapper.resolve(entry.entityId),
                        localData = entry.payload,
                        serverData = error.errorBody ?: "{}",
                        localVersion = entry.version,
                        serverVersion = entry.version + 1,
                    )
                    outboxManager.markConflict(entry.id)
                    appEventBus.tryEmit(AppEvent.ConflictDetected(entry.entityType, entry.entityId))
                } catch (_: Exception) {
                    // Conflict storage failed — mark as failed so user can retry
                    outboxManager.markFailed(entry.id, "Conflict detected but failed to record for resolution")
                }
            }
            is NetworkError.ConnectivityError -> {
                // No network: leave as pending, will retry next cycle
                outboxManager.incrementRetry(entry.id)
            }
            is NetworkError.ServerError,
            is NetworkError.TimeoutError -> {
                // Retryable server/timeout errors
                if (entry.retryCount < MAX_RETRIES - 1) {
                    outboxManager.incrementRetry(entry.id)
                } else {
                    outboxManager.markFailed(entry.id, error.message ?: "Server error after $MAX_RETRIES retries")
                }
            }
            is NetworkError.ValidationError,
            is NetworkError.Forbidden,
            is NetworkError.NotFound -> {
                // Non-retryable: mark as failed immediately
                outboxManager.markFailed(entry.id, error.message ?: "Non-retryable error")
            }
            is NetworkError.Unauthorized -> {
                // Auth issue: mark failed, user needs to re-login
                outboxManager.markFailed(entry.id, "Unauthorized — please re-login")
            }
            else -> {
                // Unknown errors: mark failed (don't retry blindly)
                outboxManager.markFailed(entry.id, error.message ?: "Unknown sync error")
            }
        }
    }

    /**
     * Cleanup old resolved conflicts and sync history (retention policy).
     */
    private fun cleanupOldRecords() {
        try {
            val thirtyDaysAgo = Clock.System.now().toEpochMilliseconds() - (30L * 24 * 60 * 60 * 1000)
            conflictDao.deleteOldResolved(thirtyDaysAgo)

            val ninetyDaysAgo = Clock.System.now().toEpochMilliseconds() - (90L * 24 * 60 * 60 * 1000)
            syncHistoryDao.deleteOlderThan(ninetyDaysAgo)
        } catch (_: Exception) {
            // Cleanup failure is non-fatal
        }
    }

    /**
     * PULL phase: Fetch remote changes via delta sync.
     * Passes the current device ID to exclude own changes from results.
     */
    private suspend fun pullRemoteChanges(): PullPhaseResult {
        var totalPulled = 0
        var totalFailed = 0
        val excludeDevice = outboxManager.getDeviceId()

        for (entityType in entityRegistry.getEntityTypes()) {
            val handler = entityRegistry.getHandler(entityType) ?: continue
            val lastSyncKey = "$LAST_SYNC_PREFIX$entityType"
            val lastSync = keyValueStorage.getLong(lastSyncKey) ?: 0L

            try {
                var page = 1
                var hasMore = true
                var pullSucceeded = true
                val maxPages = 100

                while (hasMore && page <= maxPages) {
                    when (val result = handler.pull(lastSync, page, excludeDevice)) {
                        is Result.Success -> {
                            totalPulled += result.data.upsertedCount
                            hasMore = result.data.hasMore
                            page++
                        }
                        is Result.Error -> {
                            totalFailed++
                            hasMore = false
                            pullSucceeded = false
                        }
                    }
                }

                // Only update timestamp when all pages fetched successfully
                if (pullSucceeded) {
                    val now = Clock.System.now().toEpochMilliseconds()
                    keyValueStorage.putLong(lastSyncKey, now)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                totalFailed++
            }
        }

        return PullPhaseResult(totalPulled, totalFailed)
    }
}

private data class PushPhaseResult(val pushed: Int, val failed: Int, val conflicts: Int)
private data class PullPhaseResult(val pulled: Int, val failed: Int)
