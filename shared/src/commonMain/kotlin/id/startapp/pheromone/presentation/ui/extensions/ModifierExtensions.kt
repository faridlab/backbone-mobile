package id.startapp.pheromone.presentation.ui.extensions

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager

/**
 * Clears focus from the currently focused component when the user taps
 * outside any focusable element. This dismisses the keyboard and removes
 * the cursor from text fields.
 *
 * Usage:
 * ```
 * val focusManager = LocalFocusManager.current
 * Column(
 *     modifier = Modifier.clearFocusOnTap(focusManager)
 * ) { ... }
 * ```
 *
 * @param focusManager The FocusManager obtained from LocalFocusManager.current
 */
fun Modifier.clearFocusOnTap(focusManager: androidx.compose.ui.focus.FocusManager): Modifier =
    this.pointerInput(Unit) {
        detectTapGestures { focusManager.clearFocus() }
    }
