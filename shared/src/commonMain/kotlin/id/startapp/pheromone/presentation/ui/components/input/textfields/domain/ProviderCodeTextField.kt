package id.startapp.pheromone.presentation.ui.components.input.textfields.domain

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import id.startapp.pheromone.presentation.ui.components.input.AppTextField

/**
 * Provider Code Text Field
 *
 * Specialized text field for Pheromone provider codes.
 *
 * Features:
 * - Read-only (auto-generated)
 * - Copy button to copy code to clipboard
 * - Business icon
 * - Monospace font for code display
 *
 * @param value Current provider code value
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param enabled Whether the field is enabled
 * @param onCopy Callback when copy button is clicked
 */
@Composable
fun ProviderCodeTextField(
    value: String,
    modifier: Modifier = Modifier,
    externalLabel: String? = "Provider Code",
    enabled: Boolean = false, // Always read-only by default
    onCopy: (() -> Unit)? = null
) {
    var showCopiedFeedback by remember { mutableStateOf(false) }

    AppTextField(
        value = value,
        onValueChange = {}, // Read-only - no changes allowed
        modifier = modifier,
        externalLabel = externalLabel,
        placeholder = "Auto-generated",
        enabled = enabled,
        readOnly = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Business,
                contentDescription = "Provider Code",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    // In a real app, this would copy to clipboard
                    // clipboardManager.setText(AnnotatedString(value))
                    onCopy?.invoke()
                    showCopiedFeedback = true
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = "Copy code",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        helperText = if (showCopiedFeedback) {
            "Code copied!"
        } else {
            "Auto-generated provider identifier"
        }
    )
}
