package id.startapp.pheromone.presentation.ui.components.feedback

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import id.startapp.pheromone.presentation.ui.theme.PheromoneBlue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import kotlinx.coroutines.delay

/**
 * Countdown Timer with Resend Link
 *
 * 60-second countdown timer that shows a "Resend" link when complete.
 *
 * Usage:
 * - Timer counts down from 60 seconds
 * - When 0, shows "Resend" link
 * - Tapping "Resend" resets timer and triggers callback
 *
 * @param duration Countdown duration in seconds (default 60)
 * @param onResend Callback when resend is tapped
 * @param modifier Modifier for the component
 */
@Composable
fun CountdownTimer(
    duration: Int = 60,
    onResend: () -> Unit,
    modifier: Modifier = Modifier
) {
    var timeLeft by remember { mutableIntStateOf(duration) }
    var isFinished by remember { mutableStateOf(false) }

    LaunchedEffect(timeLeft, isFinished) {
        if (timeLeft > 0 && !isFinished) {
            delay(1000L)
            timeLeft--
        } else if (timeLeft == 0 && !isFinished) {
            isFinished = true
        }
    }

    if (isFinished) {
        Text(
            text = "Resend",
            modifier = modifier.clickable(onClick = {
                timeLeft = duration
                isFinished = false
                onResend()
            }),
            style = MaterialTheme.typography.bodyMedium,
            color = PheromoneBlue,
            fontWeight = FontWeight.Medium
        )
    } else {
        val minutes = timeLeft / 60
        val seconds = timeLeft % 60
        Text(
            text = "Resend in ${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}",
            modifier = modifier,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Simple Countdown Text
 *
 * Just displays the countdown text without resend functionality.
 *
 * @param timeLeft Time remaining in seconds
 * @param modifier Modifier for the component
 */
@Composable
fun CountdownText(
    timeLeft: Int,
    modifier: Modifier = Modifier
) {
    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    Text(
        text = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}",
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}

/**
 * SLA Countdown with Color Coding
 *
 * Countdown timer that changes color based on remaining time percentage.
 * Used for processing queue and delivery tasks.
 *
 * @param totalTime Total time in seconds
 * @param timeLeft Time remaining in seconds
 * @param modifier Modifier for the component
 * @param warningThreshold Percentage (0-1) when to show warning (default 0.2 = 20%)
 */
@Composable
fun SlaCountdown(
    totalTime: Int,
    timeLeft: Int,
    modifier: Modifier = Modifier,
    warningThreshold: Float = 0.2f
) {
    val percentage = timeLeft.toFloat() / totalTime
    val color = when {
        percentage <= warningThreshold -> {
            MaterialTheme.colorScheme.error // Red
        }
        percentage <= 0.5f -> {
            MaterialTheme.colorScheme.tertiary // Orange
        }
        else -> {
            MaterialTheme.colorScheme.primary // Blue/Green
        }
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60

    Text(
        text = "${minutes}h ${seconds}m",
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        fontWeight = if (percentage <= warningThreshold) FontWeight.Bold else FontWeight.Normal
    )
}

/**
 * Reset the countdown timer
 *
 * Utility function to reset an external countdown timer.
 * Use with remember and mutableIntStateOf.
 *
 * @param setTime Lambda to set the time back to duration
 * @param setFinished Lambda to reset finished state
 */
fun resetCountdown(
    setTime: (Int) -> Unit,
    setFinished: (Boolean) -> Unit,
    duration: Int = 60
) {
    setTime(duration)
    setFinished(false)
}
