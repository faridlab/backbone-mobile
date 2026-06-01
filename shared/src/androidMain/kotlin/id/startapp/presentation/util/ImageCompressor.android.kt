package id.startapp.presentation.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Android image compressor using [Bitmap.compress].
 *
 * Strategy:
 * 1. If input ≤ 100KB → return as-is (no decode needed).
 * 2. Decode to Bitmap. If the image is very large, down-sample first
 *    to reduce memory and output size.
 * 3. Binary search on JPEG quality (100→down) to find the highest quality
 *    that produces output ≤ 100KB.
 * 4. Minimum quality floor is 20 to avoid unacceptable artifacts.
 */
actual object ImageCompressor {

    actual val MAX_SIZE_BYTES: Int = 100 * 1024 // 100KB

    private const val MIN_QUALITY = 20
    private const val MAX_DIMENSION = 1280

    actual suspend fun compress(imageBytes: ByteArray): ByteArray = withContext(Dispatchers.Default) {
        if (imageBytes.size <= MAX_SIZE_BYTES) {
            return@withContext imageBytes
        }

        val bitmap = decodeSampled(imageBytes)
            ?: return@withContext imageBytes // undecodable → return original

        val resized = downscaleIfNeeded(bitmap)

        // Try quality 90 first (common fast path)
        val quick = compressToJpeg(resized, 90)
        if (quick.size <= MAX_SIZE_BYTES) {
            recycleSafely(bitmap, resized)
            return@withContext quick
        }

        // Binary search: find highest quality that fits
        var lo = MIN_QUALITY
        var hi = 89
        var best = quick // fallback

        while (lo <= hi) {
            val mid = (lo + hi) / 2
            val attempt = compressToJpeg(resized, mid)
            if (attempt.size <= MAX_SIZE_BYTES) {
                best = attempt
                lo = mid + 1 // try higher quality
            } else {
                hi = mid - 1 // need lower quality
            }
        }

        recycleSafely(bitmap, resized)
        best
    }

    /**
     * Decode with inSampleSize to avoid OOM on very large images.
     */
    private fun decodeSampled(bytes: ByteArray): Bitmap? {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)

        val width = opts.outWidth
        val height = opts.outHeight
        if (width <= 0 || height <= 0) return null

        var sampleSize = 1
        while (width / sampleSize > MAX_DIMENSION * 2 || height / sampleSize > MAX_DIMENSION * 2) {
            sampleSize *= 2
        }

        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts)
    }

    /**
     * Scale down to MAX_DIMENSION if either side exceeds it, preserving aspect ratio.
     */
    private fun downscaleIfNeeded(bitmap: Bitmap): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= MAX_DIMENSION && h <= MAX_DIMENSION) return bitmap

        val scale = MAX_DIMENSION.toFloat() / maxOf(w, h)
        val newW = (w * scale).toInt().coerceAtLeast(1)
        val newH = (h * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
    }

    private fun compressToJpeg(bitmap: Bitmap, quality: Int): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.toByteArray()
    }

    private fun recycleSafely(vararg bitmaps: Bitmap) {
        val seen = mutableSetOf<Bitmap>()
        for (bmp in bitmaps) {
            if (seen.add(bmp) && !bmp.isRecycled) {
                bmp.recycle()
            }
        }
    }
}
