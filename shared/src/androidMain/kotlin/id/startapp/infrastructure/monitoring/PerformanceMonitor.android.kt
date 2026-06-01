package id.startapp.infrastructure.monitoring

import kotlinx.datetime.Clock

/**
 * Android implementation of PerformanceMonitor.
 *
 * Logs traces and metrics to console in development.
 * To activate production monitoring:
 * 1. Add Sentry Performance or Firebase Performance dependency
 * 2. Replace println calls with SDK calls
 *
 * Example with Sentry:
 *   val transaction = Sentry.startTransaction(name, "ui")
 *   transaction.finish()
 */
actual object PerformanceMonitor {

    private var _isInitialized: Boolean = false
    actual val isInitialized: Boolean get() = _isInitialized

    private val activeTraces = mutableMapOf<String, Long>()
    private var traceCounter = 0L
    private const val STALE_TRACE_THRESHOLD_MS = 5 * 60 * 1000L // 5 minutes

    actual fun init(isDebug: Boolean) {
        if (isInitialized) return

        // Sentry: already initialized via CrashReporter (tracesSampleRate controls perf)
        // Firebase: FirebasePerformance.getInstance().isPerformanceCollectionEnabled = true
        _isInitialized = true
        if (isDebug) {
            println("PerformanceMonitor: Initialized (Android)")
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
        // Sentry: Sentry.startTransaction(name, "app")
        // Firebase: Firebase.performance.newTrace(name).start()
        return traceId
    }

    actual fun stopTrace(traceId: String, attributes: Map<String, String>) {
        if (!isInitialized || traceId.isBlank()) return
        val startTime = activeTraces.remove(traceId) ?: return
        val durationMs = Clock.System.now().toEpochMilliseconds() - startTime
        // Sentry: transaction.finish()
        // Firebase: trace.stop()
        println("PerformanceMonitor: Trace $traceId completed in ${durationMs}ms")
    }

    actual fun recordMetric(name: String, value: Double, unit: String) {
        if (!isInitialized) return
        // Sentry: Sentry.metrics().distribution(name, value)
        // Firebase: trace.putMetric(name, value.toLong())
        println("PerformanceMonitor: Metric $name = $value $unit")
    }
}
