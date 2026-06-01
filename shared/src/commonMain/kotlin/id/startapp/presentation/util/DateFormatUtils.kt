package id.startapp.presentation.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Multiplatform-compatible date formatting utilities.
 *
 * Provides formatting functions that work across all Kotlin targets
 * (JVM, iOS, JS) without relying on platform-specific APIs.
 */
object DateFormatUtils {

    // Format pattern constants
    private const val DATE_SEPARATOR = "/"
    private const val TIME_SEPARATOR = ":"
    private const val DATETIME_SEPARATOR = " "
    private const val PAD_CHAR = '0'
    private const val PAD_LENGTH = 2

    /**
     * Format a LocalDateTime as a date string in DD/MM/YYYY format.
     *
     * @param dateTime The LocalDateTime to format
     * @return Formatted string like "09/02/2026"
     */
    fun formatDate(dateTime: LocalDateTime): String {
        val day = dateTime.dayOfMonth.toString().padStart(PAD_LENGTH, PAD_CHAR)
        val month = dateTime.monthNumber.toString().padStart(PAD_LENGTH, PAD_CHAR)
        val year = dateTime.year.toString()
        return "$day$DATE_SEPARATOR$month$DATE_SEPARATOR$year"
    }

    /**
     * Format a LocalDateTime as a time string in HH:mm format.
     *
     * @param dateTime The LocalDateTime to format
     * @return Formatted string like "14:30"
     */
    fun formatTime(dateTime: LocalDateTime): String {
        val hour = dateTime.hour.toString().padStart(PAD_LENGTH, PAD_CHAR)
        val minute = dateTime.minute.toString().padStart(PAD_LENGTH, PAD_CHAR)
        return "$hour$TIME_SEPARATOR$minute"
    }

    /**
     * Format a LocalDateTime as a date string in YYMMDD format.
     * Used for invoice ID prefixes.
     *
     * @param dateTime The LocalDateTime to format
     * @return Formatted string like "260209"
     */
    fun formatDateCompact(dateTime: LocalDateTime): String {
        val year = (dateTime.year % 100).toString().padStart(PAD_LENGTH, PAD_CHAR)
        val month = dateTime.monthNumber.toString().padStart(PAD_LENGTH, PAD_CHAR)
        val day = dateTime.dayOfMonth.toString().padStart(PAD_LENGTH, PAD_CHAR)
        return "$year$month$day"
    }

    /**
     * Format a LocalDateTime as a full datetime string.
     *
     * @param dateTime The LocalDateTime to format
     * @return Formatted string like "09/02/2026 14:30"
     */
    fun formatDateTime(dateTime: LocalDateTime): String {
        return "${formatDate(dateTime)}$DATETIME_SEPARATOR${formatTime(dateTime)}"
    }

    /**
     * Format an Instant as a localized Indonesian datetime string.
     *
     * @param instant The Instant to format
     * @return Formatted string like "Senin, 5 Mar 2026 • 14:00"
     */
    fun formatIndonesianDateTime(instant: Instant): String {
        val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val dayName = INDONESIAN_DAY_NAMES[dt.dayOfWeek.ordinal]
        val monthName = INDONESIAN_MONTH_ABBREVIATIONS[dt.monthNumber - 1]
        return "$dayName, ${dt.dayOfMonth} $monthName ${dt.year} \u2022 ${formatTime(dt)}"
    }

    /**
     * Format an Instant as DD/MM/YYYY HH:mm using the system timezone.
     *
     * @param instant The Instant to format
     * @return Formatted string like "09/03/2026 14:30"
     */
    fun formatDateTime(instant: Instant): String {
        val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return formatDateTime(dt)
    }

    private val INDONESIAN_DAY_NAMES = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
    private val INDONESIAN_MONTH_ABBREVIATIONS = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
}
