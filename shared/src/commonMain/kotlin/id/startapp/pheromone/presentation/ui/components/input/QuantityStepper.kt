package id.startapp.pheromone.presentation.ui.components.input

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.startapp.pheromone.presentation.ui.theme.PheromoneBlue

/**
 * Quantity Stepper Component
 *
 * A stepper control for incrementing/decrementing quantities with configurable size and styling.
 *
 * @param value Current quantity value
 * @param onValueChange Callback when value changes
 * @param modifier Modifier for the component
 * @param minValue Minimum allowed value
 * @param maxValue Maximum allowed value
 * @param accentColor Accent color for large-sized plus button
 * @param size Size variant (affects button and font sizes)
 */
@Composable
fun QuantityStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minValue: Int = 0,
    maxValue: Int = 999,
    accentColor: Color = PheromoneBlue,
    size: QuantityStepperSize = QuantityStepperSize.Medium
) {
    val buttonSize = size.buttonSize()
    val fontSize = size.fontSize()
    val iconSize = size.iconSize()
    val isLarge = size == QuantityStepperSize.Large

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (isLarge) 16.dp else 8.dp)
    ) {
        // Minus button
        QuantityButton(
            onClick = { onValueChange((value - 1).coerceAtLeast(minValue)) },
            enabled = value > minValue,
            buttonSize = buttonSize,
            iconSize = iconSize,
            iconVector = Icons.Default.Remove,
            contentDescription = "Decrease",
            iconTint = getMinusButtonIconTint(value, minValue),
            backgroundColor = getButtonBackgroundColor(value > minValue)
        )

        // Value display
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = if (isLarge) 48.dp else 32.dp)
        )

        // Plus button
        QuantityButton(
            onClick = { onValueChange((value + 1).coerceAtMost(maxValue)) },
            enabled = value < maxValue,
            buttonSize = buttonSize,
            iconSize = iconSize,
            iconVector = Icons.Default.Add,
            contentDescription = "Increase",
            shape = if (isLarge) CircleShape else RoundedCornerShape(8.dp),
            iconTint = getPlusButtonIconTint(isLarge, value, maxValue),
            backgroundColor = getPlusButtonBackgroundColor(isLarge, value, maxValue, accentColor)
        )
    }
}

/**
 * Reusable quantity button component
 */
@Composable
private fun QuantityButton(
    onClick: () -> Unit,
    enabled: Boolean,
    buttonSize: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
    iconVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp),
    iconTint: Color,
    backgroundColor: Color
) {
    Box(
        modifier = Modifier
            .size(buttonSize)
            .clip(shape)
            .background(backgroundColor)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = iconTint
        )
    }
}

/**
 * Get background color for buttons based on enabled state
 */
private fun getButtonBackgroundColor(enabled: Boolean): Color {
    return if (enabled) Color(0xFFF5F5F5) else Color(0xFFF5F5F5).copy(alpha = 0.5f)
}

/**
 * Get background color for plus button based on size and value
 */
private fun getPlusButtonBackgroundColor(
    isLarge: Boolean,
    value: Int,
    maxValue: Int,
    accentColor: Color
): Color {
    if (value >= maxValue) return Color(0xFFF5F5F5).copy(alpha = 0.5f)
    return if (isLarge) accentColor else Color(0xFFF5F5F5)
}

/**
 * Get icon tint for minus button
 */
@Composable
private fun getMinusButtonIconTint(value: Int, minValue: Int): Color {
    return if (value > minValue) MaterialTheme.colorScheme.onSurface else Color.Gray
}

/**
 * Get icon tint for plus button
 */
@Composable
private fun getPlusButtonIconTint(isLarge: Boolean, value: Int, maxValue: Int): Color {
    if (value >= maxValue) return Color.Gray
    return if (isLarge) Color.White else MaterialTheme.colorScheme.onSurface
}

/**
 * Size properties for QuantityStepper
 */
enum class QuantityStepperSize {
    Small,
    Medium,
    Large;

    fun buttonSize() = when (this) {
        Small -> 32.dp
        Medium -> 40.dp
        Large -> 56.dp
    }

    fun fontSize() = when (this) {
        Small -> 14.sp
        Medium -> 16.sp
        Large -> 28.sp
    }

    fun iconSize() = when (this) {
        Small -> 16.dp
        Medium -> 20.dp
        Large -> 24.dp
    }
}
