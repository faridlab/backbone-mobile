package id.startapp.pheromone.presentation.util

import android.content.Intent
import android.net.Uri

/**
 * Android implementation of [ShareHelper].
 *
 * Uses Android Intents for WhatsApp deep-linking and the system share sheet.
 */
actual object ShareHelper {

    actual fun shareViaWhatsApp(phone: String, message: String) {
        val context = PlatformContext.applicationContext
        if (context == null) {
            android.util.Log.e("ShareHelper", "PlatformContext.applicationContext is null — did you call PlatformContext.applicationContext = this in Application.onCreate()?")
            return
        }
        // Sanitize phone: keep only digits and leading '+'
        val hasLeadingPlus = phone.startsWith('+')
        val digitsOnly = phone.filter { it.isDigit() }
        val sanitizedPhone = if (hasLeadingPlus) "+$digitsOnly" else digitsOnly
        if (sanitizedPhone.isBlank()) return
        val encodedMessage = Uri.encode(message)
        val url = "https://wa.me/$sanitizedPhone?text=$encodedMessage"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to generic share if WhatsApp is not installed
            shareText(message, "Share Receipt")
        }
    }

    actual fun shareText(text: String, title: String) {
        val context = PlatformContext.applicationContext
        if (context == null) {
            android.util.Log.e("ShareHelper", "PlatformContext.applicationContext is null — did you call PlatformContext.applicationContext = this in Application.onCreate()?")
            return
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            if (title.isNotEmpty()) putExtra(Intent.EXTRA_SUBJECT, title)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(
            Intent.createChooser(intent, title).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
    }
}

/**
 * Thread-safe holder for the Android application context.
 *
 * Initialize this in your Application.onCreate() (runs on the main thread
 * before any other component):
 * ```
 * class MyApp : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         PlatformContext.applicationContext = this
 *     }
 * }
 * ```
 */
object PlatformContext {
    @Volatile
    var applicationContext: android.content.Context? = null
        set(value) {
            // Always store the Application context to prevent Activity memory leaks
            field = value?.applicationContext
        }
}
