package id.startapp.pheromone.infrastructure.auth

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import platform.Foundation.NSError
import kotlin.coroutines.resume

/**
 * iOS implementation of BiometricAuthManager.
 *
 * Uses LAContext (LocalAuthentication framework) for Face ID / Touch ID.
 * Face ID requires NSFaceIDUsageDescription in Info.plist.
 */
actual class BiometricAuthManager actual constructor() {

    actual fun isAvailable(): Boolean {
        val context = LAContext()
        return context.canEvaluatePolicy(
            LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            error = null,
        )
    }

    actual suspend fun authenticate(reason: String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val context = LAContext()

            context.evaluatePolicy(
                LAPolicyDeviceOwnerAuthenticationWithBiometrics,
                localizedReason = reason,
            ) { success, _ ->
                if (continuation.isActive) {
                    continuation.resume(success)
                }
            }
        }
    }
}
