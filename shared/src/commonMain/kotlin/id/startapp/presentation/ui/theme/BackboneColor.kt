package id.startapp.presentation.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Backbone Provider Mobile App Brand Colors
 *
 * Color palette based on the design system specification:
 * https://github.com/backbone/backbone/docs/blueprint/development/provider-mobile/design/00-design-system.md
 */

// ============================================================================
// Primary Brand Colors (Blue)
// ============================================================================

/**
 * Main brand blue - Primary color for the app
 * Used for: Primary buttons, active states, brand elements
 */
val BackboneBlue = Color(0xFF004AAD)

/**
 * Light variant of brand blue
 * Used for: Hover states, light backgrounds
 */
val BackboneBlueLight = Color(0xFF3377CC)

/**
 * Dark variant of brand blue
 * Used for: Text on light backgrounds, dark theme
 */
val BackboneBlueDark = Color(0xFF003377)

/**
 * Tint background color
 * Used for: Section backgrounds, light accents
 */
val BackboneBlueTint = Color(0xFFE1F5FE)

// ============================================================================
// Status Colors (Semantic)
// ============================================================================

/**
 * Success green (cyan color)
 * Used for: Completed status, success messages, positive indicators
 */
val SuccessGreen = Color(0xFF0CC0DF)

/**
 * Success green light variant
 */
val SuccessGreenLight = Color(0xFF66E0FF)

/**
 * Success green background tint
 */
val SuccessGreenBg = Color(0xFFE0F7FA)

/**
 * Warning orange
 * Used for: Pending status, warnings, caution indicators
 */
val WarningOrange = Color(0xFFFF9800)

/**
 * Warning orange light variant
 */
val WarningOrangeLight = Color(0xFFFFB74D)

/**
 * Warning orange background tint
 */
val WarningOrangeBg = Color(0xFFFFF3E0)

/**
 * Error red
 * Used for: Error states, failed status, critical alerts
 */
val ErrorRed = Color(0xFFF44336)

/**
 * Error red light variant
 */
val ErrorRedLight = Color(0xFFE57373)

/**
 * Error red background tint
 */
val ErrorRedBg = Color(0xFFFFEBEE)

/**
 * Info blue
 * Used for: Information messages, neutral states
 */
val InfoBlue = Color(0xFF2196F3)

/**
 * Info blue light variant
 */
val InfoBlueLight = Color(0xFF64B5F6)

/**
 * Info blue background tint
 */
val InfoBlueBg = Color(0xFFE3F2FD)

// ============================================================================
// Special Purpose Colors
// ============================================================================

/**
 * Express purple
 * Used for: Express service badge, premium features
 */
val ExpressPurple = Color(0xFF9C27B0)

/**
 * Express purple light variant
 */
val ExpressPurpleLight = Color(0xFFBA68C8)

/**
 * Express purple background tint
 */
val ExpressPurpleBg = Color(0xFFF3E5F5)

// ============================================================================
// Neutral Colors (Text & Background)
// ============================================================================

/**
 * Primary text color
 * Used for: Main content text, headings
 */
val TextPrimary = Color(0xFF212121)

/**
 * Secondary text color
 * Used for: Subtitles, descriptions, less important text
 */
val TextSecondary = Color(0xFF757575)

/**
 * Disabled text color
 * Used for: Disabled elements, placeholders
 */
val TextDisabled = Color(0xFFBDBDBD)

/**
 * Inverse text color
 * Used for: Text on dark backgrounds
 */
val TextInverse = Color(0xFFFFFFFF)

// ============================================================================
// Background Colors
// ============================================================================

/**
 * Primary background color
 * Used for: Main screen background
 */
val BackgroundPrimary = Color(0xFFFFFFFF)

/**
 * Secondary background color
 * Used for: Card backgrounds, nested containers
 */
val BackgroundSecondary = Color(0xFFF5F5F5)

/**
 * Tertiary background color
 * Used for: Dividers, borders
 */
val BackgroundTertiary = Color(0xFFEEEEEE)

/**
 * Overlay color
 * Used for: Modals, bottom sheets backdrop (50% opacity)
 */
val BackgroundOverlay = Color(0x80000000)

// ============================================================================
// Surface Colors (Cards)
// ============================================================================

/**
 * White surface
 */
val SurfaceWhite = Color(0xFFFFFFFF)

/**
 * Gray 50 - Very light gray surface
 */
val SurfaceGray50 = Color(0xFFFAFAFA)

/**
 * Gray 100 - Light gray surface
 */
val SurfaceGray100 = Color(0xFFF5F5F5)

/**
 * Settings background - Slightly blue-tinted light gray
 */
val SettingsBackground = Color(0xFFF5F6FA)

/**
 * Gray 200 - Medium light gray surface
 */
val SurfaceGray200 = Color(0xFFEEEEEE)

/**
 * Gray 300 - Medium gray surface
 */
val SurfaceGray300 = Color(0xFFE0E0E0)

/**
 * Gray 400 - Darker gray surface
 */
val SurfaceGray400 = Color(0xFFBDBDBD)

// ============================================================================
// Dark Mode Colors
// ============================================================================

/**
 * Dark mode primary background
 */
val DarkBackgroundPrimary = Color(0xFF121212)

/**
 * Dark mode secondary background
 */
val DarkBackgroundSecondary = Color(0xFF1E1E1E)

/**
 * Dark mode tertiary background
 */
val DarkBackgroundTertiary = Color(0xFF2C2C2C)

/**
 * Dark mode surface (elevated)
 */
val DarkSurfaceElevated = Color(0xFF1E1E1E)

// ============================================================================
// Priority Colors (for Queue/Tasks)
// ============================================================================

/**
 * Critical priority (red)
 */
val PriorityCritical = Color(0xFFF44336)

/**
 * High priority (orange)
 */
val PriorityHigh = Color(0xFFFF9800)

/**
 * Medium priority (cyan)
 */
val PriorityMedium = Color(0xFF0CC0DF)

/**
 * Low priority (gray)
 */
val PriorityLow = Color(0xFF757575)

// ============================================================================
// Role Card Colors
// ============================================================================

/**
 * Role card icon background (unselected state)
 * Used for: Role selection icon background when not selected
 */
val RoleCardIconBgUnselected = SurfaceGray100

/**
 * Role card icon background (selected state)
 * Used for: Role selection icon background when selected
 */
val RoleCardIconBgSelected = InfoBlueBg

// ============================================================================
// Input Field Colors
// ============================================================================

/**
 * Input field border color (light gray)
 * Used for: Input field outlines, unfocused borders
 */
val InputBorder = Color(0xFFD1D5DB)

/**
 * Input field focused border color
 * Used for: Focused input field borders
 */
val InputBorderFocused = BackboneBlue

// ============================================================================
// Onboarding/Illustration Colors
// ============================================================================

/**
 * Primary text for onboarding headings
 */
val OnboardingTextPrimary = Color(0xFF1A1A1A)

/**
 * Secondary text for onboarding descriptions
 */
val OnboardingTextSecondary = Color(0xFF666666)

/**
 * Inactive pagination dot color
 */
val PaginationDotInactive = Color(0xFFE0E0E0)

/**
 * Chart growth green arrow color
 */
val ChartGrowthGreen = Color(0xFF4CAF50)
