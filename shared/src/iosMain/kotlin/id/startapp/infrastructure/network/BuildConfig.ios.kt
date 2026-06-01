package id.startapp.infrastructure.network

import platform.Foundation.NSBundle

/**
 * iOS implementation of BuildConfig.
 *
 * Values are read from the app's Info.plist or can be hardcoded here.
 *
 * To customize these values, edit your iosApp/Info.plist or use xcconfig files:
 *
 * ```xml
 * <key>API_BASE_URL</key>
 * <string>https://api.production.com</string>
 * <key>IS_DEBUG</key>
 * <false/>
 * <key>REQUEST_TIMEOUT_MS</key>
 * <string>30000</string>
 * <key>CONNECT_TIMEOUT_MS</key>
 * <string>10000</string>
 * <key>SOCKET_TIMEOUT_MS</key>
 * <string>30000</string>
 * ```
 */
actual object BuildConfig {
    actual val API_BASE_URL: String = readPlistString("API_BASE_URL") ?: getDefaultApiUrl()

    actual val IS_DEBUG: Boolean = readPlistBoolean("IS_DEBUG") ?: true

    actual val VERSION_NAME: String = readPlistString("CFBundleShortVersionString") ?: "1.0.0"

    actual val VERSION_CODE: Int = readPlistString("CFBundleVersion")?.toIntOrNull() ?: 1

    // Default timeout values (can be overridden via Info.plist)
    actual val REQUEST_TIMEOUT_MS: Long = readPlistString("REQUEST_TIMEOUT_MS")?.toLongOrNull() ?: 30000L

    actual val CONNECT_TIMEOUT_MS: Long = readPlistString("CONNECT_TIMEOUT_MS")?.toLongOrNull() ?: 10000L

    actual val SOCKET_TIMEOUT_MS: Long = readPlistString("SOCKET_TIMEOUT_MS")?.toLongOrNull() ?: 30000L

    private fun readPlistString(key: String): String? {
        return NSBundle.mainBundle.objectForInfoDictionaryKey(key) as? String
    }

    private fun readPlistBoolean(key: String): Boolean? {
        return NSBundle.mainBundle.objectForInfoDictionaryKey(key) as? Boolean
    }

    private fun getDefaultApiUrl(): String = if (IS_DEBUG) {
        "http://localhost:3000"  // iOS simulator can use localhost directly
    } else {
        "https://api.example.com"
    }
}
