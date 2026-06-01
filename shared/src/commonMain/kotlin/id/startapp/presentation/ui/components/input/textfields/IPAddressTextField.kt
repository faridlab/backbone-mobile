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
 * IP Address Text Field
 *
 * For IPv4 addresses (server/device configuration).
 *
 * @param value Current IP value
 * @param onValueChange Callback
 * @param modifier Modifier
 * @param externalLabel Label
 * @param enabled Whether enabled
 * @param supportIPv6 Whether to support IPv6
 */
@Composable
fun IPAddressTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = "IP Address",
    enabled: Boolean = true,
    supportIPv6: Boolean = false,
    error: String? = null
) {
    var internalError by remember { mutableStateOf<String?>(null) }

    AppTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        externalLabel = externalLabel,
        placeholder = "192.168.1.1",
        enabled = enabled,
        error = error ?: internalError,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        ),
        onBlur = {
            internalError = when {
                value.isBlank() -> "IP address is required"
                supportIPv6 && !isValidIPv6(value) -> "Invalid IPv6 address"
                !supportIPv6 && !isValidIPv4(value) -> "Invalid IPv4 address"
                else -> null
            }
        }
    )
}

private fun isValidIPv4(ip: String): Boolean {
    val ipv4Regex = Regex("""^(\d{1,3}\.){3}\d{1,3}$""")
    return ipv4Regex.matches(ip) && ip.split(".").all { it.toIntOrNull()?.let { it in 0..255 } == true }
}

private fun isValidIPv6(ip: String): Boolean {
    val ipv6Regex = Regex("""^(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|::)${'$'}""")
    return ipv6Regex.matches(ip)
}
