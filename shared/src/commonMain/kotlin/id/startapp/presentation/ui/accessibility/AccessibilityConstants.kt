package id.startapp.presentation.ui.accessibility

/**
 * Test tag constants for UI automation and accessibility testing.
 *
 * Use with Modifier.testTag() to identify key UI elements in tests.
 */
object TestTags {
    // Navigation
    const val BOTTOM_NAV = "bottom_nav"
    const val BOTTOM_NAV_HOME = "bottom_nav_home"
    const val BOTTOM_NAV_ORDERS = "bottom_nav_orders"
    const val BOTTOM_NAV_INVENTORY = "bottom_nav_inventory"
    const val BOTTOM_NAV_SETTINGS = "bottom_nav_settings"
    const val BOTTOM_NAV_TASKS = "bottom_nav_tasks"

    // Screens
    const val OWNER_DASHBOARD = "owner_dashboard"
    const val CASHIER_DASHBOARD = "cashier_dashboard"
    const val OPERATOR_DASHBOARD = "operator_dashboard"
    const val ORDER_LIST = "order_list"
    const val ORDER_DETAIL = "order_detail"
    const val STOCK_LIST = "stock_list"
    const val STOCK_DETAIL = "stock_detail"
    const val QUICK_ADJUSTMENT = "quick_adjustment"
    const val TASK_LIST = "task_list"
    const val PROFILE_VIEW = "profile_view"
    const val SETTINGS_MENU = "settings_menu"
    const val NOTIFICATION_SETTINGS = "notification_settings"
    const val OUTLET_SETTINGS = "outlet_settings"

    // Components
    const val FAB_BUTTON = "fab_button"
    const val SEARCH_FIELD = "search_field"
    const val STAT_CARD = "stat_card"
    const val ALERT_CARD = "alert_card"
}

/**
 * Reusable content description strings for common icon actions.
 *
 * Use with Icon(contentDescription = ContentDescriptions.REFRESH) for consistent
 * accessibility labels across the app.
 */
object ContentDescriptions {
    const val REFRESH = "Refresh"
    const val SETTINGS = "Settings"
    const val HELP = "Help"
    const val CLOSE = "Close"
    const val FILTER = "Filter"
    const val SORT = "Sort"
    const val ADD = "Add"
    const val SEARCH = "Search"
    const val CLEAR_SEARCH = "Clear search"
    const val BACK = "Back"
    const val MORE_OPTIONS = "More options"
    const val NAVIGATE = "Navigate"
    const val TIME_REMAINING = "Time remaining"
    const val NOTIFICATIONS = "Notifications"
    const val QUIET_HOURS = "Quiet hours"
    const val EXPAND = "Expand"
    const val PROFILE = "Profile"
}
