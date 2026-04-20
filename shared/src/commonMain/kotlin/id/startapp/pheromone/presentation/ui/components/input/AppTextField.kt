package id.startapp.pheromone.presentation.ui.components.input

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import id.startapp.pheromone.presentation.ui.theme.PheromoneBlue
import id.startapp.pheromone.presentation.ui.theme.PheromoneShapes
import id.startapp.pheromone.presentation.ui.theme.ErrorRed
import id.startapp.pheromone.presentation.ui.theme.SurfaceGray300

/**
 * Pheromone App Text Field
 *
 * Outlined text field following Pheromone design system.
 *
 * Specs:
 * - Height: 56dp
 * - Border Radius: 4dp (outlined)
 * - Padding: 16dp
 * - Font: Body Medium
 * - Label: Label Medium (floating or external above field)
 * - Border Color: Gray-400 → Brand Blue (focused)
 * - Error Color: Error Red
 *
 * @param value Current text value
 * @param onValueChange Callback when text changes
 * @param modifier Modifier for the text field
 * @param label Optional label text (floating label inside field)
 * @param placeholder Optional placeholder text
 * @param leadingIcon Optional leading icon
 * @param trailingIcon Optional trailing icon
 * @param error Optional error message
 * @param helperText Optional helper text
 * @param enabled Whether the field is enabled
 * @param readOnly Whether the field is read-only
 * @param singleLine Whether to restrict to single line
 * @param maxLines Maximum number of lines
 * @param minLines Minimum number of lines
 * @param keyboardOptions Keyboard options
 * @param visualTransformation Visual transformation for input masking
 * @param externalLabel Optional external label text (rendered above field, not floating)
 * @param externalLabelRequired Whether to show required asterisk (*) with external label
 * @param testTag Optional test tag for UI testing
 * @param contentDescription Optional content description for accessibility
 * @param onBlur Callback when field loses focus (user clicks outside)
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    error: String? = null,
    helperText: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    externalLabel: String? = null,
    externalLabelRequired: Boolean = false,
    testTag: String? = null,
    contentDescription: String? = null,
    onBlur: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Track previous focus state to detect focus loss transitions only
    var previousFocused by remember { mutableStateOf(false) }
    LaunchedEffect(isFocused) {
        if (previousFocused && !isFocused) {
            onBlur()
        }
        previousFocused = isFocused
    }

    val isError = error != null

    Column(modifier = modifier) {
        // External label rendered above the field (null-safe)
        externalLabel?.let { label ->
            ExternalLabel(
                text = label,
                required = externalLabelRequired,
                isError = isError
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    Modifier.semantics {
                        // Content description for accessibility
                        contentDescription?.let {
                            this.contentDescription = it
                        }
                        // Error announcement for screen readers
                        if (error != null) {
                            this.error(error)
                        }
                        // Current value for screen readers
                        this.text = AnnotatedString(value)
                    }
                )
                .then(
                    // Test tag for UI testing
                    if (testTag != null) Modifier.testTag(testTag) else Modifier
                ),
            label = buildFloatingLabel(externalLabel, label),
            placeholder = buildPlaceholder(placeholder),
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            interactionSource = interactionSource,
            supportingText = if (error != null || helperText != null) {
                {
                    Text(
                        text = error ?: helperText ?: "",
                        color = if (error != null) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else null,
            isError = error != null,
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) ErrorRed else PheromoneBlue,
                unfocusedBorderColor = if (isError) ErrorRed else SurfaceGray300,
                disabledBorderColor = SurfaceGray300,
                errorBorderColor = ErrorRed,
                focusedLabelColor = if (isError) ErrorRed else PheromoneBlue,
                unfocusedLabelColor = if (isError) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = PheromoneBlue,
                errorCursorColor = ErrorRed
            ),
            shape = RoundedCornerShape(PheromoneShapes.InputRadius.dp)
        )
    }
}

/**
 * App Text Field with TextFieldValue
 *
 * Version that accepts TextFieldValue for more control (cursor position, selection).
 *
 * @param value Current TextFieldValue
 * @param onValueChange Callback when value changes
 * @param modifier Modifier for the text field
 * @param label Optional label text
 * @param placeholder Optional placeholder text
 * @param leadingIcon Optional leading icon
 * @param trailingIcon Optional trailing icon
 * @param error Optional error message
 * @param helperText Optional helper text
 * @param enabled Whether the field is enabled
 * @param readOnly Whether the field is read-only
 * @param singleLine Whether to restrict to single line
 * @param maxLines Maximum number of lines
 * @param keyboardOptions Keyboard options
 * @param visualTransformation Visual transformation for input masking
 * @param externalLabel Optional external label text (rendered above field, not floating)
 * @param externalLabelRequired Whether to show required asterisk (*) with external label
 * @param testTag Optional test tag for UI testing
 * @param contentDescription Optional content description for accessibility
 * @param onBlur Callback when field loses focus (user clicks outside)
 */
