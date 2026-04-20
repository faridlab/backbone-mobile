package id.startapp.pheromone.infrastructure.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android implementation of ConnectivityMonitor.
 *
 * Uses [ConnectivityManager.registerDefaultNetworkCallback] to observe
 * real-time network state changes including metered detection.
 */
actual class ConnectivityMonitor(
    private val context: Context,
) {
    private val _status = MutableStateFlow(ConnectivityStatus.ONLINE)
    actual val status: StateFlow<ConnectivityStatus> = _status.asStateFlow()

    private val connectivityManager: ConnectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    actual fun startMonitoring() {
        if (networkCallback != null) return

        // Check initial state
        _status.value = getCurrentStatus()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                updateStatus()
            }

            override fun onLost(network: Network) {
                _status.value = ConnectivityStatus.OFFLINE
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities,
            ) {
                updateStatus(capabilities)
            }
        }

        networkCallback = callback

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)
    }

    actual fun stopMonitoring() {
        networkCallback?.let { callback ->
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (_: IllegalArgumentException) {
                // Already unregistered
            }
        }
        networkCallback = null
    }

    private fun getCurrentStatus(): ConnectivityStatus {
        val network = connectivityManager.activeNetwork ?: return ConnectivityStatus.OFFLINE
        val capabilities = connectivityManager.getNetworkCapabilities(network)
            ?: return ConnectivityStatus.OFFLINE

        return mapCapabilities(capabilities)
    }

    private fun updateStatus(capabilities: NetworkCapabilities? = null) {
        val caps = capabilities
            ?: connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork ?: return)
            ?: return

        _status.value = mapCapabilities(caps)
    }

    private fun mapCapabilities(capabilities: NetworkCapabilities): ConnectivityStatus {
        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        if (!hasInternet) return ConnectivityStatus.OFFLINE

        val isNotMetered = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        return if (isNotMetered) ConnectivityStatus.ONLINE else ConnectivityStatus.METERED
    }
}
