package id.startapp.infrastructure.monitoring

/**
 * Platform-independent analytics interface.
 *
 * Provides screen tracking, event tracking, and user properties
 * across Android and iOS using platform-native SDKs.
 *
 * Android: Firebase Analytics / Mixpanel / PostHog
 * iOS: Firebase Analytics / Mixpanel / PostHog
 *
 * To activate: add the analytics SDK dependency and call [init] with config.
 */
expect object Analytics {

    /**
     * Initialize analytics with the given configuration.
     * Call once during app startup (Application.onCreate / AppDelegate).
     *
     * @param isDebug Whether this is a debug build (enables debug logging)
     */
    fun init(isDebug: Boolean = false)

    /**
     * Track a screen view.
     *
     * Call this when a new screen becomes visible.
     *
     * @param screenName Name of the screen (e.g., "LoginScreen", "OrderList")
     * @param screenClass Optional class name for categorization
     */
    fun trackScreen(screenName: String, screenClass: String? = null)

    /**
     * Track a custom event.
     *
     * @param name Event name (e.g., "order_created", "login_success")
     * @param params Optional key-value parameters
     */
    fun trackEvent(name: String, params: Map<String, Any> = emptyMap())

    /**
     * Set a user property for segmentation.
     *
     * @param key Property key (e.g., "role", "outlet_id")
     * @param value Property value
     */
    fun setUserProperty(key: String, value: String)

    /**
     * Identify the current user for analytics association.
     * Call after login.
     *
     * @param userId User ID
     * @param email Optional email
     */
    fun identifyUser(userId: String, email: String? = null)

    /**
     * Reset user identity (call on logout).
     */
    fun resetUser()

    /**
     * Whether analytics has been initialized.
     */
    val isInitialized: Boolean
}
