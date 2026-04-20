package id.startapp.pheromone.presentation.ui.components.input.textfields

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.startapp.pheromone.presentation.ui.theme.PheromoneBlue
import id.startapp.pheromone.presentation.ui.theme.ErrorRed
import kotlinx.datetime.LocalDate

/**
 * Date Text Field
 *
 * Specialized text field for date input with date picker integration.
 *
 * Features:
 * - Date picker dialog on click
 * - Formatted display (DD MMM YYYY)
 * - Min/max date constraints
 * - Calendar icon
 *
 * @param date Selected date
 * @param onDateChange Callback when date changes
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param minDate Minimum selectable date
 * @param maxDate Maximum selectable date
 * @param allowEmpty Whether empty value is allowed
 * @param error Error message
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTextField(
    date: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = null,
    placeholder: String? = "Select date",
    enabled: Boolean = true,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    allowEmpty: Boolean = true,
    error: String? = null
) {
    var showDatePicker by remember { mutableStateOf(false) }

    // Simple date formatter for display
    fun formatDate(localDate: LocalDate): String {
        val monthNames = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        return "${localDate.dayOfMonth} ${monthNames[localDate.monthNumber - 1]} ${localDate.year}"
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = localDateToMillis(date),
        selectableDates = object : androidx.compose.material3.SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val localDate = millisToLocalDate(utcTimeMillis)
                return (minDate == null || localDate >= minDate) &&
                       (maxDate == null || localDate <= maxDate)
            }
            override fun isSelectableYear(year: Int): Boolean = true
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = millisToLocalDate(millis)
                            onDateChange(localDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            androidx.compose.material3.DatePicker(state = datePickerState)
        }
    }

    androidx.compose.foundation.layout.Column(modifier = modifier) {
        // External label
        externalLabel?.let { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        OutlinedButton(
            onClick = { if (enabled) showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp, if (error != null) ErrorRed else PheromoneBlue
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.CalendarMonth,
                contentDescription = "Select date",
                tint = if (error != null) ErrorRed else PheromoneBlue,
                modifier = Modifier.width(18.dp).height(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = date?.let { formatDate(it) } ?: (placeholder ?: "Select date"),
                color = if (error != null) ErrorRed else MaterialTheme.colorScheme.onSurface
            )
        }
    }
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
