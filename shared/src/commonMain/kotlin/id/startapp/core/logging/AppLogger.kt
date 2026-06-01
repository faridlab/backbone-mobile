package id.startapp.core.logging

import id.startapp.infrastructure.monitoring.CrashReporter

/**
 * Lightweight structured logger for commonMain (Phase 8A).
 *
 * Routes info/error breadcrumbs through the existing [CrashReporter] (Sentry)
 * so all service lifecycle events appear in crash reports and the Logcat/Xcode
 * console simultaneously. Debug logs are printed to console only to avoid
 * breadcrumb noise in non-error sessions.
 *
 * ## Usage
 * ```kotlin
 * private val logger = AppLogger.withTag("OrderService")
 *
 * logger.d { "Creating order" }
 * logger.i { "Order created: $id" }
 * logger.e(throwable) { "Order creation failed" }
 * ```
 */
object AppLogger {
    fun withTag(tag: String): TaggedLogger = TaggedLogger(tag)
}

class TaggedLogger(private val tag: String) {

    /** Debug — console only. Not sent to Sentry to avoid breadcrumb noise. */
    fun d(message: () -> String) {
        println("D/$tag: ${message()}")
    }

    /** Info — console + Sentry breadcrumb for operational visibility. */
    fun i(message: () -> String) {
        val msg = message()
        println("I/$tag: $msg")
        if (CrashReporter.isInitialized) {
            CrashReporter.addBreadcrumb(category = tag, message = msg)
        }
    }

    /**
     * Error — console + Sentry breadcrumb + exception capture.
     *
     * @param throwable Optional exception to forward to the crash reporter.
     */
    fun e(throwable: Throwable? = null, message: () -> String) {
        val msg = message()
        println("E/$tag: $msg${throwable?.let { " — ${it.message}" } ?: ""}")
        if (CrashReporter.isInitialized) {
            CrashReporter.addBreadcrumb(
                category = tag,
                message = msg,
                data = throwable?.let { mapOf("exception" to (it::class.simpleName ?: "Unknown")) }
                    ?: emptyMap(),
            )
            throwable?.let { CrashReporter.captureException(it) }
        }
    }
}
