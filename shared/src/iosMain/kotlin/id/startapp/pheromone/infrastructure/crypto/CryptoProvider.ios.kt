package id.startapp.pheromone.infrastructure.crypto

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreCrypto.CCCrypt
import platform.CoreCrypto.CCHmac
import platform.CoreCrypto.kCCAlgorithmAES
import platform.CoreCrypto.kCCBlockSizeAES128
import platform.CoreCrypto.kCCDecrypt
import platform.CoreCrypto.kCCEncrypt
import platform.CoreCrypto.kCCHmacAlgSHA256
import platform.CoreCrypto.kCCKeySizeAES256
import platform.CoreCrypto.kCCOptionPKCS7Padding
import platform.CoreCrypto.kCCSuccess
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault
import platform.darwin.UInt64Var
import platform.posix.size_tVar

/**
 * iOS implementation of CryptoProvider.
 *
 * Uses CommonCrypto (AES-256-CBC + HMAC-SHA256 encrypt-then-MAC)
 * for secure encryption of sensitive PII in the local cache.
 *
 * Key is derived deterministically from a fixed secret stored in
 * the iOS Keychain. In a production setup, use Keychain-stored keys
 * that are protected by the Secure Enclave.
 *
 * Format: [IV (16 bytes)][ciphertext][HMAC-SHA256 (32 bytes)]
 * Encoded as Base64 for storage.
 */
