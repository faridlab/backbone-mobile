package id.startapp.infrastructure.monitoring

import kotlinx.datetime.Clock

/**
 * Platform-independent performance monitoring interface.
 *
 * Tracks screen load times, network request durations, and custom spans.
 * Uses platform-native SDKs (Sentry Performance / Firebase Performance).
 *
 * To activate: add the performance SDK dependency and call [init].
 */
expect object PerformanceMonitor {

    /**
     * Initialize performance monitoring.
     * Call once during app startup.
     *
     * @param isDebug Whether this is a debug build
     */
    fun init(isDebug: Boolean = false)

    /**
     * Start a named trace/span for measuring duration.
     *
     * @param name Trace name (e.g., "screen_load_OrderList", "api_login")
     * @return Trace ID to pass to [stopTrace]
     */
    fun startTrace(name: String): String

    /**
     * Stop a previously started trace and record its duration.
     *
     * @param traceId The ID returned by [startTrace]
     * @param attributes Optional attributes to attach to the trace
     */
    fun stopTrace(traceId: String, attributes: Map<String, String> = emptyMap())

    /**
     * Record a metric value (e.g., frame drop count, memory usage).
     *
     * @param name Metric name
     * @param value Metric value
     * @param unit Optional unit (e.g., "ms", "bytes", "frames")
     */
    fun recordMetric(name: String, value: Double, unit: String = "")

    /**
     * Whether performance monitoring has been initialized.
     */
    val isInitialized: Boolean
}

/**
 * Convenience: measure a suspend block's duration and record it as a trace.
 */
suspend fun <T> PerformanceMonitor.measure(name: String, block: suspend () -> T): T {
    val traceId = startTrace(name)
    return try {
        block()
    } finally {
        stopTrace(traceId)
    }
}

/**
 * Convenience: measure a non-suspend block's duration.
 */
inline fun <T> PerformanceMonitor.measureSync(name: String, block: () -> T): T {
    val start = Clock.System.now().toEpochMilliseconds()
    return try {
        block()
    } finally {
        val durationMs = Clock.System.now().toEpochMilliseconds() - start
        recordMetric("$name.duration_ms", durationMs.toDouble(), "ms")
    }
}
