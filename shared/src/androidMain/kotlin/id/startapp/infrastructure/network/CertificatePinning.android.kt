package id.startapp.infrastructure.network

import io.ktor.client.HttpClient
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Base64

/**
 * Android implementation of certificate pinning configuration.
 *
 * For Android, certificate pinning is configured via network_security_config.xml
 * rather than programmatically. This provides better security and maintainability.
 *
 * To enable certificate pinning:
 * 1. Create res/xml/network_security_config.xml
 * 2. Add pinned certificates for your API domain
 * 3. Reference it in AndroidManifest.xml:
 *    <application
 *        android:networkSecurityConfig="@xml/network_security_config"
 *        ...>
 *
 * Example network_security_config.xml:
 * ```xml
 * <?xml version="1.0" encoding="utf-8"?>
 * <network-security-config>
 *     <domain-config>
 *         <domain includeSubdomains="true">api.example.com</domain>
 *         <pin-set>
 *             <pin digest="SHA-256">AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=</pin>
 *             <pin digest="SHA-256">BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=</pin>
 *             <!-- Backup pin for rotation -->
 *             <pin digest="SHA-256">CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC=</pin>
 *         </pin-set>
 *         <!-- Trust system certificates for other domains -->
 *         <trust-anchors>
 *             <certificates src="system" />
 *         </trust-anchors>
 *     </domain-config>
 * </network-security-config>
 * ```
 *
 * To get certificate pins for your domain, run:
 * ```bash
 * openssl s_client -connect api.example.com:443 -showcerts </dev/null 2>/dev/null | \
 *   openssl x509 -pubkey -noout | openssl rsa -pubin -outform der | \
 *   openssl dgst -sha256 -binary | openssl enc -base64
 * ```
 */
fun configureCertificatePinning(client: HttpClient) {
    // Certificate pinning on Android is handled via network_security_config.xml
    // No programmatic configuration needed here.
    // The Android engine will automatically apply the security config.

    // Note: If programmatic pinning is still desired, you can use a custom
    // OkHttpClient with certificate pinner configured.
}

/**
 * Android implementation of certificate SHA-256 hash computation.
 * Uses java.security for SHA-256 hashing.
 *
 * @param certificateData DER-encoded certificate data
 * @return Base64-encoded SHA-256 hash
 */
actual fun computeCertificateSha256Hash(certificateData: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(certificateData)
    return Base64.getEncoder().encodeToString(hash)
}

/**
 * Android implementation of public key hash computation.
 * Extracts the public key from the certificate and hashes it.
 *
 * This is more flexible than pinning the entire certificate as it allows
 * certificate rotation while keeping the same key pair.
 *
 * @param certificateData DER-encoded certificate data
 * @return Base64-encoded SHA-256 hash of the public key
 */
actual fun computeCertificatePublicKeyHash(certificateData: ByteArray): String {
    val certFactory = CertificateFactory.getInstance("X.509")
    val cert = certFactory.generateCertificate(certificateData.inputStream()) as X509Certificate
    val publicKey = cert.publicKey.encoded

    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(publicKey)
    return Base64.getEncoder().encodeToString(hash)
}

/**
 * Helper function to extract certificate from an SSL connection.
 * Useful for debugging and obtaining certificate pins.
 *
 * @param host The hostname to connect to
 * @param port The port (typically 443 for HTTPS)
 * @return The DER-encoded certificate data, or null if extraction fails
 */
fun extractCertificateFromHost(host: String, port: Int = 443): ByteArray? {
    return try {
        val sslContext = javax.net.ssl.SSLContext.getInstance("TLS")
        sslContext.init(null, null, null)

        val socketFactory = sslContext.socketFactory
        val socket = socketFactory.createSocket(host, port) as javax.net.ssl.SSLSocket
        socket.startHandshake()

        val session = socket.session
        val certificates = session.peerCertificates

        if (certificates.isNotEmpty()) {
            certificates[0].encoded
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Generate a certificate pin for a host.
 * Useful for obtaining pins to add to network_security_config.xml.
 *
 * @param host The hostname
 * @param port The port (typically 443 for HTTPS)
 * @return The base64-encoded SHA-256 hash, or an error message
 */
fun generateCertificatePin(host: String, port: Int = 443): String {
    val certData = extractCertificateFromHost(host, port)
    return if (certData != null) {
        computeCertificateSha256Hash(certData)
    } else {
        "Failed to extract certificate from $host:$port"
    }
}
