package id.startapp.presentation.ui.components.tree

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.startapp.presentation.ui.theme.BackboneBlue
import kotlinx.coroutines.launch

/**
 * Generic Tree View Bottom Sheet
 *
 * A bottom sheet containing a hierarchical tree view.
 * Supports both multi-select (default) and single-select modes.
 *
 * Multi-select: checkbox on every level, parent cascade, Save button.
 * Single-select: only leaf nodes selectable, one at a time, "Pilih" button.
 *
 * @param T The type of data associated with tree nodes
 * @param isVisible Whether the bottom sheet is visible
 * @param onDismiss Callback when the sheet is dismissed
 * @param title Title for the bottom sheet
 * @param treeData The hierarchical tree data
 * @param onSave Callback when Save/Pilih is clicked with selected nodes
 * @param initialSelectedIds Optional initially selected node IDs
 * @param singleSelect When true, only one leaf node can be selected at a time
 * @param modifier Modifier for the sheet content
 * @param sheetState Bottom sheet state
 * @param maxHeight Maximum height for the tree content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> TreeViewBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    treeData: List<TreeNode<T>>,
    onSave: (selectedNodes: List<TreeSelection<T>>) -> Unit,
    initialSelectedIds: Set<String> = emptySet(),
    singleSelect: Boolean = false,
    modifier: Modifier = Modifier,
    sheetState: SheetState? = null,
    maxHeight: androidx.compose.ui.unit.Dp = 400.dp
) {
    if (!isVisible) return

    val actualSheetState = sheetState ?: rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Initialize tree with pre-selected nodes
    var currentTree by remember {
        val initialized = if (initialSelectedIds.isNotEmpty()) {
            if (singleSelect) {
                // For single-select, select only the first matching leaf
                val leafId = initialSelectedIds.first()
                selectSingleLeaf(treeData, leafId)
            } else {
                applyInitialSelection(treeData, initialSelectedIds)
            }
        } else {
            treeData
        }
        mutableStateOf(initialized)
    }

    // Flatten tree to show only visible (expanded) nodes
    val visibleNodes = remember(currentTree) {
        flattenTreeNodes(currentTree, expandedOnly = true)
    }

    // Count nodes for display
    val totalLeafCount = remember(currentTree) { countLeafNodes(currentTree) }
    val selectedLeafCount = remember(currentTree) { countSelectedLeafNodes(currentTree) }
    val hasAnySelection = remember(currentTree) { getSelectedNodes(currentTree).isNotEmpty() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = actualSheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Tree Content
            Box(modifier = Modifier.height(maxHeight)) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        items = visibleNodes,
                        key = { it.id }
                    ) { node ->
                        if (singleSelect) {
                            // Single-select mode: all nodes selectable, tap to select or expand
                            TreeNodeItem(
                                node = node,
                                selectable = true,
                                onClick = {
                                    if (node.hasChildren) {
                                        // Toggle expand/collapse
                                        currentTree = updateTreeNode(
                                            currentTree,
                                            node.id
                                        ) { it.withExpanded(!node.isExpanded) }
                                    } else {
                                        // Leaf: single-select
                                        currentTree = selectSingleLeaf(currentTree, node.id)
                                    }
                                },
                                onCheckClick = {
                                    // Select this node (works for both parent and leaf)
                                    currentTree = selectSingleLeaf(currentTree, node.id)
                                }
                            )
                        } else {
                            // Multi-select mode (original behavior)
                            TreeNodeItem(
                                node = node,
                                selectable = true,
                                onClick = {
                                    if (node.hasChildren) {
                                        currentTree = updateTreeNode(
                                            currentTree,
                                            node.id
                                        ) { it.withExpanded(!node.isExpanded) }
                                    } else {
                                        currentTree = toggleNodeWithCascade(
                                            currentTree,
                                            node.id
                                        )
                                    }
                                },
                                onCheckClick = {
                                    currentTree = toggleNodeWithCascade(
                                        currentTree,
                                        node.id
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save / Pilih Button
            Button(
                onClick = {
                    val selected = getSelectedNodes(currentTree)
                        .map { node ->
                            TreeSelection(
                                nodeId = node.id,
                                nodeName = node.name,
                                data = node.data,
                                path = buildNodePath(currentTree, node.id)
                            )
                        }
                    onSave(selected)
                },
                enabled = if (singleSelect) hasAnySelection else selectedLeafCount > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BackboneBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = if (singleSelect) {
                        "Pilih"
                    } else {
                        if (selectedLeafCount > 0) "Save ($selectedLeafCount/$totalLeafCount selected)" else "Save"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Apply initial selection to tree nodes
 */
private fun <T> applyInitialSelection(
    roots: List<TreeNode<T>>,
    selectedIds: Set<String>
): List<TreeNode<T>> {
    return roots.map { node ->
        node.copy(
            isSelected = node.id in selectedIds,
            children = applyInitialSelection(node.children, selectedIds)
        )
    }
}

// ServiceTypeTemplateTreeBottomSheet removed — depended on deleted business entity.
// Use TreeViewBottomSheet directly with your own data type.

private fun __skeletonStub() {
    // placeholder
}
