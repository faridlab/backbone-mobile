package id.startapp.presentation.ui.components.input

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import id.startapp.presentation.ui.theme.BackboneBlue
import id.startapp.presentation.ui.theme.BackboneDimensions
import id.startapp.presentation.ui.theme.ErrorRed
import id.startapp.presentation.ui.theme.SurfaceGray300

/**
 * Backbone Phone Input Field
 *
 * Phone input with country code selector following Backbone design system.
 *
 * Specs:
 * - Height: 56dp
 * - Country Code: 100dp width, Flag + Code
 * - Phone Field: Number type, Formatted (XXX XXXX XXXX)
 * - Max Length: 15 digits
 * - Validation: 10-15 digits
 *
 * @param phoneValue Current phone number value (without country code)
 * @param onPhoneChange Callback when phone number changes
 * @param modifier Modifier for the phone input
 * @param label Optional label text
 * @param placeholder Optional placeholder text
 * @param error Optional error message
 * @param enabled Whether the field is enabled
 * @param defaultCountryCode Default country code (default: +62)
 */
@Composable
fun PhoneInput(
    phoneValue: String,
    onPhoneChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    error: String? = null,
    enabled: Boolean = true,
    defaultCountryCode: CountryCode = CountryCode.Indonesia
) {
    var selectedCountry by rememberSaveable { mutableStateOf(defaultCountryCode) }
    var expanded by remember { mutableStateOf(false) }
    var countryCodeBounds by remember { mutableStateOf(Rect.Zero) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Country Code Selector
        Row(
            modifier = Modifier
                .width(100.dp)
                .height(BackboneDimensions.InputHeight.dp)
                .clickable(enabled = enabled) { expanded = true }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Flag emoji
            Text(
                text = selectedCountry.flag,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Country code
            Text(
                text = selectedCountry.code,
                style = MaterialTheme.typography.bodyLarge
            )

            // Dropdown arrow
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Select country",
                tint = if (error != null) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Spacer instead of divider
        Spacer(modifier = Modifier.width(8.dp))

        // Phone Number Input
        OutlinedTextField(
            value = phoneValue,
            onValueChange = { newValue ->
                // Only allow digits
                val digits = newValue.filter { it.isDigit() }
                // Limit to 15 digits
                if (digits.length <= 15) {
                    onPhoneChange(digits)
                }
            },
            modifier = Modifier.weight(1f),
            label = if (label != null) {
                { Text(label, style = MaterialTheme.typography.labelMedium) }
            } else null,
            placeholder = if (placeholder != null) {
                { Text(placeholder, style = MaterialTheme.typography.bodyMedium) }
            } else null,
            supportingText = if (error != null) {
                { Text(error, color = ErrorRed, style = MaterialTheme.typography.bodySmall) }
            } else null,
            isError = error != null,
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (error != null) ErrorRed else BackboneBlue,
                unfocusedBorderColor = if (error != null) ErrorRed else SurfaceGray300,
                disabledBorderColor = SurfaceGray300,
                errorBorderColor = ErrorRed,
                cursorColor = BackboneBlue
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Dropdown Menu
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.width(200.dp)
    ) {
        CountryCode.allCountries.forEach { country ->
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = country.flag, style = MaterialTheme.typography.bodyLarge)
                        Text(text = country.displayName, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = country.code,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                onClick = {
                    selectedCountry = country
                    expanded = false
                }
            )
        }
    }
}

/**
 * Phone Input with formatted display
 *
 * Displays phone number with spaces for better readability (XXX XXXX XXXX).
 *
 * @param phoneValue Current phone number value
 * @param onPhoneChange Callback with complete phone (country code + number)
 * @param modifier Modifier for the phone input
 * @param label Optional label text
 * @param error Optional error message
 * @param enabled Whether the field is enabled
 * @param defaultCountryCode Default country code
 */
@Composable
fun PhoneInputFormatted(
    phoneValue: String,
    onPhoneChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    error: String? = null,
    enabled: Boolean = true,
    defaultCountryCode: CountryCode = CountryCode.Indonesia
) {
    var selectedCountry by rememberSaveable { mutableStateOf(defaultCountryCode) }
    var expanded by remember { mutableStateOf(false) }

    // Format phone number as XXX XXXX XXXX
    val formattedPhone = remember(phoneValue) {
        phoneValue.filter { it.isDigit() }.chunked(4).joinToString(" ")
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Country Code Selector
        Row(
            modifier = Modifier
                .width(100.dp)
                .height(BackboneDimensions.InputHeight.dp)
                .clickable(enabled = enabled) { expanded = true }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedCountry.flag,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = selectedCountry.code,
                style = MaterialTheme.typography.bodyLarge
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Select country",
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Phone Number Input with formatting
        OutlinedTextField(
            value = TextFieldValue(
                text = formattedPhone,
                selection = TextRange(formattedPhone.length)
            ),
            onValueChange = { newValue ->
                val digits = newValue.text.filter { it.isDigit() }
                if (digits.length <= 15) {
                    // Notify with country code included
                    onPhoneChange("${selectedCountry.code}${digits}")
                }
            },
            modifier = Modifier.weight(1f),
            label = if (label != null) {
                { Text(label, style = MaterialTheme.typography.labelMedium) }
            } else null,
            supportingText = if (error != null) {
                { Text(error, color = ErrorRed, style = MaterialTheme.typography.bodySmall) }
            } else null,
            isError = error != null,
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (error != null) ErrorRed else BackboneBlue,
                unfocusedBorderColor = if (error != null) ErrorRed else SurfaceGray300,
                errorBorderColor = ErrorRed,
                cursorColor = BackboneBlue
            )
        )
    }

    // Dropdown Menu
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        CountryCode.allCountries.forEach { country ->
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = country.flag)
                        Text(text = country.displayName)
                    }
                },
                onClick = {
                    selectedCountry = country
                    expanded = false
                }
            )
        }
    }
}

/**
 * Supported country codes
 */
enum class CountryCode(
    val displayName: String,
    val code: String,
    val flag: String,
    val dialLength: Int
) {
    Indonesia("Indonesia", "+62", "🇮🇩", 13),
    Malaysia("Malaysia", "+60", "🇲🇾", 11),
    Singapore("Singapore", "+65", "🇸🇬", 10),
    Thailand("Thailand", "+66", "🇹🇭", 11),
    Philippines("Philippines", "+63", "🇵🇭", 12),
    Vietnam("Vietnam", "+84", "🇻🇳", 13),
    India("India", "+91", "🇮🇳", 12),
    China("China", "+86", "🇨🇳", 13),
    Australia("Australia", "+61", "🇦🇺", 11),
    USA("United States", "+1", "🇺🇸", 12);

    companion object {
        /**
         * Get country code from string
         */
        fun fromCode(code: String): CountryCode? = listOf(
            Indonesia, Malaysia, Singapore, Thailand, Philippines,
            Vietnam, India, China, Australia, USA
        ).find {
            it.code == code
        }

        val allCountries = listOf(
            Indonesia, Malaysia, Singapore, Thailand, Philippines,
            Vietnam, India, China, Australia, USA
        )
    }
}
