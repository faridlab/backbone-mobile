package id.startapp.presentation.util

/**
 * Platform-specific image compressor.
 *
 * Compresses image bytes to fit within [MAX_SIZE_BYTES] while maintaining
 * the highest possible JPEG quality via binary search on the quality parameter.
 *
 * On Android: Uses Bitmap.compress() with quality binary search.
 * On iOS: Not yet implemented (returns input unchanged).
 */
expect object ImageCompressor {
    /**
     * Maximum allowed image size in bytes (100KB).
     */
    val MAX_SIZE_BYTES: Int

    /**
     * Compress image bytes to fit within [MAX_SIZE_BYTES].
     *
     * Uses binary search on JPEG quality (100→down) to find the highest
     * quality that produces output ≤ [MAX_SIZE_BYTES].
     *
     * If the input is already within the size limit, it is returned unchanged.
     *
     * @param imageBytes Raw image bytes (JPEG, PNG, or WebP)
     * @return Compressed JPEG bytes ≤ [MAX_SIZE_BYTES], or original if already small enough
     */
    suspend fun compress(imageBytes: ByteArray): ByteArray
}
