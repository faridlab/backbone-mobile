package id.startapp.pheromone.infrastructure.push

/**
 * Cross-platform push notification manager.
 *
 * Handles push token lifecycle, permission requests, and message display.
 * Platform implementations provide the actual SDK integration.
 *
 * To activate production push notifications:
 * - Android: Enable Firebase Cloud Messaging in build.gradle.kts
 * - iOS: Enable APNs in Xcode capabilities and AppDelegate
 */
expect object PushNotificationManager {

    /** Whether the manager has been initialized. */
    val isInitialized: Boolean

    /** Whether push notification permission has been granted. */
    val hasPermission: Boolean

    /**
     * Initialize the push notification system.
     *
     * @param isDebug When true, logs operations to console
     */
    fun init(isDebug: Boolean = false)

    /**
     * Get the current push token, or null if not available.
     */
    suspend fun getToken(): String?

    /**
     * Called when a new push token is issued by the platform.
     * Stores the token locally and logs it for debugging.
     *
     * @param token The new push token
     */
    fun onNewToken(token: String)

    /**
     * Called when a push message is received.
     * Displays a local notification or processes data payload.
     *
     * @param title Notification title
     * @param body Notification body text
     * @param data Additional data payload
     */
    fun onMessageReceived(title: String, body: String, data: Map<String, String>)

    /**
     * Request push notification permission from the user.
     *
     * @return true if permission was granted
     */
    suspend fun requestPermission(): Boolean
}
