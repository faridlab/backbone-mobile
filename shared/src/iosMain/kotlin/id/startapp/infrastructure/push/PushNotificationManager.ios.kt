package id.startapp.infrastructure.push

/**
 * iOS implementation of PushNotificationManager.
 *
 * Logs push operations to console in development.
 * To activate production push notifications:
 * 1. Enable Push Notifications capability in Xcode
 * 2. Configure APNs certificate or key in Apple Developer Portal
 * 3. Replace println calls with UNUserNotificationCenter calls:
 *    - UNUserNotificationCenter.current().requestAuthorization()
 *    - UIApplication.shared.registerForRemoteNotifications()
 *    - UNMutableNotificationContent for local notification display
 */
actual object PushNotificationManager {

    private var _isInitialized: Boolean = false
    actual val isInitialized: Boolean get() = _isInitialized

    actual val hasPermission: Boolean
        get() {
            // Production: check UNUserNotificationCenter authorization status
            return isInitialized
        }

    private var currentToken: String? = null
    private var debugMode: Boolean = false

    actual fun init(isDebug: Boolean) {
        if (isInitialized) return
        debugMode = isDebug

        // Production: Register for remote notifications
        // UIApplication.shared.registerForRemoteNotifications()
        //
        // Set UNUserNotificationCenter delegate:
        // UNUserNotificationCenter.current().delegate = notificationDelegate

        _isInitialized = true
        if (isDebug) {
            println("PushNotificationManager: Initialized (iOS)")
        }
    }

    actual suspend fun getToken(): String? {
        if (!isInitialized) return null

        // Production: Token is provided via AppDelegate callback
        // application(_:didRegisterForRemoteNotificationsWithDeviceToken:)
        if (debugMode) {
            println("PushNotificationManager: getToken() -> ${currentToken?.take(10)?.plus("...") ?: "no token"}")
        }
        return currentToken
    }

    actual fun onNewToken(token: String) {
        currentToken = token
        // Production: Store in KeyValueStorage and register with backend
        // Production: PushTokenApiClient.registerToken(token, "ios")
        if (debugMode) {
            println("PushNotificationManager: New APNs token received (${token.take(10)}...)")
        }
    }

    actual fun onMessageReceived(title: String, body: String, data: Map<String, String>) {
        if (!isInitialized) return

        // Production: Show local notification via UNUserNotificationCenter
        //
        // val content = UNMutableNotificationContent().apply {
        //     setTitle(title)
        //     setBody(body)
        //     setSound(UNNotificationSound.defaultSound())
        // }
        // val request = UNNotificationRequest.requestWithIdentifier(
        //     UUID().UUIDString, content, null
        // )
        // UNUserNotificationCenter.current().addNotificationRequest(request) {}

        if (debugMode) {
            println("PushNotificationManager: Message received - $title: $body (data: ${data.size} keys)")
        }
    }

    actual suspend fun requestPermission(): Boolean {
        if (!isInitialized) return false

        // Production:
        // UNUserNotificationCenter.current().requestAuthorization(
        //     options = UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound
        // ) { granted, error -> ... }

        if (debugMode) {
            println("PushNotificationManager: Permission requested (iOS)")
        }
        return true // Dev stub: assume granted
    }
}
