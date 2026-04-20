package id.startapp.pheromone.infrastructure.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

/**
 * Android implementation of BackgroundSyncManager.
 *
 * Uses WorkManager for reliable background sync scheduling.
 * Requires dependency: implementation("androidx.work:work-runtime-ktx:2.9.1")
 *
 * Call [setContext] from Application.onCreate() to provide the app context.
 */
actual object BackgroundSyncManager {

    private var _isInitialized: Boolean = false
    actual val isInitialized: Boolean get() = _isInitialized

    actual val isSyncScheduled: Boolean
        get() = _isSyncScheduled

    private var _isSyncScheduled: Boolean = false
    private var debugMode: Boolean = false
    private var contextRef: WeakReference<Context>? = null

    /**
     * Set the application context for WorkManager access.
     * Call from Application.onCreate().
     */
    fun setContext(context: Context) {
        contextRef = WeakReference(context.applicationContext)
    }

    actual fun init(isDebug: Boolean) {
        if (isInitialized) return
        debugMode = isDebug
        _isInitialized = true
        if (isDebug) {
            println("BackgroundSyncManager: Initialized (Android/WorkManager)")
        }
    }

    actual fun schedulePeriodicSync(intervalMinutes: Int) {
        if (!isInitialized) return
        val context = contextRef?.get()
        if (context == null) {
            if (debugMode) println("BackgroundSyncManager: No context — skipping schedule")
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<PheromoneSyncWorker>(
            repeatInterval = intervalMinutes.toLong(),
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
            .setInputData(Data.Builder().putString("trigger_type", "auto").build())
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PheromoneSyncWorker.SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest,
        )

        _isSyncScheduled = true
        if (debugMode) {
            println("BackgroundSyncManager: Periodic sync scheduled every ${intervalMinutes}min (Android)")
        }
    }

    actual fun cancelPeriodicSync() {
        if (!isInitialized) return
        val context = contextRef?.get()
        if (context != null) {
            WorkManager.getInstance(context).cancelUniqueWork(PheromoneSyncWorker.SYNC_WORK_NAME)
        }
        _isSyncScheduled = false
        if (debugMode) {
            println("BackgroundSyncManager: Periodic sync cancelled (Android)")
        }
    }

    actual fun requestImmediateSync() {
        if (!isInitialized) return
        val context = contextRef?.get()
        if (context == null) {
            if (debugMode) println("BackgroundSyncManager: No context — skipping immediate sync")
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val immediateRequest = OneTimeWorkRequestBuilder<PheromoneSyncWorker>()
            .setConstraints(constraints)
            .setInputData(Data.Builder().putString("trigger_type", "on_connect").build())
            .addTag(PheromoneSyncWorker.IMMEDIATE_SYNC_TAG)
            .build()

        WorkManager.getInstance(context).enqueue(immediateRequest)

        if (debugMode) {
            println("BackgroundSyncManager: Immediate sync requested (Android)")
        }
    }
}
