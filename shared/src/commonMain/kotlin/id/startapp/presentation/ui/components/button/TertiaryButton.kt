package id.startapp.presentation.ui.components.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.startapp.presentation.ui.theme.BackboneBlue
import id.startapp.presentation.ui.theme.BackboneDimensions

/**
 * Backbone Tertiary Button
 *
 * Text button with Brand Blue text following Material 3 guidelines.
 *
 * Specs:
 * - Height: 40dp
 * - Background: Transparent
 * - Text: Brand Blue, Label Large (14sp, Medium)
 *
 * @param text Button text label
 * @param onClick Callback when button is clicked
 * @param modifier Modifier for the button
 * @param enabled Whether the button is enabled
 * @param leading Optional leading icon
 */
@Composable
fun TertiaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: ImageVector? = null
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            containerColor = Color.Transparent,
            contentColor = BackboneBlue,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = BackboneBlue.copy(alpha = 0.38f)
        ),
        contentPadding = ButtonDefaults.TextButtonContentPadding
    ) {
        if (leading != null) {
            Icon(
                imageVector = leading,
                contentDescription = null,
                tint = if (enabled) BackboneBlue else BackboneBlue.copy(alpha = 0.38f),
                modifier = Modifier.height(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
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
 * Tertiary button with icon
 *
 * @param text Button text label
 * @param icon Leading icon
 * @param onClick Callback when button is clicked
 * @param modifier Modifier for the button
 * @param enabled Whether the button is enabled
 */
@Composable
fun TertiaryButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            containerColor = Color.Transparent,
            contentColor = BackboneBlue,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = BackboneBlue.copy(alpha = 0.38f)
        ),
        contentPadding = ButtonDefaults.TextButtonContentPadding
    ) {
        Row(
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) BackboneBlue else BackboneBlue.copy(alpha = 0.38f)
            )
            Spacer(modifier = Modifier.width(8.dp))
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
}

/**
 * Link-style tertiary button (underlined)
 *
 * @param text Button text label
 * @param onClick Callback when button is clicked
 * @param modifier Modifier for the button
 * @param enabled Whether the button is enabled
 */
@Composable
fun LinkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            containerColor = Color.Transparent,
            contentColor = BackboneBlue,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = BackboneBlue.copy(alpha = 0.38f)
        ),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = MaterialTheme.typography.labelLarge.fontSize,
                fontWeight = FontWeight.Medium,
                lineHeight = MaterialTheme.typography.labelLarge.lineHeight,
                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
            )
        )
    }
}
