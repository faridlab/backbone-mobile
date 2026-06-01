package id.startapp.presentation.ui.components.button

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.startapp.presentation.ui.theme.BackboneBlue
import id.startapp.presentation.ui.theme.BackboneShapes

/**
 * Backbone Primary Button
 *
 * Filled button with Brand Blue background following Material 3 guidelines.
 *
 * Specs:
 * - Height: 48dp
 * - Border Radius: 8dp
 * - Padding: 24dp horizontal, 12dp vertical
 * - Background: Brand Blue (#0288D1)
 * - Text: White, Label Large (14sp, Medium)
 *
 * @param text Button text label
 * @param onClick Callback when button is clicked
 * @param modifier Modifier for the button
 * @param enabled Whether the button is enabled
 * @param leading Optional leading icon composable
 * @param trailing Optional trailing icon composable
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonColors(
            containerColor = BackboneBlue,
            contentColor = Color.White,
            disabledContainerColor = BackboneBlue.copy(alpha = 0.12f),
            disabledContentColor = Color.White.copy(alpha = 0.38f)
        ),
        contentPadding = PaddingValues(
            horizontal = 24.dp,
            vertical = 12.dp
        ),
        shape = RoundedCornerShape(BackboneShapes.Full.dp)
    ) {
        ButtonContent(text, leading, trailing)
    }
}

/**
 * Primary Button with custom colors
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
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        colors = colors,
        contentPadding = PaddingValues(
            horizontal = 24.dp,
            vertical = 12.dp
        ),
        shape = RoundedCornerShape(BackboneShapes.Full.dp)
    ) {
        ButtonContent(text, leading, trailing)
    }
}

/**
 * Internal content composable for primary button
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

/**
 * Primary button with loading state
 *
 * Shows a loading indicator when isLoading is true.
 *
 * @param text Button text label
 * @param onClick Callback when button is clicked
 * @param modifier Modifier for the button
 * @param enabled Whether the button is enabled
 * @param isLoading Whether to show loading indicator
 * @param leading Optional leading icon composable
 */
@Composable
fun PrimaryLoadingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leading: (@Composable () -> Unit)? = null
) {
    PrimaryButton(
        text = if (isLoading) "" else text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
        leading = if (isLoading) {
            {
                // TODO: Add loading indicator when CircularProgressIndicator is available
            }
        } else {
            leading
        }
    )
}

/**
 * Full-width primary button
 *
 * Fills the maximum width of its parent.
 *
 * @param text Button text label
 * @param onClick Callback when button is clicked
 * @param modifier Modifier for the button
 * @param enabled Whether the button is enabled
 * @param leading Optional leading icon composable
 * @param trailing Optional trailing icon composable
 */
@Composable
fun PrimaryButtonFullWidth(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    PrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier.then(Modifier.fillMaxWidth()),
        enabled = enabled,
        leading = leading,
        trailing = trailing
    )
}
