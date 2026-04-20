package id.startapp.pheromone.presentation.ui.components.input.textfields

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
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
import kotlinx.serialization.json.Json

/**
 * JSON Text Field
 *
 * For configuration/settings editing with validation.
 *
 * @param value Current JSON string
 * @param onValueChange Callback
 * @param modifier Modifier
 * @param externalLabel Label
 * @param enabled Whether enabled
 * @param singleLine Whether single line (false = multiline)
 */
@Composable
fun JSONTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = "JSON Configuration",
    enabled: Boolean = true,
    singleLine: Boolean = false,
    error: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }
    var isValidJson by remember { mutableStateOf(true) }

    AppTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        externalLabel = externalLabel,
        placeholder = "{\n  \"key\": \"value\"\n}",
        enabled = enabled,
        error = error ?: internalError,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 5,
        maxLines = 10,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Code,
                contentDescription = "JSON",
                tint = if (isValidJson) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    id.startapp.pheromone.presentation.ui.theme.ErrorRed
                }
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = if (singleLine) ImeAction.Done else ImeAction.Default
        ),
        onBlur = {
            isValidJson = try {
                Json.parseToJsonElement(value)
                true
            } catch (e: Exception) {
                false
            }

            internalError = if (value.isNotBlank() && !isValidJson) {
                "Invalid JSON format"
            } else null
        }
    )
}
