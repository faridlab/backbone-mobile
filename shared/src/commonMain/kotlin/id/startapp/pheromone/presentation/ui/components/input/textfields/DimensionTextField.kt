package id.startapp.pheromone.presentation.ui.components.input.textfields

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import id.startapp.pheromone.presentation.ui.components.input.AppTextField

/**
 * Dimension Text Field
 *
 * For L x W x H input (package/item sizing).
 *
 * @param dimensions Triple of (length, width, height)
 * @param onDimensionsChange Callback
 * @param modifier Modifier
 * @param externalLabel Label
 * @param enabled Whether enabled
 * @param unit Unit suffix (cm, mm, inch)
 * @param testTag Test tag for UI testing
 * @param contentDescription Content description for accessibility
 */
@Composable
fun DimensionTextField(
    dimensions: Triple<String, String, String>,
    onDimensionsChange: (Triple<String, String, String>) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = "Dimensions",
    enabled: Boolean = true,
    unit: String = "cm",
    testTag: String? = null,
    contentDescription: String? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        externalLabel?.let { label ->
            Text(label, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.width(8.dp))
        }

        // Length
        AppTextField(
            value = dimensions.first,
            onValueChange = { newValue ->
                val filtered = newValue.filter { c -> c.isDigit() || c == '.' }
                onDimensionsChange(Triple(filtered, dimensions.second, dimensions.third))
            },
            modifier = Modifier.weight(1f),
            placeholder = "Length",
            enabled = enabled,
            singleLine = true,
            testTag = testTag?.let { "${it}_length" },
            contentDescription = contentDescription?.let { "$it Length" },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            )
        )
        Text(unit, style = MaterialTheme.typography.bodySmall)

        // Width
        AppTextField(
            value = dimensions.second,
            onValueChange = { newValue ->
                val filtered = newValue.filter { c -> c.isDigit() || c == '.' }
                onDimensionsChange(Triple(dimensions.first, filtered, dimensions.third))
            },
            modifier = Modifier.weight(1f),
            placeholder = "Width",
            enabled = enabled,
            singleLine = true,
            testTag = testTag?.let { "${it}_width" },
            contentDescription = contentDescription?.let { "$it Width" },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            )
        )
        Text(unit, style = MaterialTheme.typography.bodySmall)

        // Height
        AppTextField(
            value = dimensions.third,
            onValueChange = { newValue ->
                val filtered = newValue.filter { c -> c.isDigit() || c == '.' }
                onDimensionsChange(Triple(dimensions.first, dimensions.second, filtered))
            },
            modifier = Modifier.weight(1f),
            placeholder = "Height",
            enabled = enabled,
            singleLine = true,
            testTag = testTag?.let { "${it}_height" },
            contentDescription = contentDescription?.let { "$it Height" },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            )
        )
        Text(unit, style = MaterialTheme.typography.bodySmall)
    }
}
