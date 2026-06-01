package id.startapp.infrastructure.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Application-wide event bus for cross-screen communication.
 *
 * Uses [SharedFlow] (no replay) so events are only delivered to active collectors.
 * Register as a Koin singleton to share across the app.
 *
 * Usage:
 * ```
 * // Emit
 * appEventBus.emit(AppEvent.OrderCreated(orderId))
 *
 * // Collect in a composable
 * LaunchedEffect(Unit) {
 *     appEventBus.events.collect { event ->
 *         when (event) {
 *             is AppEvent.OrderCreated -> refreshOrders()
 *             ...
 *         }
 *     }
 * }
 * ```
 */
class AppEventBus {
    private val _events = MutableSharedFlow<AppEvent>(
        replay = 0,
        extraBufferCapacity = 16,
    )
    val events: SharedFlow<AppEvent> = _events.asSharedFlow()

    suspend fun emit(event: AppEvent) {
        _events.emit(event)
    }

    /**
     * Non-suspending emit for use outside coroutine scope (e.g., HTTP interceptors).
     * Relies on [extraBufferCapacity] to avoid dropping events.
     */
    fun tryEmit(event: AppEvent): Boolean {
        return _events.tryEmit(event)
    }
}

/**
 * Application-level events for cross-screen communication.
 */
sealed class AppEvent {
    data class OrderCreated(val orderId: String) : AppEvent()
    data class OrderUpdated(val orderId: String) : AppEvent()
    data object OnboardingCompleted : AppEvent()
    data object SessionExpired : AppEvent()

    // Sync lifecycle events
    data object ConnectivityChanged : AppEvent()
    data class SyncCompleted(val pushed: Int, val pulled: Int, val failed: Int, val conflicts: Int) : AppEvent()
    data class ConflictDetected(val entityType: String, val entityId: String) : AppEvent()
    data class OutboxUpdated(val pendingCount: Long) : AppEvent()
}
