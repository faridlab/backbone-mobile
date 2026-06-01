package id.startapp.presentation.ui.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import id.startapp.presentation.ui.theme.BackboneBlue
import id.startapp.presentation.ui.theme.BackboneDimensions
import id.startapp.presentation.ui.theme.ErrorRed
import id.startapp.presentation.ui.theme.SurfaceGray300

/**
 * Backbone Bottom Sheet
 *
 * Modal bottom sheet following Backbone design system.
 *
 * Specs:
 * - Handle bar: 32dp height, Centered, 4dp × 32dp rounded bar
 * - Corner Radius: 28dp (top corners only)
 * - Padding: 24dp
 * - Elevation: Modal elevation
 *
 * @param onDismiss Callback when sheet is dismissed
 * @param modifier Modifier for the sheet
 * @param dragHandle Whether to show drag handle
 * @param content Sheet content
 */
@Composable
fun BackboneBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dragHandle: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 6.dp,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            // Drag handle
            if (dragHandle) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(4.dp)
                            .background(
                                SurfaceGray300,
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }

            // Content
            content()
        }
    }
}

/**
 * Modal Bottom Sheet Container
 *
 * Container for bottom sheet with backdrop overlay.
 *
 * @param isVisible Whether the sheet is visible
 * @param onDismiss Callback when sheet is dismissed
 * @param modifier Modifier for the container
 * @param content Sheet content
 */
@Composable
fun ModalBottomSheetContainer(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    if (isVisible) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            // Backdrop
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onDismiss)
            )

            // Sheet
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                BackboneBottomSheet(
                    onDismiss = onDismiss,
                    content = content
                )
            }
        }
    }
}

/**
 * Action Bottom Sheet
 *
 * Bottom sheet for action selection (e.g., photo picker options).
 *
 * @param title Optional title
 * @param actions List of actions to display
 * @param onDismiss Callback when sheet is dismissed
 * @param modifier Modifier for the sheet
 */
@Composable
fun ActionBottomSheet(
    title: String? = null,
    actions: List<BottomSheetAction>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackboneBottomSheet(
        onDismiss = onDismiss,
        modifier = modifier
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = BackboneDimensions.LG.dp)
            )
        }

        actions.forEach { action ->
            BottomSheetActionItem(
                text = action.text,
                icon = action.icon,
                onClick = {
                    action.onClick()
                    onDismiss()
                },
                danger = action.danger
            )
        }
    }
}

/**
 * Bottom Sheet Action Item
 *
 * Single action item in a bottom sheet.
 *
 * @param text Action text
 * @param icon Optional icon
 * @param onClick Callback when action is clicked
 * @param danger Whether this is a destructive action
 * @param modifier Modifier for the item
 */
@Composable
private fun BottomSheetActionItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit,
    danger: Boolean = false,
    modifier: Modifier = Modifier
) {
    val iconTint = if (danger) {
        ErrorRed
    } else {
        BackboneBlue
    }

    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = BackboneDimensions.LG.dp,
                vertical = BackboneDimensions.MD.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (danger) ErrorRed else MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Confirmation Bottom Sheet
 *
 * Bottom sheet for confirmation dialogs.
 *
 * @param title Title text
 * @param message Message text
 * @param confirmLabel Confirm button label
 * @param onConfirm Callback for confirm action
 * @param onDismiss Callback for dismiss/cancel
 * @param modifier Modifier for the sheet
 */
@Composable
fun ConfirmationBottomSheet(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackboneBottomSheet(
        onDismiss = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BackboneDimensions.LG.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(BackboneDimensions.SM.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(BackboneDimensions.LG.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(BackboneDimensions.MD.dp)
            ) {
                // Cancel button
                id.startapp.presentation.ui.components.button.SecondaryButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )

                // Confirm button
                id.startapp.presentation.ui.components.button.PrimaryButton(
                    text = confirmLabel,
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Data class for bottom sheet actions
 */
data class BottomSheetAction(
    val text: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val onClick: () -> Unit,
    val danger: Boolean = false
)
