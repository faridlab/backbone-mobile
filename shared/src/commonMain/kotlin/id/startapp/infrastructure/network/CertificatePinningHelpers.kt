package id.startapp.infrastructure.network

import kotlinx.datetime.Clock

/**
 * Certificate pinning utilities and helpers.
 *
 * Provides functions for computing certificate hashes and validating
 * certificates against pinned hashes.
 *
 * Note: Certificate validation is platform-specific due to different
 * certificate APIs on Android (java.security.cert) and iOS (Security framework).
 * This file provides common utilities and interface definitions.
 */

/**
 * Result of certificate validation.
 */
sealed class CertificateValidationResult {
    /** Certificate is valid and matches a pinned hash */
    data object Valid : CertificateValidationResult()

    /** Certificate is invalid or doesn't match any pinned hash */
    data class Invalid(val reason: String) : CertificateValidationResult()

    /** Certificate pinning is not configured for this host */
    data object NotConfigured : CertificateValidationResult()
}

/**
 * Certificate pinning configuration.
 *
 * @param host The hostname to pin certificates for
 * @param pinnedHashes List of base64-encoded SHA-256 certificate hashes
 * @param includeSubdomains Whether to pin subdomains as well
 * @param expiryDate Optional expiry date for the pinning configuration
 */
data class CertificatePinningConfig(
    val host: String,
    val pinnedHashes: Set<String>,
    val includeSubdomains: Boolean = false,
    val expiryDate: Long? = null
) {
    /**
     * Check if this config applies to the given host.
     */
    fun appliesTo(host: String): Boolean {
        return when {
            this.host == host -> true
            includeSubdomains && host.endsWith(".$this.host") -> true
            else -> false
        }
    }

    /**
     * Check if a certificate hash matches any of the pinned hashes.
     *
     * @param hash Base64-encoded SHA-256 hash of the certificate
     * @return true if the hash matches a pinned hash
     */
    fun matchesHash(hash: String): Boolean {
        return pinnedHashes.contains(hash.trim())
    }

    /**
     * Check if this configuration has expired.
     */
    fun isExpired(): Boolean {
        if (expiryDate == null) return false
        // Use kotlinx.datetime.Clock for KMP compatibility
        val currentTime = Clock.System.now().toEpochMilliseconds()
        return currentTime > expiryDate
    }

    companion object {
        /**
         * Create a config for a single host with one pinned hash.
         */
        fun forHost(host: String, vararg hashes: String): CertificatePinningConfig {
            return CertificatePinningConfig(
                host = host,
                pinnedHashes = hashes.toSet()
            )
        }

        /**
         * Create a config for a host and subdomains.
         */
        fun forDomain(domain: String, vararg hashes: String): CertificatePinningConfig {
            return CertificatePinningConfig(
                host = domain,
                pinnedHashes = hashes.toSet(),
                includeSubdomains = true
            )
        }
    }
}

/**
 * Certificate pinning manager.
 *
 * Manages certificate pinning configurations for multiple hosts.
 *
 * Threading Model:
 * - Configurations should be set up once during app initialization
 * - Reads are thread-safe on all platforms
 * - Modifications after initialization are not thread-safe and should be avoided
 *
 * This pattern is appropriate for certificate pinning which is typically
 * configured once at startup and read frequently during network operations.
 */
object CertificatePinningManager {

    /** All configured certificate pins */
    private val configs = mutableSetOf<CertificatePinningConfig>()

    /**
     * Add a certificate pinning configuration.
     *
     * Note: This should only be called during app initialization
     * before any network requests are made.
     *
     * @param config The configuration to add
     */
    fun addConfig(config: CertificatePinningConfig) {
        configs.add(config)
    }

    /**
     * Add certificate pinning configurations.
     *
     * Note: This should only be called during app initialization
     * before any network requests are made.
     *
     * @param configs The configurations to add
     */
    fun addConfigs(configs: List<CertificatePinningConfig>) {
        this.configs.addAll(configs)
    }

    /**
     * Remove a certificate pinning configuration for a host.
     *
     * @param host The host to remove configuration for
     */
    fun removeConfig(host: String) {
        configs.removeAll { it.host == host }
    }

    /**
     * Clear all certificate pinning configurations.
     */
    fun clearConfigs() {
        configs.clear()
    }

    /**
     * Get the configuration for a given host.
     *
     * @param host The hostname to look up
     * @return The matching configuration, or null if not found
     */
    fun getConfigForHost(host: String): CertificatePinningConfig? {
        return configs
            .filter { !it.isExpired() }
            .firstOrNull { it.appliesTo(host) }
    }