@Composable
fun AppTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    error: String? = null,
    helperText: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    externalLabel: String? = null,
    externalLabelRequired: Boolean = false,
    testTag: String? = null,
    contentDescription: String? = null,
    onBlur: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Track previous focus state to detect focus loss transitions only
    var previousFocused by remember { mutableStateOf(false) }
    LaunchedEffect(isFocused) {
        if (previousFocused && !isFocused) {
            onBlur()
        }
        previousFocused = isFocused
    }

    val isError = error != null

    Column(modifier = modifier) {
        // External label rendered above the field (null-safe)
        externalLabel?.let { label ->
            ExternalLabel(
                text = label,
                required = externalLabelRequired,
                isError = isError
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    Modifier.semantics {
                        // Content description for accessibility
                        contentDescription?.let {
                            this.contentDescription = it
                        }
                        // Error announcement for screen readers
                        if (error != null) {
                            this.error(error)
                        }
                        // Current value for screen readers
                        this.text = AnnotatedString(value.text)
                    }
                )
                .then(
                    // Test tag for UI testing
                    if (testTag != null) Modifier.testTag(testTag) else Modifier
                ),
            label = buildFloatingLabel(externalLabel, label),
            placeholder = buildPlaceholder(placeholder),
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            interactionSource = interactionSource,
            supportingText = if (error != null || helperText != null) {
                {
                    Text(
                        text = error ?: helperText ?: "",
                        color = if (error != null) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else null,
            isError = error != null,
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = 1,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) ErrorRed else PheromoneBlue,
                unfocusedBorderColor = if (isError) ErrorRed else SurfaceGray300,
                disabledBorderColor = SurfaceGray300,
                errorBorderColor = ErrorRed,
                focusedLabelColor = if (isError) ErrorRed else PheromoneBlue,
                unfocusedLabelColor = if (isError) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = PheromoneBlue,
                errorCursorColor = ErrorRed
            ),
            shape = RoundedCornerShape(PheromoneShapes.InputRadius.dp)
        )
    }
}

/**
 * Label Medium composable for input field labels (floating labels)
 */
@Composable
private fun LabelMedium(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium
    )
}

/**
 * Build floating label composable if external label is not used
 *
 * @param externalLabel External label text (if provided, floating label is disabled)
 * @param label Optional floating label text
 * @return Label composable or null
 */
@Composable
private fun buildFloatingLabel(
    externalLabel: String?,
    label: String?
): (@Composable () -> Unit)? {
    return if (externalLabel == null && label != null) {
        @Composable { LabelMedium(text = label) }
    } else null
}

/**
 * Build placeholder composable
 *
 * @param placeholder Optional placeholder text
 * @return Text composable or null
 */
@Composable
private fun buildPlaceholder(
    placeholder: String?
): (@Composable () -> Unit)? {
    return placeholder?.let {
        @Composable {
            Text(placeholder, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/**
 * External Label composable for labels positioned above the text field
 *
 * @param text Label text
 * @param required Whether to show required asterisk
 * @param isError Whether the field has an error (affects label color)
 */
@Composable
private fun ExternalLabel(
    text: String,
    required: Boolean,
    isError: Boolean
) {
    val labelColor = if (isError) ErrorRed else MaterialTheme.colorScheme.onSurface

    Text(
        text = if (required) "$text *" else text,
        style = MaterialTheme.typography.labelMedium,
        color = labelColor
    )

    Spacer(modifier = Modifier.height(4.dp))
}
