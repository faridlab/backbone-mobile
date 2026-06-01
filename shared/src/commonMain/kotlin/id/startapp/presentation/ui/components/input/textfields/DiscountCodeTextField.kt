package id.startapp.presentation.ui.components.input.textfields

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Discount
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import id.startapp.presentation.ui.theme.BackboneBlue
import id.startapp.presentation.ui.theme.SurfaceGray300
import id.startapp.presentation.ui.theme.SuccessGreen

/**
 * Discount Code Text Field
 *
 * Specialized text field for promo/voucher/discount code input with validation and apply button.
 *
 * Features:
 * - Auto-uppercase conversion
 * - Apply button
 * - Success/error state indication
 * - Valid code formatting (alphanumeric, 6-12 chars)
 * - Discount preview display
 *
 * @param value Current discount code value
 * @param onValueChange Callback when code changes
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param isValid Whether the current code is valid
 * @param discount Applied discount info (e.g., "20% OFF", "Rp 50.000")
 * @param onApply Callback when apply button is clicked
 * @param onRemove Callback when remove button is clicked (if valid)
 * @param errorMessage Error message from validation
 * @param minCodeLength Minimum code length (default: 6)
 * @param maxCodeLength Maximum code length (default: 12)
 */
@Composable
fun DiscountCodeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = "Discount Code",
    placeholder: String? = "Enter promo code",
    enabled: Boolean = true,
    isValid: Boolean = false,
    discount: String? = null,
    onApply: () -> Unit = {},
    onRemove: () -> Unit = {},
    errorMessage: String? = null,
    minCodeLength: Int = 6,
    maxCodeLength: Int = 12
) {
    val borderColor = when {
        !enabled -> SurfaceGray300
        isValid -> SuccessGreen
        errorMessage != null -> Color.Red
        else -> SurfaceGray300
    }

    val displayValue = value.uppercase()

    Column(modifier = modifier) {
        // External label
        externalLabel?.let { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Discount preview if valid
        if (isValid && discount != null) {
            Surface(
                color = SuccessGreen.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocalOffer,
                        contentDescription = "Discount Applied",
                        tint = SuccessGreen,
                        modifier = Modifier.width(16.dp).height(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = discount,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SuccessGreen
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Input field with apply button
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = displayValue,
                onValueChange = { newValue ->
                    val filtered = newValue.filter { it.isLetterOrDigit() }
                        .take(maxCodeLength)
                    onValueChange(filtered)
                },
                modifier = Modifier.weight(1f),
                placeholder = if (placeholder != null) {
                    { Text(placeholder, style = MaterialTheme.typography.bodyMedium) }
                } else null,
                enabled = enabled && !isValid,
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = borderColor,
                    unfocusedBorderColor = borderColor,
                    disabledBorderColor = SurfaceGray300
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Discount,
                        contentDescription = "Discount Code",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = if (isValid) {
                    {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Valid",
                            tint = SuccessGreen
                        )
                    }
                } else null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Apply or Remove button
            androidx.compose.material3.Button(
                onClick = if (isValid) onRemove else onApply,
                enabled = if (isValid) true else (enabled && displayValue.length >= minCodeLength),
                shape = RoundedCornerShape(4.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = if (isValid) Color.Red else BackboneBlue
                ),
                modifier = Modifier.height(
                    androidx.compose.material3.OutlinedTextFieldDefaults.MinHeight
                )
            ) {
                Text(
                    text = if (isValid) "Remove" else "Apply",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        // Helper text
        Spacer(modifier = Modifier.height(4.dp))
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Red
            )
        } else if (!isValid) {
            Text(
                text = "Minimum $minCodeLength characters",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
