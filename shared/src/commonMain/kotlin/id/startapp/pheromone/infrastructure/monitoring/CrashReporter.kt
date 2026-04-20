package id.startapp.pheromone.infrastructure.monitoring

/**
 * Platform-independent crash reporting interface.
 *
 * Provides crash reporting, breadcrumbs, and user context
 * across Android and iOS using platform-native SDKs.
 *
 * Android: Sentry Android SDK
 * iOS: Sentry Cocoa SDK (via Swift bridge)
 */
expect object CrashReporter {

    /**
     * Initialize crash reporting with the given DSN.
     * Call once during app startup (Application.onCreate / AppDelegate).
     *
     * @param dsn Sentry DSN (e.g., "https://key@sentry.io/project")
     * @param isDebug Whether this is a debug build (enables debug logging)
     */
    fun init(dsn: String, isDebug: Boolean = false)

    /**
     * Record a non-fatal exception for tracking.
     */
    fun captureException(throwable: Throwable)

    /**
     * Record a message for tracking.
     */
    fun captureMessage(message: String)

    /**
     * Add a breadcrumb for debugging crash context.
     *
     * @param category Category of the breadcrumb (e.g., "navigation", "api", "ui")
     * @param message Human-readable description
     * @param data Additional key-value data
     */
    fun addBreadcrumb(category: String, message: String, data: Map<String, String> = emptyMap())

    /**
     * Set the current user context for crash reports.
     * Call after login, clear after logout.
     *
     * @param userId User ID
     * @param email User email (optional)
     * @param username Display name (optional)
     */
    fun setUser(userId: String, email: String? = null, username: String? = null)

    /**
     * Clear user context (call on logout).
     */
    fun clearUser()

    /**
     * Set a custom tag on all future events.
     */
    fun setTag(key: String, value: String)

    /**
     * Whether crash reporting has been initialized.
     */
    val isInitialized: Boolean
}
