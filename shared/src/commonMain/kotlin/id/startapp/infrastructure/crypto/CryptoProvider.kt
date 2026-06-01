package id.startapp.infrastructure.crypto

/**
 * Cross-platform cryptographic provider for AES-256-GCM encryption.
 *
 * Used to encrypt sensitive PII fields (customer name, phone, email,
 * payment amounts) when stored in the local SQLDelight cache.
 *
 * Platform implementations:
 * - Android: AndroidKeyStore + AES-256-GCM (hardware-backed)
 * - iOS: CryptoKit AES.GCM (Secure Enclave when available)
 */
expect class CryptoProvider() {

    /**
     * Encrypt plaintext to a Base64-encoded ciphertext string.
     * Uses AES-256-GCM with a randomly generated IV prepended to the output.
     */
    fun encrypt(plaintext: String): String

    /**
     * Decrypt a Base64-encoded ciphertext string back to plaintext.
     * Expects the IV to be prepended to the ciphertext.
     */
    fun decrypt(ciphertext: String): String
}

/**
 * Encrypt sensitive entity JSON before caching.
 *
 * Wraps a [CryptoProvider] to provide transparent encrypt/decrypt
 * for the cache layer. Non-sensitive entities bypass encryption.
 */
class CacheEncryptor(
    private val cryptoProvider: CryptoProvider,
) {
    companion object {
        /** Entity types containing PII that require encryption */
        val SENSITIVE_ENTITY_TYPES = setOf(
            "customers",
            "addresses",
            "payments",
            "order_pickups",
            "order_deliveries",
        )
    }

    /**
     * Encrypt JSON if the entity type is sensitive.
     */
    fun encryptIfSensitive(entityType: String, jsonData: String): String {
        if (entityType !in SENSITIVE_ENTITY_TYPES) return jsonData
        return try {
            val encrypted = cryptoProvider.encrypt(jsonData)
            "ENCRYPTED:$encrypted" // Prefix to distinguish from unencrypted data
        } catch (e: Exception) {
            // Log encryption failure; store unencrypted as last resort to prevent data loss.
            // In production, consider failing the cache write instead.
            println("CacheEncryptor: Encryption failed for $entityType: ${e.message}")
            jsonData
        }
    }

    /**
     * Decrypt JSON if the entity type is sensitive.
     */
    fun decryptIfSensitive(entityType: String, data: String): String {
        if (entityType !in SENSITIVE_ENTITY_TYPES) return data
        // Check if data was actually encrypted (has prefix)
        if (!data.startsWith("ENCRYPTED:")) return data // Legacy unencrypted data
        val ciphertext = data.removePrefix("ENCRYPTED:")
        return try {
            cryptoProvider.decrypt(ciphertext)
        } catch (e: Exception) {
            println("CacheEncryptor: Decryption failed for $entityType: ${e.message}")
            ciphertext // Return raw ciphertext as fallback
        }
    }
}
