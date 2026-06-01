package id.startapp.infrastructure.network

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Android implementation of HMAC-SHA256 using javax.crypto.
 */
@OptIn(ExperimentalEncodingApi::class)
actual fun hmacSha256Platform(key: String, data: String): String {
    val hmacSha256 = "HmacSHA256"
    val keySpec = SecretKeySpec(key.toByteArray(), hmacSha256)

    val mac = Mac.getInstance(hmacSha256)
    mac.init(keySpec)
    mac.update(data.toByteArray())

    val signature = mac.doFinal()
    return Base64.encode(signature)
}
