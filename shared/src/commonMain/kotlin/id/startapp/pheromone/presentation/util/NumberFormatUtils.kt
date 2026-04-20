package id.startapp.pheromone.presentation.util

/**
 * Multiplatform-compatible number formatting utilities.
 *
 * Provides formatting functions that work across all Kotlin targets
 * (JVM, iOS, JS) without relying on platform-specific APIs like String.format().
 */
object NumberFormatUtils {

    /**
     * Format a double value to a specified number of decimal places.
     *
     * @param value The double value to format
     * @param decimals The number of decimal places (default: 4)
     * @return Formatted string with exact decimal places (e.g., "123.4567")
     */
    fun formatDouble(value: Double, decimals: Int = 4): String {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        val rounded = kotlin.math.round(value * multiplier) / multiplier
        val str = rounded.toString()
        val dotIndex = str.indexOf('.')
        return if (dotIndex == -1) {
            "$str.${"0".repeat(decimals)}"
        } else {
            val currentDecimals = str.length - dotIndex - 1
            if (currentDecimals >= decimals) {
                str.substring(0, dotIndex + decimals + 1)
            } else {
                str + "0".repeat(decimals - currentDecimals)
            }
        }
    }

    /**
     * Format coordinates for display.
     *
     * @param latitude Latitude value
     * @param longitude Longitude value
     * @param decimals Number of decimal places (default: 4)
     * @return Formatted string like "Lat: -6.2088, Long: 106.8456"
     */
    fun formatCoordinates(latitude: Double, longitude: Double, decimals: Int = 4): String {
        return "Lat: ${formatDouble(latitude, decimals)}, Long: ${formatDouble(longitude, decimals)}"
    }

    /**
     * Format an integer as Indonesian Rupiah currency.
     *
     * Uses Indonesian thousand separator (period) and "Rp " prefix.
     *
     * @param amount The amount to format
     * @return Formatted string like "Rp 10.000"
     */
    fun formatRupiah(amount: Int): String {
        val prefix = if (amount < 0) "-" else ""
        val abs = kotlin.math.abs(amount)
        return "${prefix}Rp ${abs.toString().reversed().chunked(3).joinToString(".").reversed()}"
    }

    /**
     * Format a long as Indonesian Rupiah currency.
     *
     * @param amount The amount to format
     * @return Formatted string like "Rp 10.000.000"
     */
    fun formatRupiah(amount: Long): String {
        val prefix = if (amount < 0) "-" else ""
        val abs = kotlin.math.abs(amount)
        return "${prefix}Rp ${abs.toString().reversed().chunked(3).joinToString(".").reversed()}"
    }

    /**
     * Format a Double as Indonesian Rupiah currency.
     *
     * Rounds to the nearest whole number using [kotlin.math.roundToLong] before
     * formatting, avoiding truncation issues with `.toInt()` (e.g., 9999.9 → 10000
     * instead of 9999). Safe for IDR which has no sub-unit denominations.
     *
     * @param amount The amount to format
     * @return Formatted string like "Rp 10.000"
     */
    fun formatRupiah(amount: Double): String {
        return formatRupiah(kotlin.math.round(amount).toLong())
    }

    /**
     * Format a double with Indonesian thousands separator (dots).
     * Drops decimal places if the value is a whole number.
     * Handles negative values correctly.
     *
     * Examples:
     * - 10000.0   → "10.000"
     * - 1500.50   → "1.500,50"
     * - -1500.50  → "-1.500,50"
     *
     * @param value The number to format
     * @return Formatted string with dot separators and optional comma decimals
     */
    fun formatNumber(value: Double): String {
        // Handle negative values: format the absolute value and prepend "-"
        val negative = value < 0
        val absValue = kotlin.math.abs(value)

        // Use integer math to avoid floating-point precision issues.
        // Multiply by 100 first, round, then split into integer and decimal parts.
        val totalCents = kotlin.math.round(absValue * 100).toLong()
        val intPart = totalCents / 100
        val decPart = (totalCents % 100).toInt()

        val formattedInt = intPart.toString().reversed().chunked(3).joinToString(".").reversed()
        val prefix = if (negative) "-" else ""
        return if (decPart == 0) {
            "$prefix$formattedInt"
        } else {
            "$prefix$formattedInt,${decPart.toString().padStart(2, '0')}"
        }
    }
}
