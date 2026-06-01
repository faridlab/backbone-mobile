package id.startapp.presentation.ui.components.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.startapp.presentation.ui.theme.BackboneBlue

/**
 * Backbone Secondary Button
 *
 * Outlined button with Brand Blue border following Material 3 guidelines.
 *
 * Specs:
 * - Height: 48dp
 * - Border Radius: 8dp
 * - Border: 1dp Brand Blue
 * - Padding: 24dp horizontal, 12dp vertical
 * - Background: Transparent
 * - Text: Brand Blue, Label Large (14sp, Medium)
 *
 * @param text Button text label
 * @param onClick Callback when button is clicked
 * @param modifier Modifier for the button
 * @param enabled Whether the button is enabled
 * @param leading Optional leading icon composable
 * @param trailing Optional trailing icon composable
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = BackboneBlue,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = BackboneBlue.copy(alpha = 0.38f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) BackboneBlue else BackboneBlue.copy(alpha = 0.12f)
        ),
        contentPadding = PaddingValues(
            horizontal = 24.dp,
            vertical = 12.dp
        ),
        shape = MaterialTheme.shapes.small
    ) {
        ButtonContent(text, leading, trailing)
    }
}

/**
 * Secondary button with custom colors
 *
 * @param text Button text label
 * @param onClick Callback when button is clicked
 * @param modifier Modifier for the button
 * @param enabled Whether the button is enabled
 * @param colors Custom button colors
 * @param leading Optional leading icon composable
 * @param trailing Optional trailing icon composable
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) BackboneBlue else BackboneBlue.copy(alpha = 0.12f)
        ),
        contentPadding = PaddingValues(
            horizontal = 24.dp,
            vertical = 12.dp
        ),
        shape = MaterialTheme.shapes.small
    ) {
        ButtonContent(text, leading, trailing)
    }
}

/**
 * Secondary button with destructive (red) styling
 *
 * Used for destructive actions like logout, delete.
 *
 * @param text Button text label
 * @param onClick Callback when button is clicked
 * @param modifier Modifier for the button
 * @param enabled Whether the button is enabled
 */
@Composable
fun SecondaryButtonDestructive(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = ErrorRed,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = ErrorRed.copy(alpha = 0.38f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) ErrorRed else ErrorRed.copy(alpha = 0.12f)
        ),
        contentPadding = PaddingValues(
            horizontal = 24.dp,
            vertical = 12.dp
        ),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = MaterialTheme.typography.labelLarge.fontSize,
                fontWeight = FontWeight.Medium,
                lineHeight = MaterialTheme.typography.labelLarge.lineHeight
            )
        )
    }
}

/**
 * Internal content composable for secondary button
 */
@Composable
private fun RowScope.ButtonContent(
    text: String,
    leading: (@Composable () -> Unit)?,
    trailing: (@Composable () -> Unit)?
) {
    leading?.invoke()
    Text(
        text = text,
        style = TextStyle(
            fontSize = MaterialTheme.typography.labelLarge.fontSize,
            fontWeight = FontWeight.Medium,
            lineHeight = MaterialTheme.typography.labelLarge.lineHeight
        )
    )
    trailing?.invoke()
}

// Import color
private val ErrorRed = Color(0xFFF44336)
