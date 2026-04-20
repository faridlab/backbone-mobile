package id.startapp.pheromone.infrastructure.monitoring

/**
 * iOS implementation of Analytics.
 *
 * Logs events to console in development. To activate production analytics:
 * 1. Add Firebase Analytics via SPM or PostHog via CocoaPods
 * 2. Replace println calls with SDK calls
 *
 * Example with Firebase:
 *   FIRAnalytics.logEvent(withName:parameters:)
 */
actual object Analytics {

    private var _isInitialized: Boolean = false
    actual val isInitialized: Boolean get() = _isInitialized

    actual fun init(isDebug: Boolean) {
        if (isInitialized) return

        // Firebase: FIRApp.configure() in AppDelegate.swift
        // PostHog: PHGPostHog.setup(with: config) in AppDelegate.swift
        _isInitialized = true
        if (isDebug) {
            println("Analytics: Initialized (iOS, debug=$isDebug)")
        }
    }

    actual fun trackScreen(screenName: String, screenClass: String?) {
        if (!isInitialized) return
        // FIRAnalytics.logEvent(AnalyticsEventScreenView, parameters: [...])
        println("Analytics: Screen -> $screenName")
    }

    actual fun trackEvent(name: String, params: Map<String, Any>) {
        if (!isInitialized) return
        // FIRAnalytics.logEvent(name, parameters: params)
        println("Analytics: Event -> $name ${if (params.isNotEmpty()) params else ""}")
    }

    actual fun setUserProperty(key: String, value: String) {
        if (!isInitialized) return
        // FIRAnalytics.setUserProperty(value, forName: key)
    }

    actual fun identifyUser(userId: String, email: String?) {
        if (!isInitialized) return
        // FIRAnalytics.setUserID(userId)
    }

    actual fun resetUser() {
        if (!isInitialized) return
        // FIRAnalytics.setUserID(nil)
    }
}
