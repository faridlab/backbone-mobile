package id.startapp.pheromone.presentation.ui.components.input.textfields

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import id.startapp.pheromone.presentation.ui.theme.PheromoneBlue
import id.startapp.pheromone.presentation.ui.theme.SurfaceGray300

/**
 * Tags Text Field
 *
 * Specialized text field for tag/chip input with multi-value support.
 *
 * Features:
 * - Add tags by pressing Enter
 * - Remove tags with X button
 * - Visual chip display
 * - Duplicate prevention
 *
 * @param tags List of tags
 * @param onTagsChange Callback when tags change
 * @param modifier Modifier for the text field
 * @param externalLabel Label text shown above field
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param allowedTags Optional whitelist of allowed tags
 * @param maxTags Maximum number of tags (0 = unlimited)
 * @param tagColor Color for tag chips
 */
@Composable
fun TagsTextField(
    tags: List<String>,
    onTagsChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    externalLabel: String? = "Tags",
    placeholder: String? = "Add tag and press Enter",
    enabled: Boolean = true,
    allowedTags: List<String>? = null,
    maxTags: Int = 0,
    tagColor: Color = PheromoneBlue
) {
    var currentInput by remember { mutableStateOf("") }

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

        // Tags display
        if (tags.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                tags.forEach { tag ->
                    TagChip(
                        tag = tag,
                        onRemove = {
                            onTagsChange(tags - tag)
                        },
                        color = tagColor,
                        enabled = enabled
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Input field
        OutlinedTextField(
            value = currentInput,
            onValueChange = { currentInput = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = placeholder?.let { { Text(it) } },
            enabled = enabled && (maxTags == 0 || tags.size < maxTags),
            singleLine = true,
            shape = RoundedCornerShape(4.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PheromoneBlue,
                unfocusedBorderColor = SurfaceGray300
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            trailingIcon = if (currentInput.isNotEmpty()) {
                {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Add tag",
                        tint = PheromoneBlue,
                        modifier = Modifier
                            .clickable(enabled) {
                                if (currentInput.isNotBlank()) {
                                    val newTag = currentInput.trim()
                                    if (tags.none { existing -> existing.equals(newTag, ignoreCase = true) }) {
                                        if (allowedTags == null || allowedTags.any { it.equals(newTag, ignoreCase = true) }) {
                                            onTagsChange(tags + newTag)
                                        }
                                    }
                                    currentInput = ""
                                }
                            }
                    )
                }
            } else null
        )

        // Helper text
        if (maxTags > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${tags.size}/$maxTags tags",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TagChip(
    tag: String,
    onRemove: () -> Unit,
    color: Color,
    enabled: Boolean
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, color
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tag,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Remove tag",
                tint = color,
                modifier = Modifier
                    .clickable(enabled) { onRemove() }
                    .height(16.dp)
                    .width(16.dp)
            )
        }
    }
}
