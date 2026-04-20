package id.startapp.pheromone.infrastructure.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_get_status
import platform.Network.nw_path_is_expensive
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_queue_create
import platform.darwin.DISPATCH_QUEUE_SERIAL

/**
 * iOS implementation of ConnectivityMonitor.
 *
 * Uses NWPathMonitor from the Network framework to observe
 * real-time network state changes.
 */
actual class ConnectivityMonitor {

    private val _status = MutableStateFlow(ConnectivityStatus.ONLINE)
    actual val status: StateFlow<ConnectivityStatus> = _status.asStateFlow()

    private var monitor: platform.Network.nw_path_monitor_t? = null
    private val queue = dispatch_queue_create("id.startapp.pheromone.connectivity", DISPATCH_QUEUE_SERIAL)

    actual fun startMonitoring() {
        if (monitor != null) return

        val pathMonitor = nw_path_monitor_create()
        monitor = pathMonitor

        nw_path_monitor_set_update_handler(pathMonitor) { path ->
            val status = nw_path_get_status(path)
            val isExpensive = nw_path_is_expensive(path)

            val connectivityStatus = when {
                status != nw_path_status_satisfied -> ConnectivityStatus.OFFLINE
                isExpensive -> ConnectivityStatus.METERED
                else -> ConnectivityStatus.ONLINE
            }

            _status.value = connectivityStatus
        }

        nw_path_monitor_set_queue(pathMonitor, queue)
        nw_path_monitor_start(pathMonitor)
    }

    actual fun stopMonitoring() {
        monitor?.let { nw_path_monitor_cancel(it) }
        monitor = null
    }
}
