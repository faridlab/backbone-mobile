package id.startapp.pheromone.infrastructure.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android WorkManager worker that delegates to [SyncWorker] for background sync.
 *
 * Scheduled by [BackgroundSyncManager] for periodic (every 15 min) and
 * immediate one-time sync operations.
 */
class PheromoneSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val syncWorker: SyncWorker by inject()

    override suspend fun doWork(): Result {
        return try {
            val triggerType = inputData.getString("trigger_type") ?: "auto"
            val result = syncWorker.performSync(triggerType)

            if (result.failedCount == 0) {
                Result.success()
            } else {
                // Some items failed but sync completed — don't retry the whole batch
                Result.success()
            }
        } catch (_: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val SYNC_WORK_NAME = "pheromone_periodic_sync"
        const val IMMEDIATE_SYNC_TAG = "pheromone_immediate_sync"
    }
}
