package id.startapp.pheromone.presentation.ui.components.tree

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.startapp.pheromone.presentation.ui.theme.PheromoneBlue
import id.startapp.pheromone.presentation.ui.theme.SurfaceGray200
import id.startapp.pheromone.presentation.ui.theme.SurfaceGray300

/**
 * Generic Tree Node Item
 *
 * Displays a single tree node with:
 * - Expand/collapse chevron (for parent nodes)
 * - Optional icon (emoji)
 * - Node name
 * - Selection indicator on right (checkbox-style circle)
 *
 * Row is always clickable:
 * - Parent nodes: clicking toggles expand/collapse
 * - Leaf nodes / selectable nodes: clicking selects
 *
 * @param T The type of data associated with nodes
 * @param node The tree node to display
 * @param onClick Callback when node row is clicked (expand/collapse or select)
 * @param onCheckClick Optional callback specifically for check icon click
 * @param selectable Whether this node can be selected (controls check indicator)
 * @param contentPadding Vertical padding for the content
 * @param indentPerLevel Indentation per hierarchy level
 */
@Composable
fun <T> TreeNodeItem(
    node: TreeNode<T>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onCheckClick: (() -> Unit)? = null,
    selectable: Boolean = true,
    contentPadding: Dp = 12.dp,
    indentPerLevel: Dp = 16.dp
) {
    val isSelected = node.isSelected
    val backgroundColor = if (isSelected) {
        PheromoneBlue.copy(alpha = 0.08f)
    } else {
        Color.Transparent
    }

    val textColor = Color.Black
    val chevronColor = Color(0xFF9E9E9E)

    // Calculate indentation based on level
    val indent = (node.level * indentPerLevel.value).dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = contentPadding)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indentation spacer
        Spacer(modifier = Modifier.width(indent))

        // Expand/Collapse Chevron
        if (node.hasChildren) {
            Icon(
                imageVector = if (node.isExpanded) {
                    Icons.Default.KeyboardArrowDown
                } else {
                    Icons.Default.KeyboardArrowRight
                },
                contentDescription = if (node.isExpanded) "Collapse" else "Expand",
                tint = chevronColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        } else {
            // Leaf indent to align with parent text
            Spacer(modifier = Modifier.width(24.dp))
        }

        // Node Icon (optional - only show if provided)
        if (node.icon != null) {
            Text(
                text = node.icon,
                fontSize = 18.sp,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        // Node Name
        Text(
            text = node.name,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            fontSize = 15.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // Selection indicator (right side) - only for selectable nodes
        if (selectable) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clickable(enabled = selectable) {
                        if (onCheckClick != null) {
                            onCheckClick()
                        } else {
                            onClick()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    // Selected: filled blue circle with white checkmark
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(PheromoneBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else {
                    // Unselected: empty circle outline
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .border(width = 2.dp, color = SurfaceGray300, shape = CircleShape)
                    )
                }
            }
        }
    }
}

/**
 * Divider between tree sections
 */
@Composable
fun TreeDivider(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(SurfaceGray200)
    )
}
