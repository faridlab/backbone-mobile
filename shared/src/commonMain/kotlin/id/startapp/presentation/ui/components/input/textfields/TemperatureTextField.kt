package id.startapp.presentation.ui.components.input.textfields

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import id.startapp.presentation.ui.components.input.AppTextField

/**
 * Temperature Text Field
 *
 * For equipment monitoring (Celsius default).
 *
 * @param value Current temperature
 * @param onValueChange Callback
 * @param modifier Modifier
 * @param externalLabel Label
 * @param unit Temperature unit (C, F, K)
 * @param minTemp Min temperature
 * @param maxTemp Max temperature
 * @param testTag Test tag for UI testing
 * @param contentDescription Content description for accessibility
 */
@Composable
fun TemperatureTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = "Temperature",
    placeholder: String? = "25",
    enabled: Boolean = true,
    unit: String = "°C",
    minTemp: Double? = null,
    maxTemp: Double? = null,
    error: String? = null,
    testTag: String? = null,
    contentDescription: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }

    AppTextField(
        value = value,
        onValueChange = { newValue ->
            val filtered = newValue.filter { it.isDigit() || it == '.' || it == '-' }
            onValueChange(filtered)
        },
        modifier = modifier,
        externalLabel = externalLabel,
        placeholder = placeholder,
        enabled = enabled,
        error = error ?: internalError,
        testTag = testTag,
        contentDescription = contentDescription,
        trailingIcon = {
            Text(
                text = unit,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done
        ),
        onBlur = {
            val temp = value.toDoubleOrNull()
            internalError = when {
                value.isBlank() -> "Temperature is required"
                temp == null -> "Invalid temperature"
                minTemp != null && temp < minTemp -> "Must be at least $minTemp$unit"
                maxTemp != null && temp > maxTemp -> "Must not exceed $maxTemp$unit"
                else -> null
            }
        }
    )
}
