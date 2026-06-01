package id.startapp.infrastructure.network

import kotlinx.coroutines.flow.StateFlow

/**
 * Cross-platform connectivity monitor.
 *
 * Observes real-time network state changes and exposes them
 * via a [StateFlow] for reactive UI updates (offline banner, sync triggers).
 *
 * Platform implementations:
 * - Android: ConnectivityManager.registerDefaultNetworkCallback()
 * - iOS: NWPathMonitor from Network framework
 */
expect class ConnectivityMonitor {

    /**
     * Current connectivity status as a reactive flow.
     * Collectors receive immediate updates when network state changes.
     */
    val status: StateFlow<ConnectivityStatus>

    /**
     * Start monitoring network state changes.
     * Call once during app initialization.
     */
    fun startMonitoring()

    /**
     * Stop monitoring network state changes.
     * Call during app teardown or when monitoring is no longer needed.
     */
    fun stopMonitoring()
}

/**
 * Network connectivity status.
 */
enum class ConnectivityStatus {
    /** Full connectivity available */
    ONLINE,

    /** No connectivity */
    OFFLINE,

    /** Limited connectivity (cellular/metered) */
    METERED,
}
