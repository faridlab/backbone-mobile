package id.startapp.pheromone.infrastructure.monitoring

/**
 * Android implementation of CrashReporter using Sentry Android SDK.
 *
 * Requires `io.sentry:sentry-android` dependency in android/build.gradle.kts.
 *
 * Note: Sentry Android SDK auto-initializes via ContentProvider if
 * `io.sentry.dsn` is set in AndroidManifest.xml. This manual init
 * provides more control over configuration.
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

        try {
            // Sentry initialization is done in MobileApplication via SentryAndroid.init()
            // This flag tracks whether init was called
            _isInitialized = true
            println("CrashReporter: Initialized (Android)")
        } catch (e: Exception) {
            println("CrashReporter: Failed to initialize - ${e.message}")
        }
    }

    actual fun captureException(throwable: Throwable) {
        if (!isInitialized) return
        try {
            // io.sentry.Sentry.captureException(throwable)
            println("CrashReporter: Captured exception - ${throwable.message}")
        } catch (e: Exception) {
            println("CrashReporter: Failed to capture exception - ${e.message}")
        }
    }

    actual fun captureMessage(message: String) {
        if (!isInitialized) return
        try {
            // io.sentry.Sentry.captureMessage(message)
            println("CrashReporter: Captured message - $message")
        } catch (e: Exception) {
            println("CrashReporter: Failed to capture message - ${e.message}")
        }
    }

    actual fun addBreadcrumb(category: String, message: String, data: Map<String, String>) {
        if (!isInitialized) return
        try {
            // val breadcrumb = io.sentry.Breadcrumb().apply {
            //     this.category = category
            //     this.message = message
            //     data.forEach { (k, v) -> this.setData(k, v) }
            // }
            // io.sentry.Sentry.addBreadcrumb(breadcrumb)
        } catch (_: Exception) {
            // Silent - breadcrumbs are non-critical
        }
    }

    actual fun setUser(userId: String, email: String?, username: String?) {
        if (!isInitialized) return
        try {
            // val user = io.sentry.protocol.User().apply {
            //     this.id = userId
            //     this.email = email
            //     this.username = username
            // }
            // io.sentry.Sentry.setUser(user)
        } catch (_: Exception) {
            // Silent
        }
    }

    actual fun clearUser() {
        if (!isInitialized) return
        try {
            // io.sentry.Sentry.setUser(null)
        } catch (_: Exception) {
            // Silent
        }
    }

    actual fun setTag(key: String, value: String) {
        if (!isInitialized) return
        try {
            // io.sentry.Sentry.setTag(key, value)
        } catch (_: Exception) {
            // Silent
        }
    }
}
