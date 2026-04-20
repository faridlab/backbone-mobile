package id.startapp.pheromone.infrastructure.monitoring

import kotlinx.datetime.Clock

/**
 * iOS implementation of PerformanceMonitor.
 *
 * Logs traces and metrics to console in development.
 * To activate production monitoring:
 * 1. Add Sentry Performance or Firebase Performance via SPM/CocoaPods
 * 2. Replace println calls with SDK calls
 */
actual object PerformanceMonitor {

    private var _isInitialized: Boolean = false
    actual val isInitialized: Boolean get() = _isInitialized

    private val activeTraces = mutableMapOf<String, Long>()
    private var traceCounter = 0L
    private const val STALE_TRACE_THRESHOLD_MS = 5 * 60 * 1000L // 5 minutes

    actual fun init(isDebug: Boolean) {
        if (isInitialized) return

        // Sentry: already initialized from Swift AppDelegate
        // Firebase: Performance.sharedInstance().isDataCollectionEnabled = true
        _isInitialized = true
        if (isDebug) {
            println("PerformanceMonitor: Initialized (iOS)")
        }
    }

    actual fun startTrace(name: String): String {
        if (!isInitialized) return ""
        // Evict stale traces to prevent unbounded growth
        val now = Clock.System.now().toEpochMilliseconds()
        if (activeTraces.size > 10) {
            activeTraces.entries.removeAll { now - it.value > STALE_TRACE_THRESHOLD_MS }
        }
        val traceId = "trace_${++traceCounter}"
        activeTraces[traceId] = now
        return traceId
    }

    actual fun stopTrace(traceId: String, attributes: Map<String, String>) {
        if (!isInitialized || traceId.isBlank()) return
        val startTime = activeTraces.remove(traceId) ?: return
        val durationMs = Clock.System.now().toEpochMilliseconds() - startTime
        println("PerformanceMonitor: Trace $traceId completed in ${durationMs}ms")
    }

    actual fun recordMetric(name: String, value: Double, unit: String) {
        if (!isInitialized) return
        println("PerformanceMonitor: Metric $name = $value $unit")
    }
}
