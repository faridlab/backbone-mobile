package id.startapp.presentation.ui.components.input.textfields

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.startapp.presentation.ui.theme.BackboneBlue
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * Date Range Text Field
 *
 * Combined date field for selecting a date range (start and end).
 *
 * Features:
 * - Start and end date pickers
 * - Range validation (end >= start)
 * - Pre-defined range presets (Today, This Week, This Month, etc.)
 *
 * @param dateRange Pair of start and end dates
 * @param onDateRangeChange Callback when date range changes
 * @param modifier Modifier for the text fields
 * @param externalLabel Label text shown above fields
 * @param enabled Whether the fields are enabled
 * @param minDate Minimum selectable date
 * @param maxDate Maximum selectable date
 * @param showPresets Whether to show quick preset buttons
 * @param startError Error message for start date
 * @param endError Error message for end date
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeTextField(
    dateRange: Pair<LocalDate?, LocalDate?>,
    onDateRangeChange: (Pair<LocalDate?, LocalDate?>) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = "Date Range",
    enabled: Boolean = true,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    showPresets: Boolean = true,
    startError: String? = null,
    endError: String? = null
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    // Simple date formatter for display
    fun formatDate(localDate: LocalDate): String {
        val monthNames = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        return "${localDate.dayOfMonth} ${monthNames[localDate.monthNumber - 1]} ${localDate.year}"
    }

    // Start date picker
    if (showStartPicker) {
        val startState = rememberDatePickerState(
            initialSelectedDateMillis = localDateToMillis(dateRange.first),
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val localDate = millisToLocalDate(utcTimeMillis)
                    return (minDate == null || localDate >= minDate) &&
                           (maxDate == null || localDate <= (dateRange.second ?: maxDate ?: localDate))
                }
                override fun isSelectableYear(year: Int): Boolean = true
            }
        )

        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        startState.selectedDateMillis?.let { millis ->
                            val localDate = millisToLocalDate(millis)
                            onDateRangeChange(localDate to dateRange.second)
                        }
                        showStartPicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showStartPicker = false }
                ) { Text("Cancel") }
            }
        ) {
            androidx.compose.material3.DatePicker(state = startState)
        }
    }

    // End date picker
    if (showEndPicker) {
        val endState = rememberDatePickerState(
            initialSelectedDateMillis = localDateToMillis(dateRange.second),
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val localDate = millisToLocalDate(utcTimeMillis)
                    return (dateRange.first == null || localDate >= dateRange.first!!) &&
                           (maxDate == null || localDate <= maxDate)
                }
                override fun isSelectableYear(year: Int): Boolean = true
            }
        )

        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        endState.selectedDateMillis?.let { millis ->
                            val localDate = millisToLocalDate(millis)
                            onDateRangeChange(dateRange.first to localDate)
                        }
                        showEndPicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEndPicker = false }
                ) { Text("Cancel") }
            }
        ) {
            androidx.compose.material3.DatePicker(state = endState)
        }
    }

    Column(modifier = modifier) {
        // External label
        externalLabel?.let { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Presets
        if (showPresets && enabled) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateRangePresetChip(text = "Today", onClick = {
                    val today = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
                    onDateRangeChange(Pair(today, today))
                })
                DateRangePresetChip(text = "This Week", onClick = {
                    val today = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
                    val dayOfWeek = today.dayOfWeek.ordinal  // Monday = 0, Sunday = 6
                    val weekStart = today.minus(kotlinx.datetime.DatePeriod(days = dayOfWeek))
                    val weekEnd = weekStart.plus(kotlinx.datetime.DatePeriod(days = 6))
                    onDateRangeChange(Pair(weekStart, weekEnd))
                })
                DateRangePresetChip(text = "This Month", onClick = {
                    val today = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
                    val monthStart = LocalDate(today.year, today.month, 1)
                    val monthEnd = monthStart.plus(kotlinx.datetime.DatePeriod(months = 1)).minus(kotlinx.datetime.DatePeriod(days = 1))
                    onDateRangeChange(Pair(monthStart, monthEnd))
                })
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Date fields row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Start date
            OutlinedButton(
                onClick = { if (enabled) showStartPicker = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, if (startError != null) id.startapp.presentation.ui.theme.ErrorRed else BackboneBlue
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = "Select start date",
                    tint = if (startError != null) id.startapp.presentation.ui.theme.ErrorRed else BackboneBlue,
                    modifier = Modifier.width(18.dp).height(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dateRange.first?.let { formatDate(it) } ?: "Start",
                    color = if (startError != null) id.startapp.presentation.ui.theme.ErrorRed else MaterialTheme.colorScheme.onSurface
                )
            }

            // Separator
            Text(
                text = "—",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // End date
            OutlinedButton(
                onClick = { if (enabled) showEndPicker = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, if (endError != null) id.startapp.presentation.ui.theme.ErrorRed else BackboneBlue
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = "Select end date",
                    tint = if (endError != null) id.startapp.presentation.ui.theme.ErrorRed else BackboneBlue,
                    modifier = Modifier.width(18.dp).height(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dateRange.second?.let { formatDate(it) } ?: "End",
                    color = if (endError != null) id.startapp.presentation.ui.theme.ErrorRed else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Validation error for range
        if (dateRange.first != null && dateRange.second != null && dateRange.second!! < dateRange.first!!) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "End date must be after start date",
                style = MaterialTheme.typography.bodySmall,
                color = id.startapp.presentation.ui.theme.ErrorRed
            )
        }
    }
}

@Composable
private fun DateRangePresetChip(
    text: String,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(text, style = MaterialTheme.typography.bodySmall) }
    )
}

// Helper functions for LocalDate <-> millis conversion
// Using epoch days for simpler conversion in common code
private fun localDateToMillis(localDate: LocalDate?): Long? {
    if (localDate == null) return null
    // Approximate conversion: days since epoch * milliseconds per day
    val epochDay = localDate.toEpochDays()
    return epochDay * 86400000L
}

private fun millisToLocalDate(millis: Long): LocalDate {
    val epochDay = (millis / 86400000L).toInt()
    return LocalDate.fromEpochDays(epochDay)
}

private fun LocalDate.toEpochDays(): Int {
    // Simplified epoch days calculation
    var year = this.year
    var month = this.monthNumber
    var day = this.dayOfMonth

    // Days from year 0 to year-1
    val daysFromYears = (year - 1970) * 365 +
        (year - 1) / 4 - (year - 1) / 100 + (year - 1) / 400 -
        (1969 / 4) + (1969 / 100) - (1969 / 400)

    // Days from months in current year (before current month)
    val daysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    var daysFromMonths = 0
    for (m in 1 until month) {
        daysFromMonths += daysInMonth[m - 1]
    }

    // Add leap day if current year is leap and month is after February
    val isLeap = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    if (isLeap && month > 2) {
        daysFromMonths += 1
    }

    return daysFromYears + daysFromMonths + (day - 1)
}
