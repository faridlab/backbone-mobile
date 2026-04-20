package id.startapp.pheromone

import android.app.Application
import id.startapp.pheromone.infrastructure.di.initKoin
import id.startapp.pheromone.infrastructure.monitoring.Analytics
import id.startapp.pheromone.infrastructure.monitoring.CrashReporter
import id.startapp.pheromone.infrastructure.monitoring.PerformanceMonitor
import id.startapp.pheromone.infrastructure.network.BuildConfig
import id.startapp.pheromone.infrastructure.network.ConnectivityMonitor
import id.startapp.pheromone.infrastructure.network.HttpClientFactory
import id.startapp.pheromone.infrastructure.push.PushNotificationManager
import id.startapp.pheromone.infrastructure.sync.BackgroundSyncManager
import id.startapp.pheromone.infrastructure.sync.OutboxManager
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level
import org.koin.mp.KoinPlatform

/**
 * Android Application entry. Initializes Koin and bootstraps monitoring/sync.
 *
 * Firebase push is wired up but left disabled in the skeleton. To enable,
 * add `google-services.json`, re-enable the google-services plugin and
 * firebase-messaging dep in android/build.gradle.kts, and register a real
 * [PushNotificationManager.tokenProvider] that resolves the FCM token.
 */
class MobileApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        CrashReporter.init(dsn = "", isDebug = BuildConfig.IS_DEBUG)
        Analytics.init(isDebug = BuildConfig.IS_DEBUG)
        PerformanceMonitor.init(isDebug = BuildConfig.IS_DEBUG)
        PushNotificationManager.init(isDebug = BuildConfig.IS_DEBUG)

        BackgroundSyncManager.setContext(this)
        BackgroundSyncManager.init(isDebug = BuildConfig.IS_DEBUG)
        BackgroundSyncManager.schedulePeriodicSync()

        initKoin {
            androidContext(this@MobileApplication)
            androidLogger(Level.ERROR)
        }

        KoinPlatform.getKoin().get<app.cash.sqldelight.db.SqlDriver>()
        KoinPlatform.getKoin().get<ConnectivityMonitor>().startMonitoring()

        runBlocking {
            val outboxManager = KoinPlatform.getKoin().get<OutboxManager>()
            val deviceId = outboxManager.getDeviceId()
            val deviceName = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
            HttpClientFactory.setDeviceIdentity(deviceId, deviceName)
        }
    }
}
