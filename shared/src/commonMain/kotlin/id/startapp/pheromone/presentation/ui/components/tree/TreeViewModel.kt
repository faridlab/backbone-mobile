package id.startapp.pheromone.presentation.ui.components.tree

/**
 * Tree Node Data Model
 *
 * Generic tree node for hierarchical data with support for
 * nested children and selection state. Can be used for any
 * tree-like UI component (category selection, file browser, etc.).
 *
 * @property T The type of data associated with this node
 * @property id Unique identifier for the node
 * @property name Display name of the node
 * @property level Hierarchy level (0 = root, 1 = first child, etc.)
 * @property icon Optional icon identifier (emoji or icon name)
 * @property data Optional custom data associated with this node
 * @property isExpanded Whether the node is currently expanded (for parent nodes)
 * @property isSelected Whether the node is currently selected
 * @property isEnabled Whether the node is enabled for selection
 * @property children Child nodes (empty for leaf nodes)
 * @property parentId Optional parent node ID
 */
data class TreeNode<T>(
    val id: String,
    val name: String,
    val level: Int = 0,
    val icon: String? = null,
    val data: T? = null,
    val isExpanded: Boolean = false,
    val isSelected: Boolean = false,
    val isEnabled: Boolean = true,
    val children: List<TreeNode<T>> = emptyList(),
    val parentId: String? = null
) {
    /**
     * Check if this node has children
     */
    val hasChildren: Boolean
        get() = children.isNotEmpty()

    /**
     * Check if this is a leaf node
     */
    val isLeaf: Boolean
        get() = children.isEmpty()

    /**
     * Create a copy with updated expansion state
     */
    fun withExpanded(expanded: Boolean): TreeNode<T> {
        return copy(isExpanded = expanded)
    }

    /**
     * Create a copy with updated selection state
     */
    fun withSelected(selected: Boolean): TreeNode<T> {
        return copy(isSelected = selected)
    }

    /**
     * Create a copy with updated enabled state
     */
    fun withEnabled(enabled: Boolean): TreeNode<T> {
        return copy(isEnabled = enabled)
    }

    /**
     * Create a copy with updated children
     */
    fun withChildren(newChildren: List<TreeNode<T>>): TreeNode<T> {
        return copy(children = newChildren)
    }

    /**
     * Recursively update a node by ID
     */
    fun updateNode(
        nodeId: String,
        update: (TreeNode<T>) -> TreeNode<T>
    ): TreeNode<T> {
        return if (this.id == nodeId) {
            update(this)
        } else {
            copy(children = children.map { it.updateNode(nodeId, update) })
        }
    }

    /**
     * Flatten the tree to a list (only expanded nodes' children)
     */
    fun flatten(expandedOnly: Boolean = true): List<TreeNode<T>> {
        val result = mutableListOf<TreeNode<T>>()
        result.add(this)

        if (!expandedOnly || isExpanded) {
            children.forEach { child ->
                result.addAll(child.flatten(expandedOnly))
            }
        }

        return result
    }

    /**
     * Find a node by ID
     */
    fun findNode(nodeId: String): TreeNode<T>? {
        return if (this.id == nodeId) {
            this
        } else {
            children.firstNotNullOfOrNull { it.findNode(nodeId) }
        }
    }

    /**
     * Get the path from root to this node
     */
    fun getPath(): List<String> {
        val path = mutableListOf(id)
        var current: TreeNode<T>? = this
        while (current?.parentId != null) {
            // Would need parent reference to properly build path
            // This is a simplified version
            break
        }
        return path
    }

    companion object {
        /**
         * Create a root node
         */
        fun <T> root(
            id: String,
            name: String,
            icon: String? = null,
            data: T? = null,
            children: List<TreeNode<T>> = emptyList(),
            isExpanded: Boolean = false
        ): TreeNode<T> {
            return TreeNode(
                id = id,
                name = name,
                level = 0,
                icon = icon,
                data = data,
                children = children,
                isExpanded = isExpanded
            )
        }

        /**
         * Create a child node
         */
        fun <T> child(
            id: String,
            name: String,
            parentId: String,
            level: Int = 1,
            icon: String? = null,
            data: T? = null,
            children: List<TreeNode<T>> = emptyList(),
            isExpanded: Boolean = false
        ): TreeNode<T> {
            return TreeNode(
                id = id,
                name = name,
                level = level,
                icon = icon,
                data = data,
                children = children,
                isExpanded = isExpanded,
                parentId = parentId
            )
        }

        /**
         * Create a leaf node
         */
        fun <T> leaf(
            id: String,
            name: String,
            parentId: String,
            level: Int = 1,
            icon: String? = null,
            data: T? = null
        ): TreeNode<T> {
            return TreeNode(
                id = id,
                name = name,
                level = level,
                icon = icon,
                data = data,
                children = emptyList(),
                parentId = parentId
            )
        }
    }
}

/**
 * Tree Selection State
 *
 * Generic selection state for tree components.
 *
 * @property T The type of data associated with the selected node
 * @property nodeId Selected node ID
 * @property nodeName Selected node name
 * @property data Optional custom data from the selected node
 * @property path Full path from root to selected node
 */
data class TreeSelection<T>(
    val nodeId: String,
    val nodeName: String,
    val data: T? = null,
    val path: List<String> = emptyList()
)

/**
 * Flatten a list of tree roots to a single list of visible nodes
 */
fun <T> flattenTreeNodes(
    roots: List<TreeNode<T>>,
    expandedOnly: Boolean = true
): List<TreeNode<T>> {
    return roots.flatMap { it.flatten(expandedOnly) }
}

/**
 * Update a node in a list of tree roots by ID
 */
