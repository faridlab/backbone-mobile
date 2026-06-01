package id.startapp.infrastructure.sync

/**
 * iOS implementation of BackgroundSyncManager.
 *
 * Logs sync operations to console in development.
 * To activate production background sync:
 * 1. Add "Permitted background task scheduler identifiers" to Info.plist:
 *    id.startapp.sync.refresh
 * 2. Register task in AppDelegate didFinishLaunchingWithOptions:
 *    BGTaskScheduler.shared.register(forTaskWithIdentifier: "id.startapp.sync.refresh")
 * 3. Replace println calls with BGTaskScheduler API calls:
 *    - BGAppRefreshTaskRequest for periodic sync
 *    - BGProcessingTaskRequest for longer sync operations
 */
actual object BackgroundSyncManager {

    private var _isInitialized: Boolean = false
    actual val isInitialized: Boolean get() = _isInitialized

    actual val isSyncScheduled: Boolean
        get() = _isSyncScheduled

    private var _isSyncScheduled: Boolean = false
    private var debugMode: Boolean = false

    actual fun init(isDebug: Boolean) {
        if (isInitialized) return
        debugMode = isDebug

        // Production: BGTaskScheduler registration happens in Swift AppDelegate
        // BGTaskScheduler.shared.register(
        //     forTaskWithIdentifier: "id.startapp.sync.refresh",
        //     using: nil
        // ) { task in
        //     self.handleAppRefresh(task: task as! BGAppRefreshTask)
        // }

        _isInitialized = true
        if (isDebug) {
            println("BackgroundSyncManager: Initialized (iOS)")
        }
    }

    actual fun schedulePeriodicSync(intervalMinutes: Int) {
        if (!isInitialized) return

        // Production: Schedule BGAppRefreshTask
        //
        // let request = BGAppRefreshTaskRequest(identifier: "id.startapp.sync.refresh")
        // request.earliestBeginDate = Date(timeIntervalSinceNow: Double(intervalMinutes * 60))
        // try BGTaskScheduler.shared.submit(request)

        _isSyncScheduled = true
        if (debugMode) {
            println("BackgroundSyncManager: Periodic sync scheduled every ${intervalMinutes}min (iOS)")
        }
    }

    actual fun cancelPeriodicSync() {
        if (!isInitialized) return

        // Production: BGTaskScheduler.shared.cancel(taskRequestWithIdentifier: "id.startapp.sync.refresh")

        _isSyncScheduled = false
        if (debugMode) {
            println("BackgroundSyncManager: Periodic sync cancelled (iOS)")
        }
    }

    actual fun requestImmediateSync() {
        if (!isInitialized) return

        // Production: Use BGProcessingTaskRequest for immediate sync
        //
        // let request = BGProcessingTaskRequest(identifier: "id.startapp.sync.process")
        // request.requiresNetworkConnectivity = true
        // request.requiresExternalPower = false
        // try BGTaskScheduler.shared.submit(request)

        if (debugMode) {
            println("BackgroundSyncManager: Immediate sync requested (iOS)")
        }
    }
}
