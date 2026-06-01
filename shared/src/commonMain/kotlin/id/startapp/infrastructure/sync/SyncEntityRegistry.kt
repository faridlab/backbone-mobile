package id.startapp.infrastructure.sync

import id.startapp.domain.types.Result
import id.startapp.infrastructure.pagination.PaginatedApiResponse

/**
 * Handler for syncing a specific entity type.
 *
 * Implementations wrap the API client calls needed for push/pull operations.
 */
interface SyncEntityHandler {

    /** Entity type string (e.g., "orders", "customers") */
    val entityType: String

    /**
     * Push a single outbox entry to the server.
     *
     * @param operation "CREATE", "UPDATE", "DELETE", "TRANSITION"
     * @param entityId Entity ID (may be local UUID for creates)
     * @param payload Serialized JSON of the mutation
     * @return Result with the server response (may contain new ID for creates)
     */
    suspend fun push(operation: String, entityId: String, payload: String): Result<PushResult>

    /**
     * Pull changes from server since a given timestamp.
     *
     * @param sinceMs Epoch millis of last sync
     * @param page Page number for pagination
     * @param excludeDevice Device ID to exclude from results (avoid re-downloading own changes)
     * @return Paginated response of updated entities as JSON strings
     */
    suspend fun pull(sinceMs: Long, page: Int, excludeDevice: String? = null): Result<PullResult>
}

/**
 * Result of pushing a single outbox entry.
 */
data class PushResult(
    /** Server-assigned ID (for CREATE operations, may differ from local ID) */
    val serverId: String? = null,
    /** Server version after the operation */
    val serverVersion: Long = 1,
)

/**
 * Result of pulling changes for an entity type.
 */
data class PullResult(
    /** Number of entities upserted into local cache */
    val upsertedCount: Int = 0,
    /** Whether there are more pages to fetch */
    val hasMore: Boolean = false,
)

/**
 * Registry of entity type handlers for the sync engine.
 *
 * Each entity type that supports offline sync must register a handler.
 * The SyncEngine uses this to dispatch push/pull operations.
 */
class SyncEntityRegistry {

    private val handlers = mutableMapOf<String, SyncEntityHandler>()

    /**
     * Register a handler for an entity type.
     */
    fun register(handler: SyncEntityHandler) {
        handlers[handler.entityType] = handler
    }

    /**
     * Get the handler for an entity type.
     */
    fun getHandler(entityType: String): SyncEntityHandler? = handlers[entityType]

    /**
     * Get all registered entity types.
     */
    fun getEntityTypes(): Set<String> = handlers.keys.toSet()

    /**
     * Check if a handler is registered for an entity type.
     */
    fun hasHandler(entityType: String): Boolean = entityType in handlers
}
