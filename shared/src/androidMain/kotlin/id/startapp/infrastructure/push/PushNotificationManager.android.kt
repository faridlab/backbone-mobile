package id.startapp.infrastructure.push

import kotlinx.coroutines.launch

/**
 * Android implementation of PushNotificationManager.
 *
 * Uses a token provider callback set by the Android app module
 * (which has the Firebase dependency) to retrieve push tokens.
 * Token refresh is handled by BackboneFirebaseMessagingService
 * which delegates to onNewToken().
 */
actual object PushNotificationManager {

    private var _isInitialized: Boolean = false
    actual val isInitialized: Boolean get() = _isInitialized

    actual val hasPermission: Boolean
        get() = isInitialized

    private var currentToken: String? = null
    private var debugMode: Boolean = false

    /**
     * Token provider callback — set by the Android app module to supply
     * the Firebase token without a direct Firebase dependency in shared.
     */
    var tokenProvider: (suspend () -> String?)? = null

    actual fun init(isDebug: Boolean) {
        if (isInitialized) return
        debugMode = isDebug
        _isInitialized = true

        if (isDebug) {
            println("PushNotificationManager: Initialized (Android)")
        }
    }

    actual suspend fun getToken(): String? {
        if (!isInitialized) return null

        if (currentToken != null) return currentToken

        return try {
            val token = tokenProvider?.invoke()
            currentToken = token
            if (debugMode && token != null) {
                println("PushNotificationManager: Token retrieved (${token.take(10)}...)")
            }
            token
        } catch (e: Exception) {
            if (debugMode) {
                println("PushNotificationManager: Failed to get token: ${e.message}")
            }
            null
        }
    }

    actual fun onNewToken(token: String) {
        currentToken = token
        kotlinx.coroutines.GlobalScope.launch {
            try {
                val apiClient = org.koin.mp.KoinPlatform.getKoin().getOrNull<PushTokenApiClient>()
                apiClient?.registerToken(token, "android")
                if (debugMode) {
                    println("PushNotificationManager: Token registered with backend")
                }
            } catch (e: Exception) {
                if (debugMode) {
                    println("PushNotificationManager: Failed to register token: ${e.message}")
                }
            }
        }
        if (debugMode) {
            println("PushNotificationManager: New token received (${token.take(10)}...)")
        }
    }

    actual fun onMessageReceived(title: String, body: String, data: Map<String, String>) {
        if (!isInitialized) return

        if (debugMode) {
            println("PushNotificationManager: Message received - $title: $body (data: ${data.size} keys)")
        }
    }

    actual suspend fun requestPermission(): Boolean {
        if (!isInitialized) return false
        if (debugMode) {
            println("PushNotificationManager: Permission requested (Android)")
        }
        return true
    }
}
