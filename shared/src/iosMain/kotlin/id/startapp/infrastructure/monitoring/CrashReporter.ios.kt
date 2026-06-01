package id.startapp.infrastructure.monitoring

/**
 * iOS implementation of CrashReporter.
 *
 * Sentry iOS SDK is initialized from Swift (AppDelegate.swift)
 * using SentrySDK.start(). This Kotlin layer provides the common
 * API for breadcrumbs, user context, and exception capture.
 *
 * To activate, add Sentry via SPM or CocoaPods in the iOS project
 * and call SentrySDK.start() in AppDelegate before initKoinIOS().
 */
actual object CrashReporter {

    private var _isInitialized: Boolean = false
    actual val isInitialized: Boolean get() = _isInitialized

    actual fun init(dsn: String, isDebug: Boolean) {
        if (isInitialized) return
        if (dsn.isBlank()) {
            println("CrashReporter: DSN is blank, skipping initialization")
            return
        }

        // Sentry iOS SDK is initialized from Swift side (AppDelegate.swift):
        //   SentrySDK.start { options in
        //       options.dsn = "your-dsn"
        //       options.tracesSampleRate = 0.1
        //       options.debug = isDebug
        //   }
        _isInitialized = true
        println("CrashReporter: Initialized (iOS)")
    }

    actual fun captureException(throwable: Throwable) {
        if (!isInitialized) return
        try {
            // SentrySDK.capture(error:) from Swift bridge
            println("CrashReporter: Captured exception - ${throwable.message}")
        } catch (_: Exception) {
            // Silent
        }
    }

    actual fun captureMessage(message: String) {
        if (!isInitialized) return
        try {
            // SentrySDK.capture(message:)
            println("CrashReporter: Captured message - $message")
        } catch (_: Exception) {
            // Silent
        }
    }

    actual fun addBreadcrumb(category: String, message: String, data: Map<String, String>) {
        if (!isInitialized) return
        // SentrySDK.addBreadcrumb() from Swift bridge
    }

    actual fun setUser(userId: String, email: String?, username: String?) {
        if (!isInitialized) return
        // let user = Sentry.User()
        // user.userId = userId
        // SentrySDK.setUser(user)
    }

    actual fun clearUser() {
        if (!isInitialized) return
        // SentrySDK.setUser(nil)
    }

    actual fun setTag(key: String, value: String) {
        if (!isInitialized) return
        // SentrySDK.configureScope { scope in scope.setTag(value: value, key: key) }
    }
}