fun <T> updateTreeNode(
    roots: List<TreeNode<T>>,
    nodeId: String,
    update: (TreeNode<T>) -> TreeNode<T>
): List<TreeNode<T>> {
    return roots.map { it.updateNode(nodeId, update) }
}

/**
 * Find a node in a list of tree roots by ID
 */
fun <T> findTreeNode(
    roots: List<TreeNode<T>>,
    nodeId: String
): TreeNode<T>? {
    return roots.firstNotNullOfOrNull { it.findNode(nodeId) }
}

/**
 * Collapse all nodes in the tree
 */
fun <T> collapseAll(roots: List<TreeNode<T>>): List<TreeNode<T>> {
    return roots.map { node ->
        node.copy(
            isExpanded = false,
            children = collapseAll(node.children)
        )
    }
}

/**
 * Expand all nodes in the tree
 */
fun <T> expandAll(roots: List<TreeNode<T>>): List<TreeNode<T>> {
    return roots.map { node ->
        node.copy(
            isExpanded = true,
            children = expandAll(node.children)
        )
    }
}

/**
 * Clear all selections in the tree
 */
fun <T> clearSelection(roots: List<TreeNode<T>>): List<TreeNode<T>> {
    return roots.map { node ->
        node.copy(
            isSelected = false,
            children = clearSelection(node.children)
        )
    }
}

/**
 * Select/deselect an entire subtree (node + all descendants)
 */
fun <T> selectSubtree(node: TreeNode<T>, selected: Boolean): TreeNode<T> {
    return node.copy(
        isSelected = selected,
        children = node.children.map { selectSubtree(it, selected) }
    )
}

/**
 * Toggle selection on a node with full cascade:
 * - Downward: selecting/deselecting a node cascades to all descendants
 * - Upward: if any child is selected, parent becomes selected;
 *   if all children are deselected, parent becomes deselected
 */
fun <T> toggleNodeWithCascade(
    roots: List<TreeNode<T>>,
    nodeId: String
): List<TreeNode<T>> {
    // Step 1: Toggle the target node + cascade down to all descendants
    val afterDownward = roots.map { node ->
        toggleDownward(node, nodeId)
    }
    // Step 2: Cascade upward - parent selected if any child is selected
    return afterDownward.map { node ->
        cascadeUpward(node)
    }
}

/**
 * Toggle target node and select/deselect all its descendants
 */
private fun <T> toggleDownward(node: TreeNode<T>, targetId: String): TreeNode<T> {
    return if (node.id == targetId) {
        selectSubtree(node, !node.isSelected)
    } else {
        node.copy(
            children = node.children.map { toggleDownward(it, targetId) }
        )
    }
}

/**
 * Cascade selection upward: a parent is selected if ANY of its children are selected.
 * Applied bottom-up so leaf states drive parent states.
 */
private fun <T> cascadeUpward(node: TreeNode<T>): TreeNode<T> {
    if (node.isLeaf) return node

    // First cascade children
    val updatedChildren = node.children.map { cascadeUpward(it) }
    // Parent is selected if any child is selected
    val anyChildSelected = updatedChildren.any { it.isSelected }

    return node.copy(
        isSelected = anyChildSelected,
        children = updatedChildren
    )
}

/**
 * Get all selected nodes from tree (flat list)
 */
fun <T> getSelectedNodes(roots: List<TreeNode<T>>): List<TreeNode<T>> {
    val result = mutableListOf<TreeNode<T>>()
    roots.forEach { node ->
        if (node.isSelected) result.add(node)
        result.addAll(getSelectedNodes(node.children))
    }
    return result
}

/**
 * Count total selected nodes in tree
 */
fun <T> countSelectedNodes(roots: List<TreeNode<T>>): Int {
    return roots.sumOf { node ->
        (if (node.isSelected) 1 else 0) + countSelectedNodes(node.children)
    }
}

/**
 * Count total leaf nodes in tree
 */
fun <T> countLeafNodes(roots: List<TreeNode<T>>): Int {
    return roots.sumOf { node ->
        if (node.isLeaf) 1 else countLeafNodes(node.children)
    }
}

/**
 * Count selected leaf nodes in tree
 */
fun <T> countSelectedLeafNodes(roots: List<TreeNode<T>>): Int {
    return roots.sumOf { node ->
        if (node.isLeaf && node.isSelected) 1 else countSelectedLeafNodes(node.children)
    }
}

/**
 * Single-select a node: clears all selections then selects only the given node.
 * Works for both leaf and parent nodes. Used for single-select tree mode.
 */
fun <T> selectSingleLeaf(
    roots: List<TreeNode<T>>,
    leafId: String
): List<TreeNode<T>> {
    return roots.map { node ->
        node.copy(
            isSelected = node.id == leafId,
            children = selectSingleLeaf(node.children, leafId)
        )
    }
}

/**
 * Build the display path for a node by walking up the tree via parentId.
 * Returns list of ancestor names from root to parent (not including the node itself).
 */
fun <T> buildNodePath(
    roots: List<TreeNode<T>>,
    nodeId: String
): List<String> {
    val path = mutableListOf<String>()
    var currentId: String? = nodeId

    // Build a lookup map of id -> node for efficient traversal
    val nodeMap = mutableMapOf<String, TreeNode<T>>()
    fun indexNodes(nodes: List<TreeNode<T>>) {
        for (n in nodes) {
            nodeMap[n.id] = n
            indexNodes(n.children)
        }
    }
    indexNodes(roots)

    // Walk up from the node to root, collecting names
    val node = nodeMap[nodeId] ?: return emptyList()
    currentId = node.parentId
    while (currentId != null) {
        val parent = nodeMap[currentId] ?: break
        path.add(0, parent.name)
        currentId = parent.parentId
    }
    return path
}
