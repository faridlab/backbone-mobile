package id.startapp.pheromone.presentation.ui.components.input.textfields

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.startapp.pheromone.presentation.ui.theme.PheromoneBlue
import id.startapp.pheromone.presentation.ui.theme.ErrorRed
import id.startapp.pheromone.presentation.ui.theme.SurfaceGray300

/**
 * Color Text Field
 *
 * For hex color input with visual preview.
 *
 * @param hexColor Current hex color (with or without #)
 * @param onColorChange Callback with hex string (without #)
 * @param modifier Modifier
 * @param externalLabel Label
 * @param enabled Whether enabled
 * @param showColorPreview Whether to show color preview
 */
@Composable
fun ColorTextField(
    hexColor: String,
    onColorChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = "Color",
    placeholder: String? = "#RRGGBB",
    enabled: Boolean = true,
    showColorPreview: Boolean = true
) {
    // Ensure # prefix for display
    val displayValue = if (hexColor.isNotEmpty() && !hexColor.startsWith("#")) {
        "#$hexColor"
    } else {
        hexColor
    }

    val isValidColor = remember(displayValue) {
        isValidHexColor(displayValue)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = displayValue,
            onValueChange = { newValue ->
                val clean = newValue.removePrefix("#")
                onColorChange(clean)
            },
            modifier = Modifier.weight(1f),
            placeholder = { Text(placeholder ?: "") },
            enabled = enabled,
            isError = !isValidColor && displayValue.isNotEmpty(),
            singleLine = true,
            shape = RoundedCornerShape(4.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isValidColor) PheromoneBlue else ErrorRed,
                unfocusedBorderColor = if (isValidColor) SurfaceGray300 else ErrorRed
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Palette,
                    contentDescription = "Color",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )

        if (showColorPreview && isValidColor && displayValue.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = parseColor(displayValue),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = SurfaceGray300,
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        }
    }
}

private fun isValidHexColor(color: String): Boolean {
    val hexRegex = Regex("""^#?[0-9A-Fa-f]{6}${'$'}""")
    return hexRegex.matches(color)
}

private fun parseColor(hex: String): Color {
    val clean = hex.removePrefix("#")
    return Color(
        red = clean.take(2).toInt(16) / 255f,
        green = clean.drop(2).take(2).toInt(16) / 255f,
        blue = clean.drop(4).take(2).toInt(16) / 255f,
        alpha = 1f
    )
}
