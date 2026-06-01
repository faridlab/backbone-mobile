package id.startapp.infrastructure.database.dao

import id.startapp.infrastructure.database.DatabaseManager
import kotlinx.datetime.Clock

/**
 * Data Access Object for the sync_conflicts table.
 *
 * Manages sync conflicts that require user resolution.
 * Conflicts occur when the same entity is modified both locally
 * and on the server by another user/device.
 */
class ConflictDao {

    private val queries get() = DatabaseManager.getDatabase().syncConflictQueriesQueries

    /**
     * Add a new conflict for user resolution.
     */
    fun addConflict(
        id: String,
        entityType: String,
        entityId: String,
        localData: String,
        serverData: String,
        localVersion: Long,
        serverVersion: Long,
    ) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.insert(
            id = id,
            entity_type = entityType,
            entity_id = entityId,
            local_data = localData,
            server_data = serverData,
            local_version = localVersion,
            server_version = serverVersion,
            detected_at = now,
        )
    }

    /**
     * Get all unresolved conflicts.
     */
    fun getUnresolved(): List<ConflictEntry> {
        return queries.selectUnresolved().executeAsList().map { it.toConflictEntry() }
    }

    /**
     * Get unresolved conflict for a specific entity.
     */
    fun getUnresolvedForEntity(entityType: String, entityId: String): ConflictEntry? {
        return queries.selectUnresolvedForEntity(entityType, entityId)
            .executeAsOneOrNull()
            ?.toConflictEntry()
    }

    /**
     * Get a single conflict by ID.
     */
    fun getById(id: String): ConflictEntry? {
        return queries.selectById(id).executeAsOneOrNull()?.toConflictEntry()
    }

    /**
     * Count unresolved conflicts (for badge display).
     */
    fun countUnresolved(): Long {
        return queries.countUnresolved().executeAsOne()
    }

    /**
     * Resolve a conflict with the chosen resolution.
     *
     * @param id Conflict ID
     * @param resolution One of: "keep_local", "keep_server", "merged"
     */
    fun resolve(id: String, resolution: String) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.resolve(
            resolved_at = now,
            resolution = resolution,
            id = id,
        )
    }

    /**
     * Get recently resolved conflicts (for history).
     */
    fun getResolved(limit: Long = 50): List<ConflictEntry> {
        return queries.selectResolved(limit).executeAsList().map {
            ConflictEntry(
                id = it.id,
                entityType = it.entity_type,
                entityId = it.entity_id,
                localData = it.local_data,
                serverData = it.server_data,
                localVersion = it.local_version,
                serverVersion = it.server_version,
                detectedAt = it.detected_at,
                resolvedAt = it.resolved_at,
                resolution = it.resolution,
            )
        }
    }

    /**
     * Delete old resolved conflicts (retention cleanup).
     */
    fun deleteOldResolved(olderThanMs: Long) {
        queries.deleteOldResolved(olderThanMs)
    }
}

/**
 * Conflict entry data class for use in the application layer.
 */
data class ConflictEntry(
    val id: String,
    val entityType: String,
    val entityId: String,
    val localData: String,
    val serverData: String,
    val localVersion: Long,
    val serverVersion: Long,
    val detectedAt: Long,
    val resolvedAt: Long?,
    val resolution: String?,
)

/**
 * Extension to convert SQLDelight generated row to ConflictEntry.
 */
private fun id.startapp.Sync_conflicts.toConflictEntry() = ConflictEntry(
    id = id,
    entityType = entity_type,
    entityId = entity_id,
    localData = local_data,
    serverData = server_data,
    localVersion = local_version,
    serverVersion = server_version,
    detectedAt = detected_at,
    resolvedAt = resolved_at,
    resolution = resolution,
)
