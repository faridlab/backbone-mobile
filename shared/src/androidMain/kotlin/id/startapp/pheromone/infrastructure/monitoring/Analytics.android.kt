package id.startapp.pheromone.infrastructure.monitoring

/**
 * Android implementation of Analytics.
 *
 * Logs events to console in development. To activate production analytics:
 * 1. Add Firebase Analytics or PostHog dependency
 * 2. Replace println calls with SDK calls
 *
 * Example with Firebase:
 *   FirebaseAnalytics.getInstance(context).logEvent(name, bundle)
 */
actual object Analytics {

    private var _isInitialized: Boolean = false
    actual val isInitialized: Boolean get() = _isInitialized

    actual fun init(isDebug: Boolean) {
        if (isInitialized) return

        // Firebase Analytics auto-initializes via google-services plugin.
        // PostHog: PostHog.setup(context) { apiKey = "..."; host = "..." }
        _isInitialized = true
        if (isDebug) {
            println("Analytics: Initialized (Android, debug=$isDebug)")
        }
    }

    actual fun trackScreen(screenName: String, screenClass: String?) {
        if (!isInitialized) return
        // FirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
        //     param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        //     param(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass ?: screenName)
        // }
        println("Analytics: Screen -> $screenName")
    }

    actual fun trackEvent(name: String, params: Map<String, Any>) {
        if (!isInitialized) return
        // FirebaseAnalytics.logEvent(name) { params.forEach { (k, v) -> param(k, v.toString()) } }
        println("Analytics: Event -> $name ${if (params.isNotEmpty()) params else ""}")
    }

    actual fun setUserProperty(key: String, value: String) {
        if (!isInitialized) return
        // FirebaseAnalytics.setUserProperty(key, value)
    }

    actual fun identifyUser(userId: String, email: String?) {
        if (!isInitialized) return
        // FirebaseAnalytics.setUserId(userId)
    }

    actual fun resetUser() {
        if (!isInitialized) return
        // FirebaseAnalytics.setUserId(null)
    }
}
