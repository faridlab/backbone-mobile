package id.startapp.presentation.ui.components.input.textfields

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.startapp.presentation.ui.components.input.AppTextField

/**
 * Barcode Text Field
 *
 * Specialized text field for barcode/SKU input with scanner integration.
 *
 * Features:
 * - Barcode/SKU input
 * - Scanner button hook
 * - Uppercase conversion
 * - Trim support
 *
 * @param value Current barcode value
 * @param onValueChange Callback when barcode changes
 * @param modifier Modifier for the text field
 * @param externalLabel Label text
 * @param enabled Whether enabled
 * @param onScan Callback when scan button clicked
 * @param placeholder Placeholder text
 * @param error Error message
 * @param testTag Test tag for UI testing
 * @param contentDescription Content description for accessibility
 */
@Composable
fun BarcodeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = "Barcode / SKU",
    placeholder: String? = "Scan or enter barcode",
    enabled: Boolean = true,
    onScan: (() -> Unit)? = null,
    error: String? = null,
    testTag: String? = null,
    contentDescription: String? = null
) {
    Row(modifier = modifier) {
        AppTextField(
            value = value,
            onValueChange = { onValueChange(it.trim().uppercase()) },
            modifier = Modifier.weight(1f),
            placeholder = placeholder,
            enabled = enabled,
            error = error,
            singleLine = true,
            testTag = testTag,
            contentDescription = contentDescription
        )

        if (onScan != null && enabled) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onScan,
                enabled = enabled
            ) {
                Icon(
                    imageVector = Icons.Rounded.QrCodeScanner,
                    contentDescription = "Scan barcode",
                    tint = id.startapp.presentation.ui.theme.BackboneBlue
                )
            }
        }
    }
}
