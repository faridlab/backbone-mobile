package id.startapp.pheromone.infrastructure.network

/**
 * Platform-specific build configuration.
 *
 * Provides runtime configuration values that can differ between debug and release builds,
 * and between platforms (Android/iOS).
 */
expect object BuildConfig {
    /**
     * The base URL for the API.
     *
     * Android: Configured via BuildConfig in build.gradle.kts
     * iOS: Configured via xcconfig or Info.plist
     */
    val API_BASE_URL: String

    /**
     * Whether this is a debug build.
     *
     * Debug builds enable logging and disable some security features for development.
     * Release builds disable logging and enable all security features.
     */
    val IS_DEBUG: Boolean

    /**
     * Application version.
     */
    val VERSION_NAME: String

    /**
     * Application version code.
     */
    val VERSION_CODE: Int

    // Network Timeout Configuration

    /**
     * HTTP request timeout in milliseconds.
     * Default: 30 seconds
     */
    val REQUEST_TIMEOUT_MS: Long

    /**
     * Connection timeout in milliseconds.
     * Default: 10 seconds
     */
    val CONNECT_TIMEOUT_MS: Long

    /**
     * Socket timeout in milliseconds.
     * Default: 30 seconds
     */
    val SOCKET_TIMEOUT_MS: Long
}
