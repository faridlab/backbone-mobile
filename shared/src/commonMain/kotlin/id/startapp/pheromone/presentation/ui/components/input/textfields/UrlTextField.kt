package id.startapp.pheromone.presentation.ui.components.input.textfields

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import id.startapp.pheromone.presentation.ui.components.input.AppTextField

/**
 * URL Text Field
 *
 * Specialized text field for website/social media URL input.
 *
 * Features:
 * - URI keyboard type
 * - URL format validation
 * - Auto-adds https:// prefix if missing
 * - Language icon
 *
 * @param value Current URL value
 * @param onValueChange Callback when URL changes
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param externalLabelRequired Whether to show required asterisk
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param readOnly Whether the field is read-only
 * @param allowEmpty Whether empty value is allowed
 * @param autoPrefix Whether to auto-add https:// prefix
 * @param onError Callback when validation fails
 * @param error External error message (overrides internal validation)
 */
@Composable
fun UrlTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = null,
    externalLabelRequired: Boolean = false,
    placeholder: String? = "https://example.com",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    allowEmpty: Boolean = true,
    autoPrefix: Boolean = true,
    onError: ((String) -> Unit)? = null,
    error: String? = null,
    testTag: String? = null,
    contentDescription: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }

    // Auto-add https:// prefix on blur if missing
    fun formatUrl(input: String): String {
        return if (autoPrefix && input.isNotBlank() && !input.startsWith("http")) {
            "https://$input"
        } else {
            input
        }
    }

    // Simple URL validation
    fun isValidUrl(url: String): Boolean {
        val urlRegex = Regex("""^https?://[a-zA-Z0-9\-._~:/?#\[\]@!$&'()*+,;=]+$""")
        return urlRegex.matches(url)
    }

    AppTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        externalLabel = externalLabel ?: "Website",
        externalLabelRequired = externalLabelRequired,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = readOnly,
        error = error ?: internalError,
        testTag = testTag,
        contentDescription = contentDescription,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Language,
                contentDescription = "Website",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Next
        ),
        onBlur = {
            // Format URL on blur
            val formatted = formatUrl(value)
            if (formatted != value) {
                onValueChange(formatted)
            }

            val errorMsg = when {
                allowEmpty && formatted.isBlank() -> null
                formatted.isBlank() -> "URL is required"
                !isValidUrl(formatted) -> "Invalid URL format"
                else -> null
            }

            internalError = errorMsg
            if (errorMsg != null) {
                onError?.invoke(errorMsg)
            }
        }
    )
}

/**
 * Social Media URL Text Field
 *
 * Specialized for social media URLs (Instagram, Facebook, etc.).
 *
 * @param value Current social media URL value
 * @param onValueChange Callback when URL changes
 * @param platform Social media platform name
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param externalLabelRequired Whether to show required asterisk
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param readOnly Whether the field is read-only
 * @param allowEmpty Whether empty value is allowed
 * @param onError Callback when validation fails
 * @param error External error message (overrides internal validation)
 */
@Composable
fun SocialMediaUrlTextField(
    value: String,
    onValueChange: (String) -> Unit,
    platform: String,
    modifier: Modifier = Modifier,
    externalLabel: String? = null,
    externalLabelRequired: Boolean = false,
    placeholder: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    allowEmpty: Boolean = true,
    onError: ((String) -> Unit)? = null,
    error: String? = null,
    testTag: String? = null,
    contentDescription: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }

    // Simple URL validation
    fun isValidUrl(url: String): Boolean {
        val urlRegex = Regex("""^https?://[a-zA-Z0-9\-._~:/?#\[\]@!$&'()*+,;=]+$""")
        return urlRegex.matches(url)
    }

    AppTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        externalLabel = externalLabel ?: "$platform URL",
        externalLabelRequired = externalLabelRequired,
        placeholder = placeholder ?: "https://$platform.com/username",
        enabled = enabled,
        readOnly = readOnly,
        error = error ?: internalError,
        testTag = testTag,
        contentDescription = contentDescription,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Language,
                contentDescription = platform,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Next
        ),
        helperText = "Enter your $platform profile URL",
        onBlur = {
            val formatted = if (value.isNotBlank() && !value.startsWith("http")) {
                "https://$value"
            } else {
                value
            }

            if (formatted != value) {
                onValueChange(formatted)
            }

            val errorMsg = when {
                allowEmpty && formatted.isBlank() -> null
                formatted.isBlank() -> "URL is required"
                !isValidUrl(formatted) -> "Invalid URL format"
                else -> null
            }

            internalError = errorMsg
            if (errorMsg != null) {
                onError?.invoke(errorMsg)
            }
        }
    )
}
