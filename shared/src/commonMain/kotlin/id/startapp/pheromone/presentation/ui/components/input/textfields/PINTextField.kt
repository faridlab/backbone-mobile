package id.startapp.pheromone.presentation.ui.components.input.textfields

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import id.startapp.pheromone.presentation.ui.theme.PheromoneBlue
import id.startapp.pheromone.presentation.ui.theme.ErrorRed
import id.startapp.pheromone.presentation.ui.theme.SurfaceGray300

/**
 * Constants for PINTextField default values
 */
object PINTextFieldDefaults {
    const val DEFAULT_PIN_DIGIT_COUNT = 4
}

/**
 * PIN Text Field
 *
 * Specialized text field for PIN input with masked display.
 *
 * Features:
 * - Configurable length (default: 6 digits)
 * - Masked display (dots)
 * - Show/hide toggle
 * - Numeric only
 * - For transaction PINs, secure codes
 *
 * @param pin Current PIN value
 * @param onPinChange Callback when PIN changes
 * @param modifier Modifier for the text field
 * @param pinLength PIN length (default: 6)
 * @param externalLabel Label text shown above field
 * @param externalLabelRequired Whether to show required asterisk
 * @param enabled Whether the field is enabled
 * @param showMask Whether to mask the PIN (default: true)
 * @param error Error message
 */
@Composable
fun PINTextField(
    pin: String,
    onPinChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    pinLength: Int = PINTextFieldDefaults.DEFAULT_PIN_DIGIT_COUNT,
    externalLabel: String? = null,
    externalLabelRequired: Boolean = true,
    enabled: Boolean = true,
    showMask: Boolean = true,
    error: String? = null,
    testTag: String? = null,
    contentDescription: String? = null
) {
    var isVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .then(
                if (testTag != null) Modifier.testTag(testTag) else Modifier
            )
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                }
            )
    ) {
        // External label
        externalLabel?.let { label ->
            PINExternalLabel(
                text = label,
                required = externalLabelRequired,
                isError = error != null
            )
        }

        Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(pinLength) { index ->
                PINDigitBox(
                    digit = pin.getOrNull(index)?.toString(),
                    isFocused = index == pin.length,
                    isVisible = isVisible,
                    isError = error != null,
                    enabled = enabled
                )

                if (index < pinLength - 1) {
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }
        }

        // Show/hide toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
        ) {
            TextButton(
                onClick = { isVisible = !isVisible },
                enabled = pin.isNotEmpty()
            ) {
                Text(
                    if (isVisible) "Hide" else "Show",
                    style = MaterialTheme.typography.bodySmall,
                    color = PheromoneBlue
                )
            }
        }
    }
}

@Composable
private fun PINDigitBox(
    digit: String?,
    isFocused: Boolean,
    isVisible: Boolean,
    isError: Boolean,
    enabled: Boolean
) {
    val borderColor = when {
        isError -> ErrorRed
        isFocused -> PheromoneBlue
        else -> SurfaceGray300
    }

    Box(
        modifier = Modifier
            .height(56.dp)
            .width(48.dp)
            .border(
                border = BorderStroke(2.dp, borderColor),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when {
                digit != null && isVisible -> digit
                digit != null -> "●"
                else -> ""
            },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PINExternalLabel(
    text: String,
    required: Boolean,
    isError: Boolean
) {
    val labelColor = if (isError) ErrorRed else MaterialTheme.colorScheme.onSurface

    Text(
        text = if (required) "$text *" else text,
        style = MaterialTheme.typography.labelMedium,
        color = labelColor
    )

    Spacer(modifier = Modifier.height(8.dp))
}