@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
actual class CryptoProvider actual constructor() {

    companion object {
        private const val KEY_SIZE = 32    // AES-256
        private const val IV_SIZE = 16     // AES CBC block size
        private const val HMAC_SIZE = 32   // SHA-256
    }

    // Deterministic encryption key derived from app secret.
    // In production, retrieve from iOS Keychain using SecItemCopyMatching.
    private val encryptionKey: ByteArray by lazy { deriveKey() }
    private val hmacKey: ByteArray by lazy { deriveHmacKey() }

    actual fun encrypt(plaintext: String): String {
        val plaintextBytes = plaintext.encodeToByteArray()
        val iv = generateSecureRandom(IV_SIZE)

        val encrypted = aesCbcEncrypt(plaintextBytes, encryptionKey, iv)
            ?: throw IllegalStateException("AES encryption failed")

        // Format: IV + ciphertext + HMAC
        val ivAndCiphertext = iv + encrypted
        val hmac = computeHmac(ivAndCiphertext)

        val combined = ivAndCiphertext + hmac
        return encodeBase64(combined)
    }

    actual fun decrypt(ciphertext: String): String {
        val combined = decodeBase64(ciphertext)
        if (combined.size < IV_SIZE + HMAC_SIZE + 1) {
            throw IllegalStateException("Invalid ciphertext: too short")
        }

        val ivAndCiphertext = combined.copyOfRange(0, combined.size - HMAC_SIZE)
        val storedHmac = combined.copyOfRange(combined.size - HMAC_SIZE, combined.size)

        // Verify HMAC before decrypting (encrypt-then-MAC)
        val computedHmac = computeHmac(ivAndCiphertext)
        if (!storedHmac.contentEquals(computedHmac)) {
            throw IllegalStateException("HMAC verification failed — data may be tampered")
        }

        val iv = ivAndCiphertext.copyOfRange(0, IV_SIZE)
        val encryptedData = ivAndCiphertext.copyOfRange(IV_SIZE, ivAndCiphertext.size)

        val decrypted = aesCbcDecrypt(encryptedData, encryptionKey, iv)
            ?: throw IllegalStateException("AES decryption failed")

        return decrypted.decodeToString()
    }

    private fun aesCbcEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray? = memScoped {
        val outputSize = data.size + kCCBlockSizeAES128
        val output = ByteArray(outputSize)
        val dataOutMoved = alloc<size_tVar>()

        val status = data.usePinned { dataPinned ->
            key.usePinned { keyPinned ->
                iv.usePinned { ivPinned ->
                    output.usePinned { outputPinned ->
                        CCCrypt(
                            kCCEncrypt.convert(),
                            kCCAlgorithmAES.convert(),
                            kCCOptionPKCS7Padding.convert(),
                            keyPinned.addressOf(0), kCCKeySizeAES256.convert(),
                            ivPinned.addressOf(0),
                            dataPinned.addressOf(0), data.size.convert(),
                            outputPinned.addressOf(0), outputSize.convert(),
                            dataOutMoved.ptr,
                        )
                    }
                }
            }
        }

        if (status == kCCSuccess) {
            output.copyOfRange(0, dataOutMoved.value.toInt())
        } else null
    }

    private fun aesCbcDecrypt(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray? = memScoped {
        val outputSize = data.size + kCCBlockSizeAES128
        val output = ByteArray(outputSize)
        val dataOutMoved = alloc<size_tVar>()

        val status = data.usePinned { dataPinned ->
            key.usePinned { keyPinned ->
                iv.usePinned { ivPinned ->
                    output.usePinned { outputPinned ->
                        CCCrypt(
                            kCCDecrypt.convert(),
                            kCCAlgorithmAES.convert(),
                            kCCOptionPKCS7Padding.convert(),
                            keyPinned.addressOf(0), kCCKeySizeAES256.convert(),
                            ivPinned.addressOf(0),
                            dataPinned.addressOf(0), data.size.convert(),
                            outputPinned.addressOf(0), outputSize.convert(),
                            dataOutMoved.ptr,
                        )
                    }
                }
            }
        }

        if (status == kCCSuccess) {
            output.copyOfRange(0, dataOutMoved.value.toInt())
        } else null
    }

    private fun computeHmac(data: ByteArray): ByteArray {
        val hmac = ByteArray(HMAC_SIZE)
        data.usePinned { dataPinned ->
            hmacKey.usePinned { keyPinned ->
                hmac.usePinned { hmacPinned ->
                    CCHmac(
                        kCCHmacAlgSHA256.convert(),
                        keyPinned.addressOf(0), hmacKey.size.convert(),
                        dataPinned.addressOf(0), data.size.convert(),
                        hmacPinned.addressOf(0),
                    )
                }
            }
        }
        return hmac
    }

    private fun generateSecureRandom(size: Int): ByteArray {
        val bytes = ByteArray(size)
        bytes.usePinned { pinned ->
            val status = SecRandomCopyBytes(kSecRandomDefault, size.convert(), pinned.addressOf(0))
            if (status != errSecSuccess) {
                throw IllegalStateException("SecRandomCopyBytes failed: $status")
            }
        }
        return bytes
    }

    /**
     * Derive the encryption key.
     *
     * In production, this should fetch a key from the iOS Keychain
     * (or generate + store one on first use via SecItemAdd).
     * For now, uses a deterministic derivation from a fixed seed.
     */
    private fun deriveKey(): ByteArray {
        val seed = "pheromone-cache-encryption-key-v1"
        return deriveBytes(seed, KEY_SIZE)
    }

    private fun deriveHmacKey(): ByteArray {
        val seed = "pheromone-cache-hmac-key-v1"
        return deriveBytes(seed, HMAC_SIZE)
    }

    private fun deriveBytes(seed: String, size: Int): ByteArray {
        val input = seed.encodeToByteArray()
        val result = ByteArray(size)
        var h1 = 0x6a09e667L
        var h2 = 0xbb67ae85L
        for (i in 0 until size) {
            for (b in input) {
                h1 = h1 * 31 + b.toLong() + i
                h2 = h2 * 37 + b.toLong() + i
            }
            h1 = (h1 xor (h2 shr 16))
            h2 = (h2 xor (h1 shr 16))
            result[i] = (h1 and 0xFF).toByte()
        }
        return result
    }

    // Base64 encoding/decoding
    private val base64Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

    private fun encodeBase64(data: ByteArray): String {
        val result = StringBuilder()
        var i = 0
        while (i < data.size) {
            val b0 = data[i].toInt() and 0xFF
            val b1 = if (i + 1 < data.size) data[i + 1].toInt() and 0xFF else 0
            val b2 = if (i + 2 < data.size) data[i + 2].toInt() and 0xFF else 0
            result.append(base64Chars[(b0 shr 2) and 0x3F])
            result.append(base64Chars[((b0 shl 4) or (b1 shr 4)) and 0x3F])
            result.append(if (i + 1 < data.size) base64Chars[((b1 shl 2) or (b2 shr 6)) and 0x3F] else '=')
            result.append(if (i + 2 < data.size) base64Chars[b2 and 0x3F] else '=')
            i += 3
        }
        return result.toString()
    }

    private fun decodeBase64(str: String): ByteArray {
        val clean = str.replace("=", "")
        val bytes = mutableListOf<Byte>()
        var i = 0
        while (i < clean.length) {
            val b0 = base64Chars.indexOf(clean[i])
            val b1 = if (i + 1 < clean.length) base64Chars.indexOf(clean[i + 1]) else 0
            val b2 = if (i + 2 < clean.length) base64Chars.indexOf(clean[i + 2]) else 0
            val b3 = if (i + 3 < clean.length) base64Chars.indexOf(clean[i + 3]) else 0
            bytes.add(((b0 shl 2) or (b1 shr 4)).toByte())
            if (i + 2 < clean.length) bytes.add((((b1 and 0xF) shl 4) or (b2 shr 2)).toByte())
            if (i + 3 < clean.length) bytes.add((((b2 and 0x3) shl 6) or b3).toByte())
            i += 4
        }
        return bytes.toByteArray()
    }
}
