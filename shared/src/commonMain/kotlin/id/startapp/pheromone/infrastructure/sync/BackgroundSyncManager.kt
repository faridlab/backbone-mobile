package id.startapp.pheromone.infrastructure.sync

/**
 * Cross-platform background sync manager.
 *
 * Schedules periodic data synchronization in the background to keep
 * local cache up-to-date. Platform implementations use native scheduling APIs.
 *
 * To activate production background sync:
 * - Android: Enable WorkManager in build.gradle.kts dependencies
 * - iOS: Register BGAppRefreshTask in Info.plist and AppDelegate
 */
expect object BackgroundSyncManager {

    /** Whether the manager has been initialized. */
    val isInitialized: Boolean

    /** Whether periodic sync is currently scheduled. */
    val isSyncScheduled: Boolean

    /**
     * Initialize the background sync system.
     *
     * @param isDebug When true, logs operations to console
     */
    fun init(isDebug: Boolean = false)

    /**
     * Schedule periodic background sync.
     *
     * @param intervalMinutes Interval between syncs (minimum 15 on Android/WorkManager)
     */
    fun schedulePeriodicSync(intervalMinutes: Int = DEFAULT_SYNC_INTERVAL_MINUTES)

    /**
     * Cancel any scheduled periodic sync.
     */
    fun cancelPeriodicSync()

    /**
     * Request an immediate one-time sync.
     */
    fun requestImmediateSync()
}

/** Default sync interval: 15 minutes (WorkManager minimum on Android). */
const val DEFAULT_SYNC_INTERVAL_MINUTES = 15
