package id.startapp.presentation.ui.components.input.textfields

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import id.startapp.presentation.ui.components.input.AppTextField
import kotlinx.coroutines.delay

/**
 * Search Text Field
 *
 * Specialized text field for search input with debouncing and clear button.
 *
 * Features:
 * - Text keyboard type
 * - Search icon
 * - Clear button (when not empty)
 * - Debounced search callback (default: 300ms)
 * - Action button support
 *
 * @param value Current search query
 * @param onValueChange Callback when query changes (immediate)
 * @param onSearch Callback when search is triggered (debounced or on action)
 * @param modifier Modifier for the text field
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param debounceMs Debounce delay in milliseconds
 * @param onClear Callback when clear button is clicked
 * @param showClearButton Whether to show the clear button
 */
@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = "Search...",
    enabled: Boolean = true,
    debounceMs: Long = 300,
    onClear: (() -> Unit)? = null,
    showClearButton: Boolean = true,
    testTag: String? = null,
    contentDescription: String? = null
) {
    var lastSearchQuery by remember { mutableStateOf("") }

    // Debounce search callback
    LaunchedEffect(value) {
        if (value != lastSearchQuery) {
            delay(debounceMs)
            if (value == value) { // Check if value hasn't changed during delay
                lastSearchQuery = value
                onSearch(value)
            }
        }
    }

    AppTextField(
        value = value,
        onValueChange = { newValue ->
            onValueChange(newValue)
            lastSearchQuery = newValue // Update to prevent duplicate searches
        },
        modifier = modifier,
        placeholder = placeholder,
        enabled = enabled,
        testTag = testTag,
        contentDescription = contentDescription,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = if (showClearButton && value.isNotEmpty()) {
            {
                IconButton(onClick = {
                    onValueChange("")
                    lastSearchQuery = ""
                    onClear?.invoke()
                    onSearch("")
                }) {
                    Icon(
                        imageVector = Icons.Rounded.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search,
            autoCorrectEnabled = false
        ),
        onBlur = {
            // Trigger search immediately on blur
            if (value != lastSearchQuery) {
                lastSearchQuery = value
                onSearch(value)
            }
        }
    )
}

/**
 * Search Text Field with Live Results
 *
 * Variant that shows search results count and loading state.
 *
 * @param value Current search query
 * @param onValueChange Callback when query changes
 * @param onSearch Callback when search is triggered
 * @param modifier Modifier for the text field
 * @param placeholder Placeholder text
 * @param enabled Whether the field is enabled
 * @param resultCount Number of search results (null = not searching/loading)
 * @param isLoading Whether a search is in progress
 * @param debounceMs Debounce delay in milliseconds
 */
@Composable
fun SearchTextFieldWithResults(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = "Search...",
    enabled: Boolean = true,
    resultCount: Int? = null,
    isLoading: Boolean = false,
    debounceMs: Long = 300,
    testTag: String? = null,
    contentDescription: String? = null
) {
    var lastSearchQuery by remember { mutableStateOf("") }

    LaunchedEffect(value) {
        if (value != lastSearchQuery) {
            delay(debounceMs)
            if (value == value) {
                lastSearchQuery = value
                onSearch(value)
            }
        }
    }

    AppTextField(
        value = value,
        onValueChange = { newValue ->
            onValueChange(newValue)
            lastSearchQuery = newValue
        },
        modifier = modifier,
        placeholder = placeholder,
        enabled = enabled,
        testTag = testTag,
        contentDescription = contentDescription,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = if (value.isNotEmpty() || isLoading) {
            {
                IconButton(onClick = {
                    onValueChange("")
                    lastSearchQuery = ""
                    onSearch("")
                }) {
                    Icon(
                        imageVector = Icons.Rounded.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search,
            autoCorrectEnabled = false
        ),
        helperText = when {
            isLoading -> "Searching..."
            resultCount != null -> "$resultCount result${if (resultCount != 1) "s" else ""} found"
            value.isNotEmpty() -> "Press Enter or wait to search"
            else -> null
        }
    )
}
