package id.startapp.pheromone

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import id.startapp.pheromone.infrastructure.di.initKoin
import id.startapp.pheromone.infrastructure.di.platformModule
import id.startapp.pheromone.infrastructure.monitoring.Analytics
import id.startapp.pheromone.infrastructure.monitoring.CrashReporter
import id.startapp.pheromone.infrastructure.monitoring.PerformanceMonitor
import id.startapp.pheromone.infrastructure.network.BuildConfig
import id.startapp.pheromone.infrastructure.push.PushNotificationManager
import id.startapp.pheromone.infrastructure.sync.BackgroundSyncManager
import platform.UIKit.UIViewController

/**
 * Initialize Koin for iOS.
 *
 * @param sentryDsn Sentry DSN passed from Swift AppDelegate (optional)
 */
fun initKoinIOS(sentryDsn: String = "") {
    // Initialize observability (crash reporting, analytics, performance monitoring)
    CrashReporter.init(dsn = sentryDsn, isDebug = BuildConfig.IS_DEBUG)
    Analytics.init(isDebug = BuildConfig.IS_DEBUG)
    PerformanceMonitor.init(isDebug = BuildConfig.IS_DEBUG)
    PushNotificationManager.init(isDebug = BuildConfig.IS_DEBUG)
    BackgroundSyncManager.init(isDebug = BuildConfig.IS_DEBUG)
    BackgroundSyncManager.schedulePeriodicSync()

    initKoin {
        // iOS-specific modules
        modules(platformModule)
    }
}

/**
 * Main View Controller for iOS.
 *
 * This is a simplified implementation for the skeleton.
 * For production, you'll need to properly integrate with the iOS lifecycle
 * and Compose Multiplatform's UIKit integration.
 */
fun MainViewController(): UIViewController {
    // Initialize Koin
    initKoinIOS()

    // Create a lifecycle registry
    val lifecycle = LifecycleRegistry()

    // Create component context
    val componentContext = DefaultComponentContext(lifecycle = lifecycle)

    // Create and return a simple view controller
    // In production, this would use ComposeUIViewController
    return object : UIViewController(null, null) {
        override fun viewDidLoad() {
            super.viewDidLoad()
            lifecycle.onCreate()
        }

        override fun viewWillAppear(animated: Boolean) {
            super.viewWillAppear(animated)
            lifecycle.onStart()
        }

        override fun viewDidAppear(animated: Boolean) {
            super.viewDidAppear(animated)
            lifecycle.onResume()
        }

        override fun viewWillDisappear(animated: Boolean) {
            super.viewWillDisappear(animated)
            lifecycle.onPause()
        }

        override fun viewDidDisappear(animated: Boolean) {
            super.viewDidDisappear(animated)
            lifecycle.onStop()
        }

        override fun viewDidUnload() {
            super.viewDidUnload()
            lifecycle.onDestroy()
        }
    }
}

/**
 * Helper to access UserDefaults on iOS.
 */
fun iosUserDefaults(): platform.Foundation.NSUserDefaults {
    return platform.Foundation.NSUserDefaults.standardUserDefaults
}
