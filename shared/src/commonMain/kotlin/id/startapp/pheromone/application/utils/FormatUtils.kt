package id.startapp.pheromone.application.utils

/**
 * Format time duration with proper pluralization for countdown display.
 *
 * Examples:
 * - "1 second", "5 seconds"
 * - "1 minute", "30 minutes"
 * - "1 hour", "3 hours"
 *
 * @param seconds Duration in seconds
 * @return Formatted duration string with proper pluralization
 */
fun formatDuration(seconds: Long): String {
    return when {
        seconds < 60 -> {
            val unit = if (seconds == 1L) "second" else "seconds"
            "$seconds $unit"
        }
        seconds < 3600 -> {
            val minutes = seconds / 60
            val unit = if (minutes == 1L) "minute" else "minutes"
            "$minutes $unit"
        }
        else -> {
            val hours = seconds / 3600
            val unit = if (hours == 1L) "hour" else "hours"
            "$hours $unit"
        }
    }
}
