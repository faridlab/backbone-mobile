package id.startapp.presentation.ui.components.input.textfields

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import id.startapp.presentation.ui.components.input.AppTextField

/**
 * Social Handle Text Field
 *
 * For Instagram, TikTok, and other social media usernames.
 *
 * Features:
 * - @ prefix handling (optional)
 * - Lowercase conversion
 * - Platform-specific validation
 *
 * @param value Current handle value
 * @param onValueChange Callback when handle changes
 * @param modifier Modifier
 * @param externalLabel Label
 * @param platform Platform name (Instagram, TikTok, etc.)
 * @param enabled Whether enabled
 * @param includeAtSign Whether to include @ prefix
 * @param error Error message
 * @param testTag Test tag for UI testing
 * @param contentDescription Content description for accessibility
 */
@Composable
fun SocialHandleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = null,
    placeholder: String? = "username",
    enabled: Boolean = true,
    platform: String = "Social Media",
    includeAtSign: Boolean = false,
    error: String? = null,
    testTag: String? = null,
    contentDescription: String? = null
) {
    // Auto-lowercase for social handles
    val displayValue = if (includeAtSign && value.isNotEmpty() && !value.startsWith("@")) {
        "@${value.lowercase()}"
    } else {
        value.lowercase()
    }

    SideEffect {
        if (value != displayValue && value.isNotBlank()) {
            onValueChange(displayValue)
        }
    }

    AppTextField(
        value = displayValue,
        onValueChange = { newValue ->
            val clean = newValue.trim().removePrefix("@")
            onValueChange(clean)
        },
        modifier = modifier,
        externalLabel = externalLabel ?: "$platform Handle",
        placeholder = placeholder,
        enabled = enabled,
        error = error,
        testTag = testTag,
        contentDescription = contentDescription,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.AlternateEmail,
                contentDescription = platform,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        ),
        helperText = if (includeAtSign) "Include @" else "Without @"
    )
}
