package id.startapp.infrastructure.sync

/**
 * Tracks local ID -> server ID mappings during a single sync cycle.
 *
 * When a CREATE operation syncs to the server, the server assigns
 * a real ID that may differ from the optimistic local UUID.
 * This mapper tracks those replacements so dependent outbox entries
 * can be updated with the correct server ID.
 *
 * Thread safety: This class is created per sync cycle and used within
 * a single coroutine in [SyncEngine.pushOutbox]. Not shared across threads.
 */
class LocalIdMapper {

    private val mappings = mutableMapOf<String, String>()

    /**
     * Record a local-to-server ID mapping.
     */
    fun record(localId: String, serverId: String) {
        mappings[localId] = serverId
    }

    /**
     * Resolve a potentially local ID to its server ID.
     * Returns the server ID if mapped, or the original ID if not.
     */
    fun resolve(id: String): String = mappings[id] ?: id

    /**
     * Check if a mapping exists for the given local ID.
     */
    fun hasMapping(localId: String): Boolean = localId in mappings

    /**
     * Get all recorded mappings.
     */
    fun getAll(): Map<String, String> = mappings.toMap()

    /**
     * Clear all mappings (call after sync cycle completes).
     */
    fun clear() {
        mappings.clear()
    }
}
