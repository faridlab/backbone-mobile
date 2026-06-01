package id.startapp.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Backbone Provider Mobile App Typography Scale
 *
 * Typography system based on the design system specification:
 * https://github.com/backbone/backbone/docs/blueprint/development/provider-mobile/design/00-design-system.md
 *
 * Type Scale follows an 8pt grid system with Material 3 typography.
 */

// ============================================================================
// Display Styles (Page Titles, Section Headers)
// ============================================================================

/**
 * Display Large (H1) - Page Titles
 * Size: 57sp, Weight: Regular, Line Height: 64
 * Usage: Splash screen, onboarding titles, main page headers
 */
val DisplayLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 57.sp,
    lineHeight = 64.sp
)

/**
 * Display Medium (H2) - Section Headers
 * Size: 45sp, Weight: Regular, Line Height: 52
 * Usage: Dashboard headers, screen section titles
 */
val DisplayMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 45.sp,
    lineHeight = 52.sp
)

/**
 * Headline Large (H3) - Card Titles
 * Size: 32sp, Weight: Regular, Line Height: 40
 * Usage: Order detail title, card titles
 */
val HeadlineLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 32.sp,
    lineHeight = 40.sp
)

/**
 * Headline Medium (H4) - List Headers
 * Size: 28sp, Weight: Regular, Line Height: 36
 * Usage: Section headers within screens
 */
val HeadlineMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 28.sp,
    lineHeight = 36.sp
)

/**
 * Headline Small (H5) - Small Headlines
 * Size: 24sp, Weight: Regular, Line Height: 32
 * Usage: Small section headers, dialog titles
 */
val HeadlineSmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 24.sp,
    lineHeight = 32.sp
)

/**
 * Title Large (H5) - Subsection Titles
 * Size: 22sp, Weight: Medium, Line Height: 28
 * Usage: Form section titles, subsection headers
 */
val TitleLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 22.sp,
    lineHeight = 28.sp
)

/**
 * Title Medium - Card Headers, Menu Items
 * Size: 16sp, Weight: Medium, Line Height: 24
 * Usage: Card title, menu item, button text
 */
val TitleMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp
)

// ============================================================================
// Body Styles (Content Text)
// ============================================================================

/**
 * Body Large - Primary Body Text
 * Size: 16sp, Weight: Regular, Line Height: 24
 * Usage: Main content, descriptions, primary information
 */
val BodyLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp
)

/**
 * Body Medium - Secondary Body Text
 * Size: 14sp, Weight: Regular, Line Height: 20
 * Usage: List items, captions, secondary information
 */
val BodyMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp
)

/**
 * Body Small - Tertiary Text
 * Size: 12sp, Weight: Regular, Line Height: 16
 * Usage: Timestamps, hints, helper text
 */
val BodySmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp
)

// ============================================================================
// Label Styles (Buttons, Form Labels, Tags)
// ============================================================================

/**
 * Label Large - Buttons, Tabs
 * Size: 14sp, Weight: Medium, Line Height: 20
 * Usage: Button text, tab labels
 */
val LabelLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp
)

/**
 * Label Medium - Form Labels
 * Size: 12sp, Weight: Medium, Line Height: 16
 * Usage: Input labels, field labels
 */
val LabelMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp
)

/**
 * Label Small - Tags, Badges
 * Size: 11sp, Weight: Medium, Line Height: 16
 * Usage: Status badges, chips, small tags
 */
val LabelSmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 16.sp
)

// ============================================================================
// Material 3 Typography Configuration
// ============================================================================

/**
 * Backbone Typography System
 *
 * Complete typography configuration using Material 3 Typography.
 * Maps Backbone type scale to Material 3 typography tokens.
 */
val BackboneTypography = Typography(
    // Display styles
    displayLarge = DisplayLarge,
    displayMedium = DisplayMedium,
    displaySmall = HeadlineLarge,

    // Headline styles
    headlineLarge = HeadlineLarge,
    headlineMedium = HeadlineMedium,
    headlineSmall = HeadlineSmall,

    // Title styles
    titleLarge = TitleLarge,
    titleMedium = TitleMedium,
    titleSmall = BodyLarge,

    // Body styles
    bodyLarge = BodyLarge,
    bodyMedium = BodyMedium,
    bodySmall = BodySmall,

    // Label styles
    labelLarge = LabelLarge,
    labelMedium = LabelMedium,
    labelSmall = LabelSmall
)

// ============================================================================
// Typography Extension Helpers
// ============================================================================

/**
 * Extension function to get text style by name
 * Useful for dynamic text style selection based on design requirements
 */
fun TextStyle.Companion.byName(name: String): TextStyle = when (name) {
    "h1", "displayLarge" -> DisplayLarge
    "h2", "displayMedium" -> DisplayMedium
    "h3", "headlineLarge" -> HeadlineLarge
    "h4", "headlineMedium" -> HeadlineMedium
    "h5", "titleLarge" -> TitleLarge
    "titleMedium" -> TitleMedium
    "bodyLarge" -> BodyLarge
    "bodyMedium" -> BodyMedium
    "bodySmall" -> BodySmall
    "labelLarge" -> LabelLarge
    "labelMedium" -> LabelMedium
    "labelSmall" -> LabelSmall
    else -> BodyMedium
}
