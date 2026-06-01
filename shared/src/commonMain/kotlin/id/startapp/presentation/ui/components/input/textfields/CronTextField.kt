package id.startapp.presentation.ui.components.input.textfields

import androidx.compose.foundation.text.KeyboardOptions
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
 * Cron Expression Text Field
 *
 * For automated task scheduling (e.g., "0 0 * * *").
 *
 * @param value Current cron expression
 * @param onValueChange Callback
 * @param modifier Modifier
 * @param externalLabel Label
 * @param enabled Whether enabled
 */
@Composable
fun CronTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = "Cron Expression",
    placeholder: String? = "0 0 * * *",
    enabled: Boolean = true,
    error: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }

    AppTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        externalLabel = externalLabel,
        placeholder = placeholder,
        enabled = enabled,
        error = error ?: internalError,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        helperText = "Format: min hour day month dow",
        onBlur = {
            internalError = if (value.isNotBlank() && !isValidCron(value)) {
                "Invalid cron expression"
            } else null
        }
    )
}

// Simplified cron validation
private fun isValidCron(cron: String): Boolean {
    val parts = cron.trim().split(" ")
    return parts.size == 5 || parts.size == 6
}
