package id.startapp.pheromone.presentation.ui.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.startapp.pheromone.presentation.ui.theme.ErrorRed

/**
 * Pheromone Top App Bar
 *
 * Top app bar following Pheromone design system.
 *
 * Specs:
 * - Height: 64dp
 * - Background: White
 * - Title: Title Large
 * - Leading: Back button or Menu (48dp)
 * - Trailing: Actions (48dp each)
 *
 * @param title Title text
 * @param modifier Modifier for the app bar
 * @param showBackButton Whether to show back button
 * @param onBackClick Callback for back button
 * @param actions Optional trailing actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PheromoneTopBar(
    title: String,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    actions: (@Composable RowScope.() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = actions ?: {},
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * Top App Bar with menu button
 *
 * Top bar with hamburger menu for drawer navigation.
 *
 * @param title Title text
 * @param modifier Modifier for the app bar
 * @param onMenuClick Callback for menu button
 * @param actions Optional trailing actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PheromoneTopBarWithMenu(
    title: String,
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {},
    actions: (@Composable RowScope.() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu"
                )
            }
        },
        actions = actions ?: {},
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * Top App Bar with notification and settings
 *
 * Top bar with notification bell and settings actions.
 *
 * @param title Title text
 * @param modifier Modifier for the app bar
 * @param showBackButton Whether to show back button
 * @param onBackClick Callback for back button
 * @param notificationCount Notification badge count (0 = no badge)
 * @param onNotificationClick Callback for notification button
 * @param onSettingsClick Callback for settings button
 * @param onMoreClick Callback for more options button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PheromoneTopBarWithActions(
    title: String,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    notificationCount: Int = 0,
    onNotificationClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onMoreClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            // Notification button with badge
            Box {
                IconButton(onClick = onNotificationClick) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications"
                    )
                }
                if (notificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .padding(4.dp)
                            .align(Alignment.TopEnd)
                            .background(ErrorRed, CircleShape)
                    ) {
                        Text(
                            text = notificationCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            // Settings button
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings"
                )
            }

            // More options menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More options"
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Refresh") },
                        onClick = {
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = {
                            showMenu = false
                            onSettingsClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Help") },
                        onClick = {
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.QuestionMark, contentDescription = "Help")
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * Center-aligned Top App Bar
 *
 * Top bar with centered title (common for authentication screens).
 *
 * @param title Title text
 * @param modifier Modifier for the app bar
 * @param showBackButton Whether to show back button
 * @param onBackClick Callback for back button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PheromoneCenteredTopBar(
    title: String,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        modifier = modifier,
        actions = {
            Spacer(modifier = Modifier.width(48.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )

    if (showBackButton) {
        BackButton(onClick = onBackClick)
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back"
        )
    }
}

/**
 * Search Top App Bar
 *
 * Top bar with search functionality.
 *
 * @param query Current search query
 * @param onQueryChange Callback when query changes
 * @param modifier Modifier for the app bar
 * @param showBackButton Whether to show back button
 * @param onBackClick Callback for back button
 * @param placeholder Search placeholder text
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PheromoneSearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    placeholder: String = "Search..."
) {
    TopAppBar(
        title = {
            Text(
                text = if (query.isEmpty()) placeholder else query,
                style = if (query.isEmpty()) {
                    MaterialTheme.typography.bodyMedium
                } else {
                    MaterialTheme.typography.titleLarge
                },
                color = if (query.isEmpty()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            } else {
                IconButton(onClick = { /* Open search */ }) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search"
                    )
                }
            }
        },
        actions = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}
