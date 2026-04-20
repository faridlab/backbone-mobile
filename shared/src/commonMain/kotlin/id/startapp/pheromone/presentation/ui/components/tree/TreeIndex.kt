package id.startapp.pheromone.presentation.ui.components.tree

// ============================================================================
// Tree View Components - Generic reusable tree UI components
// ============================================================================

/**
 * ## Tree View Components
 *
 * A collection of generic, reusable tree view components for displaying
 * hierarchical data with expand/collapse and selection functionality.
 *
 * ### Usage Example:
 * ```kotlin
 * // State
 * var showServiceSheet by remember { mutableStateOf(false) }
 * var selectedService by remember { mutableStateOf<String?>(null) }
 *
 * // Trigger button
 * Button(onClick = { showServiceSheet = true }) {
 *     Text(selectedService ?: "Select Service")
 * }
 *
 * // Bottom Sheet
 * ServiceCategoryTreeBottomSheet(
 *     isVisible = showServiceSheet,
 *     onDismiss = { showServiceSheet = false },
 *     onSave = { selections ->
 *         selectedService = selections.firstOrNull()?.nodeName
 *     }
 * )
 * ```
 */

// Re-export all tree components for easy importing
// Typealiases and function re-exports are not needed since everything is in the same package
