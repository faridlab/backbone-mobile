package id.startapp.presentation.ui.components.input.textfields

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import id.startapp.presentation.ui.theme.BackboneBlue
import id.startapp.presentation.ui.theme.SurfaceGray300

/**
 * City data class for city selection
 */
data class City(
    val name: String,
    val province: String,
    val id: String = name.lowercase().replace(" ", "-")
) {
    override fun toString(): String = "$name, $province"
}

/**
 * City Text Field
 *
 * Autocomplete dropdown for Indonesian city selection.
 *
 * Features:
 * - Search/filter as you type
 * - Dropdown with city + province display
 * - Pre-populated with major Indonesian cities
 * - Custom cities support
 *
 * @param value Current city value
 * @param onCitySelected Callback when city is selected
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param cities List of available cities (defaults to major Indonesian cities)
 * @param allowCustomEntry Whether to allow non-listed cities
 */
@Composable
fun CityTextField(
    value: String,
    onCitySelected: (City) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = "City",
    placeholder: String? = "Select city",
    enabled: Boolean = true,
    cities: List<City> = remember { getIndonesianCities() },
    allowCustomEntry: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    var filteredCities by remember { mutableStateOf(cities) }
    var textFieldWidth by remember { mutableStateOf(0) }

    Column(modifier = modifier) {
        // External label
        externalLabel?.let { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.height(4.dp)
            )
        }

        // Input field with dropdown
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldWidth = coordinates.size.width
                }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                    filteredCities = if (newValue.isBlank()) {
                        cities
                    } else {
                        cities.filter { city ->
                            city.name.contains(newValue, ignoreCase = true) ||
                            city.province.contains(newValue, ignoreCase = true)
                        }
                    }
                    if (filteredCities.isNotEmpty() && allowCustomEntry) {
                        expanded = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = if (placeholder != null) {
                    { Text(placeholder, style = MaterialTheme.typography.bodyMedium) }
                } else null,
                enabled = enabled,
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BackboneBlue,
                    unfocusedBorderColor = SurfaceGray300
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.LocationCity,
                        contentDescription = "City",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) {
                            Icons.Rounded.KeyboardArrowUp
                        } else {
                            Icons.Rounded.KeyboardArrowDown
                        },
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable(enabled) { expanded = !expanded }
                    )
                }
            )

            // Dropdown menu
            DropdownMenu(
                expanded = expanded && filteredCities.isNotEmpty(),
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(with(LocalDensity.current) { textFieldWidth.toDp() })
            ) {
                filteredCities.forEach { city ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = city.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = city.province,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            onCitySelected(city)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Get list of major Indonesian cities
 */
fun getIndonesianCities(): List<City> = listOf(
    // Java
    City("Jakarta Selatan", "DKI Jakarta"),
    City("Jakarta Pusat", "DKI Jakarta"),
    City("Jakarta Barat", "DKI Jakarta"),
    City("Jakarta Timur", "DKI Jakarta"),
    City("Jakarta Utara", "DKI Jakarta"),
    City("Bandung", "Jawa Barat"),
    City("Bekasi", "Jawa Barat"),
    City("Bogor", "Jawa Barat"),
    City("Depok", "Jawa Barat"),
    City("Cimahi", "Jawa Barat"),
    City("Semarang", "Jawa Tengah"),
    City("Solo", "Jawa Tengah"),
    City("Magelang", "Jawa Tengah"),
    City("Tegal", "Jawa Tengah"),
    City("Surabaya", "Jawa Timur"),
    City("Malang", "Jawa Timur"),
    City("Kediri", "Jawa Timur"),
    City("Mojokerto", "Jawa Timur"),
    City("Yogyakarta", "DI Yogyakarta"),

    // Sumatra
    City("Medan", "Sumatera Utara"),
    City("Palembang", "Sumatera Selatan"),
    City("Padang", "Sumatera Barat"),
    City("Pekanbaru", "Riau"),
    City("Batam", "Kepulauan Riau"),
    City("Banda Aceh", "Aceh"),
    City("Jambi", "Jambi"),
    City("Lampung", "Lampung"),
    City("Bandar Lampung", "Lampung"),

    // Kalimantan
    City("Balikpapan", "Kalimantan Timur"),
    City("Samarinda", "Kalimantan Timur"),
    City("Banjarmasin", "Kalimantan Selatan"),
    City("Pontianak", "Kalimantan Barat"),

    // Sulawesi
    City("Makassar", "Sulawesi Selatan"),
    City("Manado", "Sulawesi Utara"),
    City("Palu", "Sulawesi Tengah"),
    City("Kendari", "Sulawesi Tenggara"),

    // Bali & Nusa Tenggara
    City("Denpasar", "Bali"),
    City("Mataram", "Nusa Tenggara Barat"),
    City("Kupang", "Nusa Tenggara Timur"),

    // Papua & Maluku
    City("Jayapura", "Papua"),
    City("Ambon", "Maluku"),
)