    /**
     * Check if a host has certificate pinning configured.
     *
     * @param host The hostname to check
     * @return true if certificate pinning is configured for this host
     */
    fun isConfiguredForHost(host: String): Boolean {
        return getConfigForHost(host) != null
    }

    /**
     * Validate a certificate hash against pinned hashes for a host.
     *
     * @param host The hostname
     * @param certificateHash Base64-encoded SHA-256 hash of the certificate
     * @return CertificateValidationResult indicating validity
     */
    fun validateCertificate(
        host: String,
        certificateHash: String
    ): CertificateValidationResult {
        val config = getConfigForHost(host)
            ?: return CertificateValidationResult.NotConfigured

        if (config.isExpired()) {
            return CertificateValidationResult.Invalid("Certificate pinning configuration has expired")
        }

        return if (config.matchesHash(certificateHash)) {
            CertificateValidationResult.Valid
        } else {
            CertificateValidationResult.Invalid("Certificate hash does not match any pinned hash")
        }
    }

    /**
     * Get all configured hosts.
     *
     * @return Set of configured hostnames
     */
    fun getConfiguredHosts(): Set<String> {
        return configs.map { it.host }.toSet()
    }
}

/**
 * Platform-specific SHA-256 hash computation for certificate pinning.
 *
 * @param certificateData DER-encoded certificate data
 * @return Base64-encoded SHA-256 hash
 */
expect fun computeCertificateSha256Hash(certificateData: ByteArray): String

/**
 * Platform-specific public key hash computation for certificate pinning.
 *
 * Extracts the public key from the certificate using platform-specific APIs
 * and hashes it with SHA-256.
 *
 * This is more flexible than pinning the entire certificate as it allows
 * certificate rotation while keeping the same key pair.
 *
 * @param certificateData DER-encoded certificate data
 * @return Base64-encoded SHA-256 hash of the public key
 */
expect fun computeCertificatePublicKeyHash(certificateData: ByteArray): String

/**
 * Helper object for computing certificate hashes.
 *
 * Provides a convenient API that wraps the platform-specific expect functions.
 */
object CertificateHashHelper {

    /**
     * Compute SHA-256 hash of a certificate.
     *
     * @param certificateData DER-encoded certificate data
     * @return Base64-encoded SHA-256 hash
     */
    fun computeSha256Hash(certificateData: ByteArray): String {
        return computeCertificateSha256Hash(certificateData)
    }

    /**
     * Extract public key info from a certificate for hashing.
     * This is more flexible than pinning the entire certificate.
     *
     * @param certificateData DER-encoded certificate data
     * @return Base64-encoded SHA-256 hash of the public key info
     */
    fun computePublicKeyHash(certificateData: ByteArray): String {
        return computeCertificatePublicKeyHash(certificateData)
    }

    /**
     * Format a certificate hash for display/comparison.
     * Removes whitespace and converts to uppercase.
     *
     * @param hash The hash to format
     * @return Formatted hash string
     */
    fun formatHash(hash: String): String {
        return hash
            .replace("\\s".toRegex(), "")
            .replace("=", "")
            .uppercase()
    }

    /**
     * Validate a hash string format.
     *
     * @param hash The hash to validate
     * @return true if the hash is valid base64
     */
    fun isValidHashFormat(hash: String): Boolean {
        val formatted = formatHash(hash)
        return formatted.isNotEmpty() &&
            formatted.all { it.isLetterOrDigit() || it == '+' || it == '/' }
    }
}

/**
 * Extension function to validate a list of certificate hashes.
 *
 * @param host The hostname
 * @param certificateHashes List of certificate hashes (in chain order)
 * @return CertificateValidationResult indicating validity
 */
fun CertificatePinningManager.validateCertificateChain(
    host: String,
    certificateHashes: List<String>
): CertificateValidationResult {
    val config = getConfigForHost(host)
        ?: return CertificateValidationResult.NotConfigured

    if (config.isExpired()) {
        return CertificateValidationResult.Invalid("Certificate pinning configuration has expired")
    }

    // At least one certificate in the chain must match a pinned hash
    val hasMatch = certificateHashes.any { hash ->
        config.matchesHash(hash)
    }

    return if (hasMatch) {
        CertificateValidationResult.Valid
    } else {
        CertificateValidationResult.Invalid("No certificate in chain matches any pinned hash")
    }
}
