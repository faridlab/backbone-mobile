package id.startapp.infrastructure.auth

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.WeakReference
import kotlin.coroutines.resume

/**
 * Android implementation of BiometricAuthManager.
 *
 * Uses AndroidX Biometric library for fingerprint/face authentication.
 *
 * Requires dependency in build.gradle.kts:
 *   implementation("androidx.biometric:biometric:1.2.0-alpha05")
 *
 * Call [setActivity] from your Activity's onResume and [clearActivity]
 * from onPause to keep the reference current.
 */
actual class BiometricAuthManager actual constructor() {

    private var activityRef: WeakReference<FragmentActivity>? = null
    private var contextRef: WeakReference<Context>? = null

    /**
     * Set the current activity for showing the BiometricPrompt.
     * Call from Activity.onResume().
     */
    fun setActivity(activity: FragmentActivity) {
        activityRef = WeakReference(activity)
        contextRef = WeakReference(activity.applicationContext)
    }

    /**
     * Clear the activity reference to avoid leaks.
     * Call from Activity.onPause().
     */
    fun clearActivity() {
        activityRef = null
    }

    actual fun isAvailable(): Boolean {
        val context = contextRef?.get() ?: return false
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    actual suspend fun authenticate(reason: String): Boolean {
        val activity = activityRef?.get() ?: return false

        return suspendCancellableCoroutine { continuation ->
            val executor = ContextCompat.getMainExecutor(activity)

            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    if (continuation.isActive) {
                        continuation.resume(true)
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (continuation.isActive) {
                        continuation.resume(false)
                    }
                }

                override fun onAuthenticationFailed() {
                    // Called on a single failed attempt; the prompt stays open.
                    // The system handles retry — we only resolve on error or success.
                }
            }

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Verifikasi Biometrik")
                .setSubtitle(reason)
                .setNegativeButtonText("Gunakan PIN")
                .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
                .build()

            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            biometricPrompt.authenticate(promptInfo)

            continuation.invokeOnCancellation {
                biometricPrompt.cancelAuthentication()
            }
        }
    }
}
