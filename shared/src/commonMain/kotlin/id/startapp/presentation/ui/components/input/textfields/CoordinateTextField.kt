package id.startapp.presentation.ui.components.input.textfields

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
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import id.startapp.presentation.ui.theme.BackboneBlue
import id.startapp.presentation.ui.theme.SurfaceGray300
import kotlin.math.abs
import kotlin.math.pow

// Helper function to format Double to fixed decimal places
private fun Double.toFixed(digits: Int): String {
    val factor = 10.0.pow(digits).toInt()
    val rounded = (this * factor).toInt().toDouble() / factor
    return if (rounded == this.toLong().toDouble()) {
        this.toLong().toString()
    } else {
        rounded.toString()
    }
}

/**
 * Coordinate Text Field
 *
 * Input for latitude and longitude coordinates with location detection.
 *
 * Features:
 * - Separate latitude and longitude inputs
 * - Detect current location button
 * - Decimal degree format validation
 * - Coordinate format hints
 *
 * @param latitude Current latitude value
 * @param onLatitudeChange Callback when latitude changes
 * @param longitude Current longitude value
 * @param onLongitudeChange Callback when longitude changes
 * @param modifier Modifier for the text field
 * @param enabled Whether the field is enabled
 * @param onDetectLocation Callback when detect location button is clicked
 * @param showDetectButton Whether to show the detect location button
 * @param readOnly Whether fields are read-only
 */
@Composable
fun CoordinateTextField(
    latitude: String,
    onLatitudeChange: (String) -> Unit,
    longitude: String,
    onLongitudeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onDetectLocation: (() -> Unit)? = null,
    showDetectButton: Boolean = true,
    readOnly: Boolean = false
) {
    var latError by remember { mutableStateOf<String?>(null) }
    var lonError by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier) {
        // Label with detect button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = "Coordinates",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            if (showDetectButton && onDetectLocation != null) {
                IconButton(
                    onClick = onDetectLocation,
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MyLocation,
                        contentDescription = "Detect Location",
                        tint = if (enabled) BackboneBlue else SurfaceGray300
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Latitude field
        OutlinedTextField(
            value = latitude,
            onValueChange = { newValue ->
                val filtered = newValue.filter { c -> c.isDigit() || c == '.' || c == '-' }
                onLatitudeChange(filtered)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Latitude") },
            placeholder = { Text("-6.2088") },
            enabled = enabled && !readOnly,
            singleLine = true,
            shape = RoundedCornerShape(4.dp),
            isError = latError != null,
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BackboneBlue,
                unfocusedBorderColor = SurfaceGray300,
                errorBorderColor = androidx.compose.ui.graphics.Color.Red
            ),
            leadingIcon = {
                Text(
                    text = "Lat",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            supportingText = {
                Text(
                    text = latError ?: "Range: -90 to 90",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (latError != null)
                        androidx.compose.ui.graphics.Color.Red
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Longitude field
        OutlinedTextField(
            value = longitude,
            onValueChange = { newValue ->
                val filtered = newValue.filter { c -> c.isDigit() || c == '.' || c == '-' }
                onLongitudeChange(filtered)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Longitude") },
            placeholder = { Text("106.8456") },
            enabled = enabled && !readOnly,
            singleLine = true,
            shape = RoundedCornerShape(4.dp),
            isError = lonError != null,
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BackboneBlue,
                unfocusedBorderColor = SurfaceGray300,
                errorBorderColor = androidx.compose.ui.graphics.Color.Red
            ),
            leadingIcon = {
                Text(
                    text = "Lon",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            supportingText = {
                Text(
                    text = lonError ?: "Range: -180 to 180",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (lonError != null)
                        androidx.compose.ui.graphics.Color.Red
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )

        // Validation button (for manual validation trigger)
        if (!readOnly && (latitude.isNotBlank() || longitude.isNotBlank())) {
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.material3.Button(
                onClick = {
                    latError = validateLatitude(latitude)
                    lonError = validateLongitude(longitude)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            ) {
                Text("Validate Coordinates")
            }
        }
    }
}

/**
 * Validate latitude value
 */
private fun validateLatitude(value: String): String? {
    val lat = value.toDoubleOrNull()
    return when {
        value.isBlank() -> null
        lat == null -> "Invalid latitude format"
        lat < -90 || lat > 90 -> "Latitude must be between -90 and 90"
        else -> null
    }
}

/**
 * Validate longitude value
 */
private fun validateLongitude(value: String): String? {
    val lon = value.toDoubleOrNull()
    return when {
        value.isBlank() -> null
        lon == null -> "Invalid longitude format"
        lon < -180 || lon > 180 -> "Longitude must be between -180 and 180"
        else -> null
    }
}

/**
 * Coordinate display card (read-only view)
 */
@Composable
fun CoordinateDisplayCard(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
    onMapClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.MyLocation,
                contentDescription = "Location",
                tint = BackboneBlue
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Coordinates",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${latitude.toFixed(6)}, ${longitude.toFixed(6)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (onMapClick != null) {
                androidx.compose.material3.TextButton(onClick = onMapClick) {
                    Text("View on Map")
                }
            }
        }
    }
}
