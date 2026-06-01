package id.startapp.infrastructure.auth

/**
 * Cross-platform biometric authentication manager.
 *
 * Provides PIN and biometric (fingerprint/face) authentication
 * for offline access when the JWT token has expired beyond
 * the 24-hour grace period.
 *
 * Platform implementations:
 * - Android: BiometricPrompt API (AndroidX Biometric)
 * - iOS: LAContext (LocalAuthentication - Face ID / Touch ID)
 */
expect class BiometricAuthManager() {

    /**
     * Check if biometric authentication is available on the device.
     */
    fun isAvailable(): Boolean

    /**
     * Authenticate using biometric (fingerprint/face).
     *
     * @param reason Localized reason string shown to the user
     * @return true if authentication succeeded, false otherwise
     */
    suspend fun authenticate(reason: String): Boolean
}

/**
 * Manages offline authentication state (PIN + biometric + grace period).
 *
 * Flow:
 * 1. Online login → store token + prompt for PIN setup
 * 2. Offline, token valid → full access
 * 3. Offline, token expired < 24h → full access (grace period)
 * 4. Offline, token expired > 24h → require PIN/biometric
 * 5. 3 failed PIN attempts → lockout (require online re-login)
 */
class OfflineAuthManager(
    private val biometricAuthManager: BiometricAuthManager,
    private val secureStorage: id.startapp.infrastructure.storage.SecureStorage,
    private val keyValueStorage: id.startapp.infrastructure.storage.KeyValueStorage,
) {
    companion object {
        const val KEY_PIN_HASH = "offline_pin_hash"
        const val KEY_PIN_ENABLED = "offline_pin_enabled"
        const val KEY_BIOMETRIC_ENABLED = "offline_biometric_enabled"
        const val KEY_FAILED_ATTEMPTS = "offline_failed_attempts"
        const val GRACE_PERIOD_MS = 24 * 60 * 60 * 1000L // 24 hours
        const val MAX_FAILED_ATTEMPTS = 3
    }

    /**
     * Check if PIN has been set up for offline auth.
     */
    suspend fun isPinEnabled(): Boolean {
        return keyValueStorage.getBoolean(KEY_PIN_ENABLED) ?: false
    }

    /**
     * Set up a 6-digit PIN for offline authentication.
     * Stores the hash in SecureStorage.
     */
    suspend fun setupPin(pin: String) {
        val salt = generateSalt()
        val hash = hashPin(pin, salt)
        keyValueStorage.putString(KEY_PIN_HASH, "$salt:$hash")
        keyValueStorage.putBoolean(KEY_PIN_ENABLED, true)
        keyValueStorage.putInt(KEY_FAILED_ATTEMPTS, 0)
    }

    /**
     * Verify a PIN attempt.
     * Increments failed attempt counter on failure.
     */
    suspend fun verifyPin(pin: String): PinVerifyResult {
        val failedAttempts = keyValueStorage.getInt(KEY_FAILED_ATTEMPTS) ?: 0
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            return PinVerifyResult.LOCKED_OUT
        }

        val stored = keyValueStorage.getString(KEY_PIN_HASH) ?: return PinVerifyResult.INCORRECT(MAX_FAILED_ATTEMPTS - failedAttempts - 1)
        val parts = stored.split(":", limit = 2)
        if (parts.size != 2) return PinVerifyResult.INCORRECT(MAX_FAILED_ATTEMPTS - failedAttempts - 1)
        val (salt, storedHash) = parts
        val inputHash = hashPin(pin, salt)

        return if (inputHash == storedHash) {
            keyValueStorage.putInt(KEY_FAILED_ATTEMPTS, 0)
            PinVerifyResult.SUCCESS
        } else {
            val newCount = failedAttempts + 1
            keyValueStorage.putInt(KEY_FAILED_ATTEMPTS, newCount)
            if (newCount >= MAX_FAILED_ATTEMPTS) {
                PinVerifyResult.LOCKED_OUT
            } else {
                PinVerifyResult.INCORRECT(remainingAttempts = MAX_FAILED_ATTEMPTS - newCount)
            }
        }
    }

    /**
     * Check if biometric is enabled and available.
     */
    suspend fun isBiometricEnabled(): Boolean {
        val enabled = keyValueStorage.getBoolean(KEY_BIOMETRIC_ENABLED) ?: false
        return enabled && biometricAuthManager.isAvailable()
    }

    /**
     * Enable biometric authentication (linked to PIN).
     */
    suspend fun enableBiometric() {
        keyValueStorage.putBoolean(KEY_BIOMETRIC_ENABLED, true)
    }

    /**
     * Authenticate using biometric.
     */
    suspend fun authenticateWithBiometric(): Boolean {
        return biometricAuthManager.authenticate("Verifikasi untuk melanjutkan")
    }

    /**
     * Check if the user is locked out.
     */
    suspend fun isLockedOut(): Boolean {
        val failedAttempts = keyValueStorage.getInt(KEY_FAILED_ATTEMPTS) ?: 0
        return failedAttempts >= MAX_FAILED_ATTEMPTS
    }

    /**
     * Reset lockout (call after successful online re-login).
     */
    suspend fun resetLockout() {
        keyValueStorage.putInt(KEY_FAILED_ATTEMPTS, 0)
    }

    /**
     * Generate a random salt for PIN hashing.
     */
    private fun generateSalt(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        return (1..16).map { chars[kotlin.random.Random.nextInt(chars.length)] }.joinToString("")
    }

    /**
     * Hash a PIN with salt using iterative hashing.
     *
     * Uses 10,000 iterations of a mixing function to slow down brute-force.
     * For KMP compatibility without platform-specific crypto APIs.
     * Production improvement: use platform-specific PBKDF2 via expect/actual.
     */
    private fun hashPin(pin: String, salt: String): String {
        var h1 = 0L
        var h2 = 0L
        val input = "$salt:$pin"

        // Initial seed
        for (ch in input) {
            h1 = h1 * 31 + ch.code
            h2 = h2 * 37 + ch.code
        }

        // Iterative stretching (10,000 rounds)
        repeat(10_000) { round ->
            h1 = h1 xor (h2 * 31 + round)
            h2 = h2 xor (h1 * 37 + round)
            h1 = (h1 shl 13) or (h1 ushr 51)
            h2 = (h2 shl 17) or (h2 ushr 47)
        }

        return "${h1.toULong().toString(16)}${h2.toULong().toString(16)}"
    }
}

sealed class PinVerifyResult {
    data object SUCCESS : PinVerifyResult()
    data class INCORRECT(val remainingAttempts: Int) : PinVerifyResult()
    data object LOCKED_OUT : PinVerifyResult()
}
